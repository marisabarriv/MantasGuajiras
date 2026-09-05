import { useEffect, useState } from "react";

import ProductService from "../services/ProductService";

function NewSalePage() {
  const [search, setSearch] = useState("");

  const user = JSON.parse(localStorage.getItem("user") || "{}");

  const username = user.username || "usuario";

  const [products, setProducts] = useState([]);

  const [categories, setCategories] = useState([]);

  const [loadingProducts, setLoadingProducts] = useState(true);

  const [productsError, setProductsError] = useState("");

  const [selectedCategory, setSelectedCategory] = useState("");

  const [cart, setCart] = useState([]);

  // =========================================
  // POPUP DE PRECIO
  // =========================================

  const [selectedCartItem, setSelectedCartItem] = useState(null);

  const [specialPrice, setSpecialPrice] = useState("");

  // =========================================
  // CARGAR PRODUCTOS Y CATEGORÍAS
  // =========================================

  useEffect(() => {
    const loadProducts = async () => {
      try {
        setLoadingProducts(true);
        setProductsError("");

        const [productsData, categoriesData] = await Promise.all([
          ProductService.findAll(),
          ProductService.findCategories(),
        ]);

        setProducts(Array.isArray(productsData) ? productsData : []);

        const activeCategories = Array.isArray(categoriesData)
          ? [...categoriesData]
              .filter((category) => category.active !== false)
              .sort((a, b) => {
                const nameA = String(a.name || "")
                  .trim()
                  .toLowerCase();

                const nameB = String(b.name || "")
                  .trim()
                  .toLowerCase();

                if (nameA === "manta") return -1;
                if (nameB === "manta") return 1;

                if (nameA === "tela") return -1;
                if (nameB === "tela") return 1;

                return 0;
              })
          : [];

        setCategories(activeCategories);

        // Seleccionar automáticamente
        // la primera categoría disponible.
        if (activeCategories.length > 0) {
          setSelectedCategory(activeCategories[0].id);
        }
      } catch (error) {
        setProductsError(
          error.message || "No fue posible cargar los productos.",
        );
      } finally {
        setLoadingProducts(false);
      }
    };

    loadProducts();
  }, []);

  // =========================================
  // CATEGORÍA SELECCIONADA
  // =========================================

  const selectedCategoryData = categories.find(
    (category) => String(category.id) === String(selectedCategory),
  );

  // =========================================
  // FILTRAR PRODUCTOS
  // =========================================

  const filteredProducts = products
    .filter(
      (product) => String(product.categoryId) === String(selectedCategory),
    )
    .filter((product) =>
      product.name?.toLowerCase().includes(search.toLowerCase()),
    );

  // =========================================
  // AGREGAR PRODUCTO AL CARRITO
  // =========================================

  const handleAddToCart = (product) => {
    setCart((currentCart) => {
      const existingProduct = currentCart.find(
        (item) => item.productId === product.id,
      );

      if (existingProduct) {
        return currentCart.map((item) =>
          item.productId === product.id
            ? {
                ...item,
                quantity: item.quantity + 1,
              }
            : item,
        );
      }

      return [
        ...currentCart,
        {
          productId: product.id,
          name: product.name,

          // Precio utilizado
          // en esta venta.
          price: Number(product.unitPrice),

          // Precio original.
          // Se conserva para
          // calcular el descuento.
          originalPrice: Number(product.unitPrice),

          quantity: 1,
        },
      ];
    });
  };

  const handleRemoveFromCart = (productId) => {
    setCart((currentCart) =>
      currentCart.filter((item) => item.productId !== productId),
    );
  };

  const handleDecreaseQuantity = (productId) => {
    setCart((currentCart) =>
        currentCart
            .map((item) =>
                item.productId === productId
                    ? {
                        ...item,
                        quantity: item.quantity - 1,
                    }
                    : item
            )
            .filter(
                (item) => item.quantity > 0
            )
    );
};

  // =========================================
  // ABRIR POPUP DE PRECIO
  // =========================================

  const handleOpenPricePopup = (item) => {
    setSelectedCartItem(item);
    setSpecialPrice(item.price);
  };

  // =========================================
  // CERRAR POPUP
  // =========================================

  const handleClosePricePopup = () => {
    setSelectedCartItem(null);
    setSpecialPrice("");
  };

  // =========================================
  // CALCULAR DESCUENTO
  // =========================================

  const calculateDiscount = () => {
    if (!selectedCartItem) {
      return 0;
    }

    const originalPrice = Number(selectedCartItem.originalPrice);

    const currentPrice = Number(specialPrice);

    if (!originalPrice || currentPrice < 0) {
      return 0;
    }

    const discount = ((originalPrice - currentPrice) / originalPrice) * 100;

    return Math.max(0, discount);
  };

  // =========================================
  // GUARDAR PRECIO PARA ESTA COMPRA
  // =========================================

  const handleSavePrice = () => {
    if (!selectedCartItem) {
      return;
    }

    const newPrice = Number(specialPrice);

    if (!newPrice || newPrice <= 0) {
      return;
    }

    setCart((currentCart) =>
      currentCart.map((item) =>
        item.productId === selectedCartItem.productId
          ? {
              ...item,
              price: newPrice,
            }
          : item,
      ),
    );

    handleClosePricePopup();
  };

  // =========================================
  // TOTAL DEL CARRITO
  // =========================================

  const cartTotal = cart.reduce(
    (total, item) => total + Number(item.price) * item.quantity,
    0,
  );

  // =========================================
  // RENDER
  // =========================================

  return (
    <div className="pos-layout">
      <div className="pos-main">
        {/* =====================================
                    ENCABEZADO
                ===================================== */}

        <header className="pos-header">
          <div>
            <h1>Mantas Guajiras</h1>

            <p>
              Hola, <b>{username}</b>
            </p>
          </div>

          <button className="logout-button">Salir</button>
        </header>

        {/* =====================================
                    CONTENIDO PRINCIPAL
                ===================================== */}

        <main className="pos-content">
          {/* =================================
                        ZONA DE PRODUCTOS
                    ================================= */}

          <section className="products-section">
            {/* =================================
                            HERRAMIENTAS
                        ================================= */}

            <div className="products-toolbar">
              {/* BÚSQUEDA + ESCÁNER */}

              <div className="search-area">
                <input
                  type="text"
                  placeholder="Buscar producto..."
                  value={search}
                  onChange={(event) => setSearch(event.target.value)}
                />

                <button className="scan-button">Escanear código</button>
              </div>

              {/* =================================
                                CATEGORÍAS
                            ================================= */}

              <div className="product-tabs">
                {categories.map((category) => (
                  <button
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
            </div>

            {/* =================================
                            PRODUCTOS
                        ================================= */}

            <div className="products-grid">
              {/* CARGANDO */}

              {loadingProducts && <p>Cargando productos...</p>}

              {/* ERROR */}

              {!loadingProducts && productsError && <p>{productsError}</p>}

              {/* SIN CATEGORÍAS */}

              {!loadingProducts &&
                !productsError &&
                categories.length === 0 && (
                  <p>No hay categorías disponibles.</p>
                )}

              {/* SIN PRODUCTOS */}

              {!loadingProducts &&
                !productsError &&
                categories.length > 0 &&
                filteredProducts.length === 0 && (
                  <p>No hay productos disponibles.</p>
                )}

              {/* PRODUCTOS REALES */}

              {!loadingProducts &&
                !productsError &&
                filteredProducts.map((product) => (
                  <div className="product-card" key={product.id}>
                    <h3>{product.name}</h3>

                    <p>${Number(product.unitPrice).toLocaleString("es-CO")}</p>

                    <button onClick={() => handleAddToCart(product)}>
                      Agregar
                    </button>
                  </div>
                ))}
            </div>
          </section>

          {/* =================================
                        CARRITO
                    ================================= */}

          <aside className="cart-section">
            <div className="cart-header">
              <h2>Carrito</h2>
            </div>

            {/* PRODUCTOS DEL CARRITO */}

            {cart.length === 0 ? (
              <div className="cart-empty">
                <p>Aún no hay productos.</p>
              </div>
            ) : (
              <div className="cart-items">
                {/* ENCABEZADOS */}

                <div className="cart-item">
                  <strong>Nombre</strong>

                  <strong>Cantidad</strong>

                  <strong>Total</strong>
                </div>

                {cart.map((item) => (
                  <div
                    className="cart-item"
                    key={item.productId}
                    onClick={() => handleOpenPricePopup(item)}
                    style={{
                      cursor: "pointer",
                    }}
                  >
                    {/* NOMBRE */}

                    <div>
                      <strong>{item.name}</strong>
                    </div>

                    {/* CANTIDAD */}

                    <div>
    <button
        type="button"
        onClick={(event) => {
            event.stopPropagation();
            handleDecreaseQuantity(
                item.productId
            );
        }}
    >
        −
    </button>

    <span>
        {item.quantity}
    </span>

    <button
        type="button"
        onClick={(event) => {
            event.stopPropagation();
            handleAddToCart({
                id: item.productId,
                name: item.name,
                unitPrice: item.originalPrice,
            });
        }}
    >
        +
    </button>
</div>

                    {/* TOTAL DEL PRODUCTO */}

                    <div>
                      <strong>
                        $
                        {(Number(item.price) * item.quantity).toLocaleString(
                          "es-CO",
                        )}
                      </strong>
                    </div>
                    <button
                      type="button"
                      onClick={(event) => {
                        event.stopPropagation();
                        handleRemoveFromCart(item.productId);
                      }}
                    >
                      Quitar
                    </button>
                  </div>
                ))}
              </div>
            )}

            {/* =================================
                            FOOTER DEL CARRITO
                        ================================= */}

            <div className="cart-footer">
              <div className="cart-total">
                <span>Total</span>

                <strong>${cartTotal.toLocaleString("es-CO")}</strong>
              </div>

              <button className="checkout-button" disabled={cart.length === 0}>
                Finalizar venta
              </button>
            </div>
          </aside>
        </main>
      </div>

      {/* =========================================
                POPUP PRECIO DEL PRODUCTO
            ========================================= */}

      {selectedCartItem && (
        <div className="modal-overlay" onClick={handleClosePricePopup}>
          <div
            className="modal-content"
            onClick={(event) => event.stopPropagation()}
          >
            <h2>Precio del producto</h2>

            <p>{selectedCartItem.name}</p>

            <div className="form-group">
              <label>Precio unitario</label>

              <input
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

            <div className="form-group">
              <label>Descuento</label>

              <div className="automatic-field">
                {calculateDiscount().toFixed(2)}%
              </div>
            </div>

            <div className="modal-actions">
              <button type="button" onClick={handleClosePricePopup}>
                Cancelar
              </button>

              <button type="button" onClick={handleSavePrice}>
                Aplicar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default NewSalePage;
