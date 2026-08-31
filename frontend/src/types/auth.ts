export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  memberId: number | null;
  username: string | null;
  displayName: string | null;
}
