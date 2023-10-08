import { createRouter, createWebHistory } from "vue-router";
import Home from "@/components/HomeComponent.vue";
import LoginComponent from "@/components/LoginComponent.vue";
import RegisterComponent from "@/components/RegisterComponent.vue";
import TransactionsComponent from "@/components/TransactionsComponent.vue";
import ProfileComponent from "@/components/ProfileComponent.vue";
import TransactionDetailsComponent from "@/components/TransactionDetailsComponent.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      name: "home",
      component: Home,
    },
    {
      path: "/login",
      name: "login",
      component: LoginComponent,
    },
    {
      path: "/register",
      name: "register",
      component: RegisterComponent,
    },
    {
      path: "/transactions",
      name: "transactions",
      component: TransactionsComponent,
    },
    {
      path: "/profile",
      name: "profile",
      component: ProfileComponent,
    },
    {
      path: "/transaction/:id",
      name: "single-transaction",
      component: TransactionDetailsComponent,
    },
  ],
});

export default router;
