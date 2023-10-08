<script>
import { transactionStore } from "@/stores/transactions";
import { Client } from "@stomp/stompjs";
import { AuthService } from "@/services/authService";
export default {
  setup() {
    return {
      transactionStore: transactionStore(),
      stompClient: new Client({
        brokerURL: "ws://localhost:8080/socket",
      }),
    };
  },
  beforeMount() {
    this.stompClient.onConnect = (frame) => {
      console.log("Connected to socket", frame);
      this.stompClient.subscribe(
        `/user/${AuthService.currentUser().username}/transactions`,
        (transactions) => {
          console.log(transactions);
          this.transactionStore.pushAll(JSON.parse(transactions.body));
        }
      );
    };
    this.stompClient.activate();
  },
  beforeUnmount() {
    this.stompClient.deactivate({ force: true });
  },
};
</script>
<template>
  <div></div>
</template>
