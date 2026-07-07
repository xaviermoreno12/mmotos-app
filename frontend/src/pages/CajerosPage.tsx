import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getCajeros, crearCajero, actualizarCajero } from '../api/cajeros';
import type { CajeroDTO, CrearCajeroRequest } from '../types';
import { Spinner } from '../components/ui/Spinner';

export function CajerosPage() {
  const qc = useQueryClient();
  const rol = localStorage.getItem('mmotos_rol');
  const esDueno = rol?.toUpperCase() === 'DUENO';
  const { data: todosCajeros = [], isLoading } = useQuery({ queryKey: ['cajeros'], queryFn: getCajeros });
  const cajeros = (todosCajeros as CajeroDTO[]).filter(c => c.activo);

  const [showModal, setShowModal] = useState(false);
  const [editando, setEditando] = useState<CajeroDTO | null>(null);
  const [form, setForm] = useState({ nombre: '', username: '', password: '', rol: 'CAJERO' });
  const [formError, setFormError] = useState('');
  const [confirmEliminar, setConfirmEliminar] = useState<string | null>(null);

  const crearMut = useMutation({
    mutationFn: crearCajero,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['cajeros'] }); cerrarModal(); },
    onError: (e: any) => setFormError(e.response?.data?.detail || 'Error al crear cajero'),
  });

  const actualizarMut = useMutation({
    mutationFn: ({ id, data }: { id: string; data: any }) => actualizarCajero(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['cajeros'] }),
  });

  const cerrarModal = () => {
    setShowModal(false);
    setEditando(null);
    setForm({ nombre: '', username: '', password: '', rol: 'CAJERO' });
    setFormError('');
  };

  const abrirEditar = (c: CajeroDTO) => {
    setEditando(c);
    setForm({ nombre: c.nombre, username: c.username, password: '', rol: c.rol });
    setShowModal(true);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setFormError('');
    if (editando) {
      const data: any = { nombre: form.nombre, rol: form.rol };
      if (form.password) data.password = form.password;
      actualizarMut.mutate({ id: editando.id, data }, { onSuccess: cerrarModal });
    } else {
      crearMut.mutate(form as CrearCajeroRequest);
    }
  };

  const toggleActivo = (c: CajeroDTO) =>
    actualizarMut.mutate({ id: c.id, data: { activo: !c.activo } });

  const eliminarCajero = (id: string) => {
    actualizarMut.mutate({ id, data: { activo: false } }, { onSuccess: () => setConfirmEliminar(null) });
  };

  return (
    <div className="p-6">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-on-surface text-sm font-semibold uppercase tracking-widest">Cajeros</h2>
        <button className="btn-primary" onClick={() => setShowModal(true)}>+ Nuevo Cajero</button>
      </div>

      {isLoading ? (
        <div className="flex justify-center py-12"><Spinner /></div>
      ) : (
        <div className="card p-0 overflow-hidden">
          <table className="w-full text-sm">
            <thead>
              <tr className="table-header">
                <th className="px-4 py-3 text-left">Nombre</th>
                <th className="px-4 py-3 text-left">Usuario</th>
                <th className="px-4 py-3 text-left">Rol</th>
                <th className="px-4 py-3 text-left">Estado</th>
                <th className="px-4 py-3 text-left">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {cajeros.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-4 py-8 text-center text-on-surface-variant text-sm">
                    Sin cajeros registrados
                  </td>
                </tr>
              ) : cajeros.map((c: CajeroDTO) => (
                <tr key={c.id} className="table-row">
                  <td className="px-4 py-3 font-medium">{c.nombre}</td>
                  <td className="px-4 py-3 text-on-surface-variant">{c.username}</td>
                  <td className="px-4 py-3">
                    <span className={`text-xs px-2 py-0.5 rounded font-medium ${c.rol === 'DUENO' ? 'bg-primary/10 text-primary' : 'bg-outline-variant/50 text-on-surface-variant'}`}>
                      {c.rol === 'DUENO' ? 'Dueño' : 'Cajero'}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <button
                      onClick={() => toggleActivo(c)}
                      disabled={actualizarMut.isPending}
                      className={`text-xs px-2 py-0.5 rounded font-medium transition-colors ${c.activo
                        ? 'bg-green-900/20 text-green-400 hover:bg-red-900/20 hover:text-red-400'
                        : 'bg-red-900/20 text-red-400 hover:bg-green-900/20 hover:text-green-400'
                      }`}
                    >
                      {c.activo ? 'Activo' : 'Inactivo'}
                    </button>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-2">
                      <button className="btn-ghost py-1 text-xs" onClick={() => abrirEditar(c)}>
                        Editar
                      </button>
                      {esDueno && c.rol !== 'DUENO' && confirmEliminar === c.id ? (
                        <>
                          <button
                            className="px-2 py-1 text-xs font-medium bg-error text-on-error rounded hover:opacity-90"
                            onClick={() => eliminarCajero(c.id)}
                            disabled={actualizarMut.isPending}
                          >
                            Confirmar
                          </button>
                          <button
                            className="px-2 py-1 text-xs text-on-surface-variant hover:text-on-surface"
                            onClick={() => setConfirmEliminar(null)}
                          >
                            Cancelar
                          </button>
                        </>
                      ) : esDueno && c.rol !== 'DUENO' ? (
                        <button
                          className="px-2 py-1 text-xs text-error hover:bg-error/10 rounded"
                          onClick={() => setConfirmEliminar(c.id)}
                        >
                          Eliminar
                        </button>
                      ) : null}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showModal && (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50" onClick={cerrarModal}>
          <div className="card w-full max-w-md space-y-4" onClick={e => e.stopPropagation()}>
            <h3 className="text-on-surface font-semibold text-sm uppercase tracking-widest">
              {editando ? 'Editar Cajero' : 'Nuevo Cajero'}
            </h3>
            <form onSubmit={handleSubmit} className="space-y-3" autoComplete="off">
              <div>
                <label className="kpi-label block mb-1">Nombre completo</label>
                <input className="input" value={form.nombre}
                  onChange={e => setForm(f => ({ ...f, nombre: e.target.value }))} required />
              </div>
              {!editando && (
                <div>
                  <label className="kpi-label block mb-1">Nombre de usuario</label>
                  <input className="input" value={form.username}
                    onChange={e => setForm(f => ({ ...f, username: e.target.value }))}
                    required autoComplete="off" />
                </div>
              )}
              <div>
                <label className="kpi-label block mb-1">
                  {editando ? 'Nueva contraseña (dejar vacío para no cambiar)' : 'Contraseña'}
                </label>
                <input className="input" type="password" value={form.password}
                  onChange={e => setForm(f => ({ ...f, password: e.target.value }))}
                  required={!editando} autoComplete="new-password" />
              </div>
              <div>
                <label className="kpi-label block mb-1">Rol</label>
                <select className="input" value={form.rol}
                  onChange={e => setForm(f => ({ ...f, rol: e.target.value }))}>
                  <option value="CAJERO">Cajero</option>
                  <option value="DUENO">Dueño</option>
                </select>
              </div>
              {formError && <p className="text-error text-xs">{formError}</p>}
              <div className="flex gap-2 pt-2">
                <button type="submit" className="btn-primary flex-1"
                  disabled={crearMut.isPending || actualizarMut.isPending}>
                  {crearMut.isPending || actualizarMut.isPending
                    ? <Spinner size="sm" />
                    : editando ? 'Guardar cambios' : 'Crear cajero'}
                </button>
                <button type="button" className="btn-secondary flex-1" onClick={cerrarModal}>
                  Cancelar
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
