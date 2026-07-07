import { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getPresupuestos, getPresupuesto, crearPresupuesto } from '../api/presupuestos';
import { buscarProductos } from '../api/productos';
import { Spinner } from '../components/ui/Spinner';
import type { ProductoDTO, PresupuestoDTO } from '../types';

function fmt(n: number) {
  return new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' }).format(n);
}

interface ItemPresupuesto {
  productoId: string;
  skuHistorico: string;
  nombreHistorico: string;
  cantidad: number;
  precioUnitario: number;
}

export function PresupuestosPage() {
  const qc = useQueryClient();
  const navigate = useNavigate();
  const [vista, setVista] = useState<'lista' | 'nuevo'>('lista');
  const [convirtiendo, setConvirtiendo] = useState<string | null>(null);
  const { data: presupuestos = [], isLoading } = useQuery({ queryKey: ['presupuestos'], queryFn: getPresupuestos });

  // --- Formulario nuevo presupuesto ---
  const [termino, setTermino] = useState('');
  const [showResults, setShowResults] = useState(false);
  const [items, setItems] = useState<ItemPresupuesto[]>([]);
  const [clienteNombre, setClienteNombre] = useState('');
  const [observaciones, setObservaciones] = useState('');
  const [fechaValidez, setFechaValidez] = useState(() => {
    const d = new Date(); d.setDate(d.getDate() + 30);
    return d.toISOString().slice(0, 10);
  });
  const [error, setError] = useState('');
  const inputRef = useRef<HTMLInputElement>(null);

  const { data: resultados } = useQuery({
    queryKey: ['buscar', termino],
    queryFn: () => buscarProductos(termino),
    enabled: termino.length >= 2,
  });

  const crearMut = useMutation({
    mutationFn: () => crearPresupuesto({
      clienteNombre: clienteNombre || undefined,
      fechaValidez,
      lineas: items.map(i => ({
        productoId: i.productoId,
        skuHistorico: i.skuHistorico,
        nombreHistorico: i.nombreHistorico,
        cantidad: i.cantidad,
        precioUnitario: i.precioUnitario,
      })),
      observaciones: observaciones || undefined,
      usuarioId: localStorage.getItem('mmotos_userId') || undefined,
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['presupuestos'] });
      setVista('lista');
      resetForm();
    },
    onError: (e: any) => setError(e.response?.data?.detail || 'Error al crear presupuesto'),
  });

  const convertirAVenta = async (p: PresupuestoDTO) => {
    setConvirtiendo(p.id);
    try {
      const detalle = await getPresupuesto(p.id);
      const items = detalle.detalle
        .filter(d => d.productoId != null)
        .map(d => ({
          productoId: d.productoId!,
          sku: d.skuHistorico,
          nombre: d.nombreHistorico,
          cantidad: d.cantidad,
          precioUnitario: d.precioUnitario,
        }));
      navigate('/pos', { state: { presupuestoId: p.id, items } });
    } finally {
      setConvirtiendo(null);
    }
  };

  const resetForm = () => {
    setItems([]); setClienteNombre(''); setObservaciones(''); setTermino(''); setError('');
  };

  const agregar = (p: ProductoDTO) => {
    setItems(prev => {
      const existe = prev.find(i => i.productoId === p.id);
      if (existe) return prev.map(i => i.productoId === p.id ? { ...i, cantidad: i.cantidad + 1 } : i);
      return [...prev, { productoId: p.id, skuHistorico: p.sku, nombreHistorico: p.nombre, cantidad: 1, precioUnitario: p.precioEnPesos }];
    });
    setTermino(''); setShowResults(false);
    inputRef.current?.focus();
  };

  const quitarItem = (id: string) => setItems(prev => prev.filter(i => i.productoId !== id));
  const setCantidad = (id: string, c: number) => {
    if (c <= 0) quitarItem(id);
    else setItems(prev => prev.map(i => i.productoId === id ? { ...i, cantidad: c } : i));
  };

  const total = items.reduce((s, i) => s + i.cantidad * i.precioUnitario, 0);

  const imprimir = () => window.print();

  if (vista === 'nuevo') {
    return (
      <div className="p-6">
        <div className="flex items-center gap-4 mb-4">
          <button className="btn-ghost text-xs flex items-center gap-1" onClick={() => { setVista('lista'); resetForm(); }}>
            <span className="material-symbols-outlined text-[16px]">arrow_back</span> Volver
          </button>
          <h2 className="text-on-surface text-sm font-semibold uppercase tracking-widest">Nuevo Presupuesto</h2>
        </div>

        <div className="grid grid-cols-2 gap-6">
          {/* Buscador */}
          <div>
            <div className="relative mb-4">
              <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant text-[18px]">search</span>
              <input ref={inputRef} className="input pl-10"
                placeholder="Buscar producto..."
                value={termino}
                onChange={e => { setTermino(e.target.value); setShowResults(true); }}
                onBlur={() => setTimeout(() => setShowResults(false), 150)}
              />
              {showResults && resultados && resultados.length > 0 && (
                <div className="absolute left-0 right-0 top-full mt-1 bg-surface-container-high border border-outline-variant rounded z-10 max-h-52 overflow-y-auto">
                  {resultados.map((p: ProductoDTO) => (
                    <button key={p.id} onMouseDown={() => agregar(p)}
                      className="w-full flex justify-between px-4 py-2 text-sm hover:bg-surface-container-highest text-on-surface border-b border-outline-variant last:border-0">
                      <span>{p.nombre} <span className="text-xs text-on-surface-variant ml-1">{p.sku}</span></span>
                      <span className="text-primary font-medium">{fmt(p.precioEnPesos)}</span>
                    </button>
                  ))}
                </div>
              )}
            </div>

            <div className="grid grid-cols-2 gap-3 mb-3">
              <div>
                <label className="kpi-label block mb-1">Cliente</label>
                <input className="input" placeholder="Nombre del cliente..." value={clienteNombre} onChange={e => setClienteNombre(e.target.value)} />
              </div>
              <div>
                <label className="kpi-label block mb-1">Válido hasta *</label>
                <input className="input" type="date" value={fechaValidez} onChange={e => setFechaValidez(e.target.value)} />
              </div>
            </div>
            <div>
              <label className="kpi-label block mb-1">Observaciones</label>
              <input className="input" placeholder="Notas opcionales..." value={observaciones} onChange={e => setObservaciones(e.target.value)} />
            </div>
          </div>

          {/* Lista de ítems */}
          <div>
            <div className="card p-0 overflow-hidden mb-3" id="print-area">
              <div className="px-4 py-3 border-b border-outline-variant print:block hidden">
                <p className="font-bold text-sm">M MOTOS CORE — Presupuesto</p>
                {clienteNombre && <p className="text-xs text-on-surface-variant">Cliente: {clienteNombre}</p>}
                <p className="text-xs text-on-surface-variant">Válido hasta: {fechaValidez}</p>
              </div>
              <table className="w-full text-sm">
                <thead><tr className="table-header">
                  <th className="text-left px-3 py-2">Producto</th>
                  <th className="text-center px-3 py-2">Cant.</th>
                  <th className="text-right px-3 py-2">Precio</th>
                  <th className="text-right px-3 py-2">Subtotal</th>
                  <th className="px-2 py-2 print:hidden"></th>
                </tr></thead>
                <tbody>
                  {items.length === 0
                    ? <tr><td colSpan={5} className="px-4 py-8 text-center text-on-surface-variant text-xs">Agregá productos para armar el presupuesto</td></tr>
                    : items.map(i => (
                      <tr key={i.productoId} className="table-row">
                        <td className="px-3 py-2">
                          <p className="font-medium text-xs">{i.nombreHistorico}</p>
                          <p className="text-xs text-on-surface-variant">{i.skuHistorico}</p>
                        </td>
                        <td className="px-3 py-2 text-center">
                          <div className="flex items-center justify-center gap-1">
                            <button onClick={() => setCantidad(i.productoId, i.cantidad - 1)} className="w-5 h-5 rounded bg-surface-container-high text-on-surface-variant hover:text-on-surface text-xs">−</button>
                            <span className="w-6 text-center">{i.cantidad}</span>
                            <button onClick={() => setCantidad(i.productoId, i.cantidad + 1)} className="w-5 h-5 rounded bg-surface-container-high text-on-surface-variant hover:text-on-surface text-xs">+</button>
                          </div>
                        </td>
                        <td className="px-3 py-2 text-right text-xs">{fmt(i.precioUnitario)}</td>
                        <td className="px-3 py-2 text-right text-xs font-medium">{fmt(i.cantidad * i.precioUnitario)}</td>
                        <td className="px-2 py-2 text-center print:hidden">
                          <button onClick={() => quitarItem(i.productoId)} className="material-symbols-outlined text-[14px] text-on-surface-variant hover:text-error">delete</button>
                        </td>
                      </tr>
                    ))}
                </tbody>
                {items.length > 0 && (
                  <tfoot>
                    <tr className="border-t border-outline-variant">
                      <td colSpan={3} className="px-3 py-2 text-right text-sm font-bold">TOTAL</td>
                      <td className="px-3 py-2 text-right text-sm font-bold text-primary">{fmt(total)}</td>
                      <td className="print:hidden" />
                    </tr>
                  </tfoot>
                )}
              </table>
            </div>

            {error && <p className="text-error text-xs mb-2">{error}</p>}
            <div className="flex gap-2">
              <button className="btn-primary flex-1 flex items-center justify-center gap-2"
                onClick={() => crearMut.mutate()} disabled={items.length === 0 || !fechaValidez || crearMut.isPending}>
                {crearMut.isPending ? <Spinner size="sm" /> : <span className="material-symbols-outlined text-[16px]">save</span>}
                Guardar
              </button>
              <button className="btn-secondary flex items-center gap-2 px-4" onClick={imprimir} disabled={items.length === 0}>
                <span className="material-symbols-outlined text-[16px]">print</span>
                Imprimir
              </button>
            </div>
          </div>
        </div>

        <style>{`
          @media print {
            body * { visibility: hidden; }
            #print-area, #print-area * { visibility: visible; }
            #print-area {
              position: absolute;
              left: 0; top: 0;
              width: 100%;
              background: white;
              color: black;
              padding: 24px;
              font-size: 12px;
            }
            #print-area th, #print-area td {
              border: 1px solid #ccc;
              padding: 4px 8px;
              color: black;
              visibility: visible;
            }
            #print-area .print\\:hidden { display: none !important; }
          }
        `}</style>
      </div>
    );
  }

  return (
    <div className="p-6">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-on-surface text-sm font-semibold uppercase tracking-widest">Presupuestos</h2>
        <button className="btn-primary" onClick={() => setVista('nuevo')}>+ Nuevo Presupuesto</button>
      </div>

      {isLoading ? <div className="flex justify-center py-12"><Spinner /></div> : (
        <div className="card p-0 overflow-hidden">
          <table className="w-full text-sm">
            <thead><tr className="table-header">
              <th className="text-left px-4 py-3">Fecha</th>
              <th className="text-left px-4 py-3">Cliente</th>
              <th className="text-left px-4 py-3">Válido hasta</th>
              <th className="text-right px-4 py-3">Total</th>
              <th className="text-center px-4 py-3">Estado</th>
              <th className="text-center px-4 py-3">Acción</th>
            </tr></thead>
            <tbody>
              {(presupuestos as PresupuestoDTO[]).length === 0
                ? <tr><td colSpan={6} className="px-4 py-8 text-center text-on-surface-variant">Sin presupuestos registrados</td></tr>
                : (presupuestos as PresupuestoDTO[]).map(p => (
                  <tr key={p.id} className="table-row">
                    <td className="px-4 py-3 text-on-surface-variant">{new Date(p.fecha).toLocaleDateString('es-AR')}</td>
                    <td className="px-4 py-3 font-medium">{p.clienteNombre || '—'}</td>
                    <td className="px-4 py-3 text-on-surface-variant">{p.fechaValidez}</td>
                    <td className="px-4 py-3 text-right font-mono">{fmt(p.total)}</td>
                    <td className="px-4 py-3 text-center">
                      <span className={`text-xs px-2 py-0.5 rounded font-medium ${
                        p.estado === 'APROBADO' ? 'bg-green-900/20 text-green-400' :
                        p.estado === 'RECHAZADO' ? 'bg-red-900/20 text-red-400' :
                        'bg-yellow-900/20 text-yellow-400'
                      }`}>{p.estado}</span>
                    </td>
                    <td className="px-4 py-3 text-center">
                      {p.estado === 'BORRADOR' && (
                        <button
                          className="text-xs text-primary hover:underline flex items-center gap-1 mx-auto"
                          onClick={() => convertirAVenta(p)}
                          disabled={convirtiendo === p.id}
                        >
                          {convirtiendo === p.id
                            ? <Spinner size="sm" />
                            : <><span className="material-symbols-outlined text-[14px]">point_of_sale</span>Convertir</>
                          }
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
