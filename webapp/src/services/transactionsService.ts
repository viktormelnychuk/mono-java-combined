import { BaseApi } from "@/services/base";
import type { Transaction } from "@/services/entities/transaction";
import type { RouteParamValue } from "vue-router";

export class TransactionsService extends BaseApi {
  async getAll(): Promise<Transaction[]> {
    const transactions = await this.get<Transaction[]>("/api/v1/transactions");
    transactions.forEach((tr) => {
      const d = new Date(tr.createdAt);
      tr.createdAtEpoch = d.getTime();
    });
    return transactions;
  }

  async getOne(id: string | RouteParamValue[]): Promise<Transaction> {
    return await this.get(`/api/v1/transactions/${id}`);
  }
}
