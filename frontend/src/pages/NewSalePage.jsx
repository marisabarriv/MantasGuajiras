import { useEffect, useMemo, useRef, useState } from "react";
import useUnsavedChanges from "../hooks/useUnsavedChanges";

import Modal from "../components/Modal";
import BarcodeScanModal from "../components/BarcodeScanModal";
import ProductService from "../services/ProductService";
import InventoryService from "../services/InventoryService";
import SaleService from "../services/SaleService";
import { isConnectionError } from "../services/apiClient";
import useAutoRefresh from "../hooks/useAutoRefresh";
import {
  addPendingSale,
  loadCatalog,
  newClientRequestId,
  removePendingSale,
  saveCachedInventory,
  saveCatalog,
  usePendingSales,
} from "../utils/offlineStore";
import { onSalesSynced, retryRejectedSale } from "../utils/offlineSync";
import { getUser } from "../utils/session";
import {
  formatNumber,
  formatQuantity,
  isWholeUnit,
} from "../utils/inventoryFormat";

import "../styles/new-sale.css";
import "../styles/offline.css";

const formatSavedAt = (iso) =>
  new Date(iso).toLocaleString("es-CO", {
    dateStyle: "short",
    timeStyle: "short",
  });

const money = (value) =>
  `$${Number(value || 0).toLocaleString("es-CO", {
    maximumFractionDigits: 2,
  })}`;

// Redondeo a 2 decimales para metros y dinero.
const round2 = (value) => Math.round(Number(value) * 100) / 100;

