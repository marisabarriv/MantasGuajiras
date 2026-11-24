"""Catálogo y vendedores de prueba para la prueba de carga.

NUNCA contra la base de trabajo: solo contra una base vacía creada para
la prueba (ver docs/12-rendimiento-y-monitoreo.md §5).

Uso: python3 seed.py http://localhost:8095
Variables opcionales: PERF_ADMIN_USER (adminperf), PERF_ADMIN_PASSWORD (Prueba123).
"""
import json, os, urllib.request, sys
API = sys.argv[1]
ADMIN = os.environ.get("PERF_ADMIN_USER", "adminperf")
PASSWORD = os.environ.get("PERF_ADMIN_PASSWORD", "Prueba123")
def call(path, method="GET", body=None, token=None):
    req = urllib.request.Request(API + path, method=method, data=None if body is None else json.dumps(body).encode(),
        headers={"Content-Type": "application/json", **({"Authorization": "Bearer " + token} if token else {})})
    with urllib.request.urlopen(req) as r:
        t = r.read().decode(); return json.loads(t) if t else {}
tok = call("/auth/login", "POST", {"identifier": ADMIN, "password": PASSWORD})["token"]
cats = {c["name"]: c["id"] for c in call("/api/product-categories", token=tok)}
units = {u["abbreviation"]: u["id"] for u in call("/api/unit", token=tok)}
# Catálogo realista: 40 mantas y 12 telas.
for i in range(40):
    call("/api/products", "POST", {"categoryId": cats["Manta"], "unitId": units["#"], "name": f"Manta modelo {i+1:02d}",
        "unitPrice": 60000 + 1000 * i, "minimumWholesaleQuantity": 0, "minimumStock": 3, "purchasable": True,
        "manufacturable": False, "active": True, "initialStock": 50}, tok)
for i in range(12):
    call("/api/products", "POST", {"categoryId": cats["Tela"], "unitId": units["m"], "name": f"Tela color {i+1:02d}",
        "unitPrice": 18000 + 500 * i, "minimumWholesaleQuantity": 0, "minimumStock": 10, "purchasable": True,
        "manufacturable": False, "active": True, "initialStock": 200}, tok)
for i in range(5):
    call("/users", "POST", {"username": f"vend{i+1}", "phone": f"30012345{i:02d}", "password": "Clave123", "role": "SELLER", "active": True}, tok)
print("catálogo y vendedores listos")
