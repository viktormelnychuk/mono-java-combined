import { BaseApi } from "@/services/base";
import type { User } from "@/services/entities/user";

export class UserService extends BaseApi {
  async current(): Promise<User> {
    return await this.get<User>("/api/v1/users/my");
  }

  async addMonoToken(token: string) {
    await this.post(
      "/api/v1/users/mono-token",
      JSON.stringify({ monoToken: token })
    );
  }
}
