import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Header } from '../components/layout/Header';
import { Badge } from '../components/ui/Badge';
import { EmptyState } from '../components/ui/EmptyState';
import { Spinner } from '../components/ui/Spinner';
import { useCajaActiva, useAbrirCaja, useCerrarCaja } from '../hooks/useCaja';
import { useVentasList } from '../hooks/useVentasList';
import { getHistorialCajas } from '../api/ventas';

function formatMoney(n: number) {
  return n.toLocaleString('es-AR', { style: 'currency', currency: 'ARS' });
}

function formatTime(iso: string) {
  return new Date(iso).toLocaleTimeString('es-AR', { hour: '2-digit', minute: '2-digit' });
}

function formatDateTime(iso: string) {
  return new Date(iso).toLocaleString('es-AR', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
}

// ======================== MODAL: Abrir Caja ========================
function AbrirCajaModal({ onClose, onConfirm, isLoading }: {
  onClose: () => void;
  onConfirm: (monto: number) => void;
  isLoading: boolean;
}) {
  const [monto, setMonto] = useState('');

  return (
    <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div className="bg-surface-container-high rounded-2xl shadow-2xl w-full max-w-md p-6 space-y-5">
        <h2 className="text-lg font-bold text-on-surface">Abrir Caja</h2>
        <p className="text-sm text-on-surface-variant">
          Ingresá el monto inicial en efectivo con el que arranca la caja.
        </p>
        <div>
          <label className="block text-xs font-medium text-on-surface-variant mb-1">
            Monto inicial ($)
          </label>
          <input
            type="number"
            min="0"
            step="0.01"
            value={monto}
            onChange={(e) => setMonto(e.target.value)}
            placeholder="0.00"
            className="w-full px-3 py-2 rounded-lg bg-surface border border-outline-variant text-on-surface text-sm focus:outline-none focus:ring-2 focus:ring-primary"
            autoFocus
          />
        </div>
        <div className="flex justify-end gap-3">
          <button
            onClick={onClose}
            className="px-4 py-2 text-sm text-on-surface-variant hover:text-on-surface transition-colors"
            disabled={isLoading}
          >
            Cancelar
          </button>
          <button
            onClick={() => onConfirm(parseFloat(monto) || 0)}
            className="btn-primary"
            disabled={isLoading}
          >
            {isLoading ? 'Abriendo...' : 'Abrir Caja'}
          </button>
        </div>
      </div>
    </div>
  );
}

// ======================== MODAL: Cerrar Caja ========================
function CerrarCajaModal({ onClose, onConfirm, isLoading, montoSistema }: {
  onClose: () => void;
  onConfirm: (montoContado: number, observaciones: string) => void;
  isLoading: boolean;
  montoSistema: number;
}) {
  const [montoContado, setMontoContado] = useState('');
  const [observaciones, setObservaciones] = useState('');

  const contado = parseFloat(montoContado) || 0;
  const diferencia = contado - montoSistema;

  return (
    <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div className="bg-surface-container-high rounded-2xl shadow-2xl w-full max-w-md p-6 space-y-5">
        <h2 className="text-lg font-bold text-on-surface">Cerrar Caja</h2>

        {/* Resumen del sistema */}
        <div className="card bg-surface-container p-4">
          <p className="text-xs text-on-surface-variant">Total en sistema (efectivo)</p>
          <p className="text-2xl font-bold text-on-surface">{formatMoney(montoSistema)}</p>
        </div>

        <div>
          <label className="block text-xs font-medium text-on-surface-variant mb-1">
            Efectivo contado ($)
          </label>
          <input
            type="number"
            min="0"
            step="0.01"
            value={montoContado}
            onChange={(e) => setMontoContado(e.target.value)}
            placeholder="0.00"
            className="w-full px-3 py-2 rounded-lg bg-surface border border-outline-variant text-on-surface text-sm focus:outline-none focus:ring-2 focus:ring-primary"
            autoFocus
          />
        </div>

        {/* Diferencia visual */}
        {montoContado && (
          <div className={`rounded-lg p-3 text-sm font-semibold ${
            diferencia === 0
              ? 'bg-green-900/30 text-green-400'
              : diferencia > 0
                ? 'bg-blue-900/30 text-blue-400'
                : 'bg-red-900/30 text-red-400'
          }`}>
            Diferencia: {diferencia >= 0 ? '+' : ''}{formatMoney(diferencia)}
            {diferencia === 0 && ' ✓ Cuadra'}
            {diferencia > 0 && ' (sobrante)'}
            {diferencia < 0 && ' (faltante)'}
          </div>
        )}

        <div>
          <label className="block text-xs font-medium text-on-surface-variant mb-1">
            Observaciones (opcional)
          </label>
          <textarea
            value={observaciones}
            onChange={(e) => setObservaciones(e.target.value)}
            rows={2}
            placeholder="Notas sobre el cierre..."
            className="w-full px-3 py-2 rounded-lg bg-surface border border-outline-variant text-on-surface text-sm focus:outline-none focus:ring-2 focus:ring-primary resize-none"
          />
        </div>

        <div className="flex justify-end gap-3">
          <button
            onClick={onClose}
            className="px-4 py-2 text-sm text-on-surface-variant hover:text-on-surface transition-colors"
            disabled={isLoading}
          >
            Cancelar
          </button>
          <button
            onClick={() => onConfirm(contado, observaciones)}
            className="px-4 py-2 rounded-lg bg-error hover:opacity-90 text-on-error text-sm font-medium transition-colors"
            disabled={isLoading}
          >
            {isLoading ? 'Cerrando...' : 'Confirmar Cierre'}
          </button>
        </div>
      </div>
    </div>
  );
}

// ======================== PÁGINA PRINCIPAL ========================
export function CajaPage() {
  const { data: caja, isLoading: loadingCaja } = useCajaActiva();
  const hoy = new Date().toISOString().slice(0, 10);
  const { data: ventas, isLoading: loadingVentas } = useVentasList(hoy, hoy);
  const { data: historialCajas = [] } = useQuery({
    queryKey: ['caja-historial'],
    queryFn: () => getHistorialCajas(10),
  });

  const abrirMutation = useAbrirCaja();
  const cerrarMutation = useCerrarCaja();

  const [showAbrirModal, setShowAbrirModal] = useState(false);
  const [showCerrarModal, setShowCerrarModal] = useState(false);

  const cajaAbierta = caja != null && caja.estado === 'ABIERTA';
  const resumen = caja?.resumen;

  const handleAbrir = (monto: number) => {
    abrirMutation.mutate({ montoInicial: monto }, {
      onSuccess: () => setShowAbrirModal(false),
    });
  };

  const handleCerrar = (montoContado: number, observaciones: string) => {
    cerrarMutation.mutate(
      { montoFinalContado: montoContado, observaciones: observaciones || null },
      { onSuccess: () => setShowCerrarModal(false) },
    );
  };

  // Calcular monto sistema para el modal de cierre
  const montoSistema = cajaAbierta
    ? (caja.montoInicial ?? 0) + (resumen?.totalEfectivo ?? 0)
    : 0;

  const isLoading = loadingCaja || loadingVentas;

  // KPIs para caja abierta
  const kpis = cajaAbierta ? [
    { label: 'Cajero', value: caja.cajeroNombre },
    { label: 'Apertura', value: formatDateTime(caja.fechaApertura) },
    { label: 'Efectivo ventas', value: formatMoney(resumen?.totalEfectivo ?? 0) },
    { label: 'Tarjeta', value: formatMoney(resumen?.totalTarjeta ?? 0) },
    { label: 'Transferencia', value: formatMoney(resumen?.totalTransferencia ?? 0) },
    { label: 'MercadoPago', value: formatMoney(resumen?.totalMercadoPago ?? 0) },
  ] : [];

  const kpis2 = cajaAbierta ? [
    { label: 'Cant. ventas', value: String(resumen?.cantidadVentas ?? 0) },
    { label: 'Total ventas', value: formatMoney(resumen?.totalVentas ?? 0) },
    { label: 'Monto inicial', value: formatMoney(caja.montoInicial ?? 0) },
    { label: 'Efectivo en caja', value: formatMoney(montoSistema) },
  ] : [];

  return (
    <div className="min-h-screen bg-surface-container">
      <Header
        title="Caja"
        action={
          cajaAbierta ? (
            <button
              className="px-4 py-1.5 rounded-lg bg-error hover:opacity-90 text-on-error text-xs font-medium transition-colors"
              onClick={() => setShowCerrarModal(true)}
            >
              Cerrar caja
            </button>
          ) : (
            <button
              className="btn-primary"
              onClick={() => setShowAbrirModal(true)}
            >
              Abrir caja
            </button>
          )
        }
      />
      <div className="pt-11 p-5 space-y-5">
        {isLoading ? (
          <div className="flex justify-center py-12"><Spinner /></div>
        ) : !cajaAbierta ? (
          /* ====== Estado: sin caja abierta ====== */
          <div className="flex flex-col items-center justify-center py-20 space-y-4">
            <div className="w-16 h-16 rounded-full bg-surface-container-high flex items-center justify-center">
              <span className="material-symbols-outlined text-3xl text-on-surface-variant">point_of_sale</span>
            </div>
            <h2 className="text-lg font-bold text-on-surface">No hay caja abierta</h2>
            <p className="text-sm text-on-surface-variant text-center max-w-sm">
              Abrí una caja para empezar a registrar ventas del turno. 
              El sistema calculará automáticamente los totales.
            </p>
            <button
              className="btn-primary mt-2"
              onClick={() => setShowAbrirModal(true)}
            >
              Abrir caja ahora
            </button>
          </div>
        ) : (
          /* ====== Estado: caja abierta ====== */
          <>
            {/* Identity strip */}
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <span className="font-bold text-on-surface text-lg">
                  Caja #{caja.id.slice(0, 8).toUpperCase()}
                </span>
                <Badge variant="active" label="Abierta" />
              </div>
              <span className="text-on-surface-variant text-xs">
                {new Date().toLocaleDateString('es-AR', { dateStyle: 'long' })}
              </span>
            </div>

            {/* KPI grids */}
            <div className="grid grid-cols-3 gap-3">
              {kpis.map((k) => (
                <div key={k.label} className="card">
                  <p className="kpi-label">{k.label}</p>
                  <p className="kpi-value">{k.value}</p>
                </div>
              ))}
            </div>

            <div className="grid grid-cols-4 gap-3">
              {kpis2.map((k) => (
                <div key={k.label} className="card">
                  <p className="kpi-label">{k.label}</p>
                  <p className="kpi-value">{k.value}</p>
                </div>
              ))}
            </div>

            {/* Ventas del día */}
            <div className="card p-0 overflow-hidden">
              <div className="px-4 py-3 border-b border-outline-variant">
                <h3 className="text-sm font-medium text-on-surface">
                  Ventas del turno ({ventas?.length ?? 0})
                </h3>
              </div>
              {!ventas || ventas.length === 0 ? (
                <div className="p-4">
                  <EmptyState label="Sin ventas en este turno" />
                </div>
              ) : (
                <div className="overflow-x-auto">
                <table className="w-full">
                  <thead>
                    <tr className="table-header">
                      <th className="text-left px-4 py-2 text-xs">Ticket</th>
                      <th className="text-left px-4 py-2 text-xs">Hora</th>
                      <th className="text-right px-4 py-2 text-xs">Total</th>
                      <th className="text-center px-4 py-2 text-xs">Pagos</th>
                    </tr>
                  </thead>
                  <tbody>
                    {ventas.map((v) => (
                      <tr key={v.id} className="border-b border-outline-variant">
                        <td className="px-4 py-2 text-sm text-on-surface font-mono">{v.numeroTicket ?? '—'}</td>
                        <td className="px-4 py-2 text-sm text-on-surface-variant">{formatTime(v.fechaEmision)}</td>
                        <td className="px-4 py-2 text-sm text-on-surface text-right font-semibold">{formatMoney(v.total)}</td>
                        <td className="px-4 py-2 text-xs text-on-surface-variant text-center">
                          {v.pagos.map((p) => p.metodo).join(', ')}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                </div>
              )}
            </div>
          </>
        )}

        {/* Historial de cajas cerradas */}
        {historialCajas.length > 0 && (
          <div className="card p-0 overflow-hidden">
            <div className="px-4 py-3 border-b border-outline-variant">
              <h3 className="text-sm font-medium text-on-surface">Últimas cajas cerradas</h3>
            </div>
            <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="table-header">
                  <th className="text-left px-4 py-2 text-xs">Apertura</th>
                  <th className="text-left px-4 py-2 text-xs">Cierre</th>
                  <th className="text-left px-4 py-2 text-xs">Cajero</th>
                  <th className="text-right px-4 py-2 text-xs">Monto inicial</th>
                  <th className="text-right px-4 py-2 text-xs">Total ventas</th>
                  <th className="text-right px-4 py-2 text-xs">Diferencia</th>
                </tr>
              </thead>
              <tbody>
                {historialCajas.map((c) => (
                  <tr key={c.id} className="border-b border-outline-variant">
                    <td className="px-4 py-2 text-xs text-on-surface-variant">{formatDateTime(c.fechaApertura)}</td>
                    <td className="px-4 py-2 text-xs text-on-surface-variant">{c.fechaCierre ? formatDateTime(c.fechaCierre) : '—'}</td>
                    <td className="px-4 py-2 text-xs text-on-surface">{c.cajeroNombre}</td>
                    <td className="px-4 py-2 text-xs text-right font-mono">{formatMoney(c.montoInicial)}</td>
                    <td className="px-4 py-2 text-xs text-right font-mono text-primary">{formatMoney(c.montoFinalSistema ?? 0)}</td>
                    <td className={`px-4 py-2 text-xs text-right font-mono ${(c.diferencia ?? 0) < 0 ? 'text-error' : (c.diferencia ?? 0) > 0 ? 'text-blue-400' : 'text-green-400'}`}>
                      {c.diferencia !== null ? `${c.diferencia >= 0 ? '+' : ''}${formatMoney(c.diferencia)}` : '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            </div>
          </div>
        )}
      </div>

      {/* Modals */}
      {showAbrirModal && (
        <AbrirCajaModal
          onClose={() => setShowAbrirModal(false)}
          onConfirm={handleAbrir}
          isLoading={abrirMutation.isPending}
        />
      )}
      {showCerrarModal && (
        <CerrarCajaModal
          onClose={() => setShowCerrarModal(false)}
          onConfirm={handleCerrar}
          isLoading={cerrarMutation.isPending}
          montoSistema={montoSistema}
        />
      )}
    </div>
  );
}
