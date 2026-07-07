# M MOTOS CORE — Contexto para Gemini

## Proyecto
Sistema POS para comercio de autopartes de motos en Argentina.
Arquitectura hexagonal. Local en Windows 10.

## Rutas
- Proyecto: `C:\Users\fabri\Desktop\xavier\skills\AplicacionesJava\MMotosApp`
- Frontend: `C:\Users\fabri\Desktop\xavier\skills\AplicacionesJava\MMotosApp\frontend`
- Java 21: `C:\Java\jdk-21`
- Maven 3.9.9: `C:\Java\maven`
- PostgreSQL 16: `C:\Program Files\PostgreSQL\16`

## Stack
- Backend: Java 21 + Spring Boot 3.3.5 + Maven
- DB: PostgreSQL 16, base: `mmotos`, user: `postgres`, pass: `G-x-m-11`
- Frontend: React 18 + TypeScript + Vite 5 + Tailwind 3 + TanStack Query v5 + Zustand
- Auth: JWT (JJWT 0.12.6), roles CAJERO / DUENO

## Estado actual (11/05/2026)
- JAR generado: `target/mmotos-app-1.0.0-SNAPSHOT.jar`
- App corriendo en: `http://localhost:8080`
- Flyway ejecutó V1 (DDL) + V2 (seed usuarios)
- Rutas `/`, `/pos`, `/assets/**` responden 200 OK

## Comandos para levantar
```bat
# Levantar backend (CMD)
"C:\Java\jdk-21\bin\java.exe" -jar "C:\Users\fabri\Desktop\xavier\skills\AplicacionesJava\MMotosApp\target\mmotos-app-1.0.0-SNAPSHOT.jar" --spring.datasource.password=G-x-m-11

# Rebuild frontend + JAR
cd frontend && npm run build
cd .. && "C:\Java\maven\bin\mvn.cmd" clean package -Dmaven.test.skip=true
```

## Credenciales de acceso
| Usuario | Contraseña | Rol    |
|---------|------------|--------|
| admin   | admin123   | DUENO  |
| cajero  | cajero123  | CAJERO |

## Endpoints API disponibles
- `POST /api/auth/login` → `{ token, username, rol, usuarioId }`
- `GET /api/productos/buscar?termino=` → `ProductoDTO[]`
- `GET /api/productos/{id}` → `ProductoDTO`
- `POST /api/ventas` → `VentaRequest` → `VentaResponse`
- `PUT /api/config/cotizacion` → OK (solo DUENO)

## Problemas pendientes

### PROBLEMA 1 — Tests no compilan
**Archivo:** `src/test/java/com/mmotos/infrastructure/rest/ProductoControllerTest.java`
**Archivo:** `src/test/java/com/mmotos/infrastructure/rest/VentaControllerTest.java`

**Error:**
```
package org.springframework.test.context.bean.override.mockito does not exist
cannot find symbol: class MockitoBean
```

**Causa:** `@MockitoBean` es de Spring Boot 3.4+. Usamos Spring Boot 3.3.5.

**Fix:** Reemplazar en ambos archivos:
```java
// Cambiar esto:
import org.springframework.test.context.bean.override.mockito.MockitoBean;
@MockitoBean RealizarVentaUseCase realizarVentaUseCase;

// Por esto:
import org.springframework.boot.test.mock.mockito.MockBean;
@MockBean RealizarVentaUseCase realizarVentaUseCase;
```

Aplicar el mismo cambio a todos los `@MockitoBean` en ambos archivos de test.

---

### PROBLEMA 2 — instalar.bat usa nombre hardcoded del JAR
**Archivo:** `instalar.bat`

**Fix:** Cambiar la línea:
```bat
start /B javaw -jar "%~dp0mmotos-app.jar" --spring.datasource.password=G-x-m-11
```
Por:
```bat
start /B javaw -jar "%~dp0mmotos-app-1.0.0-SNAPSHOT.jar" --spring.datasource.password=G-x-m-11
```

---

