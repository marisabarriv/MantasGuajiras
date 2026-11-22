import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import NewSalePage from "./pages/NewSalePage";
import ProductsPage from "./pages/ProductsPage";
import MainLayout from "./components/MainLayout";
import AdminPage from "./pages/AdminPage";
import InventoryPage from "./pages/InventoryPage";
import SalesPage from "./pages/SalesPage";
import PurchasesPage from "./pages/PurchasesPage";
import ProductionPage from "./pages/ProductionPage";
import ProfilePage from "./pages/ProfilePage";
import CustomOrdersPage from "./pages/CustomOrdersPage";
import RequireAdmin from "./components/RequireAdmin";
import RequireAuth from "./components/RequireAuth";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Públicas */}
        <Route path="/" element={<LoginPage />} />

        <Route path="/register" element={<RegisterPage />} />

        {/* Privadas: requieren sesión válida */}
        <Route
          element={
            <RequireAuth>
              <MainLayout />
            </RequireAuth>
          }
        >
          <Route path="/sale" element={<NewSalePage />} />

          {/* Para todos los roles, valores incluidos (decisión del negocio) */}
          <Route path="/custom-orders" element={<CustomOrdersPage />} />

          <Route path="/products" element={<ProductsPage />} />

          <Route path="/inventory" element={<InventoryPage />} />

          <Route path="/production" element={<ProductionPage />} />

          <Route path="/profile" element={<ProfilePage />} />

          {/* Solo ADMIN: montos de ventas y compras, y usuarios */}
          <Route
            path="/sales"
            element={
              <RequireAdmin>
                <SalesPage />
              </RequireAdmin>
            }
          />

          <Route
            path="/purchases"
            element={
              <RequireAdmin>
                <PurchasesPage />
              </RequireAdmin>
            }
          />

          <Route
            path="/admin"
            element={
              <RequireAdmin>
                <AdminPage />
              </RequireAdmin>
            }
          />
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
