import { client } from './client';
import type { ClienteDTO, CrearClienteRequest } from '../types';

export const getClientes = (termino?: string) =>
  client.get<ClienteDTO[]>('/api/clientes', { params: termino ? { termino } : {} })
    .then((r: { data: ClienteDTO[] }) => r.data);

export const crearCliente = (data: CrearClienteRequest) =>
  client.post<ClienteDTO>('/api/clientes', data).then((r: { data: ClienteDTO }) => r.data);

export const actualizarCliente = (id: string, data: Record<string, unknown>) =>
  client.put<ClienteDTO>(`/api/clientes/${id}`, data).then((r: { data: ClienteDTO }) => r.data);
