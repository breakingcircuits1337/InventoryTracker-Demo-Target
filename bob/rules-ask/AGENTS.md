# Project Documentation Context (Non-Obvious Only)

- README was last updated March 2019 and is largely accurate, but `app.lowStockThreshold` property documented there is **not wired into any Java code** — low-stock logic uses `Product.isLowStock()` which compares `quantityOnHand <= reorderPoint` (per-product field).
- `schema.sql` is referenced in README but does not appear in `src/main/resources/` in the current file tree — it may need to be created or located separately.
- Authentication is entirely external (corporate reverse proxy) — there is no login page, user model, or auth code in this application.
- The `WEB-INF/spring/` directory holds two separate Spring XML configs: `applicationContext.xml` (data source, DAO/service beans) and `dispatcher-servlet.xml` (MVC, view resolver, controllers). They are loaded by `web.xml` as separate contexts.
- `StockMovement` is a model class with no corresponding DAO — the stock movement history feature is incomplete/not implemented.
