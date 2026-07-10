import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getCobranzas, registrarCobranza } from '../api/cobranzas';
import { getClientes } from '../api/clientes';
import type { CobranzaDTO, ClienteDTO } from '../types';
import { Spinner } from '../components/ui/Spinner';
import { Header } from '../components/layout/Header';

export function CobranzasPage() {
  const qc = useQueryClient();
  const { data: cobranzas = [], isLoading } = useQuery({ queryKey: ['cobranzas'], queryFn: getCobranzas });
  const { data: clientes = [] } = useQuery({ queryKey: ['clientes'], queryFn: () => getClientes() });
  const [show, setShow] = useState(false);
  const [form, setForm] = useState({ clienteId: '', monto: '', metodoPago: 'EFECTIVO', referencia: '', observaciones: '' });
  const [error, setError] = useState('');

  const registrarMut = useMutation({
    mutationFn: () => registrarCobranza({
      clienteId: form.clienteId, monto: parseFloat(form.monto),
      metodoPago: form.metodoPago, referencia: form.referencia || undefined,
      observaciones: form.observaciones || undefined,
      usuarioId: localStorage.getItem('mmotos_userId') || undefined,
    }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['cobranzas'] }); qc.invalidateQueries({ queryKey: ['clientes'] }); setShow(false); setError(''); },
    onError: (e: any) => setError(e.response?.data?.mensaje || 'Error'),
  });

  return (
    <div className="min-h-screen bg-surface-container">
      <Header title="Cobranzas" />
      <div className="pt-11 p-6">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-on-surface text-sm font-semibold uppercase tracking-widest">Cobranzas</h2>
        <button className="btn-primary" onClick={() => setShow(true)}>+ Registrar Cobranza</button>
      </div>

      {isLoading ? <div className="flex justify-center py-12"><Spinner /></div> : (
        <div className="card p-0 overflow-hidden">
          <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead><tr className="table-header">
              <th className="px-4 py-3 text-left">Fecha</th>
              <th className="px-4 py-3 text-left">Cliente</th>
              <th className="px-4 py-3 text-left">Método</th>
              <th className="px-4 py-3 text-left">Referencia</th>
              <th className="px-4 py-3 text-right">Monto</th>
            </tr></thead>
            <tbody>
              {(cobranzas as CobranzaDTO[]).length === 0
                ? <tr><td colSpan={5} className="px-4 py-8 text-center text-on-surface-variant">Sin cobranzas registradas</td></tr>
                : (cobranzas as CobranzaDTO[]).map(c => (
                  <tr key={c.id} className="table-row">
                    <td className="px-4 py-3 text-on-surface-variant">{new Date(c.fecha).toLocaleDateString('es-AR')}</td>
                    <td className="px-4 py-3 font-medium">{c.clienteNombre}</td>
                    <td className="px-4 py-3 text-on-surface-variant text-xs">{c.metodoPago}</td>
                    <td className="px-4 py-3 text-on-surface-variant">{c.referencia || '—'}</td>
                    <td className="px-4 py-3 text-right text-green-400 font-mono">${c.monto.toLocaleString('es-AR')}</td>
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
            <h3 className="text-on-surface font-semibold text-sm uppercase tracking-widest">Registrar Cobranza</h3>
            <div><label className="kpi-label block mb-1">Cliente *</label>
              <select className="input" value={form.clienteId} onChange={e => setForm(f => ({...f, clienteId: e.target.value}))}>
                <option value="">Seleccionar cliente...</option>
                {(clientes as ClienteDTO[]).map(c => <option key={c.id} value={c.id}>{c.nombre} {c.saldo < 0 ? `(Debe $${Math.abs(c.saldo).toLocaleString('es-AR')})` : ''}</option>)}
              </select></div>
            <div className="grid grid-cols-2 gap-3">
              <div><label className="kpi-label block mb-1">Monto *</label>
                <input className="input" type="number" min="0.01" step="0.01" value={form.monto} onChange={e => setForm(f => ({...f, monto: e.target.value}))} /></div>
              <div><label className="kpi-label block mb-1">Método</label>
                <select className="input" value={form.metodoPago} onChange={e => setForm(f => ({...f, metodoPago: e.target.value}))}>
                  <option value="EFECTIVO">Efectivo</option>
                  <option value="TRANSFERENCIA">Transferencia</option>
                  <option value="TARJETA_DEBITO">Tarjeta</option>
                  <option value="MERCADO_PAGO">MercadoPago</option>
                </select></div>
            </div>
            <div><label className="kpi-label block mb-1">Referencia / Comprobante</label>
              <input className="input" value={form.referencia} onChange={e => setForm(f => ({...f, referencia: e.target.value}))} /></div>
            {error && <p className="text-error text-xs">{error}</p>}
            <div className="flex gap-2 pt-2">
              <button className="btn-primary flex-1" onClick={() => registrarMut.mutate()} disabled={!form.clienteId || !form.monto || registrarMut.isPending}>
                {registrarMut.isPending ? <Spinner size="sm" /> : 'Confirmar'}
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
