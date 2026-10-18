# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Stack

Java 8 / Spring MVC 4.3 / JSP / H2 file DB / Maven WAR packaging. No Spring Boot — all wiring is via XML config in `src/main/webapp/WEB-INF/spring/`.

## Commands

```bash
mvn clean package          # build → target/inventory-tracker.war
mvn tomcat7:run            # run locally at http://localhost:8080/inventory/
```

There are **no automated tests** in the project. No `src/test/` directory exists.

## Critical Architecture Notes

- **`deleteProduct()` is a soft-delete** — sets `active=0`, never removes rows. All DAO queries filter `WHERE active=1`.
- **`ProductController` bypasses the service layer for free-text search**: it builds raw SQL directly against `jdbcTemplate` (SQL injection risk — do not extend this pattern).
- **Export token validation has a bypass**: if `storedToken` is `null` in the session, the token check is skipped entirely (see `ProductController.exportCsv()`).
- **`StringUtils.deserializeState()`** accepts a base64-encoded serialized Java object from a request parameter and deserializes it with no class filter — unsafe deserialization.
- **Low-stock threshold** is implemented as `quantityOnHand <= reorderPoint` on the `Product` model (`isLowStock()`), not the `app.lowStockThreshold` property in `application.properties` (that property is unused in code).
- **DB schema is not auto-created** — must be applied manually via `src/main/resources/schema.sql` before first run. Other internal reporting scripts share the same H2 file; do not alter the schema without coordination.
- WAR **must deploy at context path `/inventory`** — hardcoded in the Tomcat plugin config and expected by the corporate proxy.
- Data source is wired in `applicationContext.xml` via commons-dbcp; **JNDI is not used**.

## Code Conventions

- Logging: `private static final Logger log = Logger.getLogger(ClassName.class)` (Log4j 1.x, not SLF4J).
- Getters/setters use single-char parameter names for brevity (e.g., `setDescription(String d)`).
- Java 7-style diamond operator and anonymous inner classes are used throughout (no lambdas, except one in `ProductController`).
- String helpers: use `StringUtils.isBlank()`, `StringUtils.clean()`, `StringUtils.escapeCsv()` from `com.acme.inventory.util.StringUtils` — do not use Apache Commons equivalents.
- Service layer validates and throws `IllegalArgumentException` for business rule failures; controllers catch it and forward as flash error messages.

## Configuration

Config properties in `src/main/resources/application.properties` are baked in at build time (not externalized post-deploy). To change settings, edit and rebuild.
