import { useState, useEffect } from 'react';
import { client } from '../api/client';
import type { ProductoDTO } from '../types';
import { Spinner } from '../components/ui/Spinner';
import { Header } from '../components/layout/Header';
import { useDebouncedValue } from '../hooks/useDebouncedValue';

export function EtiquetasPage() {
  const [busqueda, setBusqueda] = useState('');
  const debouncedBusqueda = useDebouncedValue(busqueda, 300);
  const [resultados, setResultados] = useState<ProductoDTO[]>([]);
  const [seleccionados, setSeleccionados] = useState<ProductoDTO[]>([]);
  const [buscando, setBuscando] = useState(false);
  const [descargando, setDescargando] = useState(false);

  useEffect(() => {
    if (debouncedBusqueda.length < 2) { setResultados([]); return; }
    setBuscando(true);
    client.get<ProductoDTO[]>(`/api/productos/buscar?termino=${debouncedBusqueda}`)
      .then(res => setResultados(res.data))
      .finally(() => setBuscando(false));
  }, [debouncedBusqueda]);

  const agregar = (p: ProductoDTO) => {
    if (!seleccionados.find(s => s.id === p.id)) setSeleccionados(prev => [...prev, p]);
    setResultados([]);
    setBusqueda('');
  };

  const quitar = (id: string) => setSeleccionados(prev => prev.filter(p => p.id !== id));

  const descargar = async () => {
    if (seleccionados.length === 0) return;
    setDescargando(true);
    try {
      const ids = seleccionados.map(p => p.id).join(',');
      const res = await client.get(`/api/etiquetas/pdf?ids=${ids}`, { responseType: 'blob' });
      const url = URL.createObjectURL(res.data as Blob);
      const a = document.createElement('a');
      a.href = url; a.download = 'etiquetas.pdf'; a.click();
      URL.revokeObjectURL(url);
    } finally { setDescargando(false); }
  };

  return (
    <div className="min-h-screen bg-surface-container">
      <Header title="Etiquetas de Precios" />
      <div className="pt-11 p-6">
      <h2 className="text-on-surface text-sm font-semibold uppercase tracking-widest mb-6">Etiquetas de Precios</h2>

      <div className="grid grid-cols-2 gap-6">
        <div>
          <p className="kpi-label mb-2">Buscar producto</p>
          <div className="relative">
            <input className="input" placeholder="Nombre o SKU..."
              value={busqueda} onChange={e => setBusqueda(e.target.value)} />
            {buscando && <div className="absolute right-3 top-2.5"><Spinner size="sm" /></div>}
            {resultados.length > 0 && (
              <div className="absolute z-10 top-full left-0 right-0 bg-surface-container-highest border border-outline-variant rounded mt-0.5 max-h-48 overflow-y-auto">
                {resultados.map(p => (
                  <button key={p.id} type="button" onClick={() => agregar(p)}
                    className="w-full text-left px-3 py-2 text-xs hover:bg-surface-container-high">
                    <span className="font-medium">{p.nombre}</span>
                    <span className="ml-2 text-on-surface-variant">{p.sku}</span>
                    <span className="ml-2 text-primary-container">${p.precioEnPesos.toLocaleString('es-AR')}</span>
                  </button>
                ))}
              </div>
            )}
          </div>

          <div className="mt-4 space-y-2">
            {seleccionados.length === 0
              ? <p className="text-on-surface-variant text-xs py-4 text-center border border-dashed border-outline-variant rounded">Sin productos seleccionados</p>
              : seleccionados.map(p => (
                <div key={p.id} className="flex items-center justify-between card py-2 px-3">
                  <div>
                    <p className="text-sm font-medium">{p.nombre}</p>
                    <p className="text-xs text-on-surface-variant">{p.sku} · ${p.precioEnPesos.toLocaleString('es-AR')}</p>
                  </div>
                  <button onClick={() => quitar(p.id)} className="text-error text-xs hover:opacity-70">✕</button>
                </div>
              ))}
          </div>
        </div>

        <div className="flex flex-col gap-4">
          <div className="card">
            <p className="kpi-label mb-2">Vista previa de etiqueta</p>
            {seleccionados.length > 0 ? (
              <div className="border border-outline-variant rounded p-4 text-center space-y-1 max-w-[140px] mx-auto">
                <p className="text-xs font-bold leading-tight">{seleccionados[0].nombre}</p>
                <p className="text-xs text-on-surface-variant">SKU: {seleccionados[0].sku}</p>
                <p className="text-lg font-bold text-error">${seleccionados[0].precioEnPesos.toLocaleString('es-AR')}</p>
              </div>
            ) : (
              <p className="text-xs text-on-surface-variant text-center py-6 border border-dashed border-outline-variant rounded">Seleccioná un producto para ver la vista previa</p>
            )}
            <p className="text-xs text-on-surface-variant text-center mt-2">4 etiquetas por fila · A4</p>
          </div>

          <button className="btn-primary flex items-center justify-center gap-2 py-3"
            onClick={descargar} disabled={seleccionados.length === 0 || descargando}>
            {descargando
              ? <Spinner size="sm" />
              : <span className="material-symbols-outlined text-sm">print</span>}
            Generar PDF ({seleccionados.length} etiquetas)
          </button>
        </div>
      </div>
      </div>
    </div>
  );
}
