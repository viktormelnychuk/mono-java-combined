<script lang="ts">
import { AuthService } from "@/services/authService";

export default {
  data() {
    return {
      activeIndex: "1",
    };
  },
  setup: () => {
    return {
      authService: new AuthService(),
    };
  },
  methods: {
    logout() {
      this.authService.logout();
      this.$router.push("/");
      this.$router.go(0);
    },
  },
};
</script>
<template>
  <el-menu
    :router="true"
    :default-active="activeIndex"
    class="el-menu-demo"
    mode="horizontal"
    :ellipsis="false"
  >
    <el-menu-item index="0">LOGO</el-menu-item>
    <el-menu-item index="/" :route="{ path: '/' }"> Home</el-menu-item>
    <el-menu-item index="/transactions">Transactions</el-menu-item>

    <div class="flex-grow" />

    <el-menu-item v-if="authService.isLoggedIn()" index="/profile"
      >Profile
    </el-menu-item>
    <el-menu-item @click="logout" v-if="authService.isLoggedIn()"
      >Logout
    </el-menu-item>
    <el-menu-item v-if="!authService.isLoggedIn()" index="/login">
      Login
    </el-menu-item>
    <el-menu-item v-if="!authService.isLoggedIn()" index="/register">
      Register
    </el-menu-item>
  </el-menu>
</template>

<style scoped>
.flex-grow {
  flex-grow: 1;
}
</style>
