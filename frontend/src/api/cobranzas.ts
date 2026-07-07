import { client } from './client';
import type { CobranzaDTO, CrearCobranzaRequest } from '../types';

export const getCobranzas = () => client.get<CobranzaDTO[]>('/api/cobranzas').then((r: any) => r.data);
export const registrarCobranza = (data: CrearCobranzaRequest) => client.post<CobranzaDTO>('/api/cobranzas', data).then((r: any) => r.data);
