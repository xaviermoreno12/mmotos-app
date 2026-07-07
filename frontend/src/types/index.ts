export interface ProductoDTO {
  id: string;
  sku: string;
  nombre: string;
  precioBase: number;
  moneda: string;
  precioEnPesos: number;
  stockActual: number;
  stockMinimo: number;
  bajominimo: boolean;
  ubicacionFisica: string;
  esKit: boolean;
  activo: boolean;
  precioCompra?: number;
}

export interface CrearProductoRequest {
  sku: string;
  nombre: string;
  precioBase: number;
  moneda: string;
  stockActual: number;
  stockMinimo: number;
  ubicacionFisica: string;
  precioCompra?: number;
}

export interface ActualizarProductoRequest {
  nombre?: string;
  precioBase?: number;
  moneda?: string;
  stockActual?: number;
  stockMinimo?: number;
  ubicacionFisica?: string;
  activo?: boolean;
  precioCompra?: number;
}

export interface CartItem {
  productoId: string;
  sku: string;
  nombre: string;
  cantidad: number;
  precioUnitario: number;
}

export interface LineaVentaRequest {
  productoId: string;
  cantidad: number;
}

export interface PagoRequest {
  metodo: string;
  monto: number;
  numeroCupon: string | null;
  cuotas: number | null;
  cbuOrigen: string | null;
  referenciaPago: string | null;
}

export interface VentaRequest {
  tipoFactura: string;
  cuitCliente: string | null;
  lineas: LineaVentaRequest[];
  pagos: PagoRequest[];
  usuarioId: string | null;
}

export interface VentaResponse {
  id: string;
  numeroTicket: string;
  cae: string | null;
  estadoFiscal: string;
  syncStatus: string;
  total: number;
  fechaEmision: string | null;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  rol: string;
  usuarioId: string;
}

export interface CotizacionRequest {
  valorDolar: number;
}

// ---- Ventas list & Caja resumen ----

export interface PagoResumenDTO {
  metodo: string;
  monto: number;
}

export interface VentaListDTO {
  id: string;
  numeroTicket: string | null;
  tipoFactura: string;
  fechaEmision: string;
  total: number;
  estadoFiscal: string;
  cajero: string;
  clienteCuit: string | null;
  pagos: PagoResumenDTO[];
  anulada: boolean;
}

export interface CajaResumenDTO {
  cantidadVentas: number;
  totalVentas: number;
  totalEfectivo: number;
  totalTarjeta: number;
  totalTransferencia: number;
  totalMercadoPago: number;
  totalOtros: number;
}

// ---- Caja (cash register sessions) ----

export interface CajaDTO {
  id: string;
  cajeroNombre: string;
  cajeroUsername: string;
  fechaApertura: string;
  fechaCierre: string | null;
  montoInicial: number;
  montoFinalSistema: number | null;
  montoFinalContado: number | null;
  diferencia: number | null;
  observaciones: string | null;
  estado: 'ABIERTA' | 'CERRADA';
  resumen: CajaResumenDTO;
}

export interface AbrirCajaRequest {
  montoInicial: number;
}

export interface CerrarCajaRequest {
  montoFinalContado: number;
  observaciones: string | null;
}

// ---- Cajeros ----

export interface CajeroDTO {
  id: string;
  nombre: string;
  username: string;
  rol: string;
  activo: boolean;
  createdAt: string | null;
}

export interface CrearCajeroRequest {
  nombre: string;
  username: string;
  password: string;
  rol: string;
}

export interface ActualizarCajeroRequest {
  nombre?: string;
  password?: string;
  rol?: string;
  activo?: boolean;
}

// ---- Métodos de pago ----

export interface MetodoPagoDTO {
  id: string;
  codigo: string;
  nombre: string;
  aceptaCobro: boolean;
  aceptaPago: boolean;
  habilitado: boolean;
  orden: number;
}

export interface ActualizarMetodoPagoRequest {
  nombre?: string;
  aceptaCobro?: boolean;
  aceptaPago?: boolean;
  habilitado?: boolean;
}

// ---- Clientes ----

