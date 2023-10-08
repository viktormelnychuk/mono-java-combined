import { defineStore } from "pinia";
import type { Transaction } from "@/services/entities/transaction";

export const transactionStore = defineStore("transactions", {
  state: () => ({
    transactions: [] as Transaction[],
  }),
  actions: {
    pushOne(tr: Transaction) {
      this.transactions.push(tr);
    },
    pushAll(list: Transaction[]) {
      this.transactions.push(...list);
    },
  },
});
