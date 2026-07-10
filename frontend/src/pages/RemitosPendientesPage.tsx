import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Header } from '../components/layout/Header';
import { Spinner } from '../components/ui/Spinner';
import {
  getBorradores, confirmarBorrador, rechazarBorrador, actualizarBorrador,
  type CompraBorradorDTO, type LineaBorradorDTO,
} from '../api/compras';

function fmt(n: number) {
  return new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' }).format(n);
}

function timeAgo(fechaStr: string) {
  const diff = Math.floor((Date.now() - new Date(fechaStr).getTime()) / 1000);
  if (diff < 60) return `hace ${diff}s`;
  if (diff < 3600) return `hace ${Math.floor(diff / 60)}min`;
  return `hace ${Math.floor(diff / 3600)}h`;
}

function BorradorCard({ borrador, onConfirmar, onRechazar, confirmando }: {
  borrador: CompraBorradorDTO;
  onConfirmar: () => void;
  onRechazar: () => void;
  confirmando?: boolean;
}) {
  const qc = useQueryClient();
  const [lineas, setLineas] = useState<LineaBorradorDTO[]>(borrador.lineas);
  const [editando, setEditando] = useState(false);
  const [verFoto, setVerFoto] = useState(false);

  const actualizarMut = useMutation({
    mutationFn: () => actualizarBorrador(borrador.id, lineas),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['borradores'] }); setEditando(false); },
  });

  const total = lineas.reduce((s, l) => s + l.cantidad * l.precioUnitario, 0);

  const updateLinea = (i: number, campo: keyof LineaBorradorDTO, valor: string | number) => {
    setLineas(prev => prev.map((l, idx) => idx === i ? { ...l, [campo]: valor } : l));
    setEditando(true);
  };

  const quitarLinea = (i: number) => {
    setLineas(prev => prev.filter((_, idx) => idx !== i));
    setEditando(true);
  };

  const agregarLinea = () => {
    setLineas(prev => [...prev, { productoId: null, sku: null, nombre: '', cantidad: 1, precioUnitario: 0 }]);
    setEditando(true);
  };

  return (
    <div className="card space-y-4">
      {/* Cabecera */}
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm font-semibold text-on-surface flex items-center gap-2">
            <span className="material-symbols-outlined text-[16px] text-primary">receipt</span>
            {borrador.numeroRemito ? `Remito #${borrador.numeroRemito}` : 'Sin número de remito'}
          </p>
          <p className="text-xs text-on-surface-variant mt-0.5">
            {borrador.proveedorNombre || 'Proveedor desconocido'} · {timeAgo(borrador.fechaRecepcion)}
          </p>
        </div>
        {borrador.imagenBase64 && (
          <button
            onClick={() => setVerFoto(v => !v)}
            className="text-xs text-primary hover:underline flex items-center gap-1"
          >
            <span className="material-symbols-outlined text-[14px]">{verFoto ? 'image_not_supported' : 'image'}</span>
            {verFoto ? 'Ocultar foto' : 'Ver foto'}
          </button>
        )}
      </div>

      {/* Foto del remito */}
      {verFoto && borrador.imagenBase64 && (
        <div className="border border-outline-variant rounded overflow-hidden">
          <img
            src={`data:image/jpeg;base64,${borrador.imagenBase64}`}
            alt="Remito"
            className="w-full max-h-80 object-contain bg-black/10"
          />
        </div>
      )}

      {/* Tabla de artículos */}
      <div className="overflow-x-auto">
        <table className="w-full text-xs">
          <thead>
            <tr className="table-header">
              <th className="px-3 py-2 text-left">Artículo</th>
              <th className="px-3 py-2 text-center w-16">Cant.</th>
              <th className="px-3 py-2 text-right w-28">Precio unit.</th>
              <th className="px-3 py-2 text-right w-28">Subtotal</th>
              <th className="px-3 py-2 w-8"></th>
            </tr>
          </thead>
          <tbody>
            {lineas.map((l, i) => (
              <tr key={i} className="table-row">
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
              <td colSpan={3} className="px-3 py-2 text-right text-xs text-on-surface-variant font-medium">Total:</td>
              <td className="px-3 py-2 text-right text-sm font-bold text-primary">{fmt(total)}</td>
              <td />
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

      {/* Acciones */}
      <div className="flex gap-3 pt-1">
        <button
          onClick={onConfirmar}
          disabled={confirmando}
          className="btn-primary flex-1 flex items-center justify-center gap-2 disabled:opacity-60"
        >
          {confirmando
            ? <Spinner size="sm" />
            : <span className="material-symbols-outlined text-[16px]">check_circle</span>}
          {confirmando ? 'Confirmando...' : 'Confirmar compra'}
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

export function RemitosPendientesPage() {
  const qc = useQueryClient();

  const { data: borradores = [], isLoading } = useQuery({
    queryKey: ['borradores'],
    queryFn: getBorradores,
    refetchInterval: 15000,
  });

  const confirmarMut = useMutation({
    mutationFn: confirmarBorrador,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['borradores'] }),
  });

  const rechazarMut = useMutation({
    mutationFn: rechazarBorrador,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['borradores'] }),
  });

  return (
    <div className="min-h-screen bg-surface-container">
      <Header title="Remitos Pendientes" />
      <div className="pt-11 p-5 max-w-3xl space-y-4">

        {isLoading && (
          <div className="flex justify-center py-12"><Spinner /></div>
        )}

        {!isLoading && borradores.length === 0 && (
          <div className="card text-center py-12">
            <span className="material-symbols-outlined text-[48px] text-on-surface-variant mb-3 block">inbox</span>
            <p className="text-on-surface-variant text-sm">No hay remitos pendientes de revisión.</p>
            <p className="text-xs text-on-surface-variant mt-1">
              Cuando mandes una foto al bot de Telegram, aparecerá aquí.
            </p>
          </div>
        )}

        {borradores.map(b => (
          <BorradorCard
            key={b.id}
            borrador={b}
            onConfirmar={() => confirmarMut.mutate(b.id)}
            onRechazar={() => rechazarMut.mutate(b.id)}
            confirmando={confirmarMut.isPending && confirmarMut.variables === b.id}
          />
        ))}

      </div>
    </div>
  );
}
