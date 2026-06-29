import { apiClient, ApiResponse } from "./api";
import { LoginRequest, RegisterRequest, TokenResponse, User } from "../types/api";
export class AuthService {
  async login(
    credentials: LoginRequest,
  ): Promise<{ user: User; tokens: TokenResponse }> {
    const response = await apiClient.post<ApiResponse<TokenResponse>>(
      "/api/auth/login",
      credentials,
    );

    if (!response.data.success || !response.data.data) {
      throw new Error(response.data.message || "Login failed");
    }

    const tokens = response.data.data;
    apiClient.setTokens(tokens);

    const user = await this.getCurrentUser();
    return {
      user,
      tokens,
    };
  }

  async register(
    credentials: RegisterRequest,
  ): Promise<{ user: User; tokens: TokenResponse }> {
    const response = await apiClient.post<ApiResponse<TokenResponse>>(
      "/api/auth/register",
      credentials,
    );

    if (!response.data.success || !response.data.data) {
      throw new Error(response.data.message || "register failed");
    }

    const tokens = response.data.data;
    apiClient.setTokens(tokens);

    const user = await this.getCurrentUser();
    return {
      user,
      tokens,
    };
  }



  async logout(): Promise<void> {
    try {
      const tokens = apiClient.getTokens();
      await apiClient.post("/api/auth/logout", {
        refreshToken: tokens?.refreshToken,
      });
    } catch (error) {
      console.warn("Logout request failed, clearing tokens anyway", error);
    }
    apiClient.clearTokens();
  }

  async getCurrentUser(): Promise<User> {
    const response = await apiClient.get<ApiResponse<User>>("/api/auth/me");
    if (!response.data.success || !response.data.data) {
      throw new Error("Failed to fetch current user");
    }
    return response.data.data;
  }

  async refreshToken(refreshToken: string): Promise<TokenResponse> {
    const response = await apiClient.post<ApiResponse<TokenResponse>>(
      "/api/auth/refresh",
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
