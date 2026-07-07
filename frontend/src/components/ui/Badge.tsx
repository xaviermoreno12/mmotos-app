interface BadgeProps {
  variant: 'active' | 'error' | 'pending' | 'synced' | 'neutral';
  label: string;
}

const styles: Record<BadgeProps['variant'], string> = {
  active:  'bg-green-900 text-green-300 border border-green-700',
  error:   'bg-error-container text-on-error-container border border-red-800',
  pending: 'bg-yellow-900 text-yellow-300 border border-yellow-700',
  synced:  'bg-tertiary-container/20 text-tertiary border border-tertiary-container/40',
  neutral: 'bg-secondary-container text-on-secondary-container border border-outline-variant',
};

export function Badge({ variant, label }: BadgeProps) {
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium uppercase tracking-wider ${styles[variant]}`}>
      {label}
    </span>
  );
}
