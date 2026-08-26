import { useEffect, useState } from "react";

import ProductService from "../services/ProductService";

function ProductForm({
    product = null,
    onSuccess,
    onCancel,
}) {

    const isEditing = Boolean(product);

    const [categories, setCategories] = useState([]);
    const [units, setUnits] = useState([]);

    const [loadingData, setLoadingData] = useState(true);
    const [loading, setLoading] = useState(false);

    const [result, setResult] = useState("");
    const [resultType, setResultType] = useState("");

    const [form, setForm] = useState({
        categoryId: "",
        unitId: "",
        barcode: "",
        name: "",
        purchasePrice: "",
        unitPrice: "",
        wholesalePrice: "",
        minimumWholesaleQuantity: "0",
        minimumStock: "",
        purchasable: true,
        manufacturable: false,
        active: true,
    });


    // =========================================
    // CARGAR CATEGORÍAS Y UNIDADES
    // =========================================

    useEffect(() => {

        const loadData = async () => {

            try {

                setLoadingData(true);
                setResult("");
                setResultType("");

                const [
                    categoriesData,
                    unitsData
                ] = await Promise.all([
                    ProductService.findCategories(),
                    ProductService.findUnits(),
                ]);

                setCategories(
                    categoriesData.filter(
                        (category) => category.active
                    )
                );

                setUnits(
                    unitsData.filter(
                        (unit) => unit.active
                    )
                );

            } catch (error) {

                setResult(
                    error.message ||
                    "No fue posible cargar las categorías y unidades."
                );

                setResultType("error");

            } finally {

                setLoadingData(false);
            }
        };

        loadData();

    }, []);


    // =========================================
    // CARGAR PRODUCTO EN MODO EDICIÓN
    // =========================================

    useEffect(() => {

        if (!product) {
            return;
        }

        setForm({
            categoryId: product.categoryId || "",
            unitId: product.unitId || "",
            barcode: product.barcode || "",
            name: product.name || "",
            purchasePrice: product.purchasePrice ?? "",
            unitPrice: product.unitPrice ?? "",
            wholesalePrice: product.wholesalePrice ?? "",
            minimumWholesaleQuantity:
                product.minimumWholesaleQuantity ?? "0",
            minimumStock: product.minimumStock ?? "",
            purchasable:
                product.purchasable ?? true,
            manufacturable:
                product.manufacturable ?? false,
            active:
                product.active ?? true,
        });

    }, [product]);


    // =========================================
    // CAMBIAR CAMPOS
    // =========================================

    const handleChange = (event) => {

        const {
            name,
            value,
            type,
            checked
        } = event.target;

        setForm((previous) => ({
            ...previous,

            [name]:
                type === "checkbox"
                    ? checked
                    : value,
        }));
    };


    // =========================================
    // ENVIAR FORMULARIO
    // =========================================

    const handleSubmit = async (event) => {

        event.preventDefault();

        setResult("");
        setResultType("");


        // =====================================
        // VALIDACIONES FRONTEND
        // =====================================

        if (!form.name.trim()) {

            setResult(
                "El nombre del producto es obligatorio."
            );

            setResultType("error");

            return;
        }

        if (!form.categoryId) {

            setResult(
                "Debes seleccionar una categoría."
            );

            setResultType("error");

            return;
        }

        if (!form.unitId) {

            setResult(
                "Debes seleccionar una unidad."
            );

            setResultType("error");

            return;
        }

        if (!form.unitPrice) {

            setResult(
                "El precio unitario es obligatorio."
            );

            setResultType("error");

            return;
        }


        // =====================================
        // PREPARAR DATOS
        // =====================================

        const request = {

            categoryId:
                form.categoryId,

            unitId:
                form.unitId,

            barcode:
                form.barcode.trim() || null,

            name:
                form.name.trim(),

            purchasePrice:
                form.purchasePrice === ""
                    ? null
                    : Number(form.purchasePrice),

            unitPrice:
                Number(form.unitPrice),

            wholesalePrice:
                form.wholesalePrice === ""
                    ? null
                    : Number(form.wholesalePrice),

            minimumWholesaleQuantity:
                Number(
                    form.minimumWholesaleQuantity || 0
                ),

            minimumStock:
                form.minimumStock === ""
                    ? null
                    : Number(form.minimumStock),

            purchasable:
                form.purchasable,

            manufacturable:
                form.manufacturable,

            active:
                form.active,
        };


        // =====================================
        // CREAR / EDITAR
        // =====================================

        try {

            setLoading(true);

            let savedProduct;

            if (isEditing) {

                savedProduct =
                    await ProductService.update(
                        product.id,
                        request
                    );

                setResult(
                    "Producto actualizado correctamente."
                );

            } else {

                savedProduct =
                    await ProductService.create(
                        request
                    );

                setResult(
                    `Producto creado correctamente. Código interno: ${savedProduct.internalCode}`
                );
            }

            setResultType("success");

            if (onSuccess) {
                onSuccess(savedProduct);
            }

        } catch (error) {

            setResult(
                error.message ||
                (
                    isEditing
                        ? "No fue posible actualizar el producto."
                        : "No fue posible crear el producto."
                )
            );

            setResultType("error");

        } finally {

            setLoading(false);
        }
    };


    // =========================================
    // CARGANDO DATOS
    // =========================================

    if (loadingData) {

        return (
            <div className="product-form">

                <p>
                    Cargando información del producto...
                </p>

            </div>
        );
    }


    // =========================================
    // FORMULARIO
    // =========================================

    return (

        <div className="product-form">

            <div className="product-form-header">

                <h2>
                    {isEditing
                        ? "Editar producto"
                        : "Crear producto"}
                </h2>

            </div>


            <form onSubmit={handleSubmit}>


                {/* =================================
                    NOMBRE
                ================================= */}

                <label htmlFor="productName">
                    Nombre
                </label>

                <input
                    id="productName"
                    name="name"
                    type="text"
                    value={form.name}
                    onChange={handleChange}
                    placeholder="Nombre del producto"
                    required
                />


                {/* =================================
                    CÓDIGO DE BARRAS
                ================================= */}

                <label htmlFor="barcode">

                    Código de barras

                    <span>
                        {" "} (opcional)
                    </span>

                </label>

                <input
                    id="barcode"
                    name="barcode"
                    type="text"
                    value={form.barcode}
                    onChange={handleChange}
                    placeholder="Código de barras"
                />


                {/* =================================
                    CATEGORÍA
                ================================= */}

                <label htmlFor="categoryId">
                    Categoría
                </label>

                <select
                    id="categoryId"
                    name="categoryId"
                    value={form.categoryId}
                    onChange={handleChange}
                    required
                >

                    <option value="">
                        Seleccionar categoría
                    </option>

                    {categories.map((category) => (

                        <option
                            key={category.id}
                            value={category.id}
                        >
                            {category.name}
                        </option>

                    ))}

                </select>


                {/* =================================
                    UNIDAD
                ================================= */}

                <label htmlFor="unitId">
                    Unidad
                </label>

                <select
                    id="unitId"
                    name="unitId"
                    value={form.unitId}
                    onChange={handleChange}
                    required
                >

                    <option value="">
                        Seleccionar unidad
                    </option>

                    {units.map((unit) => (

                        <option
                            key={unit.id}
                            value={unit.id}
                        >

                            {unit.name}

                            {unit.abbreviation
                                ? ` (${unit.abbreviation})`
                                : ""}

                        </option>

                    ))}

                </select>


                {/* =================================
                    PRECIO DE COMPRA
                ================================= */}

                <label htmlFor="purchasePrice">
                    Precio de compra
                </label>

                <input
                    id="purchasePrice"
                    name="purchasePrice"
                    type="number"
                    min="0"
                    step="0.01"
                    value={form.purchasePrice}
                    onChange={handleChange}
                    placeholder="0"
                />


                {/* =================================
                    PRECIO UNITARIO
                ================================= */}

                <label htmlFor="unitPrice">
                    Precio unitario
                </label>

                <input
                    id="unitPrice"
                    name="unitPrice"
                    type="number"
                    min="0"
                    step="0.01"
                    value={form.unitPrice}
                    onChange={handleChange}
                    placeholder="0"
                    required
                />


                {/* =================================
                    PRECIO MAYORISTA
                ================================= */}

                <label htmlFor="wholesalePrice">
                    Precio mayorista
                </label>

                <input
                    id="wholesalePrice"
                    name="wholesalePrice"
                    type="number"
                    min="0"
                    step="0.01"
                    value={form.wholesalePrice}
                    onChange={handleChange}
                    placeholder="0"
                />


                {/* =================================
                    CANTIDAD MÍNIMA MAYORISTA
                ================================= */}

                <label htmlFor="minimumWholesaleQuantity">

                    Cantidad mínima mayorista

                </label>

                <input
                    id="minimumWholesaleQuantity"
                    name="minimumWholesaleQuantity"
                    type="number"
                    min="0"
                    step="1"
                    value={form.minimumWholesaleQuantity}
                    onChange={handleChange}
                />


                {/* =================================
                    STOCK MÍNIMO
                ================================= */}

                <label htmlFor="minimumStock">
                    Stock mínimo
                </label>

                <input
                    id="minimumStock"
                    name="minimumStock"
                    type="number"
                    min="0"
                    step="0.01"
                    value={form.minimumStock}
                    onChange={handleChange}
                    placeholder="0"
                />


                {/* =================================
                    OPCIONES
                ================================= */}

                <label>

                    <input
                        type="checkbox"
                        name="purchasable"
                        checked={form.purchasable}
                        onChange={handleChange}
                    />

                    Producto comprable

                </label>


                <label>

                    <input
                        type="checkbox"
                        name="manufacturable"
                        checked={form.manufacturable}
                        onChange={handleChange}
                    />

                    Producto fabricable

                </label>


                {isEditing && (

                    <label>

                        <input
                            type="checkbox"
                            name="active"
                            checked={form.active}
                            onChange={handleChange}
                        />

                        Producto activo

                    </label>

                )}


                {/* =================================
                    BOTONES
                ================================= */}

                <div className="product-form-actions">

                    <button
                        type="submit"
                        disabled={loading}
                    >

                        {loading
                            ? (
                                isEditing
                                    ? "Guardando..."
                                    : "Creando..."
                            )
                            : (
                                isEditing
                                    ? "Guardar cambios"
                                    : "Crear producto"
                            )}

                    </button>


                    {onCancel && (

                        <button
                            type="button"
                            onClick={onCancel}
                            disabled={loading}
                        >
                            Cancelar
                        </button>

                    )}

                </div>


                {/* =================================
                    RESULTADO
                ================================= */}

                {result && (

                    <div
                        className={`result ${resultType}`}
                    >
                        {result}
                    </div>

                )}

            </form>

        </div>
    );
}

export default ProductForm;