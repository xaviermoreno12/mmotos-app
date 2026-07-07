import { client } from './client';
import type { ProductoDTO, PaginaProductosDTO, CrearProductoRequest, ActualizarProductoRequest, HistorialVentaDTO } from '../types';

export async function buscarProductos(termino: string): Promise<ProductoDTO[]> {
  const response = await client.get<ProductoDTO[]>('/api/productos/buscar', {
    params: { termino },
  });
  return response.data;
}

export async function getProductoPorId(id: string): Promise<ProductoDTO> {
  const response = await client.get<ProductoDTO>(`/api/productos/${id}`);
  return response.data;
}

export async function crearProducto(data: CrearProductoRequest): Promise<ProductoDTO> {
  const response = await client.post<ProductoDTO>('/api/productos', data);
  return response.data;
}

export async function getHistorialProducto(id: string): Promise<HistorialVentaDTO[]> {
  const response = await client.get<HistorialVentaDTO[]>(`/api/productos/${id}/historial`);
  return response.data;
}

export async function getTodosProductos(pagina = 0, tamano = 50): Promise<PaginaProductosDTO> {
  const response = await client.get<PaginaProductosDTO>('/api/productos/todos', {
    params: { pagina, tamano },
  });
  return response.data;
}

export async function actualizarProducto(id: string, data: ActualizarProductoRequest): Promise<ProductoDTO> {
  const response = await client.put<ProductoDTO>(`/api/productos/${id}`, data);
  return response.data;
}

export interface ResultadoImportacion {
  actualizados?: number;
  creados?: number;
  noEncontrados?: number;
  omitidos?: number;
  sinMatch?: string[];
  errores?: string[];
}

export async function actualizarPreciosDesdeXlsx(archivo: File): Promise<ResultadoImportacion> {
  const form = new FormData();
  form.append('archivo', archivo);
  const response = await client.post<ResultadoImportacion>('/api/productos/actualizar-precios', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return response.data;
}
