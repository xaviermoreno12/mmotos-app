import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getMetodosPago, actualizarMetodoPago, eliminarMetodoPago } from '../api/metodosPago';
import { client } from '../api/client';
import { Spinner } from '../components/ui/Spinner';
import { Header } from '../components/layout/Header';

export function MetodosPage() {
  const qc = useQueryClient();
  const rol = localStorage.getItem('mmotos_rol');
  const esDueno = rol?.toUpperCase() === 'DUENO';
  const { data: metodos = [], isLoading } = useQuery({ queryKey: ['metodos-pago'], queryFn: getMetodosPago });
  const [tab, setTab] = useState<'cobro' | 'pago'>('cobro');
  const [showAgregar, setShowAgregar] = useState(false);
  const [formNuevo, setFormNuevo] = useState({ codigo: '', nombre: '', aceptaCobro: true, aceptaPago: false });
  const [errorAgregar, setErrorAgregar] = useState('');
  const [confirmEliminar, setConfirmEliminar] = useState<string | null>(null);

  const actualizarMut = useMutation({
    mutationFn: ({ id, data }: { id: string; data: any }) => actualizarMetodoPago(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['metodos-pago'] }),
  });

  const eliminarMut = useMutation({
    mutationFn: (id: string) => eliminarMetodoPago(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['metodos-pago'] }); setConfirmEliminar(null); },
  });

  const agregarMut = useMutation({
    mutationFn: () => client.post('/api/metodos-pago', formNuevo).then((r: any) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['metodos-pago'] });
      setShowAgregar(false);
      setFormNuevo({ codigo: '', nombre: '', aceptaCobro: true, aceptaPago: false });
      setErrorAgregar('');
    },
    onError: (e: any) => setErrorAgregar(e.response?.data?.detail || 'Error al agregar método'),
  });

  const filtrados = metodos.filter((m: { aceptaCobro: boolean; aceptaPago: boolean }) => tab === 'cobro' ? m.aceptaCobro : m.aceptaPago);
  const cobrosCount = metodos.filter((m: { aceptaCobro: boolean }) => m.aceptaCobro).length;
  const pagosCount  = metodos.filter((m: { aceptaPago: boolean }) => m.aceptaPago).length;

  const toggleHabilitado = (id: string, habilitado: boolean) =>
    actualizarMut.mutate({ id, data: { habilitado: !habilitado } });

  const tabCls = (active: boolean) =>
    `px-4 py-1.5 text-xs rounded font-medium border transition-colors ${
      active
        ? 'bg-primary text-on-primary border-primary'
        : 'border-outline-variant text-on-surface-variant hover:text-on-surface'
    }`;

  return (
    <div className="min-h-screen bg-surface-container">
      <Header title="Métodos de Pago" />
      <div className="pt-11 p-6">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-on-surface text-sm font-semibold uppercase tracking-widest">Métodos de Pago</h2>
        <button className="btn-primary" onClick={() => setShowAgregar(true)}>+ Agregar método</button>
      </div>

      {isLoading ? (
        <div className="flex justify-center py-12"><Spinner /></div>
      ) : (
        <>
          <div className="flex gap-2 mb-4">
            <button className={tabCls(tab === 'cobro')} onClick={() => setTab('cobro')}>
              De cobro · {cobrosCount}
            </button>
            <button className={tabCls(tab === 'pago')} onClick={() => setTab('pago')}>
              De pago · {pagosCount}
            </button>
          </div>

          <div className="card p-0 overflow-hidden max-w-2xl">
            <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="table-header">
                  <th className="text-left px-4 py-3">Nombre</th>
                  <th className="text-center px-4 py-3">Cobro</th>
                  <th className="text-center px-4 py-3">Pago</th>
                  <th className="text-center px-4 py-3">Habilitado</th>
                  {esDueno && <th className="text-center px-4 py-3">Acciones</th>}
                </tr>
              </thead>
              <tbody>
                {filtrados.length === 0 ? (
                  <tr>
                    <td colSpan={esDueno ? 5 : 4} className="px-4 py-8 text-center text-on-surface-variant text-sm">
                      Sin métodos en esta categoría
                    </td>
                  </tr>
                ) : filtrados.map((m: import('../types').MetodoPagoDTO) => (
                  <tr key={m.id} className="table-row">
                    <td className="px-4 py-3 font-medium">{m.nombre}</td>
                    <td className="px-4 py-3 text-center">
                      <span className={`text-xs font-medium ${m.aceptaCobro ? 'text-green-400' : 'text-on-surface-variant'}`}>
                        {m.aceptaCobro ? 'Sí' : 'No'}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-center">
                      <span className={`text-xs font-medium ${m.aceptaPago ? 'text-green-400' : 'text-on-surface-variant'}`}>
                        {m.aceptaPago ? 'Sí' : 'No'}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-center">
                      <button
                        onClick={() => toggleHabilitado(m.id, m.habilitado)}
                        disabled={actualizarMut.isPending}
                        className={`text-xs px-3 py-0.5 rounded font-medium transition-colors ${
                          m.habilitado
                            ? 'bg-green-900/20 text-green-400 hover:bg-red-900/20 hover:text-red-400'
                            : 'bg-red-900/20 text-red-400 hover:bg-green-900/20 hover:text-green-400'
                        }`}
                      >
                        {m.habilitado ? 'Activo' : 'Inactivo'}
                      </button>
                    </td>
                    {esDueno && (
                      <td className="px-4 py-3 text-center">
                        {confirmEliminar === m.id ? (
                          <div className="flex items-center justify-center gap-1">
                            <button
                              className="px-2 py-0.5 text-xs font-medium bg-error text-on-error rounded hover:opacity-90"
                              onClick={() => eliminarMut.mutate(m.id)}
                              disabled={eliminarMut.isPending}
                            >
                              Confirmar
                            </button>
                            <button
                              className="px-2 py-0.5 text-xs text-on-surface-variant hover:text-on-surface"
                              onClick={() => setConfirmEliminar(null)}
                            >
                              No
                            </button>
                          </div>
                        ) : (
                          <button
                            className="px-2 py-0.5 text-xs text-error hover:bg-error/10 rounded"
                            onClick={() => setConfirmEliminar(m.id)}
                          >
                            Eliminar
                          </button>
                        )}
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
            </div>
          </div>
        </>
      )}

      {showAgregar && (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50" onClick={() => setShowAgregar(false)}>
          <div className="card w-full max-w-sm space-y-3" onClick={e => e.stopPropagation()}>
            <h3 className="text-on-surface font-semibold text-sm uppercase tracking-widest">Agregar Método</h3>
            <div>
              <label className="kpi-label block mb-1">Nombre *</label>
              <input className="input" value={formNuevo.nombre} onChange={e => setFormNuevo(f => ({...f, nombre: e.target.value}))} placeholder="Ej: Cheque" />
            </div>
            <div>
              <label className="kpi-label block mb-1">Código interno *</label>
              <input className="input" value={formNuevo.codigo} onChange={e => setFormNuevo(f => ({...f, codigo: e.target.value.toUpperCase()}))} placeholder="Ej: CHEQUE" />
              <p className="text-xs text-on-surface-variant mt-0.5">Solo letras y guiones bajos, sin espacios</p>
            </div>
            <div className="flex gap-4">
              <label className="flex items-center gap-2 text-sm cursor-pointer">
                <input type="checkbox" checked={formNuevo.aceptaCobro} onChange={e => setFormNuevo(f => ({...f, aceptaCobro: e.target.checked}))} />
                Acepta cobro
              </label>
              <label className="flex items-center gap-2 text-sm cursor-pointer">
                <input type="checkbox" checked={formNuevo.aceptaPago} onChange={e => setFormNuevo(f => ({...f, aceptaPago: e.target.checked}))} />
                Acepta pago
              </label>
            </div>
            {errorAgregar && <p className="text-error text-xs">{errorAgregar}</p>}
            <div className="flex gap-2 pt-2">
              <button className="btn-primary flex-1" onClick={() => agregarMut.mutate()}
                disabled={!formNuevo.nombre || !formNuevo.codigo || agregarMut.isPending}>
                {agregarMut.isPending ? <Spinner size="sm" /> : 'Agregar'}
              </button>
              <button className="btn-secondary flex-1" onClick={() => setShowAgregar(false)}>Cancelar</button>
            </div>
          </div>
        </div>
      )}
      </div>
    </div>
  );
}