### PROBLEMA 3 — Páginas sin datos reales del backend
Las siguientes páginas son placeholders (datos hardcodeados):
- `frontend/src/pages/CajaPage.tsx` — KPIs estáticos
- `frontend/src/pages/VentasPage.tsx` — tabla vacía
- `frontend/src/pages/ReportesPage.tsx` — solo estructura

**Para conectarlas al backend se necesitan endpoints GET que no existen aún:**
- `GET /api/ventas?fecha=hoy` → lista de ventas del día
- `GET /api/caja/resumen` → totales del día

**Opción A:** Agregar esos endpoints al backend Spring Boot
**Opción B:** Mantener como placeholder por ahora

---

### PROBLEMA 4 — SpaController es lista estática
**Archivo:** `src/main/java/com/mmotos/infrastructure/config/SpaController.java`

Si se agrega una nueva ruta en React Router, hay que agregarla manualmente al SpaController.

**Fix alternativo más robusto:** Configurar Spring Boot para servir `index.html` para cualquier ruta desconocida usando `spring.web.resources.add-mappings` y un filtro personalizado. Buscar en Google: "Spring Boot 3 serve React SPA all routes forward index.html"

---

### PROBLEMA 5 — Playwright no configurado
Para poder testear la app automáticamente con Python/Playwright:

```bash
pip install playwright
playwright install chromium
```

Script de test básico (`test_app.py`):
```python
from playwright.sync_api import sync_playwright

with sync_playwright() as p:
    browser = p.chromium.launch(headless=False)
    page = browser.new_page()

    # Test login
    page.goto("http://localhost:8080")
    page.wait_for_load_state("networkidle")
    page.screenshot(path="screenshot_login.png")

    page.fill("input[placeholder*='usuario']", "admin")
    page.fill("input[type='password']", "admin123")
    page.click("button[type='submit']")
    page.wait_for_load_state("networkidle")
    page.screenshot(path="screenshot_after_login.png")

    # Test buscar producto
    page.goto("http://localhost:8080/pos")
    page.wait_for_load_state("networkidle")
    page.screenshot(path="screenshot_pos.png")

    browser.close()
    print("Tests completados. Ver screenshots.")
```

---

## Arquitectura de paquetes
```
com.mmotos
├── domain
│   ├── model     → Repuesto, KitComposicion, Venta, Pago (sealed), Precio
│   ├── port      → FiscalPort, ProductoRepository, VentaRepository
│   ├── service   → VentaBuilder
│   └── exception → InsufficientStockException, FiscalException, ...
├── application
│   ├── usecase   → RealizarVentaUseCase, BuscarProductoUseCase, SyncVentasUseCase
│   └── dto       → VentaRequest/Response, ProductoDTO
└── infrastructure
    ├── input.rest      → VentaController, ProductoController, AuthController
    ├── config          → SecurityConfig, JwtService, SpaController, WebConfig
    ├── output.fiscal   → NoFiscalAdapter, AfipAdapter, HasarAdapter
    └── output.persistence → entities, repositories, mappers, adapters
```

## Archivos clave del frontend
```
frontend/src/
├── App.tsx                  → rutas React Router
├── api/client.ts            → Axios + JWT interceptor
├── store/cartStore.ts       → Zustand (carrito POS)
├── hooks/useVenta.ts        → mutation POST /api/ventas
├── hooks/useProductos.ts    → query GET /api/productos/buscar
├── pages/PosPage.tsx        → POS completo (funcional)
├── pages/ProductosPage.tsx  → catálogo (funcional)
└── pages/LoginPage.tsx      → login JWT (funcional)
```

## Diseño
- Racing Precision: dark `#121414`, rojo `#ff5540`, azul `#acc7ff`
- Referencia visual: `Fron-end/` (4 HTMLs + DESIGN.md)
- Sidebar: 11 ítems (Lector, Caja, Productos, Listas, Precios, Ventas, Presupuestos, Reportes, Métodos, Cajeros, Ajustes)
