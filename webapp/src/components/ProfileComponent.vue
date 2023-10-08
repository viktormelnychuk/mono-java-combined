<script>
import { UserService } from "@/services/userService";
import InputField from "@/fields/InputField.vue";

export default {
  components: { InputField },
  setup() {
    return {
      userService: new UserService(),
    };
  },
  data() {
    return {
      user: {},
      editTokenForm: false,
      newToken: "",
    };
  },
  methods: {
    toggleTokenForm() {
      this.editTokenForm = !this.editTokenForm;
    },
    async submitToken() {
      await this.userService.addMonoToken(this.newToken);
      this.user.monoToken = this.newToken;
      this.editTokenForm = false;
    },
  },
  async beforeMount() {
    this.user = await this.userService.current();
  },
};
</script>
<template>
  <div class="wrapper">
    <div class="row">
      <p class="header">Username:</p>
      <p data-id="username">{{ user.username }}</p>
    </div>
    <div class="row">
      <p class="header">Public name:</p>
      <p data-id="public-name">{{ user.publicName }}</p>
    </div>
    <div class="row">
      <p class="header">Mono Token:</p>
      <p data-id="mono-token" v-if="!editTokenForm">
        {{ user.monoToken }}
      </p>
      <div class="row">
        <button @click="toggleTokenForm" v-if="!editTokenForm">
          Update token
        </button>
        <div v-else>
          <InputField
            placeholder="Mono Token"
            v-if="editTokenForm"
            @update:fieldValue="
              (e) => {
                this.newToken = e;
              }
            "
          ></InputField>
          <button @click="submitToken">Ok</button>
          <button @click="toggleTokenForm">Cancel</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.row {
  display: flex;
  gap: 10px;
}

.header {
  font-weight: bold;
}

button {
  height: 50%;
}
</style>
