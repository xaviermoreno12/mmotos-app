import { useQuery, useMutation, useQueryClient, keepPreviousData } from '@tanstack/react-query';
import { getTodosProductos, actualizarProducto } from '../api/productos';
import { client } from '../api/client';
import { Spinner } from '../components/ui/Spinner';
import type { ProductoDTO, ActualizarProductoRequest } from '../types';
import { useState } from 'react';

function fmt(n: number) {
  return new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' }).format(n);
}

async function descargarListaProductos() {
  const res = await client.get('/api/reportes/lista-productos?formato=xlsx', { responseType: 'blob' });
  const url = URL.createObjectURL(res.data as Blob);
  const a = document.createElement('a');
  a.href = url; a.download = 'lista-productos.xlsx'; a.click();
  URL.revokeObjectURL(url);
}

export function ListasPage() {
  const qc = useQueryClient();
  const [pagina, setPagina] = useState(0);
  const [descargando, setDescargando] = useState(false);
  const [editando, setEditando] = useState<ProductoDTO | null>(null);
  const [editForm, setEditForm] = useState<ActualizarProductoRequest>({});
  const [editError, setEditError] = useState('');

  const editarMut = useMutation({
    mutationFn: () => actualizarProducto(editando!.id, editForm),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['todos-productos'] });
      setEditando(null); setEditForm({}); setEditError('');
    },
    onError: (e: any) => setEditError(e.response?.data?.detail || 'Error al guardar'),
  });

  const abrirEditar = (p: ProductoDTO) => {
    setEditando(p);
    setEditForm({
      nombre: p.nombre,
      precioBase: p.precioBase,
      moneda: p.moneda,
      stockActual: p.stockActual,
      stockMinimo: p.stockMinimo,
      ubicacionFisica: p.ubicacionFisica ?? '',
    });
    setEditError('');
  };

  const { data, isLoading } = useQuery({
    queryKey: ['todos-productos', pagina],
    queryFn: () => getTodosProductos(pagina, 50),
    placeholderData: keepPreviousData,
  });

  const productos = data?.contenido ?? [];
  const criticos = productos.filter((p: ProductoDTO) => p.stockActual <= p.stockMinimo);

  const handleDescargar = async () => {
    setDescargando(true);
    try { await descargarListaProductos(); } finally { setDescargando(false); }
  };

  if (isLoading && !data) return <div className="flex justify-center py-20"><Spinner /></div>;

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-on-surface text-sm font-semibold uppercase tracking-widest">
          Lista de Productos{' '}
          <span className="text-on-surface-variant font-normal">
            ({data?.totalElementos ?? 0} ítems)
          </span>
        </h2>
        <button className="btn-primary flex items-center gap-2" onClick={handleDescargar} disabled={descargando}>
          {descargando ? <Spinner size="sm" /> : <span className="material-symbols-outlined text-[16px]">download</span>}
          Descargar Excel
        </button>
      </div>

      {criticos.length > 0 && (
        <div>
          <p className="kpi-label mb-2 text-error">Con menos stock ({criticos.length})</p>
          <div className="card p-0 overflow-hidden border border-error/20">
            <table className="w-full text-sm">
              <thead><tr className="table-header">
                <th className="text-left px-4 py-2">SKU</th>
                <th className="text-left px-4 py-2">Nombre</th>
                <th className="text-center px-4 py-2">Stock</th>
                <th className="text-center px-4 py-2">Mínimo</th>
                <th className="text-left px-4 py-2">Ubicación</th>
                <th className="text-right px-4 py-2">Precio</th>
                <th className="px-4 py-2"></th>
              </tr></thead>
              <tbody>
                {criticos.map((p: ProductoDTO) => (
                  <tr key={p.id} className="table-row">
                    <td className="px-4 py-2 font-mono text-xs text-on-surface-variant">{p.sku}</td>
                    <td className="px-4 py-2 font-medium">{p.nombre}</td>
                    <td className="px-4 py-2 text-center text-error font-bold">{p.stockActual}</td>
                    <td className="px-4 py-2 text-center text-on-surface-variant">{p.stockMinimo}</td>
                    <td className="px-4 py-2 text-on-surface-variant text-xs">{p.ubicacionFisica || '—'}</td>
                    <td className="px-4 py-2 text-right text-primary">{fmt(p.precioEnPesos)}</td>
                    <td className="px-4 py-2 text-center">
                      <button className="btn-ghost py-1 text-xs" onClick={() => abrirEditar(p)}>Editar</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      <div>
        <p className="kpi-label mb-2">Todos los productos</p>
        <div className="card p-0 overflow-hidden">
          <table className="w-full text-sm">
            <thead><tr className="table-header">
              <th className="text-left px-4 py-2">SKU</th>
              <th className="text-left px-4 py-2">Nombre</th>
              <th className="text-center px-4 py-2">Stock</th>
              <th className="text-center px-4 py-2">Mín.</th>
              <th className="text-left px-4 py-2">Ubicación</th>
              <th className="text-right px-4 py-2">Precio</th>
              <th className="px-4 py-2"></th>
            </tr></thead>
            <tbody>
              {productos.length === 0
                ? <tr><td colSpan={7} className="px-4 py-8 text-center text-on-surface-variant">Sin productos cargados</td></tr>
                : productos.map((p: ProductoDTO) => (
                  <tr key={p.id} className="table-row">
                    <td className="px-4 py-2 font-mono text-xs text-on-surface-variant">{p.sku}</td>
                    <td className="px-4 py-2 font-medium">{p.nombre}</td>
                    <td className={`px-4 py-2 text-center font-medium ${p.bajominimo ? 'text-error' : 'text-on-surface'}`}>
                      {p.stockActual}
                    </td>
                    <td className="px-4 py-2 text-center text-on-surface-variant">{p.stockMinimo}</td>
                    <td className="px-4 py-2 text-on-surface-variant text-xs">{p.ubicacionFisica || '—'}</td>
                    <td className="px-4 py-2 text-right text-primary">{fmt(p.precioEnPesos)}</td>
                    <td className="px-4 py-2 text-center">
                      <button className="btn-ghost py-1 text-xs" onClick={() => abrirEditar(p)}>Editar</button>
                    </td>
                  </tr>
                ))}
            </tbody>
          </table>
        </div>

        {data && data.totalPaginas > 1 && (
          <div className="flex items-center gap-3 mt-4 justify-center">
            <button
              disabled={pagina === 0}
              onClick={() => setPagina(p => p - 1)}
              className="btn-secondary px-3 py-1 text-xs disabled:opacity-40"
            >
              ← Anterior
            </button>
            <span className="text-xs text-on-surface-variant">
              Página {data.paginaActual + 1} de {data.totalPaginas} · {data.totalElementos} productos
            </span>
            <button
              disabled={data.esUltima}
              onClick={() => setPagina(p => p + 1)}
              className="btn-secondary px-3 py-1 text-xs disabled:opacity-40"
            >
              Siguiente →
            </button>
          </div>
        )}
      </div>

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
                  <label className="kpi-label block mb-1">Stock actual <span className="text-on-surface-variant">(ajuste)</span></label>
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
    </div>
  );
}
