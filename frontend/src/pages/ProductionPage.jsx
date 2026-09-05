import { useEffect, useState } from "react";

import ProductService from "../services/ProductService";
import ProductionService from "../services/ProductionService";
import Modal from "../components/Modal";


function ProductionPage() {

    const [productions, setProductions] =
        useState([]);

    const [products, setProducts] =
        useState([]);

    const [showForm, setShowForm] =
        useState(false);

    const [loading, setLoading] =
        useState(true);

    const [formLoading, setFormLoading] =
        useState(false);

    const [error, setError] =
        useState("");

    const [formError, setFormError] =
        useState("");

    const [success, setSuccess] =
        useState("");


    const [form, setForm] = useState({

        fabricProductId: "",

        fabricQuantityPerUnit: "",

        outputProductId: "",

        outputQuantity: "",

        observations: "",
    });


    const loadData = async () => {

        try {

            setLoading(true);
            setError("");

            const [
                productionData,
                productData
            ] = await Promise.all([

                ProductionService.findAll(),

                ProductService.findAll(),

            ]);

            setProductions(
                productionData
            );

            setProducts(
                productData
            );

        } catch (error) {

            setError(
                error.message ||
                "No fue posible cargar la información de producción."
            );

        } finally {

            setLoading(false);
        }
    };


    useEffect(() => {

        loadData();

    }, []);


    const resetForm = () => {

        setForm({

            fabricProductId: "",

            fabricQuantityPerUnit: "",

            outputProductId: "",

            outputQuantity: "",

            observations: "",
        });

        setFormError("");
    };


    const handleNewProduction = () => {

        resetForm();

        setSuccess("");

        setShowForm(true);
    };


    const handleCancel = () => {

        setShowForm(false);

        resetForm();
    };


    const handleChange = (event) => {

        const {
            name,
            value
        } = event.target;


        setForm((previous) => ({

            ...previous,

            [name]: value,

        }));


        setFormError("");
        setSuccess("");
    };


    const handleSubmit = async (event) => {

        event.preventDefault();

        setFormError("");
        setSuccess("");


        if (!form.fabricProductId) {

            setFormError(
                "Debes seleccionar el tipo de tela."
            );

            return;
        }


        if (
            form.fabricQuantityPerUnit === "" ||
            Number(
                form.fabricQuantityPerUnit
            ) <= 0
        ) {

            setFormError(
                "La cantidad de tela por manta debe ser mayor que 0."
            );

            return;
        }


        if (!form.outputProductId) {

            setFormError(
                "Debes seleccionar el tipo de manta."
            );

            return;
        }


        if (
            form.outputQuantity === "" ||
            !Number.isInteger(
                Number(
                    form.outputQuantity
                )
            ) ||
            Number(
                form.outputQuantity
            ) <= 0
        ) {

            setFormError(
                "La cantidad de mantas debe ser un número entero mayor que 0."
            );

            return;
        }


        if (
            form.fabricProductId ===
            form.outputProductId
        ) {

            setFormError(
                "La tela y la manta producida no pueden ser el mismo producto."
            );

            return;
        }


        try {

            setFormLoading(true);


            const production = {

                fabricProductId:
                    form.fabricProductId,

                fabricQuantityPerUnit:
                    Number(
                        form.fabricQuantityPerUnit
                    ),

                outputProductId:
                    form.outputProductId,

                outputQuantity:
                    Number(
                        form.outputQuantity
                    ),

                observations:
                    form.observations.trim() ||
                    null,
            };


            await ProductionService.create(
                production
            );


            setShowForm(false);

            resetForm();

            setSuccess(
                "La producción fue registrada correctamente."
            );


            await loadData();

        } catch (error) {

            setFormError(
                error.message ||
                "No fue posible registrar la producción."
            );

        } finally {

            setFormLoading(false);
        }
    };


    const formatQuantity = (
        quantity
    ) => {

        return Number(
            quantity || 0
        ).toLocaleString(
            "es-CO",
            {
                maximumFractionDigits: 2,
            }
        );
    };


    const formatDate = (
        date
    ) => {

        if (!date) {
            return "Sin fecha";
        }

        return new Date(date)
            .toLocaleString("es-CO");
    };


    const fabricProduct =
        products.find(
            (product) =>
                product.id ===
                form.fabricProductId
        );


    const outputProduct =
        products.find(
            (product) =>
                product.id ===
                form.outputProductId
        );


    const totalFabric =
        Number(
            form.fabricQuantityPerUnit || 0
        ) *
        Number(
            form.outputQuantity || 0
        );


    if (loading) {

        return (

            <div className="production-page">

                <h1>
                    Producción
                </h1>

                <p>
                    Cargando producción...
                </p>

            </div>
        );
    }


    return (

        <div className="production-page">

            <div className="production-page-header">

                <div>

                    <h1>
                        Producción
                    </h1>

                    <p>
                        Registrar y consultar las producciones realizadas.
                    </p>

                </div>


                <button
                    type="button"
                    onClick={
                        handleNewProduction
                    }
                >
                    + Nueva
                </button>

            </div>


            {error && (

                <div className="result error">
                    {error}
                </div>

            )}


            {success && (

                <div className="result success">
                    {success}
                </div>

            )}


            <Modal
                open={showForm}
                onClose={handleCancel}
                title="Nueva producción"
            >

                {formError && (

                    <div className="result error">
                        {formError}
                    </div>

                )}


                <form
                    onSubmit={handleSubmit}
                    className="production-form"
                >

                    <div className="form-group">

                        <label htmlFor="fabricProductId">
                            Tipo de tela
                        </label>


                        <select
                            id="fabricProductId"
                            name="fabricProductId"
                            value={
                                form.fabricProductId
                            }
                            onChange={
                                handleChange
                            }
                            disabled={
                                formLoading
                            }
                        >

                            <option value="">
                                Selecciona una tela
                            </option>


                            {products
                                .filter(
                                    (product) =>
                                        product.active !== false &&
                                        product.purchasable === true
                                )
                                .map(
                                    (product) => (

                                        <option
                                            key={
                                                product.id
                                            }
                                            value={
                                                product.id
                                            }
                                        >
                                            {product.name}
                                        </option>

                                    )
                                )}

                        </select>

                    </div>


                    <div className="form-group">

                        <label htmlFor="fabricQuantityPerUnit">
                            Tela necesaria por manta
                        </label>


                        <input
                            id="fabricQuantityPerUnit"
                            name="fabricQuantityPerUnit"
                            type="number"
                            min="0.01"
                            step="0.01"
                            value={
                                form.fabricQuantityPerUnit
                            }
                            onChange={
                                handleChange
                            }
                            placeholder="Ej. 1.50"
                            disabled={
                                formLoading
                            }
                        />


                        <small>
                            Puedes utilizar valores decimales.
                        </small>

                    </div>


                    <div className="form-group">

                        <label htmlFor="outputProductId">
                            Tipo de manta
                        </label>


                        <select
                            id="outputProductId"
                            name="outputProductId"
                            value={
                                form.outputProductId
                            }
                            onChange={
                                handleChange
                            }
                            disabled={
                                formLoading
                            }
                        >

                            <option value="">
                                Selecciona una manta
                            </option>


                            {products
                                .filter(
                                    (product) =>
                                        product.active !== false &&
                                        product.manufacturable === true
                                )
                                .map(
                                    (product) => (

                                        <option
                                            key={
                                                product.id
                                            }
                                            value={
                                                product.id
                                            }
                                        >
                                            {product.name}
                                        </option>

                                    )
                                )}

                        </select>

                    </div>


                    <div className="form-group">

                        <label htmlFor="outputQuantity">
                            Cantidad de mantas
                        </label>


                        <input
                            id="outputQuantity"
                            name="outputQuantity"
                            type="number"
                            min="1"
                            step="1"
                            value={
                                form.outputQuantity
                            }
                            onChange={
                                handleChange
                            }
                            placeholder="Ej. 8"
                            disabled={
                                formLoading
                            }
                        />


                        <small>
                            La cantidad debe ser un número entero.
                        </small>

                    </div>


                    <div className="production-calculation">

                        <strong>
                            Tela total a utilizar
                        </strong>


                        <p>

                            {formatQuantity(
                                totalFabric
                            )}

                            {fabricProduct?.unit?.abbreviation
                                ? ` ${fabricProduct.unit.abbreviation}`
                                : ""}

                        </p>


                        {form.fabricQuantityPerUnit &&
                        form.outputQuantity && (

                            <small>

                                {formatQuantity(
                                    form.fabricQuantityPerUnit
                                )}

                                {fabricProduct?.unit?.abbreviation
                                    ? ` ${fabricProduct.unit.abbreviation}`
                                    : ""}

                                {" × "}

                                {formatQuantity(
                                    form.outputQuantity
                                )}

                                {" manta(s)"}

                            </small>

                        )}

                    </div>


                    <div className="form-group">

                        <label htmlFor="observations">
                            Observaciones
                        </label>


                        <textarea
                            id="observations"
                            name="observations"
                            value={
                                form.observations
                            }
                            onChange={
                                handleChange
                            }
                            placeholder="Observaciones opcionales..."
                            rows="4"
                            disabled={
                                formLoading
                            }
                        />

                    </div>


                    {outputProduct && (

                        <div className="production-summary">

                            <p>

                                Se producirán{" "}

                                <strong>
                                    {formatQuantity(
                                        form.outputQuantity
                                    )}
                                </strong>

                                {" unidad(es) de "}

                                <strong>
                                    {outputProduct.name}
                                </strong>

                                {" utilizando "}

                                <strong>
                                    {formatQuantity(
                                        totalFabric
                                    )}
                                </strong>

                                {" "}

                                {fabricProduct?.unit?.abbreviation ||
                                    "unidad(es)"}

                                {" de "}

                                <strong>
                                    {fabricProduct?.name}
                                </strong>
                                .

                            </p>

                        </div>

                    )}


                    <div className="production-form-actions">

                        <button
                            type="submit"
                            disabled={
                                formLoading
                            }
                        >
                            {formLoading
                                ? "Registrando..."
                                : "Registrar producción"}
                        </button>


                        <button
                            type="button"
                            onClick={
                                handleCancel
                            }
                            disabled={
                                formLoading
                            }
                        >
                            Cancelar
                        </button>

                    </div>

                </form>

            </Modal>


            <div className="production-list">

                <h2>
                    Historial de producciones
                </h2>


                {productions.length === 0 ? (

                    <p>
                        No hay producciones registradas.
                    </p>

                ) : (

                    productions.map(
                        (production) => (

                            <div
                                className="production-row"
                                key={
                                    production.id
                                }
                            >

                                <div>

                                    <strong>
                                        Producción
                                    </strong>


                                    <p>
                                        Fecha:{" "}
                                        {formatDate(
                                            production.createdAt
                                        )}
                                    </p>


                                    {production.items &&
                                    production.items.length > 0 ? (

                                        production.items.map(
                                            (
                                                item,
                                                index
                                            ) => (

                                                <p
                                                    key={
                                                        item.id ||
                                                        index
                                                    }
                                                >

                                                    {item.type ===
                                                    "INPUT"
                                                        ? "Tela utilizada: "
                                                        : "Mantas producidas: "}

                                                    {item.productName ||
                                                        item.product?.name ||
                                                        "Producto"}

                                                    {" — "}

                                                    {formatQuantity(
                                                        item.quantity
                                                    )}

                                                </p>

                                            )
                                        )

                                    ) : (

                                        <p>
                                            Sin detalle disponible.
                                        </p>

                                    )}

                                </div>

                            </div>

                        )
                    )

                )}

            </div>

        </div>
    );
}


export default ProductionPage;
