import { useEffect, useId } from "react";
import { setUnsaved } from "../utils/unsavedChanges";

/**
 * Marca este formulario como "con datos sin guardar" mientras dirty sea
 * true. Al guardarse o cerrarse (el componente se desmonta) se limpia solo.
 */
export default function useUnsavedChanges(dirty) {
    const key = useId();

    useEffect(() => {
        setUnsaved(key, Boolean(dirty));
        return () => setUnsaved(key, false);
    }, [key, dirty]);
}
