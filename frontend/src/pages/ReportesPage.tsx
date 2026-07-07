import { useState } from 'react';
import { descargarReporteVentas, descargarReporteStockBajo, descargarReporteCaja, descargarReporteListaProductos } from '../api/reportes';
import { client } from '../api/client';
import { Spinner } from '../components/ui/Spinner';

type Formato = 'xlsx' | 'pdf';

function hoy() { return new Date().toISOString().slice(0, 10); }
function primerDiaMes() {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-01`;
}

interface PreviewData {
  titulo: string;
  headers: string[];
  rows: any[][];
  [key: string]: any;
}

function PreviewModal({ data, onClose }: { data: PreviewData; onClose: () => void }) {
  return (
    <div className="fixed inset-0 bg-black/70 flex items-start justify-center z-50 pt-8 pb-4 px-4 overflow-y-auto" onClick={onClose}>
      <div className="card w-full max-w-5xl" onClick={e => e.stopPropagation()}>
        <div className="flex items-center justify-between mb-4">
          <div>
            <h3 className="font-semibold text-on-surface text-sm">{data.titulo}</h3>
            <p className="text-xs text-on-surface-variant mt-0.5">
              {data.cantidadVentas !== undefined && `${data.cantidadVentas} ventas`}
              {data.totalProductos !== undefined && `${data.totalProductos} productos`}
              {data.totalGeneral !== undefined && ` · Total: $${data.totalGeneral}`}
            </p>
          </div>
          <button onClick={onClose} className="material-symbols-outlined text-on-surface-variant hover:text-on-surface">close</button>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-xs">
            <thead>
              <tr className="table-header">
                {data.headers.map((h: string) => (
                  <th key={h} className="px-3 py-2 text-left whitespace-nowrap">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {data.rows.length === 0
                ? <tr><td colSpan={data.headers.length} className="px-3 py-6 text-center text-on-surface-variant">Sin datos</td></tr>
                : data.rows.map((row: any[], i: number) => (
                  <tr key={i} className="table-row">
                    {row.map((cell: any, j: number) => (
                      <td key={j} className="px-3 py-1.5 whitespace-nowrap">{String(cell ?? '—')}</td>
                    ))}
                  </tr>
                ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

export function ReportesPage() {
  const [loadingVentas, setLoadingVentas]       = useState(false);
  const [loadingStock, setLoadingStock]         = useState(false);
  const [loadingCaja, setLoadingCaja]           = useState(false);
  const [loadingProductos, setLoadingProductos] = useState(false);

  const [ventasDesde, setVentasDesde] = useState(primerDiaMes());
  const [ventasHasta, setVentasHasta] = useState(hoy());
  const [ventasFmt, setVentasFmt]     = useState<Formato>('xlsx');

  const [cajaFecha, setCajaFecha]       = useState(hoy());
  const [cajaFmt, setCajaFmt]           = useState<Formato>('xlsx');
  const [stockFmt, setStockFmt]         = useState<Formato>('xlsx');
  const [productosFmt, setProductosFmt] = useState<Formato>('xlsx');

  const [error, setError] = useState('');
  const [preview, setPreview] = useState<PreviewData | null>(null);
  const [previewLoading, setPreviewLoading] = useState<string | null>(null);

  const wrap = async (setLoading: (v: boolean) => void, fn: () => Promise<void>) => {
    setError(''); setLoading(true);
    try { await fn(); }
    catch { setError('Error al generar el reporte. Intentá de nuevo.'); }
    finally { setLoading(false); }
  };

  const verMas = async (tipo: string, params?: Record<string, string>) => {
    setPreviewLoading(tipo);
    try {
      const qs = params ? '?' + new URLSearchParams(params).toString() : '';
      const res = await client.get<PreviewData>(`/api/reportes/${tipo}/preview${qs}`);
      setPreview(res.data);
    } catch {
      setError('Error al cargar la vista previa.');
    } finally {
      setPreviewLoading(null);
    }
  };

  const fmtSelect = (val: Formato, set: (v: Formato) => void) => (
    <select className="input" value={val} onChange={e => set(e.target.value as Formato)}>
      <option value="xlsx">Excel (.xlsx)</option>
      <option value="pdf">PDF</option>
    </select>
  );

  return (
    <div className="p-6">
      <h2 className="text-on-surface text-sm font-semibold uppercase tracking-widest mb-6">Reportes</h2>

      {error && <p className="text-error text-xs mb-4 px-3 py-2 bg-error-container/20 rounded border border-error-container/30">{error}</p>}

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">

        {/* Reporte Ventas */}
        <div className="card space-y-4">
          <div>
            <p className="text-on-surface font-semibold text-sm uppercase tracking-widest mb-1">Ventas</p>
            <p className="text-on-surface-variant text-xs">Historial de ventas por rango de fechas con totales por método de pago.</p>
          </div>
          <div className="space-y-2">
            <div>
              <label className="kpi-label block mb-1">Desde</label>
              <input className="input" type="date" value={ventasDesde} onChange={e => setVentasDesde(e.target.value)} />
            </div>
            <div>
              <label className="kpi-label block mb-1">Hasta</label>
              <input className="input" type="date" value={ventasHasta} onChange={e => setVentasHasta(e.target.value)} />
            </div>
            <div>
              <label className="kpi-label block mb-1">Formato</label>
              {fmtSelect(ventasFmt, setVentasFmt)}
            </div>
          </div>
          <div className="flex gap-2">
            <button className="btn-secondary flex-1 flex items-center justify-center gap-1 text-xs"
              disabled={!!previewLoading}
              onClick={() => verMas('ventas', { desde: ventasDesde, hasta: ventasHasta })}>
              {previewLoading === 'ventas' ? <Spinner size="sm" /> : <span className="material-symbols-outlined text-[14px]">table_view</span>}
              Ver más
            </button>
            <button className="btn-primary flex-1 flex items-center justify-center gap-1 text-xs"
              disabled={loadingVentas}
              onClick={() => wrap(setLoadingVentas, () => descargarReporteVentas(ventasDesde, ventasHasta, ventasFmt))}>
              {loadingVentas ? <Spinner size="sm" /> : <span className="material-symbols-outlined text-[14px]">download</span>}
              Descargar
            </button>
          </div>
        </div>

        {/* Reporte Stock Bajo */}
        <div className="card space-y-4">
          <div>
            <p className="text-on-surface font-semibold text-sm uppercase tracking-widest mb-1">Stock Bajo</p>
            <p className="text-on-surface-variant text-xs">Productos con stock actual menor al stock mínimo, ordenados por criticidad.</p>
          </div>
          <div className="space-y-2">
            <div>
              <label className="kpi-label block mb-1">Formato</label>
              {fmtSelect(stockFmt, setStockFmt)}
            </div>
          </div>
          <div className="flex gap-2 mt-auto">
            <button className="btn-secondary flex-1 flex items-center justify-center gap-1 text-xs"
              disabled={!!previewLoading}
              onClick={() => verMas('stock-bajo')}>
              {previewLoading === 'stock-bajo' ? <Spinner size="sm" /> : <span className="material-symbols-outlined text-[14px]">table_view</span>}
              Ver más
            </button>
            <button className="btn-primary flex-1 flex items-center justify-center gap-1 text-xs"
              disabled={loadingStock}
              onClick={() => wrap(setLoadingStock, () => descargarReporteStockBajo(stockFmt))}>
              {loadingStock ? <Spinner size="sm" /> : <span className="material-symbols-outlined text-[14px]">download</span>}
              Descargar
            </button>
          </div>
        </div>

        {/* Reporte Caja */}
        <div className="card space-y-4">
          <div>
            <p className="text-on-surface font-semibold text-sm uppercase tracking-widest mb-1">Caja</p>
            <p className="text-on-surface-variant text-xs">Resumen del día de caja con KPIs por método de pago y detalle de ventas.</p>
          </div>
          <div className="space-y-2">
            <div>
              <label className="kpi-label block mb-1">Fecha</label>
              <input className="input" type="date" value={cajaFecha} onChange={e => setCajaFecha(e.target.value)} />
            </div>
            <div>
              <label className="kpi-label block mb-1">Formato</label>
              {fmtSelect(cajaFmt, setCajaFmt)}
            </div>
          </div>
          <div className="flex gap-2">
            <button className="btn-secondary flex-1 flex items-center justify-center gap-1 text-xs"
              disabled={!!previewLoading}
              onClick={() => verMas('caja', { fecha: cajaFecha })}>
              {previewLoading === 'caja' ? <Spinner size="sm" /> : <span className="material-symbols-outlined text-[14px]">table_view</span>}
              Ver más
            </button>
            <button className="btn-primary flex-1 flex items-center justify-center gap-1 text-xs"
              disabled={loadingCaja}
              onClick={() => wrap(setLoadingCaja, () => descargarReporteCaja(cajaFecha, cajaFmt))}>
              {loadingCaja ? <Spinner size="sm" /> : <span className="material-symbols-outlined text-[14px]">download</span>}
              Descargar
            </button>
          </div>
        </div>

        {/* Reporte Lista de Productos */}
        <div className="card space-y-4">
          <div>
            <p className="text-on-surface font-semibold text-sm uppercase tracking-widest mb-1">Lista de Productos</p>
            <p className="text-on-surface-variant text-xs">Catálogo completo con SKU, nombre, precio, stock actual y stock mínimo.</p>
          </div>
          <div className="space-y-2">
            <div>
              <label className="kpi-label block mb-1">Formato</label>
              {fmtSelect(productosFmt, setProductosFmt)}
            </div>
          </div>
          <div className="flex gap-2 mt-auto">
            <button className="btn-secondary flex-1 flex items-center justify-center gap-1 text-xs"
              disabled={!!previewLoading}
              onClick={() => verMas('lista-productos')}>
              {previewLoading === 'lista-productos' ? <Spinner size="sm" /> : <span className="material-symbols-outlined text-[14px]">table_view</span>}
              Ver más
            </button>
            <button className="btn-primary flex-1 flex items-center justify-center gap-1 text-xs"
              disabled={loadingProductos}
              onClick={() => wrap(setLoadingProductos, () => descargarReporteListaProductos(productosFmt))}>
              {loadingProductos ? <Spinner size="sm" /> : <span className="material-symbols-outlined text-[14px]">download</span>}
              Descargar
            </button>
          </div>
        </div>

      </div>

      {preview && <PreviewModal data={preview} onClose={() => setPreview(null)} />}
    </div>
  );
}
