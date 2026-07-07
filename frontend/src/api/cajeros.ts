import { client } from './client';
import type { CajeroDTO, CrearCajeroRequest, ActualizarCajeroRequest } from '../types';

export const getCajeros = () =>
  client.get<CajeroDTO[]>('/api/cajeros').then((r: { data: CajeroDTO[] }) => r.data);

export const crearCajero = (data: CrearCajeroRequest) =>
  client.post<CajeroDTO>('/api/cajeros', data).then((r: { data: CajeroDTO }) => r.data);

export const actualizarCajero = (id: string, data: ActualizarCajeroRequest) =>
  client.put<CajeroDTO>(`/api/cajeros/${id}`, data).then((r: { data: CajeroDTO }) => r.data);
