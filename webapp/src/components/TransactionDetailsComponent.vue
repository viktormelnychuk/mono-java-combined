<script>
import { TransactionsService } from "@/services/transactionsService";

export default {
  data() {
    return {
      transaction: {},
      transactionService: new TransactionsService(),
    };
  },
  computed: {
    transactionKeyValue() {
      return Object.entries(this.transaction).map((e) => {
        return {
          field: e[0],
          value: e[1],
        };
      });
    },
  },
  async beforeMount() {
    this.transaction = await this.transactionService.getOne(
      this.$route.params.id
    );
  },
};
</script>
<template>
  <div>
    <div>
      <button @click="$router.push('/transactions')">Back</button>
    </div>
    <table>
      <thead>
        <tr>
          <th>Field</th>
          <th>Value</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="tr in transactionKeyValue" :key="tr.field">
          <td :data-id="tr.field">{{ tr.field }}</td>
          <td :data-id="tr.value">{{ tr.value }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
<style scoped>
table {
  width: 1000px;
  border-collapse: collapse;
  margin-top: 10px;
  height: 80vh;
}

th {
  border: black 2px solid;
}

tr {
  border: #0000007d 2px solid;
}

td {
  border-right: #0000007d 1px solid;
  text-align: center;
}
</style>
