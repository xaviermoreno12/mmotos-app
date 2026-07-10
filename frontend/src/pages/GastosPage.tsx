import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getGastos, crearGasto, eliminarGasto } from '../api/gastos';
import type { GastoDTO } from '../types';
import { Spinner } from '../components/ui/Spinner';
import { Header } from '../components/layout/Header';

const CATEGORIAS = [
  'Repuestos y Mercadería', 'Herramientas y Maquinaria', 'Insumos de Taller',
  'Combustible y Flete', 'Servicios y Mantenimiento', 'Alquiler', 'Servicios',
  'Insumos', 'Sueldos', 'Impuestos', 'Logística', 'Publicidad', 'Otros',
];

export function GastosPage() {
  const qc = useQueryClient();
  const { data: gastos = [], isLoading } = useQuery({ queryKey: ['gastos'], queryFn: getGastos });
  const [show, setShow] = useState(false);
  const [confirmEliminar, setConfirmEliminar] = useState<string | null>(null);
  const [form, setForm] = useState({ descripcion: '', categoria: 'Otros', monto: '', metodoPago: 'EFECTIVO', observaciones: '' });

  const crearMut = useMutation({
    mutationFn: () => crearGasto({
      descripcion: form.descripcion, categoria: form.categoria,
      monto: parseFloat(form.monto), metodoPago: form.metodoPago,
      observaciones: form.observaciones || undefined,
      usuarioId: localStorage.getItem('mmotos_userId') || undefined,
    }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['gastos'] }); setShow(false); setForm({ descripcion: '', categoria: 'Otros', monto: '', metodoPago: 'EFECTIVO', observaciones: '' }); },
  });

  const eliminarMut = useMutation({
    mutationFn: eliminarGasto,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['gastos'] }),
  });

  const total = (gastos as GastoDTO[]).reduce((a, g) => a + g.monto, 0);

  return (
    <div className="min-h-screen bg-surface-container">
      <Header title="Gastos" />
      <div className="pt-11 p-6">
      <div className="flex items-center justify-between mb-4">
        <div>
          <h2 className="text-on-surface text-sm font-semibold uppercase tracking-widest">Gastos</h2>
          <p className="text-on-surface-variant text-xs mt-0.5">Total: <span className="text-error font-semibold">${total.toLocaleString('es-AR')}</span></p>
        </div>
        <button className="btn-primary" onClick={() => setShow(true)}>+ Nuevo Gasto</button>
      </div>

      {isLoading ? <div className="flex justify-center py-12"><Spinner /></div> : (
        <div className="card p-0 overflow-hidden">
          <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead><tr className="table-header">
              <th className="px-4 py-3 text-left">Fecha</th>
              <th className="px-4 py-3 text-left">Descripción</th>
              <th className="px-4 py-3 text-left">Categoría</th>
              <th className="px-4 py-3 text-left">Método</th>
              <th className="px-4 py-3 text-right">Monto</th>
              <th className="px-4 py-3"></th>
            </tr></thead>
            <tbody>
              {(gastos as GastoDTO[]).length === 0
                ? <tr><td colSpan={6} className="px-4 py-8 text-center text-on-surface-variant">Sin gastos registrados</td></tr>
                : (gastos as GastoDTO[]).map(g => (
                  <tr key={g.id} className="table-row">
                    <td className="px-4 py-3 text-on-surface-variant">{new Date(g.fecha).toLocaleDateString('es-AR')}</td>
                    <td className="px-4 py-3 font-medium">{g.descripcion}</td>
                    <td className="px-4 py-3"><span className="text-xs px-2 py-0.5 rounded bg-outline-variant/50 text-on-surface-variant">{g.categoria}</span></td>
                    <td className="px-4 py-3 text-on-surface-variant text-xs">{g.metodoPago}</td>
                    <td className="px-4 py-3 text-right text-error font-mono">${g.monto.toLocaleString('es-AR')}</td>
                    <td className="px-4 py-3 text-center">
                      {confirmEliminar === g.id ? (
                        <span className="flex items-center gap-1 justify-center">
                          <button className="text-xs text-error font-medium hover:opacity-70" onClick={() => { eliminarMut.mutate(g.id); setConfirmEliminar(null); }}>Sí</button>
                          <button className="text-xs text-on-surface-variant hover:opacity-70" onClick={() => setConfirmEliminar(null)}>No</button>
                        </span>
                      ) : (
                        <button className="text-error text-xs hover:opacity-70" onClick={() => setConfirmEliminar(g.id)}>✕</button>
                      )}
                    </td>
                  </tr>
                ))}
            </tbody>
          </table>
          </div>
        </div>
      )}

      {show && (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50" onClick={() => setShow(false)}>
          <div className="card w-full max-w-md space-y-3" onClick={e => e.stopPropagation()}>
            <h3 className="text-on-surface font-semibold text-sm uppercase tracking-widest">Nuevo Gasto</h3>
            <div><label className="kpi-label block mb-1">Descripción *</label>
              <input className="input" value={form.descripcion} onChange={e => setForm(f => ({...f, descripcion: e.target.value}))} required /></div>
            <div className="grid grid-cols-2 gap-3">
              <div><label className="kpi-label block mb-1">Categoría</label>
                <select className="input" value={form.categoria} onChange={e => setForm(f => ({...f, categoria: e.target.value}))}>
                  {CATEGORIAS.map(c => <option key={c}>{c}</option>)}
                </select></div>
              <div><label className="kpi-label block mb-1">Monto *</label>
                <input className="input" type="number" min="0.01" step="0.01" value={form.monto} onChange={e => setForm(f => ({...f, monto: e.target.value}))} /></div>
            </div>
            <div><label className="kpi-label block mb-1">Método de pago</label>
              <select className="input" value={form.metodoPago} onChange={e => setForm(f => ({...f, metodoPago: e.target.value}))}>
                <option value="EFECTIVO">Efectivo</option>
                <option value="TRANSFERENCIA">Transferencia</option>
                <option value="TARJETA_DEBITO">Tarjeta Débito</option>
                <option value="TARJETA_CREDITO">Tarjeta Crédito</option>
              </select></div>
            <div><label className="kpi-label block mb-1">Observaciones</label>
              <input className="input" value={form.observaciones} onChange={e => setForm(f => ({...f, observaciones: e.target.value}))} /></div>
            <div className="flex gap-2 pt-2">
              <button className="btn-primary flex-1" onClick={() => crearMut.mutate()} disabled={!form.descripcion || !form.monto || crearMut.isPending}>
                {crearMut.isPending ? <Spinner size="sm" /> : 'Guardar'}
              </button>
              <button className="btn-secondary flex-1" onClick={() => setShow(false)}>Cancelar</button>
            </div>
          </div>
        </div>
      )}
      </div>
    </div>
  );
}
