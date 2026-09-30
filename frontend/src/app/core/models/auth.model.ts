export interface LoginRequest {
  nombreUsuario: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  nombreUsuario: string;
  roles: string[];
  expiresIn?: number;
}

export interface UsuarioAuth {
  nombreUsuario: string;
  token: string;
  roles: string[];
}
