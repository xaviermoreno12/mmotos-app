import { client } from './client';
import type { PresupuestoDTO, CrearPresupuestoRequest } from '../types';

export const getPresupuestos = () => client.get<PresupuestoDTO[]>('/api/presupuestos').then(r => r.data);
export const getPresupuesto = (id: string) => client.get<PresupuestoDTO>(`/api/presupuestos/${id}`).then(r => r.data);
export const crearPresupuesto = (data: CrearPresupuestoRequest) => client.post<PresupuestoDTO>('/api/presupuestos', data).then(r => r.data);
export const cambiarEstadoPresupuesto = (id: string, estado: string) =>
  client.patch<PresupuestoDTO>(`/api/presupuestos/${id}/estado`, { estado }).then(r => r.data);
