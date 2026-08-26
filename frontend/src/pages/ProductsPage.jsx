import { useEffect, useState } from "react";
import ProductForm from "../components/ProductForm";
import ProductService from "../services/ProductService";

function ProductsPage() {

    const [products, setProducts] = useState([]);

    const [selectedProduct, setSelectedProduct] =
        useState(null);

    const [showForm, setShowForm] =
        useState(false);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState("");


    const loadProducts = async () => {

        try {

            setLoading(true);
            setError("");

            const data =
                await ProductService.findAll();

            setProducts(data);

        } catch (error) {

            setError(
                error.message ||
                "No fue posible cargar los productos."
            );

        } finally {

            setLoading(false);
        }
    };


    useEffect(() => {

        loadProducts();

    }, []);


    const handleCreate = () => {

        setSelectedProduct(null);
        setShowForm(true);
    };


    const handleEdit = (product) => {

        setSelectedProduct(product);
        setShowForm(true);
    };


    const handleSuccess = async () => {

        setShowForm(false);
        setSelectedProduct(null);

        await loadProducts();
    };


    const handleCancel = () => {

        setShowForm(false);
        setSelectedProduct(null);
    };


    if (loading) {

        return (
            <div className="products-page">

                <h1>Productos</h1>

                <p>
                    Cargando productos...
                </p>

            </div>
        );
    }


    return (

        <div className="products-page">

            <div className="products-page-header">

                <div>
                    <h1>Productos</h1>

                    <p>
                        Crear y administrar productos.
                    </p>
                </div>

                <button
                    type="button"
                    onClick={handleCreate}
                >
                    Crear producto
                </button>

            </div>


            {error && (

                <div className="result error">
                    {error}
                </div>

            )}


            {showForm && (

                <ProductForm
                    product={selectedProduct}
                    onSuccess={handleSuccess}
                    onCancel={handleCancel}
                />

            )}


            {!showForm && (

                <div className="products-list">

                    {products.length === 0 ? (

                        <p>
                            No hay productos registrados.
                        </p>

                    ) : (

                        products.map((product) => (

                            <div
                                className="product-row"
                                key={product.id}
                            >

                                <div>

                                    <strong>
                                        {product.name}
                                    </strong>

                                    <p>
                                        Código:{" "}
                                        {product.internalCode}
                                    </p>

                                    <p>
                                        Código de barras:{" "}
                                        {product.barcode || "Sin código"}
                                    </p>

                                    <p>
                                        Precio:{" "}
                                        ${Number(
                                            product.unitPrice
                                        ).toLocaleString("es-CO")}
                                    </p>

                                </div>


                                <button
                                    type="button"
                                    onClick={() =>
                                        handleEdit(product)
                                    }
                                >
                                    Editar
                                </button>

                            </div>

                        ))

                    )}

                </div>

            )}

        </div>
    );
}

export default ProductsPage;