import { useState } from 'react';
import { Header } from '../components/layout/Header';
import { Spinner } from '../components/ui/Spinner';
import { client } from '../api/client';
import { actualizarCotizacion } from '../api/ventas';

const rol = localStorage.getItem('mmotos_rol');

const DN_DEFRANCE = {
  cn: 'DEFRANCE MATIAS ELIAN',
  o: 'DEFRANCE MATIAS ELIAN',
  ou: 'Venta de Motocicletas y Accesorios',
  l: 'Quequen',
  st: 'Buenos Aires',
  cuit: '20449623453',
};

export function AjustesPage() {
  const [confirmar, setConfirmar] = useState(false);
  const [apagando, setApagando] = useState(false);

  const handleApagar = async () => {
    setApagando(true);
    try { await client.post('/api/admin/apagar'); } catch { /* conexión se corta — es esperado */ }
  };

  const [dn, setDn] = useState(DN_DEFRANCE);
  const [csrLoading, setCsrLoading] = useState(false);
  const [csrOk, setCsrOk] = useState('');
  const [csrError, setCsrError] = useState('');

  const [dolar, setDolar] = useState('');
  const [dolarLoading, setDolarLoading] = useState(false);
  const [dolarOk, setDolarOk] = useState('');
  const [dolarError, setDolarError] = useState('');

  const handleGuardarDolar = async (e: React.FormEvent) => {
    e.preventDefault();
    setDolarOk(''); setDolarError(''); setDolarLoading(true);
    try {
      await actualizarCotizacion(parseFloat(dolar));
      setDolarOk(`Cotización actualizada: 1 USD = $${dolar} ARS`);
      setDolar('');
    } catch {
      setDolarError('Error al actualizar la cotización.');
    } finally { setDolarLoading(false); }
  };

  const handleGenerarCsr = async (e: React.FormEvent) => {
    e.preventDefault();
    setCsrError(''); setCsrOk(''); setCsrLoading(true);
    try {
      const res = await client.post<{ csrPem: string }>('/api/fiscal/generar-csr', dn);
      const pem = res.data.csrPem;
      // Descarga automática
      const blob = new Blob([pem], { type: 'text/plain' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url; a.download = `mmotos-${dn.cuit}-arca.csr`; a.click();
      URL.revokeObjectURL(url);
      setCsrOk('CSR generado y descargado. Subilo en arca.afip.gob.ar → Administración de certificados.');
    } catch (err: any) {
      setCsrError(err.response?.data?.detail || 'Error al generar el CSR.');
    } finally { setCsrLoading(false); }
  };

  return (
    <div className="min-h-screen bg-surface-container">
      <Header title="Ajustes" />
      <div className="pt-11 p-5 space-y-5 max-w-xl">

        {/* Certificado Fiscal ARCA */}
        <div className="card space-y-4">
          <div>
            <h3 className="text-sm font-semibold text-on-surface mb-1 flex items-center gap-2">
              <span className="material-symbols-outlined text-[16px] text-primary">security</span>
              Certificado Fiscal (ARCA)
            </h3>
            <p className="text-xs text-on-surface-variant">
              Genera el CSR (solicitud de certificado) para conectarte con ARCA y emitir facturas electrónicas.
              RSA 2048 bits · SHA-256.
            </p>
          </div>

          <form onSubmit={handleGenerarCsr} className="space-y-3">
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="kpi-label block mb-1">Nombre / Razón social *</label>
                <input className="input" value={dn.cn}
                  onChange={e => setDn(d => ({ ...d, cn: e.target.value, o: e.target.value }))} required />
              </div>
              <div>
                <label className="kpi-label block mb-1">CUIT (sin guiones) *</label>
                <input className="input" value={dn.cuit}
                  onChange={e => setDn(d => ({ ...d, cuit: e.target.value }))} required />
              </div>
              <div>
                <label className="kpi-label block mb-1">Rubro</label>
                <input className="input" value={dn.ou}
                  onChange={e => setDn(d => ({ ...d, ou: e.target.value }))} />
              </div>
              <div>
                <label className="kpi-label block mb-1">Ciudad</label>
                <input className="input" value={dn.l}
                  onChange={e => setDn(d => ({ ...d, l: e.target.value }))} />
              </div>
              <div className="col-span-2">
                <label className="kpi-label block mb-1">Provincia</label>
                <input className="input" value={dn.st}
                  onChange={e => setDn(d => ({ ...d, st: e.target.value }))} />
              </div>
            </div>

            {csrError && <p className="text-error text-xs py-2 px-3 bg-error-container/20 rounded border border-error-container/40">{csrError}</p>}
            {csrOk && (
              <p className="text-green-400 text-xs py-2 px-3 bg-green-900/20 rounded border border-green-800/40">
                ✓ {csrOk}
              </p>
            )}

            <button type="submit" className="btn-primary w-full flex items-center justify-center gap-2" disabled={csrLoading}>
              {csrLoading
                ? <Spinner size="sm" />
                : <span className="material-symbols-outlined text-[16px]">download</span>}
              Generar y descargar CSR
            </button>
          </form>

          <div className="border border-outline-variant rounded p-3 text-xs text-on-surface-variant space-y-1">
            <p className="font-medium text-on-surface">Pasos siguientes:</p>
            <p>1. Descargá el archivo .csr con el botón de arriba</p>
            <p>2. Entrá a <span className="text-primary">arca.afip.gob.ar</span> con clave fiscal</p>
            <p>3. Ir a: Servicios → Administración de Certificados Digitales</p>
            <p>4. Subir el archivo .csr y descargar el certificado .crt firmado</p>
            <p>5. Guardá el .crt para configurar la conexión con ARCA</p>
          </div>
        </div>

        {/* Cotización del Dólar — solo DUENO */}
        {rol === 'DUENO' && (
          <div className="card space-y-4">
            <div>
              <h3 className="text-sm font-semibold text-on-surface mb-1 flex items-center gap-2">
                <span className="material-symbols-outlined text-[16px] text-primary">currency_exchange</span>
                Cotización del Dólar
              </h3>
              <p className="text-xs text-on-surface-variant">
                Actualizá el valor del dólar que se usa para calcular precios de productos en USD.
              </p>
            </div>
            <form onSubmit={handleGuardarDolar} className="flex items-end gap-3">
              <div className="flex-1">
                <label className="kpi-label block mb-1">Valor 1 USD en ARS</label>
                <input
                  className="input"
                  type="number"
                  min="1"
                  step="0.01"
                  value={dolar}
                  onChange={e => setDolar(e.target.value)}
                  placeholder="Ej: 1250.00"
                  required
                />
              </div>
              <button type="submit" className="btn-primary flex items-center gap-2" disabled={dolarLoading}>
                {dolarLoading ? <Spinner size="sm" /> : <span className="material-symbols-outlined text-[16px]">save</span>}
                Guardar
              </button>
            </form>
            {dolarError && <p className="text-error text-xs py-2 px-3 bg-error-container/20 rounded border border-error-container/40">{dolarError}</p>}
            {dolarOk && <p className="text-green-400 text-xs py-2 px-3 bg-green-900/20 rounded border border-green-800/40">{dolarOk}</p>}
          </div>
        )}

        {/* Apagar servidor — solo DUENO */}
        {rol === 'DUENO' && (
          <div className="card space-y-3 border border-error/30">
            <div>
              <h3 className="text-sm font-semibold text-error mb-1 flex items-center gap-2">
                <span className="material-symbols-outlined text-[16px]">power_settings_new</span>
                Apagar servidor
              </h3>
              <p className="text-xs text-on-surface-variant">
                Detiene el proceso Java. Para volver a usarla deberás reiniciar la app manualmente.
              </p>
            </div>

            {apagando ? (
              <div className="flex items-center gap-2 text-sm text-on-surface-variant">
                <Spinner size="sm" />
                Servidor apagándose...
              </div>
            ) : confirmar ? (
              <div className="flex items-center gap-3">
                <span className="text-xs text-on-surface-variant">¿Seguro que querés apagar el servidor?</span>
                <button
                  onClick={handleApagar}
                  className="px-3 py-1 text-xs font-medium bg-error text-on-error rounded hover:opacity-90"
                >
                  Sí, apagar
                </button>
                <button
                  onClick={() => setConfirmar(false)}
                  className="px-3 py-1 text-xs font-medium bg-surface-container-high text-on-surface rounded hover:opacity-90"
                >
                  Cancelar
                </button>
              </div>
            ) : (
              <button
                onClick={() => setConfirmar(true)}
                className="btn-secondary w-full flex items-center justify-center gap-2 border border-error/40 text-error hover:bg-error/10"
              >
                <span className="material-symbols-outlined text-[16px]">power_settings_new</span>
                Apagar servidor
              </button>
            )}
          </div>
        )}

      </div>
    </div>
  );
}
