import { client } from './client';
import type { VentaRequest, VentaResponse, VentaListDTO, VentaDetalleCompletoDTO, CajaResumenDTO, CajaDTO, AbrirCajaRequest, CerrarCajaRequest } from '../types';

export async function realizarVenta(request: VentaRequest): Promise<VentaResponse> {
  const response = await client.post<VentaResponse>('/api/ventas', request);
  return response.data;
}

export async function listarVentas(desde?: string, hasta?: string): Promise<VentaListDTO[]> {
  const params: Record<string, string> = {};
  if (desde) params.desde = desde;
  if (hasta) params.hasta = hasta;
  const response = await client.get<VentaListDTO[]>('/api/ventas', { params });
  return response.data;
}

export async function resumenCaja(fecha?: string): Promise<CajaResumenDTO> {
  const params: Record<string, string> = {};
  if (fecha) params.fecha = fecha;
  const response = await client.get<CajaResumenDTO>('/api/caja/resumen', { params });
  return response.data;
}

export async function getVentaCompleta(id: string): Promise<VentaDetalleCompletoDTO> {
  const response = await client.get<VentaDetalleCompletoDTO>(`/api/ventas/${id}`);
  return response.data;
}

export async function anularVenta(id: string, motivo: string): Promise<void> {
  await client.patch(`/api/ventas/${id}/anular`, { motivo });
}

export async function actualizarCotizacion(valorDolar: number): Promise<void> {
  await client.put('/api/config/cotizacion', { valorDolar });
}

// ---- Caja (cash register sessions) ----

export async function obtenerCajaActiva(): Promise<CajaDTO | null> {
  const response = await client.get<CajaDTO>('/api/caja/activa', {
    validateStatus: (status) => status === 200 || status === 204,
  });
  if (response.status === 204) return null;
  return response.data;
}

export async function abrirCaja(request: AbrirCajaRequest): Promise<CajaDTO> {
  const response = await client.post<CajaDTO>('/api/caja/abrir', request);
  return response.data;
}

export async function cerrarCaja(request: CerrarCajaRequest): Promise<CajaDTO> {
  const response = await client.post<CajaDTO>('/api/caja/cerrar', request);
  return response.data;
}

export async function getHistorialCajas(n = 10): Promise<CajaDTO[]> {
  const response = await client.get<CajaDTO[]>('/api/caja/historial', { params: { n } });
  return response.data;
}

