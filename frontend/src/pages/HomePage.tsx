import { useNavigate } from 'react-router-dom';
import { Header } from '../components/layout/Header';

const modules = [
  { label: 'Lector',       icon: 'barcode_reader',        path: '/pos',          soloAdmin: false },
  { label: 'Presupuestos', icon: 'request_quote',         path: '/presupuestos', soloAdmin: false },
  { label: 'Caja',         icon: 'point_of_sale',         path: '/caja',         soloAdmin: false },
  { label: 'Clientes',     icon: 'group',                 path: '/clientes',     soloAdmin: false },
  { label: 'Cobranzas',    icon: 'account_balance_wallet',path: '/cobranzas',    soloAdmin: false },
  { label: 'Productos',    icon: 'inventory_2',           path: '/productos',    soloAdmin: false },
  { label: 'Listas',       icon: 'format_list_bulleted',  path: '/listas',       soloAdmin: false },
  { label: 'Etiquetas',    icon: 'label',                 path: '/etiquetas',    soloAdmin: false },
  { label: 'Precios',      icon: 'sell',                  path: '/precios',      soloAdmin: true  },
  { label: 'Ventas',       icon: 'receipt_long',          path: '/ventas',       soloAdmin: true  },
  { label: 'Compras',      icon: 'shopping_cart',         path: '/compras',      soloAdmin: true  },
  { label: 'Gastos',       icon: 'payments',              path: '/gastos',       soloAdmin: true  },
  { label: 'Cheques',      icon: 'draft',                 path: '/cheques',      soloAdmin: true  },
  { label: 'Reportes',     icon: 'bar_chart',             path: '/reportes',     soloAdmin: true  },
  { label: 'Proveedores',  icon: 'business',              path: '/proveedores',  soloAdmin: true  },
  { label: 'Métodos',      icon: 'credit_card',           path: '/metodos',      soloAdmin: false },
  { label: 'Cajeros',      icon: 'badge',                 path: '/cajeros',      soloAdmin: true  },
  { label: 'Ajustes',      icon: 'settings',              path: '/ajustes',      soloAdmin: true  },
];

export function HomePage() {
  const navigate = useNavigate();
  const rol = localStorage.getItem('mmotos_rol');
  const esDueno = rol === 'DUENO';
  const visibles = modules.filter(m => !m.soloAdmin || esDueno);

  return (
    <div className="min-h-screen bg-surface-container">
      <Header title="Inicio" />
      <div className="pt-11 p-8">
        <h2 className="text-on-surface-variant text-xs uppercase tracking-widest mb-8">
          ¿Qué querés hacer?
        </h2>
        <div className="grid grid-cols-4 gap-4">
          {visibles.map((m) => (
            <button
              key={m.path}
              onClick={() => navigate(m.path)}
              className="card flex flex-col items-center gap-3 py-8 hover:bg-surface-container-high hover:border-primary-container/40 transition-all group cursor-pointer"
            >
              <span className="material-symbols-outlined text-[32px] text-on-surface-variant group-hover:text-primary transition-colors">
                {m.icon}
              </span>
              <span className="text-xs uppercase tracking-widest text-on-surface-variant group-hover:text-on-surface font-medium transition-colors">
                {m.label}
              </span>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
