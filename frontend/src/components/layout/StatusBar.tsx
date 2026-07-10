import { useQuery } from '@tanstack/react-query';
import { client } from '../../api/client';

interface EstadoFiscal { modo: string; }

const modoConfig: Record<string, { label: string; dotClass: string; textClass: string }> = {
  NO_FISCAL: { label: 'Sin fiscalizar', dotClass: 'bg-outline-variant', textClass: 'text-on-surface-variant' },
  AFIP:      { label: 'AFIP activo',   dotClass: 'bg-green-500',        textClass: 'text-green-400' },
  HASAR:     { label: 'HASAR activo',  dotClass: 'bg-orange-500',       textClass: 'text-orange-400' },
};

export function StatusBar() {
  const { data } = useQuery<EstadoFiscal>({
    queryKey: ['fiscal-estado'],
    queryFn: () => client.get<EstadoFiscal>('/api/fiscal/estado').then(r => r.data),
    staleTime: 60_000,
  });

  const modo = data?.modo ?? 'NO_FISCAL';
  const cfg = modoConfig[modo] ?? modoConfig.NO_FISCAL;

  return (
    <footer className="fixed bottom-0 left-[220px] right-0 h-7 bg-surface-container border-t border-outline-variant z-30 flex items-center justify-between px-5">
      <span className="text-on-surface-variant text-xs">v0.1.0</span>
      <div className="flex items-center gap-4 text-xs">
        <span className={`flex items-center gap-1 ${cfg.textClass}`}>
          <span className={`w-1.5 h-1.5 rounded-full inline-block ${cfg.dotClass}`} />
          {cfg.label}
        </span>
        <span className="text-on-surface-variant">MODO: {modo.replace('_', ' ')}</span>
      </div>
    </footer>
  );
}
