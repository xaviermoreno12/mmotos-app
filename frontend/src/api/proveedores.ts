import { client } from './client';
import type { ProveedorDTO, CrearProveedorRequest } from '../types';

export const getProveedores = (termino?: string) =>
  client.get<ProveedorDTO[]>('/api/proveedores', { params: termino ? { termino } : {} })
    .then((r: { data: ProveedorDTO[] }) => r.data);

export const crearProveedor = (data: CrearProveedorRequest) =>
  client.post<ProveedorDTO>('/api/proveedores', data).then((r: { data: ProveedorDTO }) => r.data);

export const actualizarProveedor = (id: string, data: Record<string, unknown>) =>
  client.put<ProveedorDTO>(`/api/proveedores/${id}`, data).then((r: { data: ProveedorDTO }) => r.data);
