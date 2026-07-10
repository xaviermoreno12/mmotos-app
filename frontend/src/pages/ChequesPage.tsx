import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getCheques, crearCheque, cambiarEstadoCheque } from '../api/cheques';
import type { ChequeDTO } from '../types';
import { Spinner } from '../components/ui/Spinner';
import { Header } from '../components/layout/Header';

const ESTADOS = ['PENDIENTE', 'COBRADO', 'RECHAZADO'];
const estadoColor: Record<string, string> = {
  PENDIENTE: 'bg-yellow-900/20 text-yellow-400',
  COBRADO:   'bg-green-900/20 text-green-400',
  RECHAZADO: 'bg-red-900/20 text-red-400',
};

export function ChequesPage() {
  const qc = useQueryClient();
  const [tab, setTab] = useState<'RECIBIDO' | 'EMITIDO'>('RECIBIDO');
  const { data: cheques = [], isLoading } = useQuery({ queryKey: ['cheques', tab], queryFn: () => getCheques(tab) });
  const [show, setShow] = useState(false);
  const [form, setForm] = useState({ tipo: 'RECIBIDO', numero: '', banco: '', librador: '', monto: '', fechaEmision: '', fechaCobro: '', observaciones: '' });

  const formVacio = { tipo: 'RECIBIDO', numero: '', banco: '', librador: '', monto: '', fechaEmision: '', fechaCobro: '', observaciones: '' };
  const cerrarModal = () => { setShow(false); setForm(formVacio); };

  const crearMut = useMutation({
    mutationFn: () => crearCheque({ ...form, monto: parseFloat(form.monto) } as any),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['cheques'] }); cerrarModal(); },
  });

  const estadoMut = useMutation({
    mutationFn: ({ id, estado }: { id: string; estado: string }) => cambiarEstadoCheque(id, estado),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['cheques'] }),
  });

  const hoy = new Date().toISOString().slice(0, 10);

  return (
    <div className="min-h-screen bg-surface-container">
      <Header title="Cheques" />
      <div className="pt-11 p-6">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-on-surface text-sm font-semibold uppercase tracking-widest">Cheques</h2>
        <button className="btn-primary" onClick={() => setShow(true)}>+ Nuevo Cheque</button>
      </div>
      <div className="flex gap-2 mb-4">
        {(['RECIBIDO', 'EMITIDO'] as const).map(t => (
          <button key={t} onClick={() => setTab(t)}
            className={`px-4 py-1.5 text-xs rounded font-medium border transition-colors ${tab === t ? 'bg-primary-container text-white border-primary-container' : 'border-outline-variant text-on-surface-variant'}`}>
            {t}S
          </button>
        ))}
      </div>

      {isLoading ? <div className="flex justify-center py-12"><Spinner /></div> : (
        <div className="card p-0 overflow-hidden">
          <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead><tr className="table-header">
              <th className="px-4 py-3 text-left">N° Cheque</th>
              <th className="px-4 py-3 text-left">Banco</th>
              <th className="px-4 py-3 text-left">Librador</th>
              <th className="px-4 py-3 text-left">F. Cobro</th>
              <th className="px-4 py-3 text-right">Monto</th>
              <th className="px-4 py-3 text-left">Estado</th>
            </tr></thead>
            <tbody>
              {(cheques as ChequeDTO[]).length === 0
                ? <tr><td colSpan={6} className="px-4 py-8 text-center text-on-surface-variant">Sin cheques</td></tr>
                : (cheques as ChequeDTO[]).map(c => (
                  <tr key={c.id} className="table-row">
                    <td className="px-4 py-3 font-mono">{c.numero}</td>
                    <td className="px-4 py-3">{c.banco}</td>
                    <td className="px-4 py-3 text-on-surface-variant">{c.librador || '—'}</td>
                    <td className="px-4 py-3 text-on-surface-variant">{c.fechaCobro}</td>
                    <td className="px-4 py-3 text-right font-mono">${c.monto.toLocaleString('es-AR')}</td>
                    <td className="px-4 py-3">
                      <select
                        className={`text-xs px-2 py-0.5 rounded font-medium border-0 cursor-pointer ${estadoColor[c.estado] || ''}`}
                        value={c.estado}
                        onChange={e => {
                          const nuevo = e.target.value;
                          if (nuevo !== c.estado && window.confirm(`¿Cambiar cheque #${c.numero} a ${nuevo}?`)) {
                            estadoMut.mutate({ id: c.id, estado: nuevo });
                          }
                        }}>
                        {ESTADOS.map(s => <option key={s} value={s}>{s}</option>)}
                      </select>
                    </td>
                  </tr>
                ))}
            </tbody>
          </table>
          </div>
        </div>
      )}

      {show && (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50" onClick={cerrarModal}>
          <div className="card w-full max-w-md space-y-3" onClick={e => e.stopPropagation()}>
            <h3 className="text-on-surface font-semibold text-sm uppercase tracking-widest">Nuevo Cheque</h3>
            <div className="grid grid-cols-2 gap-3">
              <div><label className="kpi-label block mb-1">Tipo</label>
                <select className="input" value={form.tipo} onChange={e => setForm(f => ({...f, tipo: e.target.value}))}>
                  <option value="RECIBIDO">Recibido</option>
                  <option value="EMITIDO">Emitido</option>
                </select></div>
              <div><label className="kpi-label block mb-1">N° Cheque *</label>
                <input className="input" value={form.numero} onChange={e => setForm(f => ({...f, numero: e.target.value}))} /></div>
              <div><label className="kpi-label block mb-1">Banco *</label>
                <input className="input" value={form.banco} onChange={e => setForm(f => ({...f, banco: e.target.value}))} /></div>
              <div><label className="kpi-label block mb-1">Librador</label>
                <input className="input" value={form.librador} onChange={e => setForm(f => ({...f, librador: e.target.value}))} /></div>
              <div><label className="kpi-label block mb-1">Monto *</label>
                <input className="input" type="number" min="0.01" step="0.01" value={form.monto} onChange={e => setForm(f => ({...f, monto: e.target.value}))} /></div>
              <div><label className="kpi-label block mb-1">F. Emisión *</label>
                <input className="input" type="date" value={form.fechaEmision || hoy} onChange={e => setForm(f => ({...f, fechaEmision: e.target.value}))} /></div>
              <div className="col-span-2"><label className="kpi-label block mb-1">F. Cobro *</label>
                <input className="input" type="date" value={form.fechaCobro} onChange={e => setForm(f => ({...f, fechaCobro: e.target.value}))} /></div>
            </div>
            <div className="flex gap-2 pt-2">
              <button className="btn-primary flex-1" onClick={() => crearMut.mutate()} disabled={!form.numero || !form.banco || !form.monto || !form.fechaCobro || crearMut.isPending}>
                {crearMut.isPending ? <Spinner size="sm" /> : 'Guardar'}
              </button>
              <button className="btn-secondary flex-1" onClick={cerrarModal}>Cancelar</button>
            </div>
          </div>
        </div>
      )}
      </div>
    </div>
  );
}
