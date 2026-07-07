import { Header } from '../components/layout/Header';

interface Props {
  title: string;
}

export function PlaceholderPage({ title }: Props) {
  return (
    <div className="min-h-screen bg-surface-container">
      <Header title={title} />
      <div className="pt-11 flex items-center justify-center h-[calc(100vh-11rem)]">
        <div className="text-center">
          <span className="material-symbols-outlined text-[48px] text-outline mb-4 block">
            construction
          </span>
          <p className="text-on-surface-variant text-sm">Próximamente</p>
        </div>
      </div>
    </div>
  );
}
