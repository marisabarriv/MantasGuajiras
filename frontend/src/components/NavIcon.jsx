/**
 * Íconos de línea de la barra inferior (celular). Dibujados a mano en una
 * cuadrícula de 24 px; toman el color del texto (currentColor).
 */
const PATHS = {
    sale: (
        <>
            <circle cx="9" cy="20" r="1.4" />
            <circle cx="18" cy="20" r="1.4" />
            <path d="M2.5 3.5h3l2.4 11.2a1.5 1.5 0 0 0 1.5 1.2h8.3a1.5 1.5 0 0 0 1.5-1.1L21 8H6.4" />
        </>
    ),
    custom: (
        <>
            <path d="M12 3l2.2 4.6 5 .7-3.6 3.5.9 5L12 14.4 7.5 16.8l.9-5L4.8 8.3l5-.7z" />
            <path d="M5 21h14" />
        </>
    ),
    inventory: (
        <>
            <path d="M3.5 7.5 12 3l8.5 4.5v9L12 21l-8.5-4.5z" />
            <path d="M3.5 7.5 12 12l8.5-4.5M12 12v9" />
        </>
    ),
    production: (
        <>
            <circle cx="6.5" cy="6.5" r="2.5" />
            <circle cx="6.5" cy="17.5" r="2.5" />
            <path d="M8.6 8 20 19M8.6 16 20 5" />
        </>
    ),
    sales: (
        <>
            <path d="M4 20V10M10 20V4M16 20v-7M21 20H3" />
        </>
    ),
    purchases: (
        <>
            <path d="M5 8h14l-1.2 12H6.2z" />
            <path d="M9 10V6.5a3 3 0 0 1 6 0V10" />
        </>
    ),
    products: (
        <>
            <path d="M3 12.5V4a1 1 0 0 1 1-1h8.5L21 11.5 12.5 20z" />
            <circle cx="8" cy="8" r="1.6" />
        </>
    ),
    users: (
        <>
            <circle cx="9" cy="8" r="3.5" />
            <path d="M2.5 20a6.5 6.5 0 0 1 13 0" />
            <path d="M16 4.6a3.5 3.5 0 0 1 0 6.8M18 14.2a6.5 6.5 0 0 1 3.5 5.8" />
        </>
    ),
    profile: (
        <>
            <circle cx="12" cy="8" r="4" />
            <path d="M4 21a8 8 0 0 1 16 0" />
        </>
    ),
    more: (
        <>
            <rect x="3.5" y="3.5" width="7" height="7" rx="1.5" />
            <rect x="13.5" y="3.5" width="7" height="7" rx="1.5" />
            <rect x="3.5" y="13.5" width="7" height="7" rx="1.5" />
            <rect x="13.5" y="13.5" width="7" height="7" rx="1.5" />
        </>
    ),
    logout: (
        <>
            <path d="M14 4h4a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2h-4" />
            <path d="M10 8l-4 4 4 4M6 12h10" />
        </>
    ),
};

function NavIcon({ name, size = 26 }) {
    return (
        <svg
            width={size}
            height={size}
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
            aria-hidden="true"
            focusable="false"
        >
            {PATHS[name]}
        </svg>
    );
}

export default NavIcon;
