import { useQuery, keepPreviousData } from '@tanstack/react-query';
import { getTodosProductos } from '../api/productos';
import { client } from '../api/client';
import { Spinner } from '../components/ui/Spinner';
import { Header } from '../components/layout/Header';
import type { ProductoDTO } from '../types';
import { useState } from 'react';
import { useDebouncedValue } from '../hooks/useDebouncedValue';

function fmt(n: number) {
  return new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' }).format(n);
}

async function descargarPrecios() {
  const res = await client.get('/api/reportes/lista-productos?formato=xlsx', { responseType: 'blob' });
  const url = URL.createObjectURL(res.data as Blob);
  const a = document.createElement('a');
  a.href = url; a.download = 'lista-precios.xlsx'; a.click();
  URL.revokeObjectURL(url);
}

export function PreciosPage() {
  const rol = localStorage.getItem('mmotos_rol');
  const esDueno = rol?.toUpperCase() === 'DUENO';
  const [pagina, setPagina] = useState(0);
  const [descargando, setDescargando] = useState(false);
  const [busqueda, setBusqueda] = useState('');
  const debouncedBusqueda = useDebouncedValue(busqueda, 300);

  const { data, isLoading } = useQuery({
    queryKey: ['todos-productos-precios', pagina, debouncedBusqueda],
    queryFn: () => getTodosProductos(pagina, 50, debouncedBusqueda),
    placeholderData: keepPreviousData,
    enabled: esDueno,
  });

  if (!esDueno) {
    return (
      <div className="flex flex-col items-center justify-center h-96 gap-3">
        <span className="material-symbols-outlined text-[48px] text-on-surface-variant">lock</span>
        <p className="text-on-surface-variant text-sm">Esta sección es solo para el dueño.</p>
      </div>
    );
  }

  const productos = data?.contenido ?? [];

  const handleDescargar = async () => {
    setDescargando(true);
    try { await descargarPrecios(); } finally { setDescargando(false); }
  };

  return (
    <div className="min-h-screen bg-surface-container">
      <Header title="Lista de Precios" />
      <div className="pt-11 p-6 space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-on-surface text-sm font-semibold uppercase tracking-widest">
          Lista de Precios{' '}
          <span className="text-on-surface-variant font-normal">
            ({data?.totalElementos ?? 0} productos)
          </span>
        </h2>
        <button className="btn-primary flex items-center gap-2" onClick={handleDescargar} disabled={descargando}>
          {descargando ? <Spinner size="sm" /> : <span className="material-symbols-outlined text-[16px]">download</span>}
          Exportar Excel
        </button>
      </div>

      <input
        className="input max-w-xs"
        placeholder="Filtrar por nombre o SKU..."
        value={busqueda}
        onChange={e => { setBusqueda(e.target.value); setPagina(0); }}

      />

      {isLoading && !data ? (
        <div className="flex justify-center py-12"><Spinner /></div>
      ) : (
        <>
          <div className="card p-0 overflow-hidden">
            <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="table-header">
                  <th className="text-left px-4 py-3">SKU</th>
                  <th className="text-left px-4 py-3">Nombre</th>
                  <th className="text-right px-4 py-3">Precio Base</th>
                  <th className="text-center px-4 py-3">Moneda</th>
                  <th className="text-right px-4 py-3">Precio en $ ARS</th>
                  <th className="text-right px-4 py-3">Costo</th>
                  <th className="text-right px-4 py-3">Margen %</th>
                  <th className="text-center px-4 py-3">Stock</th>
                </tr>
              </thead>
              <tbody>
                {productos.length === 0
                  ? <tr><td colSpan={8} className="px-4 py-8 text-center text-on-surface-variant">Sin resultados</td></tr>
                  : productos.map((p: ProductoDTO) => {
                    const margen = p.precioCompra && p.precioCompra > 0
                      ? ((p.precioEnPesos - p.precioCompra) / p.precioCompra * 100).toFixed(1)
                      : null;
                    return (
                    <tr key={p.id} className="table-row">
                      <td className="px-4 py-3 font-mono text-xs text-on-surface-variant">{p.sku}</td>
                      <td className="px-4 py-3 font-medium">{p.nombre}</td>
                      <td className="px-4 py-3 text-right">
                        {p.moneda === 'USD'
                          ? <span className="text-on-surface-variant">USD {p.precioBase.toLocaleString('es-AR')}</span>
                          : fmt(p.precioBase)}
                      </td>
                      <td className="px-4 py-3 text-center">
                        <span className={`text-xs px-2 py-0.5 rounded font-medium ${p.moneda === 'USD' ? 'bg-primary/10 text-primary' : 'bg-outline-variant/50 text-on-surface-variant'}`}>
                          {p.moneda}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-right font-semibold text-primary">{fmt(p.precioEnPesos)}</td>
                      <td className="px-4 py-3 text-right text-on-surface-variant text-xs">
                        {p.precioCompra ? fmt(p.precioCompra) : '—'}
                      </td>
                      <td className="px-4 py-3 text-right text-xs">
                        {margen !== null
                          ? <span className={parseFloat(margen) >= 0 ? 'text-green-400' : 'text-error'}>{margen}%</span>
                          : <span className="text-on-surface-variant">—</span>}
                      </td>
                      <td className={`px-4 py-3 text-center ${p.bajominimo ? 'text-error font-bold' : 'text-on-surface'}`}>
                        {p.stockActual}
                      </td>
                    </tr>
                    );
                  })}
              </tbody>
            </table>
            </div>
          </div>

          {data && data.totalPaginas > 1 && (
            <div className="flex items-center gap-3 mt-2 justify-center">
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
        </>
      )}
      </div>
    </div>
  );
}
