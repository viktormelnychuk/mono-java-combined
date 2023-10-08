<template>
  <p>{{ message }}</p>
</template>

<script>
export default {
  created() {
    window.addEventListener("message", this.setMessage);
  },
  data() {
    return {
      message: "",
    };
  },
  methods: {
    setMessage(event) {
      if (event.data && event.data.type && event.data.type === "error") {
        this.message = event.data.text;
        setTimeout(() => {
          this.message = "";
        }, 3000);
      }
    },
  },
};
</script>
