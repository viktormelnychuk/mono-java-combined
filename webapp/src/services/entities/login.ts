export type LoginRequest = {
  username: string;
  password: string;
};

export type LoginResponse = {
  token: string;
  type: string;
  id: number;
  username: string;
};
