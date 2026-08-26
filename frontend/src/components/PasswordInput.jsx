import { useState } from "react";

function PasswordIcon({ visible }) {
    if (visible) {
        return (
            <svg
                width="22"
                height="22"
                viewBox="0 0 24 24"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
                aria-hidden="true"
            >
                <path
                    d="M3.5 12C5.4 8.7 8.4 7 12 7C15.6 7 18.6 8.7 20.5 12C18.6 15.3 15.6 17 12 17C8.4 17 5.4 15.3 3.5 12Z"
                    stroke="currentColor"
                    strokeWidth="1.8"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                />

                <circle
                    cx="12"
                    cy="12"
                    r="2.5"
                    stroke="currentColor"
                    strokeWidth="1.8"
                />
            </svg>
        );
    }

    return (
        <svg
            width="22"
            height="22"
            viewBox="0 0 24 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
            aria-hidden="true"
        >
            <path
                d="M3.5 12C5.4 8.7 8.4 7 12 7C15.6 7 18.6 8.7 20.5 12C18.6 15.3 15.6 17 12 17C8.4 17 5.4 15.3 3.5 12Z"
                stroke="currentColor"
                strokeWidth="1.8"
                strokeLinecap="round"
                strokeLinejoin="round"
            />

            <path
                d="M4 4L20 20"
                stroke="currentColor"
                strokeWidth="1.8"
                strokeLinecap="round"
            />
        </svg>
    );
}

export default function PasswordInput({
    id,
    value,
    onChange,
    placeholder = "Contraseña",
    name = "password",
    autoComplete = "current-password",
}) {
    const [showPassword, setShowPassword] = useState(false);

    const handlePointerEnter = (event) => {
        if (event.pointerType === "mouse") {
            setShowPassword(true);
        }
    };

    const handlePointerLeave = (event) => {
        if (event.pointerType === "mouse") {
            setShowPassword(false);
        }
    };

    const handlePointerDown = (event) => {
        if (event.pointerType !== "mouse") {
            setShowPassword(true);
        }
    };

    const handlePointerUp = (event) => {
        if (event.pointerType !== "mouse") {
            setShowPassword(false);
        }
    };

    const handlePointerCancel = (event) => {
        if (event.pointerType !== "mouse") {
            setShowPassword(false);
        }
    };

    return (
        <div className="password-input-container">

            <input
                id={id}
                type={showPassword ? "text" : "password"}
                name={name}
                className="auth-input"
                value={value}
                onChange={onChange}
                placeholder={placeholder}
                autoComplete={autoComplete}
                required
            />

            <button
                type="button"
                className="password-eye"
                aria-label={
                    showPassword
                        ? "Ocultar contraseña"
                        : "Mostrar contraseña"
                }
                onPointerEnter={handlePointerEnter}
                onPointerLeave={handlePointerLeave}
                onPointerDown={handlePointerDown}
                onPointerUp={handlePointerUp}
                onPointerCancel={handlePointerCancel}
            >
                <PasswordIcon visible={showPassword} />
            </button>

        </div>
    );
}