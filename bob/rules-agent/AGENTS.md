# Project Coding Rules (Non-Obvious Only)

- **Never add raw SQL to controllers** — the existing free-text search in `ProductController.listProducts()` is a known bug. New queries must go through `ProductDao` using parameterized `JdbcTemplate` calls.
- **`deleteProduct()` must not do hard deletes** — always call `productDao.deactivate(id)` (sets `active=0`). Hard-deleting rows will break the shared H2 file used by external reporting scripts.
- Use `StringUtils.isBlank()` / `StringUtils.clean()` / `StringUtils.escapeCsv()` from `com.acme.inventory.util.StringUtils`. Do not add Apache Commons Lang equivalents.
- Logging must use Log4j 1.x: `Logger.getLogger(ClassName.class)` — no SLF4J, no java.util.logging.
- `ProductService.saveProduct()` throws `IllegalArgumentException` for validation failures. Controllers must catch it and redirect with a flash error attribute named `errorMsg`.
- Config properties (`application.properties`) are compiled into the WAR — they are not hot-reloadable.
- There are no unit tests. If adding tests, create `src/test/java/` following the standard Maven layout.
- The `StockMovement` model exists but has no DAO — it is not persisted yet. Do not assume it is written to the DB.
- `StringUtils.deserializeState()` / `serializeState()` perform unsafe Java serialization — do not extend usage of these methods.
