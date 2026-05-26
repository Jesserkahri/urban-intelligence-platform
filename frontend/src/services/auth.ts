import { apiClient, ApiResponse } from "./api";
import { LoginRequest, TokenResponse, User } from "../types/api";
export class AuthService {
  async login(
    credentials: LoginRequest,
  ): Promise<{ user: User; tokens: TokenResponse }> {
    const response = await apiClient.post<
      ApiResponse<{ accessToken: string; refreshToken: string; user: User }>
    >("/auth/login", credentials);

    if (!response.data.success || !response.data.data) {
      throw new Error(response.data.message || "Login failed");
    }

    const { accessToken, refreshToken, user } = response.data.data;
    apiClient.setTokens({
      accessToken,
      refreshToken,
      expiresIn: 900, // 15 minutes
    });

    return {
      user,
      tokens: {
        accessToken,
        refreshToken,
        expiresIn: 900,
      },
    };
  }

  async logout(): Promise<void> {
    try {
      await apiClient.post("/auth/logout");
    } catch (error) {
      console.warn("Logout request failed, clearing tokens anyway", error);
    }
    apiClient.clearTokens();
  }

  async getCurrentUser(): Promise<User> {
    const response = await apiClient.get<ApiResponse<User>>("/auth/me");
    if (!response.data.success || !response.data.data) {
      throw new Error("Failed to fetch current user");
    }
    return response.data.data;
  }

  async refreshToken(refreshToken: string): Promise<TokenResponse> {
    const response = await apiClient.post<ApiResponse<TokenResponse>>(
      "/auth/refresh",
      {
        refreshToken,
      },
    );
    if (!response.data.success || !response.data.data) {
      throw new Error("Token refresh failed");
    }
    return response.data.data;
  }

  getStoredTokens() {
    return apiClient.getTokens();
  }

  clearTokens() {
    apiClient.clearTokens();
  }
}

export const authService = new AuthService();
