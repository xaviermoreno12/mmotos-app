import { useAuth } from '../../hooks/useAuth';

interface HeaderProps {
  title: string;
  action?: React.ReactNode;
}

export function Header({ title, action }: HeaderProps) {
  const { logout } = useAuth();

  return (
    <header className="fixed top-0 left-[220px] right-0 h-11 bg-surface-container-high border-b border-outline-variant z-30 flex items-center justify-between px-5">
      <h1 className="text-sm font-semibold text-on-surface tracking-wide">{title}</h1>
      <div className="flex items-center gap-3">
        {action}
        <button
          onClick={logout}
          className="material-symbols-outlined text-[20px] text-on-surface-variant hover:text-on-surface transition-colors"
          title="Cerrar sesión"
        >
          logout
        </button>
      </div>
    </header>
  );
}
