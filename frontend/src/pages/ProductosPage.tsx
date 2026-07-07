import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Header } from '../components/layout/Header';
import { Badge } from '../components/ui/Badge';
import { Spinner } from '../components/ui/Spinner';
import { useBuscarProductos, useCrearProducto } from '../hooks/useProductos';
import { getHistorialProducto, actualizarProducto, actualizarPreciosDesdeXlsx } from '../api/productos';
import type { ResultadoImportacion } from '../api/productos';
import type { ActualizarProductoRequest, CrearProductoRequest, ProductoDTO } from '../types';

function fmt(n: number) {
  return new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' }).format(n);
}

const EMPTY_FORM: CrearProductoRequest = {
  sku: '', nombre: '', precioBase: 0, moneda: 'ARS',
  stockActual: 0, stockMinimo: 0, ubicacionFisica: '',
};

function buildEditForm(p: ProductoDTO): ActualizarProductoRequest {
  return {
    nombre: p.nombre,
    precioBase: p.precioBase,
    moneda: p.moneda,
    stockActual: p.stockActual,
    stockMinimo: p.stockMinimo,
    ubicacionFisica: p.ubicacionFisica ?? '',
    activo: p.activo,
  };
}

export function ProductosPage() {
  const qc = useQueryClient();
  const rol = localStorage.getItem('mmotos_rol');

  const [termino, setTermino] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [verProducto, setVerProducto] = useState<ProductoDTO | null>(null);
  const [editando, setEditando] = useState<ProductoDTO | null>(null);
  const [editForm, setEditForm] = useState<ActualizarProductoRequest>({});
  const [editError, setEditError] = useState('');
  const [confirmEliminar, setConfirmEliminar] = useState<string | null>(null);
  const [form, setForm] = useState<CrearProductoRequest>(EMPTY_FORM);
  const [formError, setFormError] = useState('');
  const [importando, setImportando] = useState(false);
  const [importResult, setImportResult] = useState<ResultadoImportacion | null>(null);

  const { data: productos, isLoading } = useBuscarProductos(termino);
  const crear = useCrearProducto();

  const { data: historial = [], isLoading: historialLoading } = useQuery({
    queryKey: ['historial-producto', verProducto?.id],
    queryFn: () => getHistorialProducto(verProducto!.id),
    enabled: !!verProducto,
  });

  const eliminarMut = useMutation({
    mutationFn: (id: string) => actualizarProducto(id, { activo: false }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['productos'] }); setConfirmEliminar(null); },
  });

  const editarMut = useMutation({
    mutationFn: () => actualizarProducto(editando!.id, editForm),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['productos'] });
      setEditando(null);
      setEditError('');
    },
    onError: (err: unknown) => {
      const msg = (err as { response?: { data?: { detail?: string } } })?.response?.data?.detail;
      setEditError(msg || 'Error al actualizar el producto.');
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setFormError('');
    if (!form.sku.trim() || !form.nombre.trim() || form.precioBase <= 0) {
      setFormError('SKU, nombre y precio son obligatorios.');
      return;
    }
    crear.mutate(form, {
      onSuccess: () => {
        setShowModal(false);
        setForm(EMPTY_FORM);
      },
      onError: (err: unknown) => {
        const msg = (err as { response?: { data?: { detail?: string } } })?.response?.data?.detail;
        setFormError(msg || 'Error al crear el producto.');
      },
    });
  };

  const handleXlsxImport = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    e.target.value = '';
    setImportando(true);
    setImportResult(null);
    try {
      const result = await actualizarPreciosDesdeXlsx(file);
      setImportResult(result);
      qc.invalidateQueries({ queryKey: ['productos'] });
    } catch {
      setImportResult({ errores: ['Error al procesar el archivo. Verificá que sea el formato correcto.'] });
    } finally {
      setImportando(false);
    }
  };

  const colSpan = rol === 'DUENO' ? 8 : 6;

  return (
    <div className="min-h-screen bg-surface-container">
      <Header
        title="Productos"
        action={
          <div className="flex items-center gap-2">
            {rol === 'DUENO' && (
              <label className={`btn-secondary flex items-center gap-1.5 cursor-pointer ${importando ? 'opacity-60 pointer-events-none' : ''}`}>
                <span className="material-symbols-outlined text-[16px]">upload_file</span>
                {importando ? 'Cargando...' : 'Actualizar precios (Excel)'}
                <input type="file" accept=".xlsx,.xls" className="hidden" onChange={handleXlsxImport} disabled={importando} />
              </label>
            )}
            <button className="btn-primary flex items-center gap-1.5" onClick={() => setShowModal(true)}>
              <span className="material-symbols-outlined text-[16px]">add</span>
              Cargar uno
            </button>
          </div>
        }
      />
      <div className="pt-11 p-5">
        <div className="flex items-center gap-4 mb-4">
          <div className="relative flex-1 max-w-md">
            <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant text-[18px]">
              search
            </span>
            <input
              className="input pl-10"
              placeholder="Buscar por nombre, SKU..."
              value={termino}
              onChange={(e) => setTermino(e.target.value)}
            />
          </div>
          {isLoading && <Spinner size="sm" />}
        </div>

        <div className="card p-0 overflow-hidden">
          <table className="w-full">
            <thead>
              <tr className="table-header">
                <th className="text-left px-4 py-3">SKU</th>
                <th className="text-left px-4 py-3">Nombre</th>
                <th className="text-right px-4 py-3">Precio</th>
                <th className="text-center px-4 py-3">Stock</th>
                <th className="text-center px-4 py-3">Estado</th>
                <th className="text-center px-4 py-3">Ver</th>
                {rol === 'DUENO' && <th className="text-center px-4 py-3">Editar</th>}
                {rol === 'DUENO' && <th className="text-center px-4 py-3">Eliminar</th>}
              </tr>
            </thead>
            <tbody>
              {termino.trim().length < 2 ? (
                <tr>
                  <td colSpan={colSpan} className="px-4 py-12 text-center text-on-surface-variant text-sm">
                    Ingresá al menos 2 caracteres para buscar
                  </td>
                </tr>
              ) : productos && productos.length === 0 ? (
                <tr>
                  <td colSpan={colSpan} className="px-4 py-12 text-center text-on-surface-variant text-sm">
                    Sin resultados para "{termino}"
                  </td>
                </tr>
              ) : (
                productos?.map((p) => (
                  <tr key={p.id} className="table-row">
                    <td className="px-4 py-3 font-mono text-xs text-on-surface-variant">{p.sku}</td>
                    <td className="px-4 py-3">
                      <p className="text-sm font-medium">{p.nombre}</p>
                      {p.ubicacionFisica && (
                        <p className="text-xs text-on-surface-variant">{p.ubicacionFisica}</p>
                      )}
                    </td>
                    <td className="px-4 py-3 text-right text-sm font-medium text-primary">
                      {fmt(p.precioEnPesos)}
                      {p.moneda === 'USD' && <span className="text-xs text-on-surface-variant ml-1">(USD)</span>}
                    </td>
                    <td className="px-4 py-3 text-center">
                      <span className={`text-sm font-medium ${p.bajominimo ? 'text-error' : 'text-on-surface'}`}>
                        {p.stockActual}
                      </span>
                      {p.bajominimo && (
                        <span className="material-symbols-outlined text-error text-[14px] ml-1 align-middle">warning</span>
                      )}
                    </td>
                    <td className="px-4 py-3 text-center">
                      <Badge variant={p.activo ? 'active' : 'error'} label={p.activo ? 'Activo' : 'Inactivo'} />
                    </td>
                    <td className="px-4 py-3 text-center">
                      <button className="text-tertiary text-xs hover:underline" onClick={() => setVerProducto(p)}>Ver</button>
                    </td>
                    {rol === 'DUENO' && (
                      <td className="px-4 py-3 text-center">
                        <button
                          className="text-xs text-on-surface-variant hover:text-on-surface"
                          onClick={() => { setEditando(p); setEditForm(buildEditForm(p)); setEditError(''); }}
                        >
                          <span className="material-symbols-outlined text-[16px] align-middle">edit</span>
                        </button>
                      </td>
                    )}
                    {rol === 'DUENO' && (
                      <td className="px-4 py-3 text-center">
                        {confirmEliminar === p.id ? (
                          <div className="flex items-center justify-center gap-1">
                            <button
                              className="px-2 py-0.5 text-xs font-medium bg-error text-on-error rounded hover:opacity-90"
                              onClick={() => eliminarMut.mutate(p.id)}
                              disabled={eliminarMut.isPending}
                            >
                              Sí
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
                            className="text-xs text-error hover:bg-error/10 rounded px-1"
                            onClick={() => setConfirmEliminar(p.id)}
                          >
                            <span className="material-symbols-outlined text-[16px] align-middle">delete</span>
                          </button>
                        )}
                      </td>
                    )}
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal ver producto + historial */}
      {verProducto && (
        <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50" onClick={() => setVerProducto(null)}>
          <div className="card w-[600px] max-h-[80vh] flex flex-col" onClick={e => e.stopPropagation()}>
            <div className="flex items-center justify-between mb-4">
              <div>
                <h3 className="font-semibold text-on-surface">{verProducto.nombre}</h3>
                <p className="text-xs text-on-surface-variant font-mono mt-0.5">{verProducto.sku}</p>
              </div>
              <button onClick={() => setVerProducto(null)}
                className="material-symbols-outlined text-on-surface-variant hover:text-on-surface">close</button>
            </div>

            <div className="grid grid-cols-3 gap-3 mb-4">
              <div className="bg-surface-container-high rounded p-3">
                <p className="kpi-label mb-1">Precio</p>
                <p className="text-sm font-semibold text-primary">{fmt(verProducto.precioEnPesos)}</p>
                {verProducto.moneda === 'USD' && <p className="text-xs text-on-surface-variant">USD {verProducto.precioBase}</p>}
              </div>
              <div className="bg-surface-container-high rounded p-3">
                <p className="kpi-label mb-1">Stock</p>
                <p className={`text-sm font-semibold ${verProducto.bajominimo ? 'text-error' : 'text-on-surface'}`}>
                  {verProducto.stockActual} <span className="text-xs font-normal text-on-surface-variant">/ mín {verProducto.stockMinimo}</span>
                </p>
              </div>
              <div className="bg-surface-container-high rounded p-3">
                <p className="kpi-label mb-1">Ubicación</p>
                <p className="text-sm font-semibold">{verProducto.ubicacionFisica || '—'}</p>
              </div>
            </div>

            <p className="kpi-label mb-2">Historial de ventas</p>
            <div className="flex-1 overflow-y-auto">
              {historialLoading ? (
                <div className="flex justify-center py-8"><Spinner /></div>
              ) : historial.length === 0 ? (
                <div className="border border-dashed border-outline-variant rounded py-8 text-center text-on-surface-variant text-xs">
                  Sin ventas registradas para este producto
                </div>
              ) : (
                <table className="w-full text-sm">
                  <thead>
                    <tr className="table-header">
                      <th className="px-3 py-2 text-left">Fecha</th>
                      <th className="px-3 py-2 text-left">Ticket</th>
                      <th className="px-3 py-2 text-right">Cant.</th>
                      <th className="px-3 py-2 text-right">Precio unit.</th>
                      <th className="px-3 py-2 text-right">Subtotal</th>
                    </tr>
                  </thead>
                  <tbody>
                    {historial.map(h => (
                      <tr key={h.ventaId} className="table-row">
                        <td className="px-3 py-2 text-on-surface-variant">
                          {new Date(h.fechaEmision).toLocaleDateString('es-AR')}
                        </td>
                        <td className="px-3 py-2 font-mono text-xs">{h.numeroTicket || '—'}</td>
                        <td className="px-3 py-2 text-right">{h.cantidad}</td>
                        <td className="px-3 py-2 text-right font-mono">{fmt(h.precioUnitario)}</td>
                        <td className="px-3 py-2 text-right font-mono text-primary">
                          {fmt(h.cantidad * h.precioUnitario)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Modal editar producto */}
      {editando && (
        <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50" onClick={() => setEditando(null)}>
          <div className="card w-[480px] space-y-4" onClick={e => e.stopPropagation()}>
            <div className="flex items-center justify-between">
              <div>
                <h3 className="font-semibold text-on-surface text-sm uppercase tracking-widest">Editar producto</h3>
                <p className="text-xs text-on-surface-variant font-mono mt-0.5">{editando.sku}</p>
              </div>
              <button onClick={() => setEditando(null)}
                className="material-symbols-outlined text-on-surface-variant hover:text-on-surface">close</button>
            </div>

            <div className="space-y-3">
              <div>
                <label className="kpi-label block mb-1">Nombre</label>
                <input className="input" value={editForm.nombre ?? ''} autoComplete="off"
                  onChange={e => setEditForm({ ...editForm, nombre: e.target.value })} />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="kpi-label block mb-1">Precio base</label>
                  <input className="input" type="number" min="0.01" step="0.01"
                    value={editForm.precioBase ?? ''} autoComplete="off"
                    onChange={e => setEditForm({ ...editForm, precioBase: parseFloat(e.target.value) || 0 })} />
                </div>
                <div>
                  <label className="kpi-label block mb-1">Moneda</label>
                  <select className="input" value={editForm.moneda ?? 'ARS'}
                    onChange={e => setEditForm({ ...editForm, moneda: e.target.value })}>
                    <option value="ARS">ARS (Pesos)</option>
                    <option value="USD">USD (Dólares)</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="kpi-label block mb-1">Stock actual <span className="text-on-surface-variant">(ajuste manual)</span></label>
                  <input className="input" type="number" min="0"
                    value={editForm.stockActual ?? ''} autoComplete="off"
                    onChange={e => setEditForm({ ...editForm, stockActual: parseInt(e.target.value) || 0 })} />
                </div>
                <div>
                  <label className="kpi-label block mb-1">Stock mínimo</label>
                  <input className="input" type="number" min="0"
                    value={editForm.stockMinimo ?? ''} autoComplete="off"
                    onChange={e => setEditForm({ ...editForm, stockMinimo: parseInt(e.target.value) || 0 })} />
                </div>
              </div>

              <div>
                <label className="kpi-label block mb-1">Ubicación física</label>
                <input className="input" value={editForm.ubicacionFisica ?? ''} autoComplete="off"
                  onChange={e => setEditForm({ ...editForm, ubicacionFisica: e.target.value })} placeholder="Ej: Estante A3" />
              </div>

              <div>
                <label className="kpi-label block mb-1">Precio de compra (costo) <span className="text-on-surface-variant">— opcional</span></label>
                <input className="input" type="number" min="0" step="0.01"
                  value={editForm.precioCompra ?? ''} autoComplete="off"
                  onChange={e => setEditForm({ ...editForm, precioCompra: parseFloat(e.target.value) || undefined })}
                  placeholder="0.00" />
              </div>

              <div className="flex items-center gap-3">
                <input type="checkbox" id="activo-check" checked={editForm.activo ?? true}
                  onChange={e => setEditForm({ ...editForm, activo: e.target.checked })}
                  className="w-4 h-4 accent-primary" />
                <label htmlFor="activo-check" className="text-sm text-on-surface">Producto activo</label>
              </div>
            </div>

            {editError && <p className="text-error text-xs">{editError}</p>}

            <div className="flex gap-2 pt-1">
              <button
                className="btn-primary flex-1 flex items-center justify-center gap-2"
                onClick={() => editarMut.mutate()}
                disabled={editarMut.isPending}
              >
                {editarMut.isPending ? <Spinner size="sm" /> : <span className="material-symbols-outlined text-[16px]">save</span>}
                Guardar cambios
              </button>
              <button className="btn-secondary flex-1" onClick={() => setEditando(null)}>Cancelar</button>
            </div>
          </div>
        </div>
      )}

      {/* Modal crear producto */}
      {showModal && (
        <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50">
          <div className="card w-[480px] space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="font-semibold text-on-surface">Nuevo producto</h3>
              <button
                onClick={() => { setShowModal(false); setForm(EMPTY_FORM); setFormError(''); }}
                className="material-symbols-outlined text-on-surface-variant hover:text-on-surface"
              >close</button>
            </div>

            <form onSubmit={handleSubmit} className="space-y-3" autoComplete="off">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="kpi-label block mb-1">SKU *</label>
                  <input className="input" value={form.sku} autoComplete="off"
                    onChange={(e) => setForm({ ...form, sku: e.target.value })} placeholder="Ej: ACE-20W50-1L" />
                </div>
                <div>
                  <label className="kpi-label block mb-1">Moneda</label>
                  <select className="input" value={form.moneda}
                    onChange={(e) => setForm({ ...form, moneda: e.target.value })}>
                    <option value="ARS">ARS (Pesos)</option>
                    <option value="USD">USD (Dólares)</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="kpi-label block mb-1">Nombre *</label>
                <input className="input" value={form.nombre} autoComplete="off"
                  onChange={(e) => setForm({ ...form, nombre: e.target.value })} placeholder="Ej: Aceite Motor 20W50 1L" />
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="kpi-label block mb-1">Precio base *</label>
                  <input className="input" type="number" min="0.01" step="0.01" value={form.precioBase || ''}
                    autoComplete="off"
                    onChange={(e) => setForm({ ...form, precioBase: parseFloat(e.target.value) || 0 })} placeholder="0.00" />
                </div>
                <div>
                  <label className="kpi-label block mb-1">Stock actual</label>
                  <input className="input" type="number" min="0" value={form.stockActual} autoComplete="off"
                    onChange={(e) => setForm({ ...form, stockActual: parseInt(e.target.value) || 0 })} />
                </div>
                <div>
                  <label className="kpi-label block mb-1">Stock mínimo</label>
                  <input className="input" type="number" min="0" value={form.stockMinimo} autoComplete="off"
                    onChange={(e) => setForm({ ...form, stockMinimo: parseInt(e.target.value) || 0 })} />
                </div>
              </div>

              <div>
                <label className="kpi-label block mb-1">Ubicación física</label>
                <input className="input" value={form.ubicacionFisica} autoComplete="off"
                  onChange={(e) => setForm({ ...form, ubicacionFisica: e.target.value })} placeholder="Ej: Estante A3" />
              </div>

              <div>
                <label className="kpi-label block mb-1">Precio de compra (costo) <span className="text-on-surface-variant">— opcional</span></label>
                <input className="input" type="number" min="0" step="0.01"
                  value={form.precioCompra ?? ''} autoComplete="off"
                  onChange={(e) => setForm({ ...form, precioCompra: parseFloat(e.target.value) || undefined })}
                  placeholder="0.00" />
              </div>

              {formError && (
                <p className="text-xs text-error bg-error-container/20 px-3 py-2 rounded border border-error-container/40">
                  {formError}
                </p>
              )}

              <div className="flex justify-end gap-2 pt-1">
                <button type="button" className="btn-ghost"
                  onClick={() => { setShowModal(false); setForm(EMPTY_FORM); setFormError(''); }}>
                  Cancelar
                </button>
                <button type="submit" className="btn-primary flex items-center gap-1.5" disabled={crear.isPending}>
                  {crear.isPending ? <Spinner size="sm" /> : <span className="material-symbols-outlined text-[16px]">save</span>}
                  Guardar
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal resultado importacion Excel */}
      {importResult && (
        <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50">
          <div className="card w-[420px] space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="font-semibold text-on-surface flex items-center gap-2">
                <span className="material-symbols-outlined text-[18px]">upload_file</span>
                Resultado de la importacion
              </h3>
              <button onClick={() => setImportResult(null)} className="material-symbols-outlined text-on-surface-variant hover:text-on-surface">close</button>
            </div>

            <div className="space-y-2">
              {importResult.actualizados !== undefined && (
                <div className="flex items-center gap-2 text-sm">
                  <span className="material-symbols-outlined text-green-600 text-[18px]">check_circle</span>
                  <span><strong>{importResult.actualizados}</strong> productos actualizados</span>
                </div>
              )}
              {importResult.creados !== undefined && importResult.creados > 0 && (
                <div className="flex items-center gap-2 text-sm">
                  <span className="material-symbols-outlined text-blue-600 text-[18px]">add_circle</span>
                  <span><strong>{importResult.creados}</strong> productos nuevos creados</span>
                </div>
              )}
              {(importResult.noEncontrados ?? 0) > 0 && (
                <div className="flex items-center gap-2 text-sm text-amber-700">
                  <span className="material-symbols-outlined text-[18px]">warning</span>
                  <span><strong>{importResult.noEncontrados}</strong> sin coincidencia en el sistema</span>
                </div>
              )}
              {(importResult.errores?.length ?? 0) > 0 && (
                <div className="mt-2">
                  <p className="text-sm text-red-600 font-medium mb-1">Errores:</p>
                  <ul className="text-xs text-red-500 space-y-0.5 max-h-32 overflow-y-auto">
                    {importResult.errores!.map((e, i) => <li key={i}>• {e}</li>)}
                  </ul>
                </div>
              )}
            </div>

            <button className="btn-primary w-full" onClick={() => setImportResult(null)}>Cerrar</button>
          </div>
        </div>
      )}
    </div>
  );
}
