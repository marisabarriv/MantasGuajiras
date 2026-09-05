import { useEffect, useState } from "react";

import ProductService from "../services/ProductService";

function ProductForm({ product = null, onSuccess, onCancel }) {
  const isEditing = Boolean(product);

  const [categories, setCategories] = useState([]);
  const [units, setUnits] = useState([]);

  const [loadingData, setLoadingData] = useState(true);
  const [loading, setLoading] = useState(false);

  const [result, setResult] = useState("");
  const [resultType, setResultType] = useState("");

  const [form, setForm] = useState({
    categoryId: "",
    barcode: "",
    name: "",
    price: "",
    minimumStock: "",
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

        const [categoriesData, unitsData] = await Promise.all([
          ProductService.findCategories(),
          ProductService.findUnits(),
        ]);

        setCategories(categoriesData.filter((category) => category.active));

        setUnits(unitsData.filter((unit) => unit.active));
      } catch (error) {
        setResult(
          error.message || "No fue posible cargar las categorías y unidades.",
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

      barcode: product.barcode || "",

      name: product.name || "",

      price: product.unitPrice ?? product.purchasePrice ?? "",

      minimumStock: product.minimumStock ?? "",

      active: product.active ?? true,
    });
  }, [product]);

  // =========================================
  // CAMBIAR CAMPOS
  // =========================================

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target;

    setForm((previous) => ({
      ...previous,
      [name]: type === "checkbox" ? checked : value,
    }));

    setResult("");
    setResultType("");
  };

  // =========================================
  // DETERMINAR UNIDAD AUTOMÁTICAMENTE
  // =========================================

  const getCategoryType = () => {
    const category = categories.find((item) => item.id === form.categoryId);

    if (!category) {
      return null;
    }

    const categoryText = `${category.name || ""}`.toLowerCase().trim();

    if (categoryText.includes("tela") || categoryText.includes("fabric")) {
      return "fabric";
    }

    if (categoryText.includes("manta")) {
      return "blanket";
    }

    return null;
  };

  const getUnitForCategory = () => {
    const categoryType = getCategoryType();

    if (!categoryType) {
      return null;
    }

    if (categoryType === "fabric") {
      return (
        units.find((unit) => {
          const text = `${unit.name || ""} ${unit.abbreviation || ""}`
            .toLowerCase()
            .trim();

          return text.includes("metro") || text === "m" || text.includes("(m)");
        }) || null
      );
    }

    if (categoryType === "blanket") {
      return (
        units.find((unit) => {
          const text = `${unit.name || ""} ${unit.abbreviation || ""}`
            .toLowerCase()
            .trim();

          return (
            text.includes("unidad") || text === "#" || text.includes("(#)")
          );
        }) || null
      );
    }

    return null;
  };

  // =========================================
  // CONFIGURACIÓN SEGÚN CATEGORÍA
  // =========================================

  const categoryType = getCategoryType();

  const selectedUnit = getUnitForCategory();

  const stockStep = categoryType === "blanket" ? "1" : "0.01";

  // =========================================
  // ENVIAR FORMULARIO
  // =========================================

  const handleSubmit = async (event) => {
    event.preventDefault();

    setResult("");
    setResultType("");

    // =====================================
    // VALIDACIONES
    // =====================================

    if (!form.name.trim()) {
      setResult("El nombre del producto es obligatorio.");
      setResultType("error");
      return;
    }

    if (!form.categoryId) {
      setResult("Debes seleccionar una categoría.");
      setResultType("error");
      return;
    }

    if (!form.price) {
      setResult("El precio es obligatorio.");
      setResultType("error");
      return;
    }

    if (Number(form.price) <= 0) {
      setResult("El precio debe ser mayor que cero.");
      setResultType("error");
      return;
    }

    // =====================================
    // UNIDAD AUTOMÁTICA
    // =====================================

    const automaticUnit = getUnitForCategory();

    if (!automaticUnit) {
      setResult(
        "No fue posible determinar automáticamente la unidad para la categoría seleccionada. Verifica que exista una unidad de metros para telas y una unidad (#) para mantas.",
      );
      setResultType("error");
      return;
    }

    // =====================================
    // VALIDAR STOCK SEGÚN CATEGORÍA
    // =====================================

    if (form.minimumStock !== "" && Number(form.minimumStock) < 0) {
      setResult("El stock mínimo no puede ser negativo.");
      setResultType("error");
      return;
    }

    if (
      categoryType === "blanket" &&
      form.minimumStock !== "" &&
      !Number.isInteger(Number(form.minimumStock))
    ) {
      setResult("El stock mínimo de las mantas debe ser un número entero.");
      setResultType("error");
      return;
    }

    // =====================================
    // PREPARAR DATOS
    // =====================================

    const price = Number(form.price);

    const request = {
      categoryId: form.categoryId,

      unitId: automaticUnit.id,

      barcode: form.barcode.trim() || null,

      name: form.name.trim(),

      // El precio de compra y el precio
      // unitario representan el mismo concepto.
      purchasePrice: price,

      unitPrice: price,

      // Compatibilidad con ProductRequest
      // actual del backend.
      wholesalePrice: null,

      minimumWholesaleQuantity: 0,

      minimumStock: form.minimumStock === "" ? null : Number(form.minimumStock),

      // Compatibilidad con ProductRequest
      // actual del backend.
      purchasable: true,

      manufacturable: false,

      active: form.active,
    };

    // =====================================
    // CREAR / EDITAR
    // =====================================

    try {
      setLoading(true);

      let savedProduct;

      if (isEditing) {
        savedProduct = await ProductService.update(product.id, request);

        setResult("Producto actualizado correctamente.");
      } else {
        savedProduct = await ProductService.create(request);

        setResult(
          `Producto creado correctamente. Código interno: ${savedProduct.internalCode}`,
        );
      }

      setResultType("success");

      if (onSuccess) {
        onSuccess(savedProduct);
      }
    } catch (error) {
      setResult(
        error.message ||
          (isEditing
            ? "No fue posible actualizar el producto."
            : "No fue posible crear el producto."),
      );

      setResultType("error");
    } finally {
      setLoading(false);
    }
  };

  // =========================================
  // CARGANDO
  // =========================================

  if (loadingData) {
    return (
      <div className="product-form-loading">
        <p>Cargando información del producto...</p>
      </div>
    );
  }

  // =========================================
  // FORMULARIO
  // =========================================

  return (
    <div className="product-form">
      <form onSubmit={handleSubmit}>
        {/* =============================
                    INFORMACIÓN GENERAL
                   ============================= */}

        <div className="product-form-section">
          <div className="product-form-section-title">
            <h3>Información general</h3>
          </div>

          <div className="product-form-grid">
            <div className="form-group">
              <label htmlFor="productName">Nombre</label>

              <input
                id="productName"
                name="name"
                type="text"
                value={form.name}
                onChange={handleChange}
                placeholder="Nombre del producto"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="barcode">
                Código de barras
                <span className="optional"> (opcional)</span>
              </label>

              <input
                id="barcode"
                name="barcode"
                type="text"
                value={form.barcode}
                onChange={handleChange}
                placeholder="Código de barras"
              />
            </div>

            <div className="form-group">
              <label htmlFor="categoryId">Categoría</label>

              <select
                id="categoryId"
                name="categoryId"
                value={form.categoryId}
                onChange={handleChange}
                required
              >
                <option value="">Seleccionar categoría</option>

                {categories.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.name}
                  </option>
                ))}
              </select>
                </div>

                {/* =====================
                            UNIDAD AUTOMÁTICA
                           ===================== */}

              <small className="field-help">
                {selectedUnit
                  ? selectedUnit.abbreviation === "#"
                    ? "Esta categoría se maneja en unidades (#)."
                    : "Esta categoría se maneja en metros."
                  : ""}
              </small>
            </div>
        </div>

        {/* =============================
                    PRECIO
                   ============================= */}

        <div className="product-form-section">
          <div className="product-form-section-title">
            <h3>Precio</h3>
          </div>

          <div className="product-form-grid">
            <div className="form-group">
              <label htmlFor="price">Precio</label>

              <input
                id="price"
                name="price"
                type="number"
                min="0"
                step="0.01"
                value={form.price}
                onChange={handleChange}
                placeholder="0"
                required
              />

              {selectedUnit && (
                <small className="field-help">
                  Precio por {selectedUnit.name}.
                </small>
              )}
            </div>
          </div>
        </div>

        {/* =============================
                    INVENTARIO
                   ============================= */}

        <div className="product-form-section">
          <div className="product-form-section-title">
            <h3>Inventario</h3>
          </div>

          <div className="product-form-grid">
            <div className="form-group">
              <label htmlFor="minimumStock">Stock mínimo</label>

              <input
                id="minimumStock"
                name="minimumStock"
                type="number"
                min="0"
                step={stockStep}
                value={form.minimumStock}
                onChange={handleChange}
                placeholder="0"
              />

              {categoryType === "blanket" && (
                <small className="field-help">
                  Las mantas se manejan en unidades enteras (#).
                </small>
              )}

              {categoryType === "fabric" && (
                <small className="field-help">
                  Las telas se manejan en metros.
                </small>
              )}
            </div>
          </div>
        </div>

        {/* =============================
                    ESTADO
                   ============================= */}

        {isEditing && (
          <div className="product-form-section">
            <div className="product-form-section-title">
              <h3>Estado</h3>
            </div>

            <div className="product-form-options">
              <label className="checkbox-option">
                <input
                  type="checkbox"
                  name="active"
                  checked={form.active}
                  onChange={handleChange}
                />

                <span>Producto activo</span>
              </label>
            </div>
          </div>
        )}

        {/* =============================
                    RESULTADO
                   ============================= */}

        {result && <div className={`result ${resultType}`}>{result}</div>}

        {/* =============================
                    BOTONES
                   ============================= */}

        <div className="product-form-actions">
          <button
            type="button"
            className="product-cancel-button"
            onClick={onCancel}
            disabled={loading}
          >
            Cancelar
          </button>

          <button
            type="submit"
            className="product-submit-button"
            disabled={loading}
          >
            {loading
              ? isEditing
                ? "Guardando..."
                : "Creando..."
              : isEditing
                ? "Guardar cambios"
                : "Crear producto"}
          </button>
        </div>
      </form>
    </div>
  );
}

export default ProductForm;
