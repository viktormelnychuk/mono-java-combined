import { describe, it, expect } from "vitest";
import { mount } from "@vue/test-utils";
import LoginComponent from "../LoginComponent.vue";

describe("LoginComponent", () => {
  it("should render", () => {
    const wrapper = mount(LoginComponent);
    expect(wrapper.html()).toMatchSnapshot();
  });

  it("should shouw error if username is empty", () => {
    const wrapper = mount(LoginComponent);
    wrapper.find("el-input[placeholder='Username']").setValue("any");
    wrapper.find("el-button").trigger('click');
    expect(wrapper).toContain("username is required")
  });
});
