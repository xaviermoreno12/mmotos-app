import { useState, useRef, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Header } from '../components/layout/Header';
import { Spinner } from '../components/ui/Spinner';
import { Badge } from '../components/ui/Badge';
import { useBuscarProductos } from '../hooks/useProductos';
import { useVenta } from '../hooks/useVenta';
import { useCartStore } from '../store/cartStore';
import { cambiarEstadoPresupuesto } from '../api/presupuestos';
import { getVentaCompleta } from '../api/ventas';
import { getMetodosPago } from '../api/metodosPago';
import type { CartItem, ProductoDTO, VentaResponse } from '../types';

function fmt(n: number) {
  return new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' }).format(n);
}

export function PosPage() {
  const location = useLocation();
  const [termino, setTermino] = useState('');
  const [showResults, setShowResults] = useState(false);
  const [historial, setHistorial] = useState<ProductoDTO[]>([]);
  const [ventaOk, setVentaOk] = useState<VentaResponse | null>(null);
  const [presupuestoId, setPresupuestoId] = useState<string | null>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const { data: metodosPago = [] } = useQuery({
    queryKey: ['metodos-pago'],
    queryFn: getMetodosPago,
  });
  const metodosCobro = metodosPago.filter(m => m.aceptaCobro && m.habilitado);

  const { data: resultados, isLoading: buscando } = useBuscarProductos(termino);
  const { items, tipoFactura, metodoPago, montoPago, addItem, loadItems, removeItem,
          updateCantidad, setTipoFactura, setMetodoPago, setMontoPago, clearCart, getTotal } = useCartStore();

  const { data: ventaCompleta, isLoading: cargandoTicket } = useQuery({
    queryKey: ['venta-completa', ventaOk?.id],
    queryFn: () => getVentaCompleta(ventaOk!.id),
    enabled: !!ventaOk?.id,
  });

  const total = getTotal();
  const vuelto = montoPago - total;
  const itemsSinPrecio = items.filter((i) => i.precioUnitario <= 0);
  const tieneItemsSinPrecio = itemsSinPrecio.length > 0;

  const venta = useVenta({
    onSuccess: (data) => {
      setVentaOk(data);
      clearCart();
      setTermino('');
      if (presupuestoId) {
        cambiarEstadoPresupuesto(presupuestoId, 'APROBADO').catch(() => {});
        setPresupuestoId(null);
      }
    },
  });

  useEffect(() => {
    const state = location.state as { presupuestoId?: string; items?: CartItem[] } | null;
    if (state?.items && state.items.length > 0) {
      loadItems(state.items);
      if (state.presupuestoId) setPresupuestoId(state.presupuestoId);
      window.history.replaceState({}, '');
    }
    inputRef.current?.focus();
  }, []);

  const handleSelectProducto = (p: ProductoDTO) => {
    addItem(p);
    setHistorial(prev => [p, ...prev.filter(h => h.id !== p.id)].slice(0, 5));
    setTermino('');
    setShowResults(false);
    inputRef.current?.focus();
  };

  const handleCobrar = () => {
    if (items.length === 0) return;
    venta.mutate({
      tipoFactura,
      cuitCliente: null,
      lineas: items.map((i) => ({ productoId: i.productoId, cantidad: i.cantidad })),
      pagos: [{ metodo: metodoPago, monto: total, numeroCupon: null, cuotas: null, cbuOrigen: null, referenciaPago: null }],
      usuarioId: localStorage.getItem('mmotos_userId'),
    });
  };

  return (
    <div className="min-h-screen bg-surface-container">
      <Header title="Lector" />
      <div className="pt-11 flex h-[calc(100vh-2.75rem-1.75rem)]">

        {/* LEFT: scan + cart */}
        <div className="flex-1 flex flex-col border-r border-outline-variant overflow-hidden">
          {/* Barcode input */}
          <div className="p-4 border-b border-outline-variant relative">
            <div className="relative">
              <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant text-[18px]">
                barcode_reader
              </span>
              <input
                ref={inputRef}
                className="input pl-10"
                placeholder="Escanear o buscar producto..."
                value={termino}
                onChange={(e) => { setTermino(e.target.value); setShowResults(true); }}
                onBlur={() => setTimeout(() => setShowResults(false), 150)}
                onFocus={() => setShowResults(true)}
                autoComplete="off"
              />
              {buscando && (
                <span className="absolute right-3 top-1/2 -translate-y-1/2">
                  <Spinner size="sm" />
                </span>
              )}
            </div>

            {/* Dropdown results / historial */}
            {showResults && termino.length < 2 && historial.length > 0 && (
              <div className="absolute left-4 right-4 top-full mt-1 bg-surface-container-high border border-outline-variant rounded shadow-lg z-10 max-h-56 overflow-y-auto">
                <p className="px-4 py-1.5 text-xs text-on-surface-variant uppercase tracking-wider border-b border-outline-variant">Recientes</p>
                {historial.map((p) => (
                  <button
                    key={p.id}
                    onMouseDown={() => handleSelectProducto(p)}
                    className="w-full flex items-center justify-between px-4 py-2.5 hover:bg-surface-container-highest text-sm text-on-surface border-b border-outline-variant last:border-0"
                  >
                    <div className="text-left">
                      <p className="font-medium">{p.nombre}</p>
                      <p className="text-on-surface-variant text-xs">{p.sku}</p>
                    </div>
                    <div className="text-right">
                      <p className="text-primary font-semibold">{fmt(p.precioEnPesos)}</p>
                      <p className="text-on-surface-variant text-xs">Stock: {p.stockActual}</p>
                    </div>
                  </button>
                ))}
              </div>
            )}
            {showResults && resultados && resultados.length > 0 && termino.length >= 2 && (
              <div className="absolute left-4 right-4 top-full mt-1 bg-surface-container-high border border-outline-variant rounded shadow-lg z-10 max-h-56 overflow-y-auto">
                {resultados.map((p) => (
                  <button
                    key={p.id}
                    onMouseDown={() => handleSelectProducto(p)}
                    className="w-full flex items-center justify-between px-4 py-2.5 hover:bg-surface-container-highest text-sm text-on-surface border-b border-outline-variant last:border-0"
                  >
                    <div className="text-left">
                      <p className="font-medium">{p.nombre}</p>
                      <p className="text-on-surface-variant text-xs">{p.sku}</p>
                    </div>
                    <div className="text-right">
                      <p className="text-primary font-semibold">{fmt(p.precioEnPesos)}</p>
                      <p className="text-on-surface-variant text-xs">Stock: {p.stockActual}</p>
                    </div>
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Cart table */}
          <div className="flex-1 overflow-y-auto">
            {items.length === 0 ? (
              <div className="flex items-center justify-center h-full text-on-surface-variant text-sm">
                Agregá productos para comenzar
              </div>
            ) : (
              <table className="w-full">
                <thead>
                  <tr className="table-header">
                    <th className="text-left px-4 py-2">Producto</th>
                    <th className="px-3 py-2 text-center">Cant.</th>
                    <th className="px-3 py-2 text-right">P. Unit.</th>
                    <th className="px-3 py-2 text-right">Subtotal</th>
                    <th className="px-2 py-2" />
                  </tr>
                </thead>
                <tbody>
                  {items.map((item) => (
                    <tr key={item.productoId} className="table-row">
                      <td className="px-4 py-2">
                        <p className="text-sm font-medium">{item.nombre}</p>
                        <p className="text-xs text-on-surface-variant">{item.sku}</p>
                      </td>
                      <td className="px-3 py-2">
                        <div className="flex items-center gap-1 justify-center">
                          <button
                            onClick={() => updateCantidad(item.productoId, item.cantidad - 1)}
                            className="w-6 h-6 rounded bg-surface-container-high text-on-surface-variant hover:text-on-surface flex items-center justify-center"
                          >−</button>
                          <span className="w-8 text-center text-sm">{item.cantidad}</span>
                          <button
                            onClick={() => updateCantidad(item.productoId, item.cantidad + 1)}
                            className="w-6 h-6 rounded bg-surface-container-high text-on-surface-variant hover:text-on-surface flex items-center justify-center"
                          >+</button>
                        </div>
                      </td>
                      <td className="px-3 py-2 text-right text-sm">{fmt(item.precioUnitario)}</td>
                      <td className="px-3 py-2 text-right text-sm font-medium">{fmt(item.cantidad * item.precioUnitario)}</td>
                      <td className="px-2 py-2">
                        <button
                          onClick={() => removeItem(item.productoId)}
                          className="material-symbols-outlined text-[16px] text-on-surface-variant hover:text-error transition-colors"
                        >delete</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>

        {/* RIGHT: payment panel */}
        <div className="w-72 flex flex-col bg-surface-container-low">
          {/* Tipo factura */}
          <div className="p-4 border-b border-outline-variant">
            <p className="kpi-label mb-2">Tipo factura</p>
            <div className="flex gap-2">
              {(['A', 'B', 'C'] as const).map((t) => (
                <button
                  key={t}
                  onClick={() => setTipoFactura(t)}
                  className={`flex-1 py-1.5 text-xs font-semibold rounded border transition-colors ${
                    tipoFactura === t
                      ? 'bg-primary-container text-white border-primary-container'
                      : 'border-outline-variant text-on-surface-variant hover:text-on-surface'
                  }`}
                >
                  {t}
                </button>
              ))}
            </div>
          </div>

          {/* Totals */}
          <div className="p-4 border-b border-outline-variant space-y-2">
            <div className="flex justify-between text-sm">
              <span className="text-on-surface-variant">Subtotal</span>
              <span>{fmt(total)}</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-on-surface-variant">IVA incluido</span>
              <span className="text-on-surface-variant">—</span>
            </div>
            <div className="flex justify-between text-base font-bold pt-1 border-t border-outline-variant">
              <span>TOTAL</span>
              <span className="text-primary">{fmt(total)}</span>
            </div>
          </div>

          {/* Método de pago */}
          <div className="p-4 border-b border-outline-variant">
            <p className="kpi-label mb-2">Método de pago</p>
            <div className="grid grid-cols-2 gap-2">
              {metodosCobro.length === 0 ? (
                <p className="col-span-2 text-xs text-on-surface-variant">Cargando métodos...</p>
              ) : metodosCobro.map((m) => (
                <button
                  key={m.codigo}
                  onClick={() => setMetodoPago(m.codigo)}
                  className={`py-2 text-xs font-medium rounded border transition-colors ${
                    metodoPago === m.codigo
                      ? 'bg-primary-container text-white border-primary-container'
                      : 'border-outline-variant text-on-surface-variant hover:text-on-surface'
                  }`}
                >
                  {m.nombre}
                </button>
              ))}
            </div>
          </div>

          {/* Monto recibido */}
          {metodoPago === 'EFECTIVO' && (
            <div className="p-4 border-b border-outline-variant">
              <label className="kpi-label block mb-1.5">Monto recibido</label>
              <input
                className="input"
                type="number"
                min={0}
                value={montoPago || ''}
                onChange={(e) => setMontoPago(Number(e.target.value))}
                placeholder="0"
              />
              {montoPago > 0 && vuelto >= 0 && (
                <p className="text-xs text-green-400 mt-1.5">
                  Vuelto: {fmt(vuelto)}
                </p>
              )}
              {montoPago > 0 && vuelto < 0 && (
                <p className="text-xs text-error mt-1.5">
                  Falta: {fmt(Math.abs(vuelto))}
                </p>
              )}
            </div>
          )}

          {/* Error */}
          {venta.isError && (
            <div className="px-4 py-2">
              <p className="text-xs text-error bg-error-container/20 px-3 py-2 rounded border border-error-container/40">
                {(venta.error as any)?.response?.data?.detail || (venta.error as Error)?.message || 'Error al procesar la venta'}
              </p>
            </div>
          )}

          {/* Advertencia precio $0 */}
          {tieneItemsSinPrecio && (
            <div className="px-4 py-2">
              <p className="text-xs text-error bg-error-container/20 px-3 py-2 rounded border border-error-container/40">
                <span className="font-semibold">Precio inválido ($0):</span>{' '}
                {itemsSinPrecio.map((i) => i.nombre).join(', ')}. Actualice el precio en Productos.
              </p>
            </div>
          )}

          {/* COBRAR */}
          <div className="p-4 mt-auto">
            <button
              onClick={handleCobrar}
              disabled={items.length === 0 || venta.isPending || tieneItemsSinPrecio}
              className="btn-primary w-full py-3 text-sm flex items-center justify-center gap-2"
            >
              {venta.isPending ? <Spinner size="sm" /> : <span className="material-symbols-outlined text-[18px]">payments</span>}
              COBRAR VENTA
            </button>
          </div>
        </div>
      </div>

      {/* Success modal */}
      {ventaOk && (
        <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50">
          <div className="card w-[420px] max-h-[85vh] flex flex-col space-y-3 overflow-hidden">
            <div className="flex items-center gap-3 flex-shrink-0">
              <span className="material-symbols-outlined text-green-400 text-[28px]">check_circle</span>
              <div>
                <p className="font-semibold text-on-surface">Venta procesada</p>
                <p className="text-on-surface-variant text-xs">Ticket emitido correctamente</p>
              </div>
            </div>

            {cargandoTicket ? (
              <div className="flex justify-center py-8"><Spinner /></div>
            ) : ventaCompleta ? (
              <div className="flex-1 overflow-y-auto space-y-2">
                <div className="bg-surface-container-high rounded p-3 space-y-1.5">
                  <div className="flex justify-between text-sm">
                    <span className="text-on-surface-variant">Ticket</span>
                    <span className="font-mono font-medium">{ventaCompleta.numeroTicket ?? '—'}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-on-surface-variant">Fecha</span>
                    <span className="text-xs">{ventaCompleta.fechaEmision ? new Date(ventaCompleta.fechaEmision).toLocaleString('es-AR') : '—'}</span>
                  </div>
                  {ventaCompleta.cae && ventaCompleta.estadoFiscal === 'APROBADO' && (
                    <div className="flex justify-between text-sm">
                      <span className="text-on-surface-variant">CAE</span>
                      <span className="font-mono text-xs">{ventaCompleta.cae}</span>
                    </div>
                  )}
                  <div className="flex justify-between text-sm">
                    <span className="text-on-surface-variant">Estado fiscal</span>
                    <Badge variant={ventaCompleta.estadoFiscal === 'APROBADO' ? 'active' : 'pending'} label={ventaCompleta.estadoFiscal} />
                  </div>
                </div>

                <table className="w-full text-xs">
                  <thead><tr className="table-header">
                    <th className="text-left px-2 py-1.5">Producto</th>
                    <th className="text-center px-2 py-1.5">Cant.</th>
                    <th className="text-right px-2 py-1.5">Subtotal</th>
                  </tr></thead>
                  <tbody>
                    {ventaCompleta.lineas.map((l, i) => (
                      <tr key={i} className="border-b border-outline-variant">
                        <td className="px-2 py-1.5">
                          <p className="font-medium">{l.nombreHistorico}</p>
                          <p className="text-on-surface-variant">{l.skuHistorico}</p>
                        </td>
                        <td className="px-2 py-1.5 text-center">{l.cantidad}</td>
                        <td className="px-2 py-1.5 text-right font-mono">{fmt(l.subtotal)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>

                <div className="bg-surface-container-high rounded p-3 space-y-1">
                  {ventaCompleta.pagos.map((p, i) => (
                    <div key={i} className="flex justify-between text-sm">
                      <span className="text-on-surface-variant">{p.metodo}</span>
                      <span className="font-mono">{fmt(p.monto)}</span>
                    </div>
                  ))}
                  <div className="flex justify-between text-sm font-bold pt-1 border-t border-outline-variant">
                    <span>TOTAL</span>
                    <span className="text-primary">{fmt(ventaCompleta.total)}</span>
                  </div>
                </div>
              </div>
            ) : null}

            <div className="flex gap-2 flex-shrink-0 pt-1">
              <button
                onClick={() => window.print()}
                className="btn-secondary flex items-center gap-1.5 px-4"
                disabled={cargandoTicket || !ventaCompleta}
              >
                {cargandoTicket
                  ? <Spinner size="sm" />
                  : <span className="material-symbols-outlined text-[16px]">print</span>}
                {cargandoTicket ? 'Cargando...' : 'Imprimir'}
              </button>
              <button onClick={() => setVentaOk(null)} className="btn-primary flex-1">
                NUEVA VENTA
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Ticket para imprimir (oculto en pantalla) */}
      {ventaCompleta && (
        <div id="ticket-print">
          <div style={{ textAlign: 'center', marginBottom: 8 }}>
            <strong>M MOTOS CORE</strong><br />
            <span style={{ fontSize: '0.8em' }}>Repuestos de motos</span>
          </div>
          <hr />
          <div>Ticket: {ventaCompleta.numeroTicket ?? '—'}</div>
          <div>Fecha: {ventaCompleta.fechaEmision ? new Date(ventaCompleta.fechaEmision).toLocaleString('es-AR') : '—'}</div>
          <hr />
          {ventaCompleta.lineas.map((l, i) => (
            <div key={i} style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span>{l.cantidad}x {l.nombreHistorico}</span>
              <span>{fmt(l.subtotal)}</span>
            </div>
          ))}
          <hr />
          {ventaCompleta.pagos.map((p, i) => (
            <div key={i} style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span>{p.metodo}</span><span>{fmt(p.monto)}</span>
            </div>
          ))}
          <div style={{ fontWeight: 'bold', display: 'flex', justifyContent: 'space-between' }}>
            <span>TOTAL</span><span>{fmt(ventaCompleta.total)}</span>
          </div>
          {ventaCompleta.cae && ventaCompleta.estadoFiscal === 'APROBADO' && (
            <><hr /><div style={{ fontSize: '0.75em' }}>CAE: {ventaCompleta.cae}</div></>
          )}
          <hr />
          <div style={{ textAlign: 'center' }}>¡Gracias por su compra!</div>
        </div>
      )}

      <style>{`
        #ticket-print { display: none; }
        @media print {
          body > * { display: none !important; }
          #ticket-print {
            display: block !important;
            width: 58mm;
            font-family: monospace;
            font-size: 10pt;
            color: #000;
          }
        }
      `}</style>
    </div>
  );
}
