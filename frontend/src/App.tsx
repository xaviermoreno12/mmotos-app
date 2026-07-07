import { Navigate, Route, Routes } from 'react-router-dom';
import { Layout } from './components/layout/Layout';
import { LoginPage } from './pages/LoginPage';
import { HomePage } from './pages/HomePage';
import { PosPage } from './pages/PosPage';
import { ProductosPage } from './pages/ProductosPage';
import { CajaPage } from './pages/CajaPage';
import { VentasPage } from './pages/VentasPage';
import { ReportesPage } from './pages/ReportesPage';
import { MetodosPage } from './pages/MetodosPage';
import { CajerosPage } from './pages/CajerosPage';
import { AjustesPage } from './pages/AjustesPage';
import { ListasPage } from './pages/ListasPage';
import { PreciosPage } from './pages/PreciosPage';
import { PresupuestosPage } from './pages/PresupuestosPage';
import { ClientesPage } from './pages/ClientesPage';
import { ProveedoresPage } from './pages/ProveedoresPage';
import { ComprasPage } from './pages/ComprasPage';
import { GastosPage } from './pages/GastosPage';
import { ChequesPage } from './pages/ChequesPage';
import { CobranzasPage } from './pages/CobranzasPage';
import { EtiquetasPage } from './pages/EtiquetasPage';
import { RemitosPendientesPage } from './pages/RemitosPendientesPage';
import { FacturasIAPage } from './pages/FacturasIAPage';

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const token = localStorage.getItem('mmotos_token');
  if (!token) return <Navigate to="/login" replace />;
  return <>{children}</>;
}

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/home" replace />} />
        <Route path="home"         element={<HomePage />} />
        <Route path="pos"          element={<PosPage />} />
        <Route path="caja"         element={<CajaPage />} />
        <Route path="productos"    element={<ProductosPage />} />
        <Route path="ventas"       element={<VentasPage />} />
        <Route path="reportes"     element={<ReportesPage />} />
        <Route path="metodos"      element={<MetodosPage />} />
        <Route path="cajeros"      element={<CajerosPage />} />
        <Route path="ajustes"      element={<AjustesPage />} />
        <Route path="listas"       element={<ListasPage />} />
        <Route path="precios"      element={<PreciosPage />} />
        <Route path="presupuestos" element={<PresupuestosPage />} />
        <Route path="gastos"       element={<GastosPage />} />
        <Route path="cheques"      element={<ChequesPage />} />
        <Route path="cobranzas"    element={<CobranzasPage />} />
        <Route path="etiquetas"    element={<EtiquetasPage />} />
        <Route path="clientes"     element={<ClientesPage />} />
        <Route path="proveedores"  element={<ProveedoresPage />} />
        <Route path="compras"            element={<ComprasPage />} />
        <Route path="remitos-pendientes" element={<RemitosPendientesPage />} />
        <Route path="facturas-ia"        element={<FacturasIAPage />} />
        <Route path="*"                  element={<Navigate to="/home" replace />} />
      </Route>
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}
