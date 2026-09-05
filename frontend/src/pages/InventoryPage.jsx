import { useEffect, useState } from "react";
import InventoryService from "../services/InventoryService";

function InventoryPage() {

    const [inventory, setInventory] = useState([]);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState("");


    const loadInventory = async () => {

        try {

            setLoading(true);
            setError("");

            const data =
                await InventoryService.findAll();

            setInventory(data);

        } catch (error) {

            setError(
                error.message ||
                "No fue posible cargar el inventario."
            );

        } finally {

            setLoading(false);
        }
    };


    useEffect(() => {

        loadInventory();

    }, []);


    if (loading) {

        return (
            <div className="inventory-page">

                <h1>Inventario</h1>

                <p>
                    Cargando inventario...
                </p>

            </div>
        );
    }


    return (

        <div className="inventory-page">

            <div className="inventory-page-header">

                <div>

                    <h1>Inventario</h1>

                    <p>
                        Consulta las existencias actuales
                        de los productos.
                    </p>

                </div>

            </div>


            {error && (

                <div className="result error">
                    {error}
                </div>

            )}


            {inventory.length === 0 ? (

                <p>
                    No hay registros de inventario.
                </p>

            ) : (

                <div className="inventory-list">

                    {inventory.map((item) => (

                        <div
                            className="inventory-row"
                            key={item.productId}
                        >

                            <div>

                                <strong>
                                    Producto
                                </strong>

                                <p>
                                    ID: {item.productId}
                                </p>

                            </div>


                            <div>

                                <strong>
                                    Cantidad
                                </strong>

                                <p>
                                    {Number(item.quantity)}
                                </p>

                            </div>


                            <div>

                                <strong>
                                    Última actualización
                                </strong>

                                <p>
                                    {item.updatedAt
                                        ? new Date(
                                            item.updatedAt
                                        ).toLocaleString("es-CO")
                                        : "Sin información"}
                                </p>

                            </div>

                        </div>

                    ))}

                </div>

            )}

        </div>
    );
}

export default InventoryPage;