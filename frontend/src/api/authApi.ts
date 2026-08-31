import { apiRequest } from './client';
import { LoginRequest, LoginResponse } from '../types';

export const authApi = {
  login: (data: LoginRequest): Promise<LoginResponse> => {
    return apiRequest<LoginResponse>('/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    });
  },

  logout: (): Promise<void> => {
    return apiRequest<void>('/api/auth/logout', {
      method: 'POST',
    });
  },

  getMe: (): Promise<LoginResponse> => {
    return apiRequest<LoginResponse>('/api/auth/me', {
      method: 'GET',
    });
  },
};
