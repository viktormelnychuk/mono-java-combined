import type { ErrorResponse } from "@/services/entities/error";

export class ErrorService {
  static dispatchError(error: ErrorResponse) {
    const message = {
      type: "error",
      text: error.message,
    };
    window.postMessage(message);
  }
  static dispatchInfo(msg: string) {
    const message = {
      type: "info",
      text: msg,
    };
    window.postMessage(message);
  }
}
