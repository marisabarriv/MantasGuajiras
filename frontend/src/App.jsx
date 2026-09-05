import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import NewSalePage from "./pages/NewSalePage";
import ProductsPage from "./pages/ProductsPage";
import MainLayout from "./components/MainLayout";
import AdminPage from "./pages/AdminPage";
import InventoryPage from "./pages/InventoryPage";
import SalesPage from "./pages/SalesPage";
import ProductionPage from "./pages/ProductionPage";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LoginPage />} />

        <Route path="/register" element={<RegisterPage />} />

        <Route element={<MainLayout />}>
          <Route path="/sale" element={<NewSalePage />} />

          <Route path="/products" element={<ProductsPage />} />

          <Route path="/inventory" element={<InventoryPage />} />

          <Route path="/sales" element={<SalesPage />} />
          <Route path="/production" element={<ProductionPage />} />
          <Route path="/admin" element={<AdminPage />} />
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
