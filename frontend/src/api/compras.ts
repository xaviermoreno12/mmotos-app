import { client } from './client';
import type { CompraDTO, CrearCompraRequest } from '../types';

export const getCompras = () =>
  client.get<CompraDTO[]>('/api/compras').then((r: { data: CompraDTO[] }) => r.data);

export const registrarCompra = (data: CrearCompraRequest) =>
  client.post<CompraDTO>('/api/compras', data).then((r: { data: CompraDTO }) => r.data);

// --- Borradores ---

export interface LineaBorradorDTO {
  productoId: string | null;
  sku: string | null;
  nombre: string;
  cantidad: number;
  precioUnitario: number;
}

export interface CompraBorradorDTO {
  id: string;
  fechaRecepcion: string;
  proveedorNombre: string | null;
  numeroRemito: string | null;
  imagenBase64: string | null;
  lineas: LineaBorradorDTO[];
  estado: string;
  observaciones: string | null;
}

export const getBorradores = () =>
  client.get<CompraBorradorDTO[]>('/api/compras/borradores').then(r => r.data);

export const actualizarBorrador = (id: string, lineas: LineaBorradorDTO[]) =>
  client.put<CompraBorradorDTO>(`/api/compras/borradores/${id}`, { lineas }).then(r => r.data);

export const confirmarBorrador = (id: string) =>
  client.post<CompraDTO>(`/api/compras/borradores/${id}/confirmar`).then(r => r.data);

export const rechazarBorrador = (id: string) =>
  client.delete(`/api/compras/borradores/${id}`);
