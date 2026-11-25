# Política de seguridad

## Versiones con soporte

Solo recibe correcciones de seguridad la versión más reciente de `main` (hoy, `1.0.0-rc.1`).

## Reportar una vulnerabilidad

**No abras un issue público ni publiques los detalles.** Usa el reporte privado de GitHub:

1. Entra a la pestaña **Security** de este repositorio.
2. Pulsa **Report a vulnerability**.
3. Explica qué encontraste, cómo reproducirlo y qué podría pasar. No incluyas datos reales de clientes ni contraseñas.

Se responde lo antes posible. Si el problema se confirma, se corrige, se publica la versión corregida y, si lo deseas, se reconoce tu aporte.

No pruebes contra la instalación del negocio ni con cuentas o datos ajenos: instala una copia local (`README.md`).

## Claves y datos

- Las claves van en `backend/.env`, `application-dev.yml` o `deploy/.env`, que git ignora. En el repositorio solo hay plantillas sin valores reales.
- Las claves de las pruebas automáticas y de la CI son ficticias y no sirven en ninguna instalación.
- Cada instalación debe usar claves nuevas (`docs/13-produccion.md`). Si una clave llega a publicarse, se cambia: borrarla del repositorio no basta.

## Controles activos

| Control | Qué hace |
| ------- | -------- |
| Regla de `main` | Todo entra por pull request, con la rama actualizada y las 5 comprobaciones de la CI en verde; sin push forzado ni borrado. |
| OSV-Scanner y Dependabot | Vulnerabilidades conocidas en las dependencias. |
| CodeQL | Análisis del código en cada pull request y cada semana. |
| Secret scanning y push protection | Detecta claves conocidas y bloquea la subida que las contenga. |
| GitHub Actions | Permisos de solo lectura; acciones fijadas a un commit exacto. |

La seguridad de la aplicación está en `docs/06-despliegue.md` §14.4A y `docs/10-certificacion.md` §9.
