# Project Architecture Rules (Non-Obvious Only)

- **Shared H2 database**: The H2 file at `./data/inventorydb` is accessed by external reporting scripts. Schema changes require coordination with the Infrastructure team — forward-only, no rollback.
- **Two Spring contexts**: `applicationContext.xml` (root context — datasource, services, DAOs) and `dispatcher-servlet.xml` (child context — controllers, MVC). Beans in the child context cannot be injected into the root context. Keep service/DAO beans in `applicationContext.xml` only.
- **`DashboardController` uses `JdbcTemplate` directly** (no service layer). This is intentional for read-only aggregate queries — follow this pattern only for dashboard/reporting aggregations.
- **`ProductController` also holds a direct `JdbcTemplate` injection** for the free-text search path — this is a bug, not a pattern to replicate.
- **Soft-delete architecture**: `active` column is the only deletion mechanism. Any new entity added to the system should follow the same `active=1/0` pattern if it participates in queries with the existing tables.
- **No dependency injection for `StringUtils`** — it is a static utility class with a private constructor. Do not make it a Spring bean.
- **Context path is fixed at `/inventory`** — configured in `pom.xml` Tomcat plugin and expected by the proxy. Cannot be changed without proxy reconfiguration.
- **Export token check has a security gap**: the token guard in `exportCsv()` is bypassed when the session has no `exportToken` attribute. Any new export or sensitive endpoint must not replicate this pattern.
