import { StrictMode } from "react";
import { createRoot } from "react-dom/client";

import "./index.css";
import "./App.css";

// Tema de colores: va después para poder aplicarse sobre los estilos anteriores.
import "./styles/theme.css";

import App from "./App.jsx";
import { setupPwa } from "./utils/pwa";
import { startOfflineSync } from "./utils/offlineSync";

// App instalable: service worker y botón "Instalar app".
setupPwa();

// Envía solas las ventas hechas sin conexión cuando vuelve la conexión.
startOfflineSync();

createRoot(document.getElementById("root")).render(
    <StrictMode>
        <App />
    </StrictMode>
);