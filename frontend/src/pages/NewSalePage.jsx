import { useEffect, useState } from "react";

import ProductService from "../services/ProductService";

function NewSalePage() {

    const [search, setSearch] = useState("");

    const user =
        JSON.parse(localStorage.getItem("user") || "{}");

    const username =
        user.username || "usuario";

    const [products, setProducts] =
        useState([]);

    const [categories, setCategories] =
        useState([]);

    const [loadingProducts, setLoadingProducts] =
        useState(true);

    const [productsError, setProductsError] =
        useState("");

    const [selectedCategory, setSelectedCategory] =
        useState("Mantas");

    const [cart, setCart] =
        useState([]);


    // =========================================
    // CARGAR PRODUCTOS Y CATEGORÍAS
    // =========================================

    useEffect(() => {

        const loadProducts = async () => {

            try {

                setLoadingProducts(true);
                setProductsError("");

                const [
                    productsData,
                    categoriesData
                ] = await Promise.all([

                    ProductService.findAll(),

                    ProductService.findCategories(),

                ]);

                setProducts(productsData);

                setCategories(categoriesData);

            } catch (error) {

                setProductsError(
                    error.message ||
                    "No fue posible cargar los productos."
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

    const selectedCategoryData =
        categories.find(
            (category) =>
                category.name.toLowerCase() ===
                selectedCategory.toLowerCase()
        );


    // =========================================
    // FILTRAR PRODUCTOS
    // =========================================

    const filteredProducts =
        products
            .filter(
                (product) =>
                    product.categoryId ===
                    selectedCategoryData?.id
            )
            .filter(
                (product) =>
                    product.name
                        .toLowerCase()
                        .includes(
                            search.toLowerCase()
                        )
            );


    // =========================================
    // AGREGAR PRODUCTO AL CARRITO
    // =========================================

    const handleAddToCart = (product) => {

        setCart((currentCart) => {

            const existingProduct =
                currentCart.find(
                    (item) =>
                        item.productId === product.id
                );

            if (existingProduct) {

                return currentCart.map((item) =>

                    item.productId === product.id

                        ? {
                            ...item,
                            quantity:
                                item.quantity + 1,
                        }

                        : item
                );
            }

            return [
                ...currentCart,

                {
                    productId: product.id,
                    name: product.name,
                    price: product.unitPrice,
                    quantity: 1,
                },
            ];
        });
    };


    // =========================================
    // TOTAL DEL CARRITO
    // =========================================

    const cartTotal =
        cart.reduce(
            (total, item) =>
                total +
                Number(item.price) *
                item.quantity,
            0
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

                        <h1>
                            Mantas Guajiras
                        </h1>

                        <p>
                            Hola, <b>{username}</b>
                        </p>

                    </div>

                    <button
                        className="logout-button"
                    >
                        Salir
                    </button>

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
                                    onChange={(event) =>
                                        setSearch(
                                            event.target.value
                                        )
                                    }
                                />

                                <button
                                    className="scan-button"
                                >
                                    Escanear código
                                </button>

                            </div>


                            {/* =================================
                                CATEGORÍAS
                            ================================= */}

                            <div className="product-tabs">

                                <button
                                    className={`product-tab ${
                                        selectedCategory ===
                                        "Mantas"
                                            ? "active"
                                            : ""
                                    }`}
                                    onClick={() =>
                                        setSelectedCategory(
                                            "Mantas"
                                        )
                                    }
                                >
                                    Mantas
                                </button>


                                <button
                                    className={`product-tab ${
                                        selectedCategory ===
                                        "Telas"
                                            ? "active"
                                            : ""
                                    }`}
                                    onClick={() =>
                                        setSelectedCategory(
                                            "Telas"
                                        )
                                    }
                                >
                                    Telas
                                </button>

                            </div>

                        </div>


                        {/* =================================
                            PRODUCTOS
                        ================================= */}

                        <div className="products-grid">


                            {/* CARGANDO */}

                            {loadingProducts && (

                                <p>
                                    Cargando productos...
                                </p>

                            )}


                            {/* ERROR */}

                            {!loadingProducts &&
                                productsError && (

                                    <p>
                                        {productsError}
                                    </p>

                                )}


                            {/* SIN PRODUCTOS */}

                            {!loadingProducts &&
                                !productsError &&
                                filteredProducts.length ===
                                    0 && (

                                    <p>
                                        No hay productos
                                        disponibles.
                                    </p>

                                )}


                            {/* PRODUCTOS REALES */}

                            {!loadingProducts &&
                                !productsError &&
                                filteredProducts.map(
                                    (product) => (

                                        <div
                                            className="product-card"
                                            key={product.id}
                                        >

                                            <h3>
                                                {product.name}
                                            </h3>

                                            <p>
                                                $
                                                {Number(
                                                    product.unitPrice
                                                ).toLocaleString(
                                                    "es-CO"
                                                )}
                                            </p>

                                            <button
                                                onClick={() =>
                                                    handleAddToCart(
                                                        product
                                                    )
                                                }
                                            >
                                                Agregar
                                            </button>

                                        </div>

                                    )
                                )}

                        </div>

                    </section>


                    {/* =================================
                        CARRITO
                    ================================= */}

                    <aside className="cart-section">


                        <div className="cart-header">

                            <h2>
                                Carrito
                            </h2>

                        </div>


                        {/* PRODUCTOS DEL CARRITO */}

                        {cart.length === 0 ? (

                            <div className="cart-empty">

                                <p>
                                    Aún no hay productos.
                                </p>

                            </div>

                        ) : (

                            <div className="cart-items">

                                {cart.map((item) => (

                                    <div
                                        className="cart-item"
                                        key={item.productId}
                                    >

                                        <div>

                                            <strong>
                                                {item.name}
                                            </strong>

                                            <p>
                                                $
                                                {Number(
                                                    item.price
                                                ).toLocaleString(
                                                    "es-CO"
                                                )}
                                            </p>

                                        </div>

                                        <div>

                                            <span>
                                                x
                                                {item.quantity}
                                            </span>

                                        </div>

                                    </div>

                                ))}

                            </div>

                        )}


                        {/* =================================
                            FOOTER DEL CARRITO
                        ================================= */}

                        <div className="cart-footer">

                            <div className="cart-total">

                                <span>
                                    Total
                                </span>

                                <strong>
                                    $
                                    {cartTotal.toLocaleString(
                                        "es-CO"
                                    )}
                                </strong>

                            </div>


                            <button
                                className="checkout-button"
                                disabled={
                                    cart.length === 0
                                }
                            >
                                Finalizar venta
                            </button>

                        </div>

                    </aside>

                </main>

            </div>

        </div>

    );
}

export default NewSalePage;