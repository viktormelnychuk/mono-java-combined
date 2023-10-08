<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
  props: {
    placeholder: String,
    name: String,
    type: {
      type: String,
      default: "input",
    },
    value: {
      type: String,
      default: "",
    },
  },
  emits: ["update:fieldValue"],
  setup(props, { emit }) {
    function onChanged(e: Event) {
      //@ts-ignore
      emit("update:fieldValue", e.currentTarget.value);
    }

    return { onChanged };
  },
});
</script>

<template>
  <input
    :placeholder="placeholder"
    :name="name"
    :type="type"
    :value="value"
    @input="onChanged"
  />
</template>

<style scoped>
input {
  height: 50px;
  margin-top: 10px;
  width: 100%;
}
</style>
