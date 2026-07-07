import { Outlet } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { StatusBar } from './StatusBar';

export function Layout() {
  return (
    <div className="min-h-screen bg-surface">
      <div className="print:hidden"><Sidebar /></div>
      <div className="ml-[220px] mb-7 min-h-screen print:ml-0 print:mb-0">
        <Outlet />
      </div>
      <div className="print:hidden"><StatusBar /></div>
    </div>
  );
}
