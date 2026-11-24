"""Prueba de carga: N usuarios simultáneos haciendo lo que hace la tienda.

NUNCA contra la base de trabajo (registra ventas y ajustes de verdad).
Ver docs/12-rendimiento-y-monitoreo.md §5.

Uso: python3 load.py BASE_URL SEGUNDOS USUARIOS [sales_query]
Vendedores (80 %): ver productos, inventario, categorías, registrar ventas.
Administradores (20 %): historial de ventas, movimientos, personalizadas."""
import http.client, json, os, random, sys, threading, time, uuid, urllib.parse
base = urllib.parse.urlparse(sys.argv[1]); dur = float(sys.argv[2]); users = int(sys.argv[3])
sales_q = sys.argv[4] if len(sys.argv) > 4 else ""
lock = threading.Lock(); stats = {}

def conn(): return http.client.HTTPConnection(base.hostname, base.port, timeout=60)

def call(c, method, path, token=None, body=None, name=None):
    h = {"Content-Type": "application/json"}
    if token: h["Authorization"] = "Bearer " + token
    t = time.perf_counter()
    try:
        c.request(method, path, body=json.dumps(body) if body is not None else None, headers=h)
        r = c.getresponse(); data = r.read(); status = r.status
    except Exception as e:
        data, status = b"", "ERR:" + type(e).__name__
    ms = (time.perf_counter() - t) * 1000
    with lock: stats.setdefault(name or path, []).append((ms, status, len(data)))
    return status, data

def login(c, user, pw):
    s, d = call(c, "POST", "/auth/login", body={"identifier": user, "password": pw}, name="POST /auth/login")
    return json.loads(d)["token"]

c0 = conn(); admin = login(c0, os.environ.get("PERF_ADMIN_USER", "adminperf"),
                           os.environ.get("PERF_ADMIN_PASSWORD", "Prueba123"))
_, d = call(c0, "GET", "/api/products", admin, name="setup"); products = [p for p in json.loads(d) if p.get("active", True)]
blankets = [p for p in products if p.get("unitAbbreviation", "#") == "#" or "Manta" in p["name"]]
stop = time.time() + dur

def seller(i):
    c = conn(); tok = login(c, f"vend{1 + i % 5}", "Clave123")
    while time.time() < stop:
        call(c, "GET", "/api/products", tok, name="GET /api/products")
        call(c, "GET", "/api/inventory", tok, name="GET /api/inventory")
        call(c, "GET", "/api/product-categories", tok, name="GET /api/product-categories")
        p = random.choice(blankets)
        s, _ = call(c, "POST", "/api/sales", tok, {"clientRequestId": str(uuid.uuid4()),
                    "items": [{"productId": p["id"], "quantity": 1}]}, name="POST /api/sales")
        if s in (400, 409):  # sin existencias: reponer como hace la tienda
            call(c, "POST", "/api/inventory/adjustments", tok, {"productId": p["id"], "countedQuantity": 100,
                 "reason": "Reposición prueba de carga"}, name="POST ajuste")
        time.sleep(random.uniform(0.5, 1.5))  # tiempo de "pensar" del vendedor

def administrator(i):
    c = conn(); tok = admin
    while time.time() < stop:
        call(c, "GET", "/api/sales" + sales_q, tok, name="GET /api/sales")
        call(c, "GET", "/api/orders", tok, name="GET /api/orders")
        call(c, "GET", "/api/inventory-movements", tok, name="GET /api/inventory-movements")
        call(c, "GET", f"/api/inventory-movements?productId={random.choice(products)['id']}", tok,
             name="GET movimientos de un producto")
        time.sleep(random.uniform(1, 3))

ts = [threading.Thread(target=administrator if i % 5 == 4 else seller, args=(i,)) for i in range(users)]
t0 = time.time(); [t.start() for t in ts]; [t.join() for t in ts]
print(f"{users} usuarios, {time.time()-t0:.0f} s")
print(f"{'endpoint':38} {'n':>5} {'p50':>7} {'p95':>7} {'max':>7} {'err':>4} {'KB med':>8}")
def pct(v, q): v = sorted(v); return v[min(len(v)-1, int(q * len(v)))]
for k, v in sorted(stats.items()):
    if k == "setup": continue
    ms = [x[0] for x in v]; err = sum(1 for x in v if not (isinstance(x[1], int) and x[1] < 300))
    print(f"{k:38} {len(v):5} {pct(ms,.5):7.0f} {pct(ms,.95):7.0f} {max(ms):7.0f} {err:4} {sum(x[2] for x in v)/len(v)/1024:8.0f}")
codes = {}
for v in stats.values():
    for x in v: codes[x[1]] = codes.get(x[1], 0) + 1
print("códigos:", codes)
