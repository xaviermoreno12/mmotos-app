import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Header } from '../components/layout/Header';
import { EmptyState } from '../components/ui/EmptyState';
import { Spinner } from '../components/ui/Spinner';
import { useVentasList } from '../hooks/useVentasList';
import { anularVenta, descargarFacturaPdf } from '../api/ventas';
import type { VentaListDTO } from '../types';

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('es-AR');
}
function formatTime(iso: string) {
  return new Date(iso).toLocaleTimeString('es-AR', { hour: '2-digit', minute: '2-digit' });
}
function formatMoney(n: number) {
  return n.toLocaleString('es-AR', { style: 'currency', currency: 'ARS' });
}

type Rango = 'hoy' | 'semana' | 'mes';

function getLocalDate(d: Date): string {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

function getRangoFechas(rango: Rango): { desde: string; hasta: string } {
  const hoy = new Date();
  const hasta = getLocalDate(hoy);
  let desde = hasta;
  if (rango === 'semana') {
    const d = new Date(hoy); d.setDate(d.getDate() - d.getDay());
    desde = getLocalDate(d);
  } else if (rango === 'mes') {
    desde = `${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, '0')}-01`;
  }
  return { desde, hasta };
}

export function VentasPage() {
  const qc = useQueryClient();
  const navigate = useNavigate();
  const rol = localStorage.getItem('mmotos_rol');
  const [rango, setRango] = useState<Rango>('mes');
  const [busqueda, setBusqueda] = useState('');
  const [anulando, setAnulando] = useState<VentaListDTO | null>(null);
  const [motivo, setMotivo] = useState('');
  const [anulError, setAnulError] = useState('');
  const [descargando, setDescargando] = useState<string | null>(null);

  const { desde, hasta } = getRangoFechas(rango);
  const { data: ventas, isLoading, error } = useVentasList(desde, hasta);

  const handleDescargarPdf = async (v: VentaListDTO) => {
    setDescargando(v.id);
    const nombre = `ticket-${v.numeroTicket ?? v.id.slice(0, 8)}.pdf`;
    try {
      await descargarFacturaPdf(v.id, nombre);
    } finally {
      setDescargando(null);
    }
  };

  const anularMut = useMutation({
    mutationFn: () => anularVenta(anulando!.id, motivo),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['ventas'] });
      setAnulando(null); setMotivo(''); setAnulError('');
    },
    onError: (e: any) => setAnulError(e.response?.data?.detail || 'Error al anular la venta'),
  });

  const ventasFiltradas = (ventas ?? []).filter(v => {
    if (!busqueda.trim()) return true;
    const term = busqueda.toLowerCase();
    return (v.numeroTicket?.toLowerCase().includes(term)) ||
      v.cajero.toLowerCase().includes(term) ||
      v.id.toLowerCase().includes(term);
  });

  const totalVentas = ventasFiltradas.filter(v => !v.anulada).reduce((acc, v) => acc + v.total, 0);

  return (
    <div className="min-h-screen bg-surface-container">
      <Header title="Ventas"
        action={<button className="btn-primary flex items-center gap-1.5" onClick={() => navigate('/pos')}>
          <span className="material-symbols-outlined text-[16px]">add</span>Cargar venta
        </button>}
      />
      <div className="pt-11 p-5 space-y-5">
        <div className="grid grid-cols-2 gap-3">
          <div className="card"><p className="kpi-label">Ventas</p><p className="kpi-value">{ventasFiltradas.length}</p></div>
          <div className="card"><p className="kpi-label">Total</p><p className="kpi-value">{formatMoney(totalVentas)}</p></div>
        </div>

        <div className="flex items-center gap-2">
          {(['hoy', 'semana', 'mes'] as Rango[]).map(r => (
            <button key={r} className={rango === r ? 'btn-secondary' : 'btn-ghost'} onClick={() => setRango(r)}>
              {r === 'hoy' ? 'Hoy' : r === 'semana' ? 'Esta semana' : 'Este mes'}
            </button>
          ))}
          <div className="ml-auto">
            <input className="input w-52" placeholder="Buscar..." value={busqueda} onChange={e => setBusqueda(e.target.value)} />
          </div>
        </div>

        <div className="card p-0 overflow-hidden">
          {isLoading ? (
            <div className="flex justify-center py-12"><Spinner /></div>
          ) : error ? (
            <div className="px-4 py-12 text-center text-error">Error al cargar ventas</div>
          ) : (
            <table className="w-full">
              <thead>
                <tr className="table-header">
                  <th className="text-left px-4 py-3">Ticket</th>
                  <th className="text-left px-4 py-3">Fecha</th>
                  <th className="text-left px-4 py-3">Hora</th>
                  <th className="text-right px-4 py-3">Total</th>
                  <th className="text-center px-4 py-3">Cajero</th>
                  <th className="text-center px-4 py-3">Pagos</th>
                  <th className="text-center px-4 py-3">Estado</th>
                  <th className="px-4 py-3"></th>
                  {rol === 'DUENO' && <th className="px-4 py-3"></th>}
                </tr>
              </thead>
              <tbody>
                {ventasFiltradas.length === 0 ? (
                  <tr><td colSpan={rol === 'DUENO' ? 9 : 8} className="px-4 py-12 text-center">
                    <EmptyState label="Sin ventas registradas" />
                  </td></tr>
                ) : ventasFiltradas.map(v => (
                  <tr key={v.id} className={`border-b border-outline-variant transition-colors ${v.anulada ? 'opacity-50' : 'hover:bg-surface-container-high'}`}>
                    <td className="px-4 py-3 text-sm font-mono">{v.numeroTicket ?? '—'}</td>
                    <td className="px-4 py-3 text-sm text-on-surface-variant">{formatDate(v.fechaEmision)}</td>
                    <td className="px-4 py-3 text-sm text-on-surface-variant">{formatTime(v.fechaEmision)}</td>
                    <td className="px-4 py-3 text-sm text-right font-semibold">{formatMoney(v.total)}</td>
                    <td className="px-4 py-3 text-sm text-on-surface-variant text-center">{v.cajero}</td>
                    <td className="px-4 py-3 text-xs text-on-surface-variant text-center">{v.pagos.map(p => p.metodo).join(', ')}</td>
                    <td className="px-4 py-3 text-center">
                      {v.anulada ? (
                        <span className="text-xs px-2 py-0.5 rounded bg-red-900/40 text-red-400 font-medium">ANULADO</span>
                      ) : (
                        <span className={`text-xs px-2 py-0.5 rounded ${
                          v.estadoFiscal === 'APROBADO' ? 'bg-green-900/30 text-green-400' :
                          v.estadoFiscal === 'PENDIENTE' ? 'bg-yellow-900/30 text-yellow-400' :
                          'bg-red-900/30 text-red-400'
                        }`}>{v.estadoFiscal}</span>
                      )}
                    </td>
                    <td className="px-4 py-3 text-center">
                      <button
                        onClick={() => handleDescargarPdf(v)}
                        disabled={descargando === v.id}
                        title="Descargar PDF"
                        className="text-on-surface-variant hover:text-primary disabled:opacity-40 transition-colors"
                      >
                        <span className="material-symbols-outlined text-[18px]">
                          {descargando === v.id ? 'hourglass_empty' : 'download'}
                        </span>
                      </button>
                    </td>
                    {rol === 'DUENO' && (
                      <td className="px-4 py-3 text-center">
                        {!v.anulada && (
                          <button onClick={() => { setAnulando(v); setMotivo(''); setAnulError(''); }}
                            className="text-xs text-error hover:opacity-70 font-medium">
                            Anular
                          </button>
                        )}
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>

      {/* Modal anular */}
      {anulando && (
        <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50" onClick={() => setAnulando(null)}>
          <div className="card w-full max-w-md space-y-4" onClick={e => e.stopPropagation()}>
            <div>
              <h3 className="font-semibold text-on-surface text-sm uppercase tracking-widest">Anular Venta</h3>
              <p className="text-xs text-on-surface-variant mt-1">
                Ticket: <span className="font-mono">{anulando.numeroTicket ?? anulando.id.slice(0, 8)}</span>
                {' · '}Total: <span className="font-semibold">{formatMoney(anulando.total)}</span>
              </p>
            </div>
            <div>
              <label className="kpi-label block mb-1">Motivo de anulación *</label>
              <textarea
                className="input resize-none h-20"
                placeholder="Ej: Error en precio, devolución del cliente..."
                value={motivo}
                onChange={e => setMotivo(e.target.value)}
              />
            </div>
            <p className="text-xs text-on-surface-variant bg-surface-container-high rounded p-2">
              ⚠️ Esta acción revierte el stock de todos los productos de la venta y no se puede deshacer.
            </p>
            {anulError && <p className="text-error text-xs">{anulError}</p>}
            <div className="flex gap-2 pt-1">
              <button className="btn-primary flex-1 flex items-center justify-center gap-2"
                style={{ background: 'rgb(220 38 38)' }}
                onClick={() => anularMut.mutate()}
                disabled={!motivo.trim() || anularMut.isPending}>
                {anularMut.isPending ? <Spinner size="sm" /> : null}
                Confirmar anulación
              </button>
              <button className="btn-secondary flex-1" onClick={() => setAnulando(null)}>Cancelar</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
