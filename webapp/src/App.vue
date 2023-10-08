<script setup lang="ts">
import { RouterView } from "vue-router";
import Nav from "@/components/NavComponent.vue";
import ErrorComponent from "@/components/ErrorComponent.vue";
</script>

<template>
  <header>
    <Nav></Nav>
  </header>
  <ErrorComponent />
  <div class="content">
    <RouterView />
  </div>
</template>
<style scoped>
.content {
  display: flex;
  justify-content: center;
  max-width: 1000px;
  background-color: aliceblue;
  height: 100vh;
  margin: 10px auto;
}
</style>
