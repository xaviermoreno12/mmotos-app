import { client } from './client';
import type { MetodoPagoDTO, ActualizarMetodoPagoRequest } from '../types';

export const getMetodosPago = () =>
  client.get<MetodoPagoDTO[]>('/api/metodos-pago').then((r: { data: MetodoPagoDTO[] }) => r.data);

export const actualizarMetodoPago = (id: string, data: ActualizarMetodoPagoRequest) =>
  client.put<MetodoPagoDTO>(`/api/metodos-pago/${id}`, data).then((r: { data: MetodoPagoDTO }) => r.data);

export const eliminarMetodoPago = (id: string) =>
  client.delete(`/api/metodos-pago/${id}`);
