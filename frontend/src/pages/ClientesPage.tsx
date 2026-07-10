import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getClientes, crearCliente, actualizarCliente } from '../api/clientes';
import type { ClienteDTO, CrearClienteRequest } from '../types';
import { Spinner } from '../components/ui/Spinner';
import { Header } from '../components/layout/Header';
import { useDebouncedValue } from '../hooks/useDebouncedValue';

const emptyForm: CrearClienteRequest = { cuit: '', nombre: '', direccion: '', telefono: '', email: '' };

export function ClientesPage() {
  const qc = useQueryClient();
  const [busqueda, setBusqueda] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editando, setEditando] = useState<ClienteDTO | null>(null);
  const [form, setForm] = useState<CrearClienteRequest>(emptyForm);
  const [formError, setFormError] = useState('');

  const debouncedBusqueda = useDebouncedValue(busqueda, 300);

  const { data: clientes = [], isLoading } = useQuery({
    queryKey: ['clientes', debouncedBusqueda],
    queryFn: () => getClientes(debouncedBusqueda || undefined),
  });

  const crearMut = useMutation({
    mutationFn: crearCliente,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['clientes'] }); cerrarModal(); },
    onError: (e: any) => setFormError(e.response?.data?.mensaje || 'Error al guardar'),
  });

  const actualizarMut = useMutation({
    mutationFn: ({ id, data }: { id: string; data: Record<string, unknown> }) =>
      actualizarCliente(id, data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['clientes'] }); cerrarModal(); },
    onError: (e: any) => setFormError(e.response?.data?.mensaje || 'Error al guardar'),
  });

  const cerrarModal = () => { setShowModal(false); setEditando(null); setForm(emptyForm); setFormError(''); };

  const abrirEditar = (c: ClienteDTO) => {
    setEditando(c);
    setForm({ cuit: c.cuit || '', nombre: c.nombre, direccion: c.direccion || '', telefono: c.telefono || '', email: c.email || '' });
    setShowModal(true);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setFormError('');
    if (editando) {
      actualizarMut.mutate({ id: editando.id, data: form as unknown as Record<string, unknown> });
    } else {
      crearMut.mutate(form);
    }
  };

  const toggleActivo = (c: ClienteDTO) =>
    actualizarMut.mutate({ id: c.id, data: { activo: !c.activo } });

  const f = (k: keyof CrearClienteRequest) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm(p => ({ ...p, [k]: e.target.value }));

  return (
    <div className="min-h-screen bg-surface-container">
      <Header title="Clientes" />
      <div className="pt-11 p-6">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-on-surface text-sm font-semibold uppercase tracking-widest">Clientes</h2>
        <button className="btn-primary" onClick={() => setShowModal(true)}>+ Nuevo Cliente</button>
      </div>

      <input
        className="input max-w-sm mb-4"
        placeholder="Buscar por nombre o CUIT..."
        value={busqueda}
        onChange={e => setBusqueda(e.target.value)}
      />

      {isLoading ? (
        <div className="flex justify-center py-12"><Spinner /></div>
      ) : (
        <div className="card p-0 overflow-hidden">
          <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="table-header">
                <th className="px-4 py-3 text-left">Nombre</th>
                <th className="px-4 py-3 text-left">CUIT</th>
                <th className="px-4 py-3 text-left">Teléfono</th>
                <th className="px-4 py-3 text-left">Email</th>
                <th className="px-4 py-3 text-right">Saldo</th>
                <th className="px-4 py-3 text-left">Estado</th>
                <th className="px-4 py-3 text-left">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {clientes.length === 0 ? (
                <tr><td colSpan={7} className="px-4 py-8 text-center text-on-surface-variant">Sin clientes</td></tr>
              ) : clientes.map((c: ClienteDTO) => (
                <tr key={c.id} className="table-row">
                  <td className="px-4 py-3 font-medium">{c.nombre}</td>
                  <td className="px-4 py-3 text-on-surface-variant">{c.cuit || '—'}</td>
                  <td className="px-4 py-3 text-on-surface-variant">{c.telefono || '—'}</td>
                  <td className="px-4 py-3 text-on-surface-variant">{c.email || '—'}</td>
                  <td className="px-4 py-3 text-right font-mono">
                    <span className={c.saldo < 0 ? 'text-error' : 'text-on-surface'}>
                      ${c.saldo.toLocaleString('es-AR')}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <button
                      onClick={() => { if (window.confirm(`¿${c.activo ? 'Desactivar' : 'Activar'} a ${c.nombre}?`)) toggleActivo(c); }}
                      className={`text-xs px-2 py-0.5 rounded font-medium transition-colors ${c.activo ? 'bg-green-900/20 text-green-400 hover:bg-red-900/20 hover:text-red-400' : 'bg-red-900/20 text-red-400 hover:bg-green-900/20 hover:text-green-400'}`}>
                      {c.activo ? 'Activo' : 'Inactivo'}
                    </button>
                  </td>
                  <td className="px-4 py-3">
                    <button className="btn-ghost py-1 text-xs" onClick={() => abrirEditar(c)}>Editar</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          </div>
        </div>
      )}

      {showModal && (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50" onClick={cerrarModal}>
          <div className="card w-full max-w-md space-y-4" onClick={e => e.stopPropagation()}>
            <h3 className="text-on-surface font-semibold text-sm uppercase tracking-widest">
              {editando ? 'Editar Cliente' : 'Nuevo Cliente'}
            </h3>
            <form onSubmit={handleSubmit} className="space-y-3">
              <div><label className="kpi-label block mb-1">Nombre *</label>
                <input className="input" value={form.nombre} onChange={f('nombre')} required /></div>
              <div><label className="kpi-label block mb-1">CUIT</label>
                <input className="input" value={form.cuit} onChange={f('cuit')} placeholder="20-12345678-9" /></div>
              <div><label className="kpi-label block mb-1">Teléfono</label>
                <input className="input" value={form.telefono} onChange={f('telefono')} /></div>
              <div><label className="kpi-label block mb-1">Email</label>
                <input className="input" type="email" value={form.email} onChange={f('email')} /></div>
              <div><label className="kpi-label block mb-1">Dirección</label>
                <input className="input" value={form.direccion} onChange={f('direccion')} /></div>
              {formError && <p className="text-error text-xs">{formError}</p>}
              <div className="flex gap-2 pt-2">
                <button type="submit" className="btn-primary flex-1"
                  disabled={crearMut.isPending || actualizarMut.isPending}>
                  {crearMut.isPending || actualizarMut.isPending ? <Spinner size="sm" /> : (editando ? 'Guardar' : 'Crear')}
                </button>
                <button type="button" className="btn-secondary flex-1" onClick={cerrarModal}>Cancelar</button>
              </div>
            </form>
          </div>
        </div>
      )}
      </div>
    </div>
  );
}