function NewSalePage() {
  const user = getUser();
  const username = user.username || "usuario";

  const searchInputRef = useRef(null);
  const submittingRef = useRef(false);

  // =========================================
  // DATOS
  // =========================================

  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [stock, setStock] = useState({});

  const [loadingProducts, setLoadingProducts] = useState(true);
  const [productsError, setProductsError] = useState("");

  // Sin conexión: hora del catálogo guardado en el dispositivo que se
  // está mostrando (null = datos en vivo del servidor).
  const [offlineCatalogAt, setOfflineCatalogAt] = useState(null);

  // Ventas hechas sin conexión en este dispositivo (de este usuario).
  const pendingSales = usePendingSales();
  const myPendingSales = pendingSales.filter(
    (sale) => sale.username === user.username,
  );

  const { refreshKey, takeSilent } = useAutoRefresh();
  const [syncedKey, setSyncedKey] = useState(0);

  const [selectedCategory, setSelectedCategory] = useState("");
  const [search, setSearch] = useState("");
  const [scanOpen, setScanOpen] = useState(false);

  // =========================================
  // CARRITO
  // =========================================

  const [cart, setCart] = useState([]);
  // El carrito no se guarda: salir con productos en él los pierde.
  useUnsavedChanges(cart.length > 0);
  const [cartMessage, setCartMessage] = useState("");

  // =========================================
  // POPUP DE PRECIO
  // =========================================

  const [selectedCartItem, setSelectedCartItem] = useState(null);
  const [specialPrice, setSpecialPrice] = useState("");

  // =========================================
  // CONFIRMAR / REGISTRAR VENTA
  // =========================================

  const [confirmOpen, setConfirmOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [saleError, setSaleError] = useState("");
  const [completedSale, setCompletedSale] = useState(null);

  // =========================================
  // CARGAR PRODUCTOS, CATEGORÍAS Y EXISTENCIAS
  // =========================================

  const applyStock = (inventoryData) => {
    const map = {};

    (Array.isArray(inventoryData) ? inventoryData : []).forEach((item) => {
      map[item.productId] = item;
    });

    setStock(map);
  };

  const applyCatalog = (productsData, categoriesData, inventoryData) => {
    // Solo productos activos se pueden vender.
    setProducts(
      (Array.isArray(productsData) ? productsData : []).filter(
        (product) => product.active !== false,
      ),
    );

    const activeCategories = (
      Array.isArray(categoriesData) ? [...categoriesData] : []
    )
      .filter((category) => category.active !== false)
      .sort((a, b) => {
        const nameA = String(a.name || "").trim().toLowerCase();
        const nameB = String(b.name || "").trim().toLowerCase();

        if (nameA === "manta") return -1;
        if (nameB === "manta") return 1;
        if (nameA === "tela") return -1;
        if (nameB === "tela") return 1;

        return 0;
      });

    setCategories(activeCategories);

    setSelectedCategory((current) =>
      activeCategories.some((category) => category.id === current)
        ? current
        : activeCategories[0]?.id || "",
    );

    applyStock(inventoryData);
  };

  // Se vuelve a cargar al volver a la app, al volver la conexión y
  // cada minuto (refreshKey), además de la primera vez.
  useEffect(() => {
    let cancelled = false;
    const silent = takeSilent();

    Promise.all([
      ProductService.findAll(),
      ProductService.findCategories(),
      InventoryService.findAll(),
    ])
      .then(([productsData, categoriesData, inventoryData]) => {
        if (cancelled) {
          return;
        }

        applyCatalog(productsData, categoriesData, inventoryData);
        setProductsError("");
        setOfflineCatalogAt(null);

        // Copia para poder vender si después se va la conexión.
        saveCatalog({
          products: productsData,
          categories: categoriesData,
          inventory: inventoryData,
        });
      })
      .catch((error) => {
        if (cancelled) {
          return;
        }

        if (isConnectionError(error)) {
          // En una recarga automática se conservan los datos en pantalla.
          if (silent) {
            setOfflineCatalogAt((current) => current || loadCatalog()?.savedAt || null);
            return;
          }

          const catalog = loadCatalog();

          if (catalog) {
            applyCatalog(catalog.products, catalog.categories, catalog.inventory);
            setOfflineCatalogAt(catalog.savedAt);
            setProductsError("");
            return;
          }

          setProductsError(
            "No hay conexión con el servidor y este dispositivo todavía no tiene productos guardados. " +
              "Abre «Nueva venta» al menos una vez con conexión para poder vender sin ella.",
          );
          return;
        }

        if (!silent) {
          setProductsError(
            error.message || "No fue posible cargar los productos.",
          );
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoadingProducts(false);
        }
      });

    return () => {
      cancelled = true;
    };
    // applyCatalog solo usa setters de estado.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [refreshKey, syncedKey, takeSilent]);

  // Cuando se envían ventas guardadas, las existencias cambiaron.
  useEffect(() => onSalesSynced(() => setSyncedKey((key) => key + 1)), []);

  function reloadStock() {
    InventoryService.findAll()
      .then((inventoryData) => {
        applyStock(inventoryData);
        saveCachedInventory(inventoryData);
        setOfflineCatalogAt(null);
      })
      .catch(() => {
        // Si falla, se conserva el último dato; el backend
        // valida igualmente las existencias al vender.
      });
  }

  // =========================================
  // AYUDANTES DE PRODUCTO
  // =========================================

  // Lo vendido sin conexión todavía no está descontado en el servidor:
  // se resta aquí para no vender dos veces lo mismo desde este dispositivo.
  const reservedOffline = useMemo(() => {
    const totals = {};
    pendingSales
      .filter((sale) => sale.status === "PENDING")
      .forEach((sale) =>
        sale.items.forEach((item) => {
          totals[item.productId] =
            (totals[item.productId] || 0) + Number(item.quantity || 0);
        }),
      );
    return totals;
  }, [pendingSales]);

  const getAvailable = (productId) =>
    round2(
      Math.max(
        0,
        Number(stock[productId]?.quantity || 0) -
          (reservedOffline[productId] || 0),
      ),
    );

  const getUnit = (productId) => ({
    unitAbbreviation: stock[productId]?.unitAbbreviation,
    unitName: stock[productId]?.unitName,
  });

  const quantityText = (value, productId) => {
    const { unitAbbreviation, unitName } = getUnit(productId);
    return formatQuantity(value, unitAbbreviation, unitName);
  };

  const getCartQuantity = (productId) =>
    Number(cart.find((item) => item.productId === productId)?.quantity || 0);

  // =========================================
  // FILTRAR PRODUCTOS
  // Con texto de búsqueda se busca en TODAS las
  // categorías (nombre, código interno o de barras).
  // =========================================

  const searchText = search.trim().toLowerCase();

  const filteredProducts = products.filter((product) => {
    if (searchText) {
      return [product.name, product.internalCode, product.barcode]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(searchText));
    }

    return String(product.categoryId) === String(selectedCategory);
  });

  // =========================================
  // CARRITO
  // =========================================

  const handleAddToCart = (product) => {
    const available = getAvailable(product.id);
    const inCart = getCartQuantity(product.id);
    const remaining = round2(available - inCart);

    if (remaining <= 0) {
      setCartMessage(
        available <= 0
          ? `${product.name} está agotado.`
          : `Ya agregaste todas las existencias de ${product.name} (${quantityText(available, product.id)}).`,
      );
      return;
    }

    // Se agrega 1 (o lo que quede, si es menos de 1 metro).
    const step = Math.min(1, remaining);

    setCartMessage("");

    setCart((currentCart) => {
      const existing = currentCart.find(
        (item) => item.productId === product.id,
      );

      if (existing) {
        return currentCart.map((item) =>
          item.productId === product.id
            ? { ...item, quantity: round2(Number(item.quantity) + step) }
            : item,
        );
      }

      const { unitAbbreviation, unitName } = getUnit(product.id);

      return [
        ...currentCart,
        {
          productId: product.id,
          name: product.name,

          // Precio usado en esta venta (puede ser especial).
          price: Number(product.unitPrice),

          // Precio normal del producto. No se modifica.
          originalPrice: Number(product.unitPrice),

          quantity: step,

          wholeUnit: isWholeUnit(unitAbbreviation, unitName),
        },
      ];
    });
  };

  const handleRemoveFromCart = (productId) => {
    setCartMessage("");
    setCart((currentCart) =>
      currentCart.filter((item) => item.productId !== productId),
    );
  };

  const handleDecreaseQuantity = (productId) => {
    setCartMessage("");
    setCart((currentCart) =>
      currentCart
        .map((item) =>
          item.productId === productId
            ? { ...item, quantity: round2(Number(item.quantity) - 1) }
            : item,
        )
        .filter((item) => Number(item.quantity) > 0),
    );
  };

  // Telas: se escribe la cantidad de metros (con decimales).
  const handleQuantityInput = (productId, value) => {
    setCart((currentCart) =>
      currentCart.map((item) =>
        item.productId === productId ? { ...item, quantity: value } : item,
      ),
    );
  };

  // Al salir del campo se corrige un valor vacío o mayor al disponible.
  const handleQuantityBlur = (item) => {
    const available = getAvailable(item.productId);
    let value = round2(Number(item.quantity));

    if (!Number.isFinite(value) || value <= 0) {
      value = Math.min(1, available);
      setCartMessage(
        `La cantidad de ${item.name} debe ser mayor que cero. Se dejó en ${quantityText(value, item.productId)}.`,
      );
    } else if (value > available) {
      value = available;
      setCartMessage(
        `Solo hay ${quantityText(available, item.productId)} de ${item.name}.`,
      );
    } else {
      setCartMessage("");
    }

    setCart((currentCart) =>
      currentCart.map((cartItem) =>
        cartItem.productId === item.productId
          ? { ...cartItem, quantity: value }
          : cartItem,
      ),
    );
  };

  // =========================================
  // POPUP DE PRECIO ESPECIAL
  // =========================================

  const handleOpenPricePopup = (item) => {
    setSelectedCartItem(item);
    setSpecialPrice(String(item.price));
  };

  const handleClosePricePopup = () => {
    setSelectedCartItem(null);
    setSpecialPrice("");
  };

  // La tecla Esc cierra el popup de precio (igual que las demás ventanas).
  useEffect(() => {
    if (!selectedCartItem) {
      return undefined;
    }

    const handleKeyDown = (event) => {
      if (event.key === "Escape") {
        setSelectedCartItem(null);
        setSpecialPrice("");
      }
    };

    document.addEventListener("keydown", handleKeyDown);

    return () => document.removeEventListener("keydown", handleKeyDown);
  }, [selectedCartItem]);

  const priceNumber = Number(specialPrice);

  let priceError = "";

  if (selectedCartItem) {
    if (specialPrice === "" || !Number.isFinite(priceNumber) || priceNumber <= 0) {
      priceError = "Escribe un precio mayor que cero.";
    } else if (priceNumber > selectedCartItem.originalPrice) {
      priceError = `El precio especial no puede ser mayor que el precio normal (${money(selectedCartItem.originalPrice)}).`;
    }
  }

  const discountPercent =
    selectedCartItem && !priceError && selectedCartItem.originalPrice > 0
      ? ((selectedCartItem.originalPrice - priceNumber) /
          selectedCartItem.originalPrice) *
        100
      : 0;

  const applyPrice = (newPrice) => {
    setCart((currentCart) =>
      currentCart.map((item) =>
        item.productId === selectedCartItem.productId
          ? { ...item, price: round2(newPrice) }
          : item,
      ),
    );

    handleClosePricePopup();
  };

  const handleSavePrice = () => {
    if (!selectedCartItem || priceError) {
      return;
    }

    applyPrice(priceNumber);
  };

  // =========================================
  // TOTAL
  // =========================================

  const cartTotal = cart.reduce(
    (total, item) => total + Number(item.price) * Number(item.quantity || 0),
    0,
  );

  const cartHasInvalidQuantity = cart.some(
    (item) => !(Number(item.quantity) > 0),
  );

  // =========================================
  // BÚSQUEDA / LECTOR DE CÓDIGO DE BARRAS
  // El lector escribe el código y pulsa Enter.
  // =========================================

  /** Busca por código de barras o código interno. */
  const findByCode = (rawCode) => {
    const code = String(rawCode || "").trim().toLowerCase();

    if (!code) {
      return null;
    }

    return (
      products.find(
        (product) =>
          String(product.barcode || "").toLowerCase() === code ||
          String(product.internalCode || "").toLowerCase() === code,
      ) || null
    );
  };

  // Lector físico: escribe el código en el buscador y pulsa Enter.
  const handleSearchKeyDown = (event) => {
    if (event.key !== "Enter") {
      return;
    }

    const typed = search.trim();

    if (!typed) {
      return;
    }

    const match = findByCode(typed);

    if (match) {
      event.preventDefault();
      handleAddToCart(match);
      setSearch("");
    } else if (filteredProducts.length === 0) {
      setCartMessage(`No hay ningún producto con el código "${typed}".`);
    }
  };

  // Cámara: al leer el código, el producto entra al carrito.
  const handleCameraCode = (code) => {
    setScanOpen(false);

    const match = findByCode(code);

    if (match) {
      handleAddToCart(match);
    } else {
      setCartMessage(
        `No hay ningún producto con el código "${code}". ` +
          "Revisa que el producto tenga ese código de barras en Productos.",
      );
    }

    searchInputRef.current?.focus();
  };

  // =========================================
  // REGISTRAR VENTA
  // =========================================

  const handleOpenConfirm = () => {
    if (cart.length === 0 || cartHasInvalidQuantity) {
      return;
    }

    setSaleError("");
    setConfirmOpen(true);
  };

  const handleConfirmSale = async () => {
    // Evita registrar dos veces la misma venta (doble clic).
    if (submittingRef.current) {
      return;
    }

    submittingRef.current = true;
    setSubmitting(true);
    setSaleError("");

    const items = cart.map((item) => {
      const special = round2(item.price) < round2(item.originalPrice);

      return special
        ? {
            productId: item.productId,
            quantity: round2(item.quantity),
            specialPrice: true,
            finalUnitPrice: round2(item.price),
          }
        : {
            productId: item.productId,
            quantity: round2(item.quantity),
            specialPrice: false,
          };
    });

    // Mismo identificador si la venta termina guardada sin conexión:
    // si el servidor alcanzó a recibirla, no se registra dos veces.
    const clientRequestId = newClientRequestId();

    const finishCart = () => {
      setCart([]);
      setCartMessage("");
      setConfirmOpen(false);
    };

    try {
      const sale = await SaleService.create({ items, clientRequestId });

      setCompletedSale({
        total: sale.total ?? cartTotal,
        lines: cart.length,
        offline: false,
      });

      finishCart();
      reloadStock();
    } catch (error) {
      // Sin conexión, o el servidor no pudo atenderla (5xx: base de datos
      // caída, reinicio...): la venta se guarda en el dispositivo y se envía
      // sola después. Su clientRequestId evita duplicarla si el servidor
      // alcanzó a guardarla.
      if (isConnectionError(error) || error.status >= 500) {
        const saved = addPendingSale({
          clientRequestId,
          username: user.username,
          createdAt: new Date().toISOString(),
          items,
          lines: cart.map((item) => ({
            name: item.name,
            quantity: round2(item.quantity),
            unit: item.wholeUnit ? "und." : "m",
            price: round2(item.price),
          })),
          estimatedTotal: round2(cartTotal),
        });

        if (saved) {
          setCompletedSale({
            total: cartTotal,
            lines: cart.length,
            offline: true,
          });
          finishCart();
        } else {
          setSaleError(
            "No hay conexión y no se pudo guardar la venta en este dispositivo (almacenamiento lleno o bloqueado). La venta NO se registró: anótala en papel.",
          );
        }
      } else {
        setSaleError(error.message || "No fue posible registrar la venta.");

        // Las existencias pudieron cambiar (otra venta, un ajuste...).
        reloadStock();
      }
    } finally {
      submittingRef.current = false;
      setSubmitting(false);
    }
  };

  // =========================================
  // RENDER
  // (Cerrar sesión está en el menú lateral, en todas las páginas.)
  // =========================================

  return (
    <div className="pos-layout">
      <div className="pos-main">
        {/* ENCABEZADO */}

        <header className="pos-header">
          <div>
            <h1>Nueva venta</h1>
            <p>
              Hola, <b>{username}</b>. Agrega productos al carrito y pulsa
              "Finalizar venta".
            </p>
          </div>
        </header>

        {offlineCatalogAt && (
          <p className="pos-offline-note" role="status">
            <strong>Vendiendo sin conexión.</strong> Productos, precios y
            existencias guardados el {formatSavedAt(offlineCatalogAt)}; pueden
            haber cambiado. El servidor los confirma al recibir la venta.
          </p>
        )}

        {myPendingSales.length > 0 && (
          <section className="offline-sales" aria-label="Ventas guardadas sin conexión">
            <h2>Ventas guardadas sin conexión ({myPendingSales.length})</h2>
            <p>
              Están en este dispositivo. Se envían solas cuando vuelve la
              conexión; no cierres sesión ni borres los datos del navegador
              hasta que desaparezcan de esta lista.
            </p>

            <ul>
              {myPendingSales.map((sale) => {
                const rejected = sale.status === "REJECTED";

                return (
                  <li
                    key={sale.clientRequestId}
                    className={`offline-sale ${rejected ? "is-rejected" : ""}`}
                  >
                    <div className="offline-sale-info">
                      <strong>
                        {formatSavedAt(sale.createdAt)} ·{" "}
                        {money(sale.estimatedTotal)}
                      </strong>
                      <span>
                        {sale.lines
                          .map(
                            (line) =>
                              `${line.name} × ${formatNumber(line.quantity)} ${line.unit}`,
                          )
                          .join(", ")}
                      </span>
                      {rejected ? (
                        <span className="offline-sale-error">
                          No se registró: {sale.error}
                        </span>
                      ) : (
                        <span>Pendiente de envío</span>
                      )}
                    </div>

                    {rejected && (
                      <div className="offline-sale-actions">
                        <button
                          type="button"
                          className="inv-button-secondary"
                          onClick={() => retryRejectedSale(sale.clientRequestId)}
                        >
                          Reintentar
                        </button>
                        <button
                          type="button"
                          className="inv-button-secondary"
                          onClick={() => {
                            if (
                              window.confirm(
                                "¿Descartar esta venta? No quedará registrada en el sistema. " +
                                  "Hazlo solo si la anulaste con el cliente o la registraste de otra forma.",
                              )
                            ) {
                              removePendingSale(sale.clientRequestId);
                            }
                          }}
                        >
                          Descartar
                        </button>
                      </div>
                    )}
                  </li>
                );
              })}
            </ul>
          </section>
        )}

        {/* CONTENIDO PRINCIPAL */}

        <main className="pos-content">
          {/* ZONA DE PRODUCTOS */}

          <section className="products-section">
            <div className="products-toolbar">
              <div className="search-area">
                {/* Queda enfocado al abrir la página: el lector físico
                    funciona sin tocar nada. */}
                <input
                  ref={searchInputRef}
                  type="search"
                  placeholder="Buscar por nombre o código..."
                  aria-label="Buscar producto"
                  value={search}
                  onChange={(event) => setSearch(event.target.value)}
                  onKeyDown={handleSearchKeyDown}
                  autoFocus
                />

                <button
                  type="button"
                  className="scan-camera-button"
                  onClick={() => {
                    setCartMessage("");
                    setScanOpen(true);
                  }}
                >
                  Escanear con cámara
                </button>
              </div>

              <p className="pos-scan-help">
                Con lector de códigos: escanea directamente y el producto se
                agrega solo al carrito.
              </p>

              {!searchText && (
                <div className="product-tabs">
                  {categories.map((category) => (
                    <button
                      type="button"
                      key={category.id}
                      className={`product-tab ${
                        String(selectedCategory) === String(category.id)
                          ? "active"
                          : ""
                      }`}
                      onClick={() => setSelectedCategory(category.id)}
                    >
                      {category.name}
                    </button>
                  ))}
                </div>
              )}

              {searchText && (
                <p className="pos-search-info">
                  Resultados en todas las categorías para "{search.trim()}"
                </p>
              )}
            </div>

            {/* PRODUCTOS */}

            <div className="products-grid">
              {loadingProducts && <p>Cargando productos...</p>}

              {!loadingProducts && productsError && (
                <div className="result error pos-result">{productsError}</div>
              )}

              {!loadingProducts &&
                !productsError &&
                categories.length === 0 && <p>No hay categorías disponibles.</p>}

              {!loadingProducts &&
                !productsError &&
                categories.length > 0 &&
                filteredProducts.length === 0 && (
                  <p>No hay productos disponibles.</p>
                )}

              {!loadingProducts &&
                !productsError &&
                filteredProducts.map((product) => {
                  const available = getAvailable(product.id);
                  const remaining = round2(
                    available - getCartQuantity(product.id),
                  );
                  const { unitAbbreviation, unitName } = getUnit(product.id);
                  const wholeUnit = isWholeUnit(unitAbbreviation, unitName);

                  return (
                    <div
                      className={`product-card ${available <= 0 ? "product-card-out" : ""}`}
                      key={product.id}
                    >
                      <h3>{product.name}</h3>

                      <p>
                        {money(product.unitPrice)}
                        <span className="pos-price-unit">
                          {wholeUnit ? " c/u" : " por metro"}
                        </span>
                      </p>

                      <p
                        className={`pos-stock ${
                          available <= 0 ? "pos-stock-out" : ""
                        }`}
                      >
                        {available <= 0
                          ? "Agotado"
                          : `Disponibles: ${quantityText(available, product.id)}`}
                      </p>

                      <button
                        type="button"
                        onClick={() => handleAddToCart(product)}
                        disabled={remaining <= 0}
                      >
                        {available <= 0
                          ? "Agotado"
                          : remaining <= 0
                            ? "Todo en el carrito"
                            : "Agregar"}
                      </button>
                    </div>
                  );
                })}
            </div>
          </section>

          {/* CARRITO */}

          <aside className="cart-section">
            <div className="cart-header">
              <h2>Carrito</h2>
            </div>

            {cart.length === 0 ? (
              <div className="cart-empty">
                <p>Aún no hay productos.</p>
              </div>
            ) : (
              <div className="cart-items pos-cart-list">
                {cart.map((item) => {
                  const isSpecial =
                    round2(item.price) < round2(item.originalPrice);
                  const lineTotal =
                    Number(item.price) * Number(item.quantity || 0);
                  const remaining = round2(
                    getAvailable(item.productId) - Number(item.quantity || 0),
                  );

                  return (
                    <div className="pos-cart-row" key={item.productId}>
                      <div className="pos-cart-info">
                        <strong>{item.name}</strong>

                        <span className="pos-cart-price">
                          {money(item.price)}
                          {item.wholeUnit ? " c/u" : " por metro"}
                          {isSpecial && (
                            <span className="pos-special-tag">
                              Precio especial
                            </span>
                          )}
                        </span>
                      </div>

                      <div className="pos-cart-line-total">
                        {money(lineTotal)}
                      </div>

                      <div
                        className="pos-cart-qty"
                        role="group"
                        aria-label={`Cantidad de ${item.name}`}
                      >
                        <button
                          type="button"
                          onClick={() => handleDecreaseQuantity(item.productId)}
                          aria-label={`Quitar una unidad de ${item.name}`}
                        >
                          −
                        </button>

                        {item.wholeUnit ? (
                          <span className="pos-cart-qty-value">
                            {formatNumber(item.quantity)}
                          </span>
                        ) : (
                          <label className="pos-cart-qty-input">
                            <input
                              type="number"
                              inputMode="decimal"
                              min="0.01"
                              step="0.01"
                              value={item.quantity}
                              onChange={(event) =>
                                handleQuantityInput(
                                  item.productId,
                                  event.target.value,
                                )
                              }
                              onBlur={() => handleQuantityBlur(item)}
                              aria-label={`Metros de ${item.name}`}
                            />
                            <span>m</span>
                          </label>
                        )}

                        <button
                          type="button"
                          onClick={() =>
                            handleAddToCart({
                              id: item.productId,
                              name: item.name,
                              unitPrice: item.originalPrice,
                            })
                          }
                          disabled={remaining <= 0}
                          aria-label={`Agregar una unidad de ${item.name}`}
                        >
                          +
                        </button>
                      </div>

                      <div className="pos-cart-actions">
                        <button
                          type="button"
                          className="pos-cart-price-button"
                          onClick={() => handleOpenPricePopup(item)}
                        >
                          Cambiar precio
                        </button>

                        <button
                          type="button"
                          className="pos-cart-remove-button"
                          onClick={() => handleRemoveFromCart(item.productId)}
                        >
                          Quitar
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}

            {/* PIE DEL CARRITO */}

            <div className="cart-footer">
              {cartMessage && (
                <div className="pos-cart-message" role="status">
                  {cartMessage}
                </div>
              )}

              <div className="cart-total">
                <span>Total</span>
                <strong>{money(cartTotal)}</strong>
              </div>

              <button
                type="button"
                className="checkout-button"
                disabled={cart.length === 0 || cartHasInvalidQuantity}
                onClick={handleOpenConfirm}
              >
                Finalizar venta
              </button>
            </div>
          </aside>
        </main>
      </div>

      {/* POPUP PRECIO DEL PRODUCTO */}

      {selectedCartItem && (
        <div className="modal-overlay" onClick={handleClosePricePopup}>
          <div
            className="modal-content"
            onClick={(event) => event.stopPropagation()}
            role="dialog"
            aria-modal="true"
            aria-labelledby="price-popup-title"
          >
            <button
              type="button"
              className="modal-close"
              onClick={handleClosePricePopup}
              aria-label="Cerrar"
              title="Cerrar"
            >
              ×
            </button>

            <h2 id="price-popup-title">Precio del producto</h2>

            <p>
              {selectedCartItem.name} · Precio normal:{" "}
              {money(selectedCartItem.originalPrice)}
            </p>

            <div className="form-group">
              <label htmlFor="specialPriceInput">
                Precio para esta venta
              </label>

              <input
                id="specialPriceInput"
                type="number"
                min="0"
                step="0.01"
                value={specialPrice}
                onChange={(event) => setSpecialPrice(event.target.value)}
                onKeyDown={(event) => {
                  if (event.key === "Enter") {
                    event.preventDefault();
                    handleSavePrice();
                  }
                }}
                autoFocus
              />
            </div>

            {priceError ? (
              <div className="pos-price-error" role="alert">
                {priceError}
              </div>
            ) : (
              <div className="form-group">
                <label>Descuento</label>
                <div className="automatic-field">
                  {discountPercent > 0
                    ? `${formatNumber(discountPercent)} % · ahorra ${money(
                        selectedCartItem.originalPrice - priceNumber,
                      )} por ${selectedCartItem.wholeUnit ? "unidad" : "metro"}`
                    : "Sin descuento (precio normal)"}
                </div>
              </div>
            )}

            <p className="pos-price-note">
              El cambio solo aplica a esta venta. El precio del producto no
              se modifica.
            </p>

            <div className="modal-actions pos-price-actions">
              <button
                type="button"
                onClick={() => applyPrice(selectedCartItem.originalPrice)}
              >
                Usar precio normal
              </button>

              <button
                type="button"
                onClick={handleSavePrice}
                disabled={Boolean(priceError)}
              >
                Aplicar
              </button>
            </div>
          </div>
        </div>
      )}

      {/* CONFIRMAR VENTA */}

      <Modal
        open={confirmOpen}
        onClose={() => {
          if (!submitting) {
            setConfirmOpen(false);
          }
        }}
        title="Confirmar venta"
      >
        <div className="pos-confirm">
          <ul className="pos-confirm-list">
            {cart.map((item) => {
              const isSpecial =
                round2(item.price) < round2(item.originalPrice);

              return (
                <li key={item.productId}>
                  <div>
                    <strong>{item.name}</strong>
                    <span>
                      {quantityText(item.quantity, item.productId)} ×{" "}
                      {money(item.price)}
                      {isSpecial && " (precio especial)"}
                    </span>
                  </div>
                  <b>{money(Number(item.price) * Number(item.quantity))}</b>
                </li>
              );
            })}
          </ul>

          <div className="pos-confirm-total">
            <span>Total a cobrar</span>
            <strong>{money(cartTotal)}</strong>
          </div>

          {saleError && (
            <div className="result error pos-result" role="alert">
              {saleError}
            </div>
          )}

          <div className="product-form-actions">
            <button
              type="button"
              className="product-cancel-button"
              onClick={() => setConfirmOpen(false)}
              disabled={submitting}
            >
              Volver al carrito
            </button>

            <button
              type="button"
              className="product-submit-button"
              onClick={handleConfirmSale}
              disabled={submitting}
            >
              {submitting ? "Registrando..." : "Confirmar venta"}
            </button>
          </div>
        </div>
      </Modal>

      {/* ESCANEAR CON CÁMARA */}

      <BarcodeScanModal
        open={scanOpen}
        onClose={() => {
          setScanOpen(false);
          searchInputRef.current?.focus();
        }}
        onDetected={handleCameraCode}
        title="Escanear producto"
      />

      {/* VENTA REGISTRADA */}

      <Modal
        open={Boolean(completedSale)}
        onClose={() => setCompletedSale(null)}
        title={completedSale?.offline ? "Venta guardada sin conexión" : "Venta registrada"}
      >
        {completedSale && (
          <div className="pos-confirm">
            <div className="pos-success">
              <span className="pos-success-icon" aria-hidden="true">
                ✓
              </span>
              <div>
                {completedSale.offline ? (
                  <>
                    <p>
                      No hay conexión con el servidor: la venta quedó guardada en este
                      dispositivo y se enviará sola cuando vuelva la conexión.
                    </p>
                    <p>
                      Total estimado: <strong>{money(completedSale.total)}</strong>
                    </p>
                  </>
                ) : (
                  <>
                    <p>La venta se guardó correctamente.</p>
                    <p>
                      Total: <strong>{money(completedSale.total)}</strong>
                    </p>
                  </>
                )}
              </div>
            </div>

            <div className="product-form-actions">
              <button
                type="button"
                className="product-submit-button"
                onClick={() => {
                  setCompletedSale(null);
                  searchInputRef.current?.focus();
                }}
                autoFocus
              >
                Nueva venta
              </button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}

export default NewSalePage;
