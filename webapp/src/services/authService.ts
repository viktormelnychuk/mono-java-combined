import { BaseApi } from "@/services/base";
import type { RegistrationRequest } from "@/services/entities/registration";
import type { LoginRequest, LoginResponse } from "@/services/entities/login";
import { ErrorService } from "@/services/errorService";

export class AuthService extends BaseApi {
  isLoggedIn() {
    const tokenString = localStorage.getItem("loginDetails");
    if (!tokenString) {
      return false;
    }
    const loginResp: LoginResponse = JSON.parse(tokenString);
    return !!loginResp.token;
  }

  static currentUser(): LoginResponse {
    const tokenString = localStorage.getItem("loginDetails");
    if (!tokenString) {
      throw new Error("Unauthorizaed");
    }
    return JSON.parse(tokenString);
  }

  logout() {
    localStorage.clear();
  }

  async register(req: RegistrationRequest) {
    await this.post("/api/v1/users", JSON.stringify(req));
    ErrorService.dispatchInfo({ message: "User have been registered" });
  }

  async login(req: LoginRequest) {
    const resp = await this.post<LoginResponse>(
      "/api/v1/auth/signin",
      JSON.stringify(req)
    );
    localStorage.setItem("loginDetails", JSON.stringify(resp));
  }

  private static tokenExpired(expDate: Date): boolean {
    return expDate.getTime() < Date.now();
  }

  private static convertDate(stringDate: string): Date {
    return new Date(stringDate);
  }
}
