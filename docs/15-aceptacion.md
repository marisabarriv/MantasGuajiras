# Aceptación del negocio

Fase 8 de la certificación (`docs/10-certificacion.md`, sección 20). Esta es **la parte que solo puede hacer la responsable del negocio**: confirmar que el sistema hace lo que la tienda necesita, con sus precios, sus productos y sus procedimientos reales.

---

## 1. Cómo se preparó

Los 24 escenarios del checklist de la auditoría ya se **ensayaron automáticamente** en un navegador real, por la interfaz y como lo haría una persona (`frontend/e2e/acceptance.e2e.cjs`):

- **24/24 correctos, 69 comprobaciones.** El vendedor se probó en tamaño de celular y el administrador en computador.
- Corre en cada cambio en la CI, también a través del kit de producción con HTTPS.
- El ensayo dejó las capturas que ilustran el manual (`docs/14-manual-de-usuario.md`).

**El ensayo encontró y corrigió, antes de esta aceptación:**

- **Faltaba el aviso al salir con datos sin guardar.** Un vendedor podía perder el carrito o un formulario a medio llenar al tocar otra opción del menú. Ahora pregunta antes de salir.
- Confirmó reglas que conviene conocer:
  - el usuario admite **hasta 12 caracteres**;
  - **"Guardar ajuste" se activa solo cuando hay motivo**;
  - las mantas solo admiten **cantidades enteras**.

El ensayo **no reemplaza** esta aceptación. Un programa puede comprobar que el sistema hace lo que se le dijo; solo tú puedes confirmar que eso es **lo que la tienda necesita**.

## 2. Antes de empezar

- **Dónde:** idealmente en la instalación definitiva (`docs/13`), **antes** de cargar los datos reales. También sirve una copia de tus datos en una base de prueba. Nunca hagas pruebas que cambien inventario en la base con la que vendes.
- **Quién:** tú como administradora y, si puedes, una vendedora real con su celular.
- **Con qué:** tus productos y precios reales (al menos una manta, una tela y una manta de pocas unidades), una factura de compra real y una producción típica.
- **Guía:** el manual (`docs/14-manual-de-usuario.md`) explica cada paso con imágenes.
- **Tiempo estimado:** 1 a 2 horas.

## 3. Los 24 escenarios

Marca **Resultado**: ✔ si se comporta como esperas, ✘ si no (y anota qué pasó en *Observaciones*). La columna *Ensayo* indica que la prueba automática ya lo comprobó el 24/11/2026.

### Productos

| # | Escenario | Cómo (manual) | Qué debe pasar | Ensayo | Resultado | Observaciones |
| - | --------- | ------------- | -------------- | :----: | :-------: | ------------- |
| 1 | Crear manta y tela | §8 | Aparecen en Productos e Inventario con sus existencias; la tela, en metros | ✔ | ☐ | |
| 2 | Editar productos | §8 | El precio nuevo se ve en Productos y en Nueva venta; las ventas anteriores conservan su precio | ✔ | ☐ | |
| 3 | Validar unidades y precios | §8 | No deja precio 0 ni media manta; la tela acepta decimales | ✔ | ☐ | |
| 4 | Desactivar productos | §8 | Aparece en "Inactivos" y ya no se puede vender | ✔ | ☐ | |

### Inventario

| # | Escenario | Cómo | Qué debe pasar | Ensayo | Resultado | Observaciones |
| - | --------- | ---- | -------------- | :----: | :-------: | ------------- |
| 5 | Ajuste por conteo físico | §5 | Explica la diferencia antes de guardar; exige motivo; las existencias quedan en lo contado | ✔ | ☐ | |
| 6 | Verificar entradas y salidas | §5 | El historial muestra cada movimiento con signo, origen, fecha y quién | ✔ | ☐ | |
| 7 | Validar stock insuficiente | §6 | Una producción sin tela suficiente se rechaza y nada cambia | ✔ | ☐ | |
| 8 | Historial de movimientos | §5 | Los filtros (origen, usuario, fechas) encuentran lo buscado | ✔ | ☐ | |

### Ventas

| # | Escenario | Cómo | Qué debe pasar | Ensayo | Resultado | Observaciones |
| - | --------- | ---- | -------------- | :----: | :-------: | ------------- |
| 9 | Venta normal | §2 | "La venta se guardó correctamente."; las existencias bajan | ✔ | ☐ | |
| 10 | Precio especial a una unidad | §2 | $80.000 → $70.000: muestra 12,5 % en el carrito, la confirmación y el historial; el precio del producto no cambia | ✔ | ☐ | |
| 11 | Descuento a un producto | §2 | 3 metros de tela a $16.000 en vez de $18.000 = $48.000 | ✔ | ☐ | |
| 12 | Venta con stock insuficiente | §2 | "No hay suficiente inventario…"; no queda negativo; el carrito se conserva | ✔ | ☐ | |
| 13 | Historial y totales | §9 | "Vendido hoy" coincide con la suma real; el detalle muestra precio normal, descuento y vendedor | ✔ | ☐ | |

### Compras y producción

