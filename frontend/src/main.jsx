import { StrictMode } from "react";
import { createRoot } from "react-dom/client";

import "./index.css";
import "./App.css";

// Tema de colores: va después para poder aplicarse sobre los estilos anteriores.
import "./styles/theme.css";

import App from "./App.jsx";
import { setupPwa } from "./utils/pwa";

// App instalable: service worker y botón "Instalar app".
setupPwa();

createRoot(document.getElementById("root")).render(
    <StrictMode>
        <App />
    </StrictMode>
);