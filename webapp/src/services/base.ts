import axios from "axios";
import { ErrorService } from "@/services/errorService";
import type { AxiosError } from "axios";
import type { ErrorResponse } from "@/services/entities/error";
import { Router, useRouter } from "vue-router";

export class BaseApi {
  baseUrl = import.meta.env.API_URL || "http://localhost:8080";
  private router: Router = useRouter();

  async post<T>(path: string, body: string): Promise<T> {
    const url = `${this.baseUrl}${path}`;

    const resp = await this.sendRequest("POST", url, body);

    // @ts-ignore
    return resp.data as T;
  }

  async get<T>(path: string): Promise<T> {
    const url = `${this.baseUrl}${path}`;

    const resp = await this.sendRequest("GET", url);

    // @ts-ignore
    return resp.data as T;
  }

  private async sendRequest(method: string, url: string, data?: string) {
    try {
      return await axios({
        method,
        url,
        data,
        headers: {
          Authorization: `Bearer ${BaseApi.getAccessToken()}`,
          "Content-Type": "application/json",
        },
      });
    } catch (e: unknown) {
      const err = e as AxiosError;
      if (!err.response) {
        throw e;
      }

      if (err.response.status === 401) {
        ErrorService.dispatchInfo("Session expired");
        await this.router.push("/login");
      } else {
        ErrorService.dispatchError(err.response.data as ErrorResponse);
      }
    }
  }

  private static getAccessToken(): string {
    const tokenString = localStorage.getItem("loginDetails");
    if (!tokenString) {
      return "";
    }
    const t = JSON.parse(tokenString);
    return t.token;
  }
}
