interface KpiCardProps {
  label: string;
  value: string | number;
  accent?: boolean;
}

export function KpiCard({ label, value, accent = false }: KpiCardProps) {
  return (
    <div className="card">
      <p className="kpi-label">{label}</p>
      <p className={`kpi-value ${accent ? 'text-primary-container' : ''}`}>{value}</p>
    </div>
  );
}
