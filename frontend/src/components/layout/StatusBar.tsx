export function StatusBar() {
  return (
    <footer className="fixed bottom-0 left-[220px] right-0 h-7 bg-surface-container border-t border-outline-variant z-30 flex items-center justify-between px-5">
      <span className="text-on-surface-variant text-xs">v0.1.0</span>
      <div className="flex items-center gap-4 text-xs text-on-surface-variant">
        <span className="flex items-center gap-1">
          <span className="w-1.5 h-1.5 rounded-full bg-green-500 inline-block" />
          CONEXIÓN FISCAL ACTIVA
        </span>
        <span>MODO: NO FISCAL</span>
      </div>
    </footer>
  );
}
