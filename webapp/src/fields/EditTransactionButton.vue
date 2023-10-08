<script>
import { defineComponent } from "vue";

export default defineComponent({
  props: {
    id: Number,
  },
  methods: {
    clicked() {
      this.$router.push({
        name: "single-transaction",
        params: { id: this.id },
      });
    },
  },
});
</script>

<template>
  <ElButton @click="clicked">Edit</ElButton>
</template>
