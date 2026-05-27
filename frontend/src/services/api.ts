import axios, {
  AxiosError,
  AxiosInstance,
  AxiosResponse,
  InternalAxiosRequestConfig,
} from "axios";
import { TokenResponse } from "@appTypes/api";

const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";
const TOKENS_STORAGE_KEY = "auth_tokens";

export interface ApiResponse<T = unknown> {
  success: boolean;
  data?: T;
  message?: string;
  timestamp?: string;
}

function delay(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

export function unwrapApiResponse<T>(
  response: AxiosResponse<ApiResponse<T>>,
): T {
  if (!response.data.success) {
    throw new Error(response.data.message || "API request failed");
  }
  return response.data.data as T;
}

class ApiClient {
  private client: AxiosInstance;
  private refreshTokenPromise: Promise<string> | null = null;

  constructor() {
    this.client = axios.create({
      baseURL: API_BASE_URL,
      timeout: 15000,
      headers: { "Content-Type": "application/json" },
    });
    this.setupInterceptors();
  }

  private setupInterceptors(): void {
    this.client.interceptors.request.use(
      (config: InternalAxiosRequestConfig) => {
        const tokens = this.getTokens();
        if (tokens?.accessToken) {
          config.headers.Authorization = `Bearer ${tokens.accessToken}`;
        }
        return config;
      },
      (error) => Promise.reject(error),
    );

    this.client.interceptors.response.use(
      (response) => response,
      async (error: AxiosError) => {
        const originalRequest = error.config as InternalAxiosRequestConfig & {
          _retry?: boolean;
          _retryCount?: number;
        };

        if (error.response?.status === 401 && !originalRequest._retry) {
          originalRequest._retry = true;
          try {
            const tokens = this.getTokens();
            if (!tokens?.refreshToken) {
              this.clearTokens();
              window.location.href = "/login";
              return Promise.reject(error);
            }

            if (this.refreshTokenPromise) {
              const newAccessToken = await this.refreshTokenPromise;
              originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
              return this.client(originalRequest);
            }

            this.refreshTokenPromise = this.refreshAccessToken(
              tokens.refreshToken,
            );
            const newAccessToken = await this.refreshTokenPromise;
            this.refreshTokenPromise = null;

            originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
            return this.client(originalRequest);
          } catch (refreshError) {
            this.clearTokens();
            window.location.href = "/login";
            return Promise.reject(refreshError);
          }
        }

        if (
          (!error.response || error.response.status >= 500) &&
          originalRequest &&
          (originalRequest._retryCount ?? 0) < 2
        ) {
          originalRequest._retryCount = (originalRequest._retryCount ?? 0) + 1;
          await delay(200 * Math.pow(2, originalRequest._retryCount));
          return this.client(originalRequest);
        }

        return Promise.reject(error);
      },
    );
  }

  async refreshAccessToken(refreshToken: string): Promise<string> {
    try {
      const response = await axios.post<ApiResponse<TokenResponse>>(
        `${API_BASE_URL}/api/auth/refresh`,
        { refreshToken },
      );
      const newTokens = response.data.data;
      if (newTokens) {
        this.setTokens(newTokens);
        return newTokens.accessToken;
      }
      throw new Error("No tokens in refresh response");
    } catch {
      throw new Error("Token refresh failed");
    }
  }

  setTokens(tokens: TokenResponse): void {
    localStorage.setItem(
      TOKENS_STORAGE_KEY,
      JSON.stringify({
        accessToken: tokens.accessToken,
        refreshToken: tokens.refreshToken,
        expiresAt: Date.now() + tokens.expiresIn,
      }),
    );
  }

  getTokens(): { accessToken: string; refreshToken: string } | null {
    const stored = localStorage.getItem(TOKENS_STORAGE_KEY);
    if (!stored) return null;
    try {
      const parsed = JSON.parse(stored);
      return {
        accessToken: parsed.accessToken,
        refreshToken: parsed.refreshToken,
      };
    } catch {
      return null;
    }
  }

  clearTokens(): void {
    localStorage.removeItem(TOKENS_STORAGE_KEY);
  }

  getClient(): AxiosInstance {
    return this.client;
  }
  async get<T>(url: string, config = {}) {
    return this.client.get<T>(url, config);
  }
  async post<T>(url: string, data?: unknown, config = {}) {
    return this.client.post<T>(url, data, config);
  }
  async put<T>(url: string, data?: unknown, config = {}) {
    return this.client.put<T>(url, data, config);
  }
  async patch<T>(url: string, data?: unknown, config = {}) {
    return this.client.patch<T>(url, data, config);
  }
  async delete<T>(url: string, config = {}) {
    return this.client.delete<T>(url, config);
  }
}

export const apiClient = new ApiClient();
