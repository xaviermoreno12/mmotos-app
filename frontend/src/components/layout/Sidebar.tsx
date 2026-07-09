import { NavLink } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { getBorradores } from '../../api/compras';
import { getFacturasBorradores } from '../../api/facturasIA';

const navItems = [
  { label: 'Inicio',            icon: 'home',                  path: '/home',               soloAdmin: false },
  { label: 'Lector',            icon: 'barcode_reader',        path: '/pos',                soloAdmin: false },
  { label: 'Presupuestos',      icon: 'request_quote',         path: '/presupuestos',       soloAdmin: false },
  { label: 'Caja',              icon: 'point_of_sale',         path: '/caja',               soloAdmin: false },
  { label: 'Clientes',          icon: 'group',                 path: '/clientes',           soloAdmin: false },
  { label: 'Cobranzas',         icon: 'account_balance_wallet',path: '/cobranzas',          soloAdmin: false },
  { label: 'Productos',         icon: 'inventory_2',           path: '/productos',          soloAdmin: false },
  { label: 'Listas',            icon: 'format_list_bulleted',  path: '/listas',             soloAdmin: false },
  { label: 'Etiquetas',         icon: 'label',                 path: '/etiquetas',          soloAdmin: false },
  { label: 'Precios',           icon: 'sell',                  path: '/precios',            soloAdmin: true  },
  { label: 'Ventas',            icon: 'receipt_long',          path: '/ventas',             soloAdmin: true  },
  { label: 'Compras',           icon: 'shopping_cart',         path: '/compras',            soloAdmin: true  },
  { label: 'Remitos IA',        icon: 'document_scanner',      path: '/remitos-pendientes', soloAdmin: true  },
  { label: 'Facturas IA',       icon: 'fact_check',            path: '/facturas-ia',        soloAdmin: true  },
  { label: 'Gastos',            icon: 'payments',              path: '/gastos',             soloAdmin: true  },
  { label: 'Cheques',           icon: 'draft',                 path: '/cheques',            soloAdmin: true  },
  { label: 'Reportes',          icon: 'bar_chart',             path: '/reportes',           soloAdmin: true  },
  { label: 'Proveedores',       icon: 'business',              path: '/proveedores',        soloAdmin: true  },
  { label: 'Métodos',           icon: 'credit_card',           path: '/metodos',            soloAdmin: false },
  { label: 'Cajeros',           icon: 'badge',                 path: '/cajeros',            soloAdmin: true  },
  { label: 'Ajustes',           icon: 'settings',              path: '/ajustes',            soloAdmin: true  },
];

interface SidebarProps {
  open: boolean;
  onClose: () => void;
}

export function Sidebar({ open, onClose }: SidebarProps) {
  const rol = localStorage.getItem('mmotos_rol');
  const esDueno = rol === 'DUENO';
  const items = navItems.filter(i => !i.soloAdmin || esDueno);

  const { data: borradores = [] } = useQuery({
    queryKey: ['borradores'],
    queryFn: getBorradores,
    refetchInterval: 5000,
    enabled: esDueno,
  });
  const pendientes = borradores.length;

  const { data: facturasBorradores = [] } = useQuery({
    queryKey: ['facturas-borradores'],
    queryFn: getFacturasBorradores,
    refetchInterval: 5000,
    enabled: esDueno,
  });
  const facturasPendientes = facturasBorradores.length;

  return (
    <aside className={`fixed top-0 left-0 bottom-0 w-[220px] bg-surface-container-low z-40 flex flex-col border-r border-outline-variant transition-transform duration-200 ease-in-out ${
      open ? 'translate-x-0' : '-translate-x-full md:translate-x-0'
    }`}>
      <div className="px-5 py-5 border-b border-outline-variant flex items-center justify-between">
        <span className="text-primary font-black text-sm tracking-widest uppercase leading-tight">
          M MOTOS<br />CORE
        </span>
        <button
          onClick={onClose}
          className="md:hidden p-1 rounded text-on-surface-variant hover:text-on-surface hover:bg-surface-container transition-colors"
          aria-label="Cerrar menú"
        >
          <span className="material-symbols-outlined text-[20px]">close</span>
        </button>
      </div>

      <nav className="flex-1 overflow-y-auto py-2">
        {items.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            onClick={onClose}
            className={({ isActive }) =>
              isActive ? 'nav-item-active' : 'nav-item'
            }
          >
            <span className="material-symbols-outlined text-[18px]">{item.icon}</span>
            <span className="flex-1">{item.label}</span>
            {item.path === '/remitos-pendientes' && pendientes > 0 && (
              <span className="ml-auto bg-error text-on-error text-[10px] font-bold rounded-full w-4 h-4 flex items-center justify-center">
                {pendientes}
              </span>
            )}
            {item.path === '/facturas-ia' && facturasPendientes > 0 && (
              <span className="ml-auto bg-error text-on-error text-[10px] font-bold rounded-full w-4 h-4 flex items-center justify-center">
                {facturasPendientes}
              </span>
            )}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
