import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { StatusBar } from './StatusBar';

export function Layout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className="min-h-screen bg-surface">

      {/* Overlay oscuro en mobile cuando el sidebar está abierto */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-black/50 z-30 md:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      <div className="print:hidden">
        <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      </div>

      {/* Botón hamburguesa — solo visible en mobile */}
      <button
        onClick={() => setSidebarOpen(true)}
        className="fixed top-2 left-2 z-50 md:hidden flex items-center justify-center w-9 h-9 rounded-lg bg-surface-container-low border border-outline-variant shadow-sm text-on-surface hover:bg-surface-container transition-colors"
        aria-label="Abrir menú"
      >
        <span className="material-symbols-outlined text-[20px]">menu</span>
      </button>

      {/* Contenido principal: sin margen en mobile (sidebar es overlay), con margen en desktop */}
      <div className="md:ml-[220px] mb-7 min-h-screen print:ml-0 print:mb-0">
        <Outlet />
      </div>

      <div className="print:hidden"><StatusBar /></div>
    </div>
  );
}
