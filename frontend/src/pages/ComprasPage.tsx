import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getCompras, registrarCompra } from '../api/compras';
import { getProveedores } from '../api/proveedores';
import type { CompraDTO, LineaCompraRequest, CrearCompraRequest, ProveedorDTO, ProductoDTO } from '../types';
import { Spinner } from '../components/ui/Spinner';
import { client } from '../api/client';

interface LineaUI extends LineaCompraRequest {
  _key: number;
}

let _key = 0;
const newLinea = (): LineaUI => ({
  _key: ++_key,
  productoId: '',
  skuHistorico: '',
  nombreHistorico: '',
  cantidad: 1,
  precioUnitario: 0,
});

export function ComprasPage() {
  const qc = useQueryClient();
  const [showForm, setShowForm] = useState(false);
  const [proveedorId, setProveedorId] = useState('');
  const [proveedorNombre, setProveedorNombre] = useState('');
  const [numeroRemito, setNumeroRemito] = useState('');
  const [metodoPago, setMetodoPago] = useState('EFECTIVO');
  const [observaciones, setObservaciones] = useState('');
  const [lineas, setLineas] = useState<LineaUI[]>([newLinea()]);
  const [busquedaProducto, setBusquedaProducto] = useState<Record<number, string>>({});
  const [resultados, setResultados] = useState<Record<number, ProductoDTO[]>>({});
  const [formError, setFormError] = useState('');

  const { data: compras = [], isLoading } = useQuery({ queryKey: ['compras'], queryFn: getCompras });
  const { data: proveedores = [] } = useQuery({ queryKey: ['proveedores'], queryFn: () => getProveedores() });

  const registrarMut = useMutation({
    mutationFn: registrarCompra,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['compras'] });
      qc.invalidateQueries({ queryKey: ['productos'] });
      resetForm();
    },
    onError: (e: any) => setFormError(e.response?.data?.mensaje || 'Error al registrar compra'),
  });

  const resetForm = () => {
    setShowForm(false); setProveedorId(''); setProveedorNombre(''); setNumeroRemito('');
    setMetodoPago('EFECTIVO'); setObservaciones(''); setLineas([newLinea()]);
    setBusquedaProducto({}); setResultados({}); setFormError('');
  };

  const buscarProducto = async (key: number, termino: string) => {
    setBusquedaProducto(p => ({ ...p, [key]: termino }));
    if (termino.length < 2) { setResultados(p => ({ ...p, [key]: [] })); return; }
    const res = await client.get<ProductoDTO[]>(`/api/productos/buscar?termino=${termino}`);
    setResultados(p => ({ ...p, [key]: res.data }));
  };

  const seleccionarProducto = (key: number, prod: ProductoDTO) => {
    setLineas(ls => ls.map(l => l._key === key
      ? { ...l, productoId: prod.id, skuHistorico: prod.sku, nombreHistorico: prod.nombre, precioUnitario: prod.precioEnPesos }
      : l
    ));
    setBusquedaProducto(p => ({ ...p, [key]: prod.nombre }));
    setResultados(p => ({ ...p, [key]: [] }));
  };

  const updateLinea = (key: number, campo: string, valor: number) =>
    setLineas(ls => ls.map(l => l._key === key ? { ...l, [campo]: valor } : l));

  const agregarLinea = () => setLineas(ls => [...ls, newLinea()]);
  const quitarLinea = (key: number) => setLineas(ls => ls.filter(l => l._key !== key));

  const total = lineas.reduce((acc, l) => acc + l.cantidad * l.precioUnitario, 0);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setFormError('');
    if (lineas.some(l => !l.productoId)) { setFormError('Completá todos los productos'); return; }
    const pNombre = proveedorId
      ? proveedores.find((p: ProveedorDTO) => p.id === proveedorId)?.nombre || proveedorNombre
      : proveedorNombre || 'Sin proveedor';

    const req: CrearCompraRequest = {
      proveedorId: proveedorId || undefined,
      proveedorNombre: pNombre,
      numeroRemito: numeroRemito || undefined,
      metodoPago,
      lineas: lineas.map(({ _key, ...l }) => l),
      observaciones: observaciones || undefined,
      usuarioId: localStorage.getItem('mmotos_userId') || undefined,
    };
    registrarMut.mutate(req);
  };

  return (
    <div className="p-6">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-on-surface text-sm font-semibold uppercase tracking-widest">Compras</h2>
        {!showForm && <button className="btn-primary" onClick={() => setShowForm(true)}>+ Nueva Compra</button>}
      </div>

      {showForm && (
        <form onSubmit={handleSubmit} className="card space-y-5 mb-6">
          <h3 className="text-on-surface font-semibold text-sm uppercase tracking-widest">Nueva Compra</h3>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="kpi-label block mb-1">Proveedor</label>
              <select className="input" value={proveedorId} onChange={e => setProveedorId(e.target.value)}>
                <option value="">Sin proveedor / ingresar manualmente</option>
                {proveedores.map((p: ProveedorDTO) => (
                  <option key={p.id} value={p.id}>{p.nombre}</option>
                ))}
              </select>
              {!proveedorId && (
                <input className="input mt-1" placeholder="Nombre del proveedor"
                  value={proveedorNombre} onChange={e => setProveedorNombre(e.target.value)} />
              )}
            </div>
            <div>
              <label className="kpi-label block mb-1">N° Remito</label>
              <input className="input" value={numeroRemito} onChange={e => setNumeroRemito(e.target.value)} placeholder="Ej: 0001-00012345" />
            </div>
            <div>
              <label className="kpi-label block mb-1">Método de pago</label>
              <select className="input" value={metodoPago} onChange={e => setMetodoPago(e.target.value)}>
                <option value="EFECTIVO">Efectivo</option>
                <option value="TRANSFERENCIA">Transferencia</option>
                <option value="TARJETA_DEBITO">Tarjeta Débito</option>
                <option value="TARJETA_CREDITO">Tarjeta Crédito</option>
                <option value="MERCADO_PAGO">MercadoPago</option>
              </select>
            </div>
            <div>
              <label className="kpi-label block mb-1">Observaciones</label>
              <input className="input" value={observaciones} onChange={e => setObservaciones(e.target.value)} />
            </div>
          </div>

          <div>
            <div className="flex items-center justify-between mb-2">
              <span className="kpi-label">Productos</span>
              <button type="button" className="btn-ghost py-1 text-xs" onClick={agregarLinea}>+ Agregar línea</button>
            </div>
            <div className="space-y-2">
              {lineas.map(linea => (
                <div key={linea._key} className="grid grid-cols-12 gap-2 items-center">
                  <div className="col-span-5 relative">
                    <input
                      className="input"
                      placeholder="Buscar producto..."
                      value={busquedaProducto[linea._key] || ''}
                      onChange={e => buscarProducto(linea._key, e.target.value)}
                    />
                    {(resultados[linea._key] || []).length > 0 && (
                      <div className="absolute z-10 top-full left-0 right-0 bg-surface-container-highest border border-outline-variant rounded mt-0.5 max-h-40 overflow-y-auto">
                        {resultados[linea._key].map(p => (
                          <button key={p.id} type="button"
                            className="w-full text-left px-3 py-2 text-xs hover:bg-surface-container-high"
                            onClick={() => seleccionarProducto(linea._key, p)}>
                            <span className="font-medium">{p.nombre}</span>
                            <span className="ml-2 text-on-surface-variant">{p.sku}</span>
                          </button>
                        ))}
                      </div>
                    )}
                  </div>
                  <div className="col-span-2">
                    <input type="number" className="input" placeholder="Cant." min={1}
                      value={linea.cantidad}
                      onChange={e => updateLinea(linea._key, 'cantidad', parseInt(e.target.value) || 1)} />
                  </div>
                  <div className="col-span-3">
                    <input type="number" className="input" placeholder="Precio unit." min={0} step={0.01}
                      value={linea.precioUnitario}
                      onChange={e => updateLinea(linea._key, 'precioUnitario', parseFloat(e.target.value) || 0)} />
                  </div>
                  <div className="col-span-1 text-right text-xs text-on-surface-variant">
                    ${(linea.cantidad * linea.precioUnitario).toLocaleString('es-AR')}
                  </div>
                  <div className="col-span-1 text-center">
                    {lineas.length > 1 && (
                      <button type="button" className="text-error text-xs hover:opacity-70"
                        onClick={() => quitarLinea(linea._key)}>✕</button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="flex items-center justify-between pt-2 border-t border-outline-variant">
            <div className="text-on-surface">
              <span className="kpi-label">Total: </span>
              <span className="font-bold text-lg">${total.toLocaleString('es-AR')}</span>
            </div>
            {formError && <p className="text-error text-xs">{formError}</p>}
            <div className="flex gap-2">
              <button type="button" className="btn-secondary" onClick={resetForm}>Cancelar</button>
              <button type="submit" className="btn-primary" disabled={registrarMut.isPending}>
                {registrarMut.isPending ? <Spinner size="sm" /> : 'Confirmar Compra'}
              </button>
            </div>
          </div>
        </form>
      )}

      {isLoading ? (
        <div className="flex justify-center py-12"><Spinner /></div>
      ) : (
        <div className="card p-0 overflow-hidden">
          <table className="w-full text-sm">
            <thead>
              <tr className="table-header">
                <th className="px-4 py-3 text-left">Fecha</th>
                <th className="px-4 py-3 text-left">Proveedor</th>
                <th className="px-4 py-3 text-left">Remito</th>
                <th className="px-4 py-3 text-left">Método</th>
                <th className="px-4 py-3 text-right">Total</th>
                <th className="px-4 py-3 text-left">Estado</th>
              </tr>
            </thead>
            <tbody>
              {compras.length === 0 ? (
                <tr><td colSpan={6} className="px-4 py-8 text-center text-on-surface-variant">Sin compras registradas</td></tr>
              ) : compras.map((c: CompraDTO) => (
                <tr key={c.id} className="table-row">
                  <td className="px-4 py-3 text-on-surface-variant">
                    {new Date(c.fecha).toLocaleDateString('es-AR')}
                  </td>
                  <td className="px-4 py-3 font-medium">{c.proveedorNombre}</td>
                  <td className="px-4 py-3 text-on-surface-variant">{c.numeroRemito || '—'}</td>
                  <td className="px-4 py-3 text-on-surface-variant text-xs">{c.metodoPago}</td>
                  <td className="px-4 py-3 text-right font-mono">${c.total.toLocaleString('es-AR')}</td>
                  <td className="px-4 py-3">
                    <span className="text-xs px-2 py-0.5 rounded bg-green-900/20 text-green-400">{c.estado}</span>
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
