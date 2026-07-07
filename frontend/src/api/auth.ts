import { client } from './client';
import type { LoginRequest, LoginResponse } from '../types';

export async function loginApi(data: LoginRequest): Promise<LoginResponse> {
  const response = await client.post<LoginResponse>('/api/auth/login', data);
  return response.data;
}
