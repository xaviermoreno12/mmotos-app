import { client } from './client';
import type { ChequeDTO, CrearChequeRequest } from '../types';

export const getCheques = (tipo?: string) =>
  client.get<ChequeDTO[]>('/api/cheques', { params: tipo ? { tipo } : {} }).then((r: any) => r.data);
export const crearCheque = (data: CrearChequeRequest) => client.post<ChequeDTO>('/api/cheques', data).then((r: any) => r.data);
export const cambiarEstadoCheque = (id: string, estado: string) =>
  client.patch<ChequeDTO>(`/api/cheques/${id}/estado`, { estado }).then((r: any) => r.data);