export interface ClienteDTO {
  id: string;
  cuit: string | null;
  nombre: string;
  direccion: string | null;
  telefono: string | null;
  email: string | null;
  saldo: number;
  activo: boolean;
}

export interface CrearClienteRequest {
  cuit?: string;
  nombre: string;
  direccion?: string;
  telefono?: string;
  email?: string;
}

// ---- Proveedores ----

export interface ProveedorDTO {
  id: string;
  cuit: string | null;
  nombre: string;
  contacto: string | null;
  telefono: string | null;
  email: string | null;
  activo: boolean;
}

export interface CrearProveedorRequest {
  cuit?: string;
  nombre: string;
  contacto?: string;
  telefono?: string;
  email?: string;
}

// ---- Compras ----

export interface LineaCompraRequest {
  productoId: string;
  skuHistorico: string;
  nombreHistorico: string;
  cantidad: number;
  precioUnitario: number;
}

export interface CrearCompraRequest {
  proveedorId?: string;
  proveedorNombre?: string;
  numeroRemito?: string;
  metodoPago: string;
  lineas: LineaCompraRequest[];
  observaciones?: string;
  usuarioId?: string;
}

export interface CompraDetalleDTO {
  productoId: string | null;
  skuHistorico: string;
  nombreHistorico: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface CompraDTO {
  id: string;
  proveedorId: string | null;
  proveedorNombre: string;
  numeroRemito: string | null;
  fecha: string;
  total: number;
  metodoPago: string;
  estado: string;
  observaciones: string | null;
  detalle: CompraDetalleDTO[];
}

// ---- Gastos ----
export interface GastoDTO {
  id: string; fecha: string; descripcion: string; categoria: string;
  monto: number; metodoPago: string; observaciones: string | null;
}
export interface CrearGastoRequest {
  descripcion: string; categoria: string; monto: number;
  metodoPago: string; observaciones?: string; usuarioId?: string;
}

// ---- Presupuestos ----
export interface PresupuestoDTO {
  id: string; clienteId: string | null; clienteNombre: string;
  fecha: string; fechaValidez: string; total: number;
  estado: string; observaciones: string | null; detalle: CompraDetalleDTO[];
}
export interface CrearPresupuestoRequest {
  clienteId?: string; clienteNombre?: string; fechaValidez: string;
  lineas: LineaCompraRequest[]; observaciones?: string; usuarioId?: string;
}

// ---- Cobranzas ----
export interface CobranzaDTO {
  id: string; clienteId: string; clienteNombre: string;
  monto: number; fecha: string; metodoPago: string;
  referencia: string | null; observaciones: string | null;
}
export interface CrearCobranzaRequest {
  clienteId: string; monto: number; metodoPago: string;
  referencia?: string; observaciones?: string; usuarioId?: string;
}

// ---- Cheques ----
export interface ChequeDTO {
  id: string; tipo: string; numero: string; banco: string;
  librador: string | null; monto: number;
  fechaEmision: string; fechaCobro: string; estado: string;
  clienteId: string | null; proveedorId: string | null; observaciones: string | null;
}
export interface CrearChequeRequest {
  tipo: string; numero: string; banco: string; librador?: string;
  monto: number; fechaEmision: string; fechaCobro: string;
  clienteId?: string; proveedorId?: string; observaciones?: string;
}

// ---- Ticket completo de venta ----
export interface LineaTicketDTO {
  nombreHistorico: string;
  skuHistorico: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface PagoTicketDTO {
  metodo: string;
  monto: number;
}

export interface VentaDetalleCompletoDTO {
  id: string;
  numeroTicket: string | null;
  cae: string | null;
  estadoFiscal: string;
  fechaEmision: string | null;
  total: number;
  lineas: LineaTicketDTO[];
  pagos: PagoTicketDTO[];
}

// ---- Paginación de productos ----
export interface PaginaProductosDTO {
  contenido: ProductoDTO[];
  paginaActual: number;
  totalPaginas: number;
  totalElementos: number;
  esUltima: boolean;
}

// ---- Historial de ventas por producto ----
export interface HistorialVentaDTO {
  ventaId: string;
  numeroTicket: string | null;
  fechaEmision: string;
  cantidad: number;
  precioUnitario: number;
}
