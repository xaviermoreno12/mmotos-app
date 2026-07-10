import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Header } from '../components/layout/Header';
import { Spinner } from '../components/ui/Spinner';
import { buscarProductos } from '../api/productos';
import type { ProductoDTO } from '../types';
import {
  getFacturasBorradores, actualizarFacturaBorrador, confirmarFacturaBorrador, rechazarFacturaBorrador,
  type FacturaBorradorDTO, type LineaFacturaDTO,
} from '../api/facturasIA';

const CATEGORIAS = [
  'Repuestos y Mercadería', 'Herramientas y Maquinaria', 'Insumos de Taller',
  'Combustible y Flete', 'Servicios y Mantenimiento', 'Alquiler', 'Impuestos', 'Otros',
];

const ESTADOS_PAGO = ['Pendiente', 'Pagado'];

function fmt(n: number) {
  return new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' }).format(n);
}

function timeAgo(fechaStr: string) {
  const diff = Math.floor((Date.now() - new Date(fechaStr).getTime()) / 1000);
  if (diff < 60) return `hace ${diff}s`;
  if (diff < 3600) return `hace ${Math.floor(diff / 60)}min`;
  return `hace ${Math.floor(diff / 3600)}h`;
}

function ProductoBuscador({ onSeleccionar, onCerrar }: {
  onSeleccionar: (p: ProductoDTO) => void;
  onCerrar: () => void;
}) {
  const [termino, setTermino] = useState('');
  const { data: resultados = [], isFetching } = useQuery({
    queryKey: ['productos-buscar-factura', termino],
    queryFn: () => buscarProductos(termino),
    enabled: termino.trim().length >= 2,
  });

  return (
    <div className="border border-outline-variant rounded p-2 mt-1 bg-surface-container space-y-1">
      <div className="flex gap-1">
        <input
          className="input text-xs py-0.5"
          placeholder="Buscar producto existente..."
          value={termino}
          onChange={e => setTermino(e.target.value)}
          autoFocus
        />
        <button className="text-xs text-on-surface-variant px-2" onClick={onCerrar}>✕</button>
      </div>
      {isFetching && <p className="text-[10px] text-on-surface-variant">Buscando...</p>}
      {resultados.slice(0, 5).map(p => (
        <button
          key={p.id}
          onClick={() => onSeleccionar(p)}
          className="block w-full text-left text-xs px-2 py-1 rounded hover:bg-primary/10"
        >
          <span className="font-medium">{p.nombre}</span>
          <span className="text-on-surface-variant"> ({p.sku}) · stock {p.stockActual}</span>
        </button>
      ))}
      {termino.trim().length >= 2 && !isFetching && resultados.length === 0 && (
        <p className="text-[10px] text-on-surface-variant px-2">Sin resultados.</p>
      )}
    </div>
  );
}