| # | Escenario | Cómo | Qué debe pasar | Ensayo | Resultado | Observaciones |
| - | --------- | ---- | -------------- | :----: | :-------: | ------------- |
| 14 | Registrar compra | §10 | "Compra registrada por $…"; queda en el historial con su total | ✔ | ☐ | |
| 15 | Entrada de inventario | §5 | Las existencias suben lo comprado; "Entrada por compra" en el historial | ✔ | ☐ | |
| 16 | Registrar producción | §6 | Queda con quién la fabricó y las observaciones | ✔ | ☐ | |
| 17 | Consumo y disponibilidad | §6 | La tela baja (mantas × metros) y las mantas suben, en un solo paso | ✔ | ☐ | |

### Usuarios y seguridad

| # | Escenario | Cómo | Qué debe pasar | Ensayo | Resultado | Observaciones |
| - | --------- | ---- | -------------- | :----: | :-------: | ------------- |
| 18 | Crear vendedor | §11 | "Usuario … creado."; puede iniciar sesión | ✔ | ☐ | |
| 19 | Restricciones de vendedor | §1 | No ve Ventas, Compras ni Usuarios ("Sección restringida"); en Productos solo consulta | ✔ | ☐ | |
| 20 | Permisos de administrador | §1 | Ve y usa todo | ✔ | ☐ | |
| 21 | Fotografía de perfil | §7 | Sube, cambia y quita la foto; rechaza un archivo que no es imagen | ✔ | ☐ | |

### Sin conexión

| # | Escenario | Cómo | Qué debe pasar | Ensayo | Resultado | Observaciones |
| - | --------- | ---- | -------------- | :----: | :-------: | ------------- |
| 22 | Venta sin conexión | §3 | Con el celular en modo avión: "quedó guardada en este dispositivo" y aparece en la lista de pendientes | ✔ | ☐ | |
| 23 | Recuperar conexión y sincronizar | §3 | Al volver el internet se envía sola; en Ventas aparece "Hecha sin conexión" con su hora real | ✔ | ☐ | |
| 24 | No se duplica la venta | §3 | Aparece una sola vez, aunque el internet se corte varias veces | ✔ | ☐ | |

**Prueba real recomendada para el 22–24:** con el celular de una vendedora, activa el **modo avión**, vende, cierra y vuelve a abrir la app (debe abrir igual), desactiva el modo avión y confirma en *Ventas*.

## 4. Criterios del negocio (NEG)

Respóndelos después del recorrido. Son los que ninguna prueba automática puede responder.

| ID | Pregunta | Sí / No | Comentario |
| -- | -------- | :-----: | ---------- |
| NEG-01 | ¿Las vendedoras pueden hacer su trabajo sin pedir ayuda a cada rato? | ☐ | |
| NEG-02 | ¿Los pasos coinciden con cómo se trabaja en la tienda? | ☐ | |
| NEG-03 | ¿Los precios y descuentos son los que la tienda decide? | ☐ | |
| NEG-04 | ¿El inventario del sistema coincide con lo que hay en el local, después de un conteo? | ☐ | |
| NEG-05 | ¿Las ventas del día coinciden con el dinero recibido? | ☐ | |
| NEG-06 | ¿Las compras suman bien al inventario? | ☐ | |
| NEG-07 | ¿La producción refleja la tela que de verdad se usa? | ☐ | |
| NEG-08 | ¿Los saldos de las personalizadas son los correctos? | ☐ | |
| NEG-09 | ¿Lo que puede hacer cada rol corresponde a las responsabilidades reales? | ☐ | |
| NEG-10 | ¿Se entienden los mensajes del sistema? | ☐ | |
| NEG-11 | ¿Se puede saber quién hizo cada operación importante? | ☐ | |
| NEG-12 | ¿Los resultados se pueden contrastar con facturas o cuadernos? | ☐ | |
| NEG-13 | ¿Revisaste los flujos principales tú misma? | ☐ | |
| NEG-14 | ¿Queda alguna diferencia conocida entre cómo trabaja la tienda y el sistema? (Debe ser **No**, o quedar anotada abajo con su decisión) | ☐ | |

## 5. Diferencias encontradas

| # | Escenario o criterio | Qué pasó | Decisión (corregir / aceptar como está) | Fecha de cierre |
| - | -------------------- | -------- | --------------------------------------- | --------------- |
| | | | | |

## 6. Acta de aceptación

| | |
| --- | --- |
| **Sistema** | Mantas Guajiras |
| **Versión** | (ver *Mi perfil* → "Versión de la app") |
| **Dirección** | https:// |
| **Fecha del recorrido** | |
| **Participantes** | |
| **Escenarios aprobados** | ___ / 24 |
| **Criterios del negocio aprobados** | ___ / 14 |
| **Diferencias abiertas** | |

**Resultado:**

- ☐ **Aceptado.** El sistema puede usarse con los datos reales del negocio.
- ☐ **Aceptado con observaciones.** Se usa, y las diferencias de la sección 5 se corrigen en las fechas acordadas.
- ☐ **No aceptado.** Se corrigen las diferencias y se repite el recorrido.

Firma de la responsable del negocio: ______________________________

Fecha: ______________________________

> Con el acta firmada se marcan NEG-01 a NEG-14 en `docs/10-certificacion.md` y la versión pasa de `1.0.0-rc.1` a `1.0.0`.
