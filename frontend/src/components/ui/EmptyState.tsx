interface EmptyStateProps {
  label: string;
}

export function EmptyState({ label }: EmptyStateProps) {
  return (
    <div className="empty-state">
      <span>{label}</span>
    </div>
  );
}
