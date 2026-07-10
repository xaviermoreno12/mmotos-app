import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getProveedores, crearProveedor, actualizarProveedor } from '../api/proveedores';
import type { ProveedorDTO, CrearProveedorRequest } from '../types';
import { Spinner } from '../components/ui/Spinner';
import { Header } from '../components/layout/Header';
import { useDebouncedValue } from '../hooks/useDebouncedValue';

const emptyForm: CrearProveedorRequest = { cuit: '', nombre: '', contacto: '', telefono: '', email: '' };

export function ProveedoresPage() {
  const qc = useQueryClient();
  const [busqueda, setBusqueda] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editando, setEditando] = useState<ProveedorDTO | null>(null);
  const [form, setForm] = useState<CrearProveedorRequest>(emptyForm);
  const [formError, setFormError] = useState('');

  const debouncedBusqueda = useDebouncedValue(busqueda, 300);

  const { data: proveedores = [], isLoading } = useQuery({
    queryKey: ['proveedores', debouncedBusqueda],
    queryFn: () => getProveedores(debouncedBusqueda || undefined),
  });

  const crearMut = useMutation({
    mutationFn: crearProveedor,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['proveedores'] }); cerrarModal(); },
    onError: (e: any) => setFormError(e.response?.data?.mensaje || 'Error al guardar'),
  });

  const actualizarMut = useMutation({
    mutationFn: ({ id, data }: { id: string; data: Record<string, unknown> }) =>
      actualizarProveedor(id, data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['proveedores'] }); cerrarModal(); },
    onError: (e: any) => setFormError(e.response?.data?.mensaje || 'Error al guardar'),
  });

  const cerrarModal = () => { setShowModal(false); setEditando(null); setForm(emptyForm); setFormError(''); };

  const abrirEditar = (p: ProveedorDTO) => {
    setEditando(p);
    setForm({ cuit: p.cuit || '', nombre: p.nombre, contacto: p.contacto || '', telefono: p.telefono || '', email: p.email || '' });
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

  const toggleActivo = (p: ProveedorDTO) =>
    actualizarMut.mutate({ id: p.id, data: { activo: !p.activo } });

  const f = (k: keyof CrearProveedorRequest) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm(prev => ({ ...prev, [k]: e.target.value }));

  return (
    <div className="min-h-screen bg-surface-container">
      <Header title="Proveedores" />
      <div className="pt-11 p-6">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-on-surface text-sm font-semibold uppercase tracking-widest">Proveedores</h2>
        <button className="btn-primary" onClick={() => setShowModal(true)}>+ Nuevo Proveedor</button>
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
                <th className="px-4 py-3 text-left">Contacto</th>
                <th className="px-4 py-3 text-left">Teléfono</th>
                <th className="px-4 py-3 text-left">Email</th>
                <th className="px-4 py-3 text-left">Estado</th>
                <th className="px-4 py-3 text-left">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {proveedores.length === 0 ? (
                <tr><td colSpan={7} className="px-4 py-8 text-center text-on-surface-variant">Sin proveedores</td></tr>
              ) : proveedores.map((p: ProveedorDTO) => (
                <tr key={p.id} className="table-row">
                  <td className="px-4 py-3 font-medium">{p.nombre}</td>
                  <td className="px-4 py-3 text-on-surface-variant">{p.cuit || '—'}</td>
                  <td className="px-4 py-3 text-on-surface-variant">{p.contacto || '—'}</td>
                  <td className="px-4 py-3 text-on-surface-variant">{p.telefono || '—'}</td>
                  <td className="px-4 py-3 text-on-surface-variant">{p.email || '—'}</td>
                  <td className="px-4 py-3">
                    <button
                      onClick={() => { if (window.confirm(`¿${p.activo ? 'Desactivar' : 'Activar'} a ${p.nombre}?`)) toggleActivo(p); }}
                      className={`text-xs px-2 py-0.5 rounded font-medium transition-colors ${p.activo ? 'bg-green-900/20 text-green-400 hover:bg-red-900/20 hover:text-red-400' : 'bg-red-900/20 text-red-400 hover:bg-green-900/20 hover:text-green-400'}`}>
                      {p.activo ? 'Activo' : 'Inactivo'}
                    </button>
                  </td>
                  <td className="px-4 py-3">
                    <button className="btn-ghost py-1 text-xs" onClick={() => abrirEditar(p)}>Editar</button>
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
              {editando ? 'Editar Proveedor' : 'Nuevo Proveedor'}
            </h3>
            <form onSubmit={handleSubmit} className="space-y-3">
              <div><label className="kpi-label block mb-1">Nombre *</label>
                <input className="input" value={form.nombre} onChange={f('nombre')} required /></div>
              <div><label className="kpi-label block mb-1">CUIT</label>
                <input className="input" value={form.cuit} onChange={f('cuit')} placeholder="30-12345678-9" /></div>
              <div><label className="kpi-label block mb-1">Contacto</label>
                <input className="input" value={form.contacto} onChange={f('contacto')} /></div>
              <div><label className="kpi-label block mb-1">Teléfono</label>
                <input className="input" value={form.telefono} onChange={f('telefono')} /></div>
              <div><label className="kpi-label block mb-1">Email</label>
                <input className="input" type="email" value={form.email} onChange={f('email')} /></div>
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
