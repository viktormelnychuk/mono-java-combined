<script>
export default {
  created() {
    window.addEventListener("message", this.setMessage);
  },
  data() {
    return {
      message: "",
      type: "",
    };
  },
  methods: {
    setMessage(event) {
      if (event.data && event.data.type) {
        this.message = event.data.text;
        this.type = event.data.type;
        setTimeout(() => {
          this.message = "";
        }, 3000);
      }
    },
  },
};
</script>

<template>
  <h4 :data-qa="type + '-message'">{{ message }}</h4>
</template>
