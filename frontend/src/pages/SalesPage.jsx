import { useEffect, useState } from "react";
import SaleService from "../services/SaleService";

function SalesPage() {

    const [sales, setSales] = useState([]);

    const [selectedSale, setSelectedSale] =
        useState(null);

    const [loading, setLoading] =
        useState(true);

    const [detailLoading, setDetailLoading] =
        useState(false);

    const [error, setError] =
        useState("");

    const [detailError, setDetailError] =
        useState("");

    const [deleteLoading, setDeleteLoading] =
        useState(false);


    const token = localStorage.getItem("token");

    const getUserRole = () => {

        if (!token) {
            return null;
        }

        try {

            const payload =
                JSON.parse(
                    atob(
                        token.split(".")[1]
                            .replace(/-/g, "+")
                            .replace(/_/g, "/")
                    )
                );

            return payload.role || payload.authorities?.[0];

        } catch {

            return null;
        }
    };


    const isAdmin =
        getUserRole() === "ADMIN";


    const loadSales = async () => {

        try {

            setLoading(true);
            setError("");

            const data =
                await SaleService.findAll();

            setSales(data);

        } catch (error) {

            setError(
                error.message ||
                "No fue posible cargar las ventas."
            );

        } finally {

            setLoading(false);
        }
    };


    useEffect(() => {

        loadSales();

    }, []);


    const handleViewDetail = async (id) => {

        try {

            setDetailLoading(true);
            setDetailError("");
            setSelectedSale(null);

            const data =
                await SaleService.findById(id);

            setSelectedSale(data);

        } catch (error) {

            setDetailError(
                error.message ||
                "No fue posible cargar el detalle de la venta."
            );

        } finally {

            setDetailLoading(false);
        }
    };


    const handleCloseDetail = () => {

        setSelectedSale(null);
        setDetailError("");
    };


    const handleDelete = async (id) => {

        const confirmed =
            window.confirm(
                "¿Estás seguro de que deseas eliminar esta venta?"
            );

        if (!confirmed) {
            return;
        }

        try {

            setDeleteLoading(true);
            setError("");

            await SaleService.delete(id);

            setSelectedSale(null);

            await loadSales();

        } catch (error) {

            setError(
                error.message ||
                "No fue posible eliminar la venta."
            );

        } finally {

            setDeleteLoading(false);
        }
    };


    if (loading) {

        return (
            <div className="sales-page">

                <h1>Ventas</h1>

                <p>
                    Cargando ventas...
                </p>

            </div>
        );
    }


    return (

        <div className="sales-page">

            <div className="sales-page-header">

                <div>

                    <h1>Ventas</h1>

                    <p>
                        Consultar las ventas registradas.
                    </p>

                </div>

            </div>


            {error && (

                <div className="result error">
                    {error}
                </div>

            )}


            {detailError && (

                <div className="result error">
                    {detailError}
                </div>

            )}


            {detailLoading && (

                <div className="sale-detail-loading">

                    <p>
                        Cargando detalle de la venta...
                    </p>

                </div>

            )}


            {selectedSale && !detailLoading && (

                <div className="sale-detail">

                    <div className="sale-detail-header">

                        <div>

                            <h2>
                                Detalle de venta
                            </h2>

                            <p>
                                ID: {selectedSale.id}
                            </p>

                        </div>

                        <div>

                            <button
                                type="button"
                                onClick={handleCloseDetail}
                                disabled={deleteLoading}
                            >
                                Cerrar
                            </button>

                            {isAdmin && (

                                <button
                                    type="button"
                                    onClick={() =>
                                        handleDelete(selectedSale.id)
                                    }
                                    disabled={deleteLoading}
                                >
                                    {deleteLoading
                                        ? "Eliminando..."
                                        : "Eliminar venta"}
                                </button>

                            )}

                        </div>

                    </div>


                    <div className="sale-detail-info">

                        <p>
                            <strong>Total:</strong>{" "}
                            ${Number(
                                selectedSale.total
                            ).toLocaleString("es-CO")}
                        </p>

                        <p>
                            <strong>Observaciones:</strong>{" "}
                            {selectedSale.observations ||
                                "Sin observaciones"}
                        </p>

                        <p>
                            <strong>Creada:</strong>{" "}
                            {selectedSale.createdAt
                                ? new Date(
                                    selectedSale.createdAt
                                ).toLocaleString("es-CO")
                                : "Sin fecha"}
                        </p>

                        <p>
                            <strong>Actualizada:</strong>{" "}
                            {selectedSale.updatedAt
                                ? new Date(
                                    selectedSale.updatedAt
                                ).toLocaleString("es-CO")
                                : "Sin fecha"}
                        </p>

                    </div>


                    <div className="sale-items">

                        <h3>
                            Productos
                        </h3>


                        {!selectedSale.items ||
                        selectedSale.items.length === 0 ? (

                            <p>
                                Esta venta no tiene productos registrados.
                            </p>

                        ) : (

                            selectedSale.items.map((item, index) => (

                                <div
                                    className="sale-item-row"
                                    key={item.id || index}
                                >

                                    <div>

                                        <strong>
                                            {item.productName}
                                        </strong>

                                        <p>
                                            Cantidad:{" "}
                                            {item.quantity}
                                        </p>

                                        <p>
                                            Precio unitario:{" "}
                                            ${Number(
                                                item.unitPrice
                                            ).toLocaleString("es-CO")}
                                        </p>

                                        <p>
                                            Descuento:{" "}
                                            {Number(
                                                item.discountPercentage || 0
                                            ).toLocaleString("es-CO")}
                                            %
                                        </p>

                                        <p>
                                            Precio final unitario:{" "}
                                            ${Number(
                                                item.finalUnitPrice
                                            ).toLocaleString("es-CO")}
                                        </p>

                                    </div>


                                    <div>

                                        <strong>
                                            Subtotal
                                        </strong>

                                        <p>
                                            ${Number(
                                                item.subtotal
                                            ).toLocaleString("es-CO")}
                                        </p>

                                    </div>

                                </div>

                            ))

                        )}

                    </div>

                </div>

            )}


            {!selectedSale && !detailLoading && (

                <div className="sales-list">

                    {sales.length === 0 ? (

                        <p>
                            No hay ventas registradas.
                        </p>

                    ) : (

                        sales.map((sale) => (

                            <div
                                className="sale-row"
                                key={sale.id}
                            >

                                <div>

                                    <strong>
                                        Venta
                                    </strong>

                                    <p>
                                        ID: {sale.id}
                                    </p>

                                    <p>
                                        Total:{" "}
                                        ${Number(
                                            sale.total
                                        ).toLocaleString("es-CO")}
                                    </p>

                                    <p>
                                        Fecha:{" "}
                                        {sale.createdAt
                                            ? new Date(
                                                sale.createdAt
                                            ).toLocaleString("es-CO")
                                            : "Sin fecha"}
                                    </p>

                                </div>


                                <button
                                    type="button"
                                    onClick={() =>
                                        handleViewDetail(sale.id)
                                    }
                                >
                                    Ver detalle
                                </button>

                            </div>

                        ))

                    )}

                </div>

            )}

        </div>
    );
}

export default SalesPage;