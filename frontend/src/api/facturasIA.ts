import { client } from './client';

export interface LineaFacturaDTO {
  productoId: string | null;
  esNuevo: boolean;
  sku: string;
  nombre: string;
  cantidad: number;
  precioUnitario: number;
  ubicacionFisica: string | null;
  stockMinimo: number;
  moneda: string;
}

export interface FacturaBorradorDTO {
  id: string;
  fechaRecepcion: string;
  fechaFactura: string | null;
  proveedorNombre: string | null;
  cuit: string | null;
  numeroFactura: string | null;
  imagenBase64: string | null;
  textoOcr: string | null;
  montoTotal: number | null;
  categoriaGasto: string | null;
  estadoPago: string;
  lineas: LineaFacturaDTO[];
  estado: string;
  observaciones: string | null;
}

export interface ConfirmarFacturaResultDTO {
  compraId: string;
  gastoId: string;
  productosCreados: number;
  productosActualizados: number;
}

export interface ActualizarFacturaBorradorRequest {
  fechaFactura: string | null;
  proveedorNombre: string | null;
  cuit: string | null;
  numeroFactura: string | null;
  montoTotal: number | null;
  categoriaGasto: string | null;
  estadoPago: string;
  lineas: LineaFacturaDTO[];
  observaciones: string | null;
}

export const getFacturasBorradores = () =>
  client.get<FacturaBorradorDTO[]>('/api/facturas-ia/borradores').then(r => r.data);

export const actualizarFacturaBorrador = (id: string, data: ActualizarFacturaBorradorRequest) =>
  client.put<FacturaBorradorDTO>(`/api/facturas-ia/borradores/${id}`, data).then(r => r.data);

export const confirmarFacturaBorrador = (id: string) => {
  const usuarioId = localStorage.getItem('mmotos_userId');
  const qs = usuarioId ? `?usuarioId=${encodeURIComponent(usuarioId)}` : '';
  return client.post<ConfirmarFacturaResultDTO>(`/api/facturas-ia/borradores/${id}/confirmar${qs}`).then(r => r.data);
};

export const rechazarFacturaBorrador = (id: string) =>
  client.delete(`/api/facturas-ia/borradores/${id}`);
