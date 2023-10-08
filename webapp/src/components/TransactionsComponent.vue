<script lang="tsx">
import { TransactionsService } from "@/services/transactionsService";
import { transactionStore } from "@/stores/transactions";
import SocketComponent from "@/components/SocketComponent.vue";
import type { Transaction } from "@/services/entities/transaction";
import type { Column, ElButton } from "element-plus";
import EditTransactionButton from "@/fields/EditTransactionButton.vue";
export default {
  data() {
    return {};
  },
  components: { SocketComponent },
  setup() {
    const columns: Column<Transaction>[] = [
      {
        key: "id",
        title: "ID",
        dataKey: "id",
        width: 75,
      },
      {
        key: "amount",
        dataKey: "amount",
        title: "Amount",
        width: 150,
      },
      {
        key: "description",
        dataKey: "description",
        title: "Description",
        width: 150,
      },
      {
        key: "comment",
        dataKey: "comment",
        title: "Comment",
        width: 150,
      },
      {
        key: "currency",
        dataKey: "currency",
        title: "Currency",
        width: 150,
      },
      {
        key: "mcc",
        dataKey: "mcc",
        title: "mcc",
        width: 100,
      },
      {
        key: "operations",
        title: "Operations",
        dataKey: "id",
        cellRenderer: ({ cellData }) => {
          return (
            <>
              <EditTransactionButton id={cellData}></EditTransactionButton>
            </>
          );
        },
        width: 100,
        align: "center",
      },
    ];
    return {
      transactionStore: transactionStore(),
      table: {
        columns: columns,
      },
    };
  },
  async beforeMount() {
    const transactionsService = new TransactionsService();
    const transactions = await transactionsService.getAll();
    this.transactionStore.$patch({ transactions: transactions });
  },
  methods: {
    openTransactionDetails(e) {
      console.log(`Pushed to ${e}`);
      //
    },
  },
};
</script>

<template>
  <SocketComponent />
  <el-auto-resizer>
    <template #default="{ height, width }">
      <el-table-v2
        :columns="table.columns"
        :data="transactionStore.transactions"
        :width="width"
        :height="height"
        fixed
      >
      </el-table-v2>
    </template>
  </el-auto-resizer>
</template>