function FacturaCard({ borrador, onConfirmar, onRechazar, confirmando, mensaje }: {
  borrador: FacturaBorradorDTO;
  onConfirmar: () => void;
  onRechazar: () => void;
  confirmando: boolean;
  mensaje: string | null;
}) {
  const qc = useQueryClient();
  const [cabecera, setCabecera] = useState({
    fechaFactura: borrador.fechaFactura || '',
    proveedorNombre: borrador.proveedorNombre || '',
    cuit: borrador.cuit || '',
    numeroFactura: borrador.numeroFactura || '',
    montoTotal: borrador.montoTotal ?? 0,
    categoriaGasto: borrador.categoriaGasto || 'Otros',
    estadoPago: borrador.estadoPago || 'Pendiente',
  });
  const [lineas, setLineas] = useState<LineaFacturaDTO[]>(borrador.lineas);
  const [editando, setEditando] = useState(false);
  const [verFoto, setVerFoto] = useState(false);
  const [verOcr, setVerOcr] = useState(false);
  const [buscadorAbierto, setBuscadorAbierto] = useState<number | null>(null);

  const actualizarMut = useMutation({
    mutationFn: () => actualizarFacturaBorrador(borrador.id, {
      ...cabecera,
      lineas,
      observaciones: borrador.observaciones,
    }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['facturas-borradores'] }); setEditando(false); },
  });

  const total = lineas.reduce((s, l) => s + l.cantidad * l.precioUnitario, 0);

  const updateCabecera = <K extends keyof typeof cabecera>(campo: K, valor: typeof cabecera[K]) => {
    setCabecera(prev => ({ ...prev, [campo]: valor }));
    setEditando(true);
  };

  const updateLinea = (i: number, campo: keyof LineaFacturaDTO, valor: string | number | boolean | null) => {
    setLineas(prev => prev.map((l, idx) => idx === i ? { ...l, [campo]: valor } : l));
    setEditando(true);
  };

  const toggleNuevo = (i: number) => {
    setLineas(prev => prev.map((l, idx) => {
      if (idx !== i) return l;
      if (l.esNuevo) {
        // Pasar a "existente": limpiar productoId hasta que se elija uno
        return { ...l, esNuevo: false, productoId: null };
      }
      return { ...l, esNuevo: true, productoId: null };
    }));
    setBuscadorAbierto(null);
    setEditando(true);
  };

  const seleccionarProducto = (i: number, producto: ProductoDTO) => {
    setLineas(prev => prev.map((l, idx) => idx === i ? {
      ...l,
      esNuevo: false,
      productoId: producto.id,
      sku: producto.sku,
      ubicacionFisica: producto.ubicacionFisica,
      stockMinimo: producto.stockMinimo,
      moneda: producto.moneda,
    } : l));
    setBuscadorAbierto(null);
    setEditando(true);
  };

  const quitarLinea = (i: number) => {
    setLineas(prev => prev.filter((_, idx) => idx !== i));
    setEditando(true);
  };

  const agregarLinea = () => {
    setLineas(prev => [...prev, {
      productoId: null, esNuevo: true, sku: 'NUEVO-PROD-' + Math.floor(Math.random() * 9000 + 1000),
      nombre: '', cantidad: 1, precioUnitario: 0, ubicacionFisica: null, stockMinimo: 2, moneda: 'ARS',
    }]);
    setEditando(true);
  };

  return (
    <div className="card space-y-4">
      {/* Cabecera */}
      <div className="flex items-start justify-between gap-2">
        <div className="flex-1">
          <p className="text-sm font-semibold text-on-surface flex items-center gap-2">
            <span className="material-symbols-outlined text-[16px] text-primary">fact_check</span>
            {cabecera.numeroFactura ? `Factura #${cabecera.numeroFactura}` : 'Sin número de factura'}
          </p>
          <p className="text-xs text-on-surface-variant mt-0.5">
            {cabecera.proveedorNombre || 'Proveedor desconocido'} · {timeAgo(borrador.fechaRecepcion)}
          </p>
        </div>
        <div className="flex flex-col items-end gap-1">
          {borrador.imagenBase64 && (
            <button
              onClick={() => setVerFoto(v => !v)}
              className="text-xs text-primary hover:underline flex items-center gap-1"
            >
              <span className="material-symbols-outlined text-[14px]">{verFoto ? 'image_not_supported' : 'image'}</span>
              {verFoto ? 'Ocultar foto' : 'Ver foto'}
            </button>
          )}
          {borrador.textoOcr && (
            <button
              onClick={() => setVerOcr(v => !v)}
              className="text-xs text-on-surface-variant hover:underline flex items-center gap-1"
            >
              <span className="material-symbols-outlined text-[14px]">description</span>
              {verOcr ? 'Ocultar texto OCR' : 'Ver texto OCR'}
            </button>
          )}
        </div>
      </div>

      {/* Foto de la factura */}
      {verFoto && borrador.imagenBase64 && (
        <div className="border border-outline-variant rounded overflow-hidden">
          <img
            src={`data:image/jpeg;base64,${borrador.imagenBase64}`}
            alt="Factura"
            className="w-full max-h-80 object-contain bg-black/10"
          />
        </div>
      )}

      {/* Texto OCR */}
      {verOcr && borrador.textoOcr && (
        <pre className="text-[10px] bg-surface-container-low border border-outline-variant rounded p-2 max-h-40 overflow-auto whitespace-pre-wrap">
          {borrador.textoOcr}
        </pre>
      )}

      {/* Campos de cabecera */}
      <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
        <div>
          <label className="kpi-label block mb-1">Proveedor</label>
          <input className="input text-xs py-1" value={cabecera.proveedorNombre}
            onChange={e => updateCabecera('proveedorNombre', e.target.value)} />
        </div>
        <div>
          <label className="kpi-label block mb-1">CUIT</label>
          <input className="input text-xs py-1" value={cabecera.cuit}
            onChange={e => updateCabecera('cuit', e.target.value)} />
        </div>
        <div>
          <label className="kpi-label block mb-1">Nº Factura</label>
          <input className="input text-xs py-1" value={cabecera.numeroFactura}
            onChange={e => updateCabecera('numeroFactura', e.target.value)} />
        </div>
        <div>
          <label className="kpi-label block mb-1">Fecha</label>
          <input className="input text-xs py-1" value={cabecera.fechaFactura}
            onChange={e => updateCabecera('fechaFactura', e.target.value)} />
        </div>
        <div>
          <label className="kpi-label block mb-1">Categoría</label>
          <select className="input text-xs py-1" value={cabecera.categoriaGasto}
            onChange={e => updateCabecera('categoriaGasto', e.target.value)}>
            {CATEGORIAS.map(c => <option key={c}>{c}</option>)}
          </select>
        </div>
        <div>
          <label className="kpi-label block mb-1">Estado de pago</label>
          <select className="input text-xs py-1" value={cabecera.estadoPago}
            onChange={e => updateCabecera('estadoPago', e.target.value)}>
            {ESTADOS_PAGO.map(e => <option key={e}>{e}</option>)}
          </select>
        </div>
        <div>
          <label className="kpi-label block mb-1">Total factura</label>
          <input className="input text-xs py-1" type="number" min="0" step="0.01"
            value={cabecera.montoTotal}
            onChange={e => updateCabecera('montoTotal', parseFloat(e.target.value) || 0)} />
        </div>
      </div>

      {/* Tabla de líneas */}
      <div className="overflow-x-auto">
        <table className="w-full text-xs">
          <thead>
            <tr className="table-header">
              <th className="px-3 py-2 text-left">Artículo</th>
              <th className="px-3 py-2 text-center w-16">Cant.</th>
              <th className="px-3 py-2 text-right w-28">Precio unit.</th>
              <th className="px-3 py-2 text-right w-28">Subtotal</th>
              <th className="px-3 py-2 text-left w-40">Producto</th>
              <th className="px-3 py-2 w-8"></th>
            </tr>
          </thead>
          <tbody>
            {lineas.map((l, i) => (
              <tr key={i} className="table-row align-top">
                <td className="px-3 py-1.5">
                  <input
                    className="input text-xs py-0.5"
                    value={l.nombre}
                    onChange={e => updateLinea(i, 'nombre', e.target.value)}
                  />
                </td>
                <td className="px-3 py-1.5">
                  <input
                    className="input text-xs py-0.5 text-center"
                    type="number" min="1"
                    value={l.cantidad}
                    onChange={e => updateLinea(i, 'cantidad', parseInt(e.target.value) || 1)}
                  />
                </td>
                <td className="px-3 py-1.5">
                  <input
                    className="input text-xs py-0.5 text-right"
                    type="number" min="0" step="0.01"
                    value={l.precioUnitario}
                    onChange={e => updateLinea(i, 'precioUnitario', parseFloat(e.target.value) || 0)}
                  />
                </td>
                <td className="px-3 py-1.5 text-right font-medium text-primary">
                  {fmt(l.cantidad * l.precioUnitario)}
                </td>
                <td className="px-3 py-1.5">
                  {l.esNuevo ? (
                    <div className="space-y-1">
                      <span className="inline-block text-[10px] px-2 py-0.5 rounded bg-primary/15 text-primary font-medium">
                        Producto nuevo
                      </span>
                      <input className="input text-[10px] py-0.5" placeholder="SKU"
                        value={l.sku} onChange={e => updateLinea(i, 'sku', e.target.value)} />
                      <input className="input text-[10px] py-0.5" placeholder="Ubicación física"
                        value={l.ubicacionFisica || ''} onChange={e => updateLinea(i, 'ubicacionFisica', e.target.value)} />
                      <input className="input text-[10px] py-0.5" type="number" min="0" placeholder="Stock mínimo"
                        value={l.stockMinimo} onChange={e => updateLinea(i, 'stockMinimo', parseInt(e.target.value) || 0)} />
                      <button onClick={() => toggleNuevo(i)} className="text-[10px] text-primary hover:underline">
                        Marcar como existente
                      </button>
                    </div>
                  ) : (
                    <div className="space-y-1">
                      {l.productoId ? (
                        <span className="inline-block text-[10px] px-2 py-0.5 rounded bg-secondary/15 text-secondary font-medium">
                          Existente: {l.sku}
                        </span>
                      ) : (
                        <span className="inline-block text-[10px] px-2 py-0.5 rounded bg-error/15 text-error font-medium">
                          Sin producto asignado
                        </span>
                      )}
                      <div className="flex gap-2">
                        <button onClick={() => setBuscadorAbierto(buscadorAbierto === i ? null : i)} className="text-[10px] text-primary hover:underline">
                          {l.productoId ? 'Cambiar' : 'Buscar producto'}
                        </button>
                        <button onClick={() => toggleNuevo(i)} className="text-[10px] text-on-surface-variant hover:underline">
                          Marcar como nuevo
                        </button>
                      </div>
                      {buscadorAbierto === i && (
                        <ProductoBuscador
                          onSeleccionar={p => seleccionarProducto(i, p)}
                          onCerrar={() => setBuscadorAbierto(null)}
                        />
                      )}
                    </div>
                  )}
                </td>
                <td className="px-3 py-1.5 text-center">
                  <button onClick={() => quitarLinea(i)} className="text-error hover:opacity-70">
                    <span className="material-symbols-outlined text-[14px]">delete</span>
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
          <tfoot>
            <tr>
              <td colSpan={3} className="px-3 py-2 text-right text-xs text-on-surface-variant font-medium">Total líneas:</td>
              <td className="px-3 py-2 text-right text-sm font-bold text-primary">{fmt(total)}</td>
              <td colSpan={2} />
            </tr>
          </tfoot>
        </table>
      </div>

      <button
        onClick={agregarLinea}
        className="text-xs text-primary hover:underline flex items-center gap-1"
      >
        <span className="material-symbols-outlined text-[14px]">add</span>
        Agregar línea
      </button>

      {/* Guardar cambios */}
      {editando && (
        <button
          onClick={() => actualizarMut.mutate()}
          disabled={actualizarMut.isPending}
          className="btn-secondary text-xs flex items-center gap-1 w-full justify-center"
        >
          {actualizarMut.isPending ? <Spinner size="sm" /> : <span className="material-symbols-outlined text-[14px]">save</span>}
          Guardar cambios
        </button>
      )}

      {mensaje && (
        <p className="text-xs text-primary bg-primary/10 rounded px-3 py-2">{mensaje}</p>
      )}

      {/* Acciones */}
      <div className="flex gap-3 pt-1">
        <button
          onClick={onConfirmar}
          disabled={confirmando}
          className="btn-primary flex-1 flex items-center justify-center gap-2"
        >
          {confirmando ? <Spinner size="sm" /> : <span className="material-symbols-outlined text-[16px]">check_circle</span>}
          Confirmar
        </button>
        <button
          onClick={onRechazar}
          className="flex-1 flex items-center justify-center gap-2 px-3 py-2 text-sm font-medium border border-error/40 text-error rounded hover:bg-error/10"
        >
          <span className="material-symbols-outlined text-[16px]">cancel</span>
          Rechazar
        </button>
      </div>
    </div>
  );
}

export function FacturasIAPage() {
  const qc = useQueryClient();
  const [mensajes, setMensajes] = useState<Record<string, string>>({});

  const { data: borradores = [], isLoading } = useQuery({
    queryKey: ['facturas-borradores'],
    queryFn: getFacturasBorradores,
    refetchInterval: 15000,
  });

  const confirmarMut = useMutation({
    mutationFn: confirmarFacturaBorrador,
    onSuccess: (resultado, id) => {
      setMensajes(prev => ({
        ...prev,
        [id]: `Listo: ${resultado.productosCreados} producto(s) nuevo(s), ` +
          `${resultado.productosActualizados} con stock actualizado. Gasto registrado.`,
      }));
      qc.invalidateQueries({ queryKey: ['facturas-borradores'] });
      qc.invalidateQueries({ queryKey: ['productos'] });
      qc.invalidateQueries({ queryKey: ['gastos'] });
      qc.invalidateQueries({ queryKey: ['compras'] });
    },
  });

  const rechazarMut = useMutation({
    mutationFn: rechazarFacturaBorrador,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['facturas-borradores'] }),
  });

  return (
    <div className="min-h-screen bg-surface-container">
      <Header title="Facturas IA" />
      <div className="pt-11 p-5 max-w-3xl space-y-4">

        {isLoading && (
          <div className="flex justify-center py-12"><Spinner /></div>
        )}

        {!isLoading && borradores.length === 0 && (
          <div className="card text-center py-12">
            <span className="material-symbols-outlined text-[48px] text-on-surface-variant mb-3 block">inbox</span>
            <p className="text-on-surface-variant text-sm">No hay facturas pendientes de revisión.</p>
            <p className="text-xs text-on-surface-variant mt-1">
              Cuando mandes una foto de una factura de compra al bot de Telegram, aparecerá aquí.
            </p>
          </div>
        )}

        {borradores.map(b => (
          <FacturaCard
            key={b.id}
            borrador={b}
            confirmando={confirmarMut.isPending && confirmarMut.variables === b.id}
            mensaje={mensajes[b.id] || null}
            onConfirmar={() => confirmarMut.mutate(b.id)}
            onRechazar={() => rechazarMut.mutate(b.id)}
          />
        ))}

      </div>
    </div>
  );
}
