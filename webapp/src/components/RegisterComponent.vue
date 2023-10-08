<script lang="ts">
import InputField from "@/fields/InputField.vue";
import { AuthService } from "@/services/authService";
import { ErrorService } from "@/services/errorService";

type RegistrationDataType = {
  username: string;
  password: string;
  publicName: string;
};
type ValidationError = {
  message: string;
  field: string;
};

export default {
  components: { InputField },
  data() {
    return {
      password: "",
      username: "",
      publicName: "",
      errors: [] as ValidationError[],
    };
  },
  setup() {
    const authService = new AuthService();
    return {
      authService,
    };
  },
  methods: {
    validateForm(registrationData: RegistrationDataType) {
      if (!registrationData.password) {
        this.errors.push({
          field: "password",
          message: "Password is required",
        });
      }
      if (!registrationData.username) {
        this.errors.push({
          field: "username",
          message: "Username is required",
        });
      }
      if (!registrationData.publicName) {
        this.errors.push({
          field: "publicName",
          message: "Public name is required",
        });
      }
    },
    async submit(e: PointerEvent) {
      this.errors = [];
      e.preventDefault();
      const registrationData = {
        username: this.username,
        password: this.password,
        publicName: this.publicName,
      };
      this.validateForm(registrationData);
      if (this.errors.length == 0) {
        await this.authService.register(registrationData);
        ErrorService.dispatchInfo({ message: "User registered successfully" });
      }
    },
  },
};
</script>

<template>
  <div class="wrapper">
    <form id="loginForm">
      <div>
        <p
          v-for="err in errors"
          v-bind:key="err.field"
          class="error"
          :data-qa="err.field + '-error'"
        >
          {{ err.message }}
        </p>
      </div>
      <div class="field">
        <InputField
          name="username"
          placeholder="Username"
          @update:fieldValue="
            (e) => {
              this.username = e;
            }
          "
        ></InputField>
      </div>
      <div class="field">
        <InputField
          name="password"
          placeholder="Password"
          type="password"
          @update:fieldValue="
            (e) => {
              this.password = e;
            }
          "
        />
      </div>
      <div class="field">
        <InputField
          name="publicName"
          placeholder="Public name"
          @update:fieldValue="
            (e) => {
              this.publicName = e;
            }
          "
        />
      </div>
      <button type="submit" @click="submit">Submit</button>
    </form>
  </div>
</template>

<style scoped>
.error {
  color: indianred;
}

.wrapper {
  display: flex;
  flex-direction: column;
  justify-content: center;
  width: 500px;
}

.field input {
  height: 50px;
  margin-top: 10px;
  width: 100%;
}

.field {
  padding-left: 10px;
  margin-left: 50px;
  margin-right: 50px;
  justify-content: center;
}

#loginForm button {
  width: 40%;
  align-self: center;
  background-color: red;
  color: white;
}

#loginForm {
  display: flex;
  flex-direction: column;
  gap: 30px;
  justify-content: center;
  height: 50vh;
}
</style>
