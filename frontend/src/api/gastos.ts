import { client } from './client';
import type { GastoDTO, CrearGastoRequest } from '../types';

export const getGastos = () => client.get<GastoDTO[]>('/api/gastos').then((r: any) => r.data);
export const crearGasto = (data: CrearGastoRequest) => client.post<GastoDTO>('/api/gastos', data).then((r: any) => r.data);
export const eliminarGasto = (id: string) => client.delete(`/api/gastos/${id}`);
