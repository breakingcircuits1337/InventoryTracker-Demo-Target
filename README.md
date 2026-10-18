# Inventory Tracker

Internal web application for managing ACME warehouse stock levels, 
product catalog, and supplier references.

**Last updated:** March 2019  
**Maintained by:** Infrastructure & Internal Tools Team  
**Version:** 2.4.1  

---

## Overview

Inventory Tracker is the primary interface for warehouse staff and 
procurement coordinators to view and update stock quantities, browse 
the product catalog by category, and export inventory snapshots for 
reporting. It replaced the previous Excel-based tracking process in 
2011 and has been extended incrementally since.

The application runs on Tomcat 7 behind the corporate reverse proxy. 
All authentication is handled by the proxy — the application itself 
trusts the authenticated session and does not manage credentials.

---

## Requirements

- Java 8 (JDK 1.8.x)
- Apache Maven 3.3+
- Apache Tomcat 7.x (deployment target)
- Network access to the H2 database file at `./data/inventorydb`

---

## Building

```
mvn clean package
```

This produces `target/inventory-tracker.war`. Deploy to Tomcat by 
copying to the webapps directory or using the manager console.

For local development with the embedded Tomcat plugin:

```
mvn tomcat7:run
```

Then open: http://localhost:8080/inventory/

---

## Configuration

Edit `src/main/resources/application.properties` before building:

| Property               | Default                                  | Description                    |
|------------------------|------------------------------------------|--------------------------------|
| `db.url`               | `jdbc:h2:file:./data/inventorydb`        | H2 file path                   |
| `db.username`          | `sa`                                     | Database user                  |
| `db.password`          | `inventory2009`                          | Database password              |
| `db.maxActive`         | `20`                                     | Max DB connections             |
| `app.pageSize`         | `25`                                     | Rows per page in product list  |
| `app.exportEnabled`    | `true`                                   | Enable CSV export endpoint     |
| `app.lowStockThreshold`| `10`                                     | Default reorder alert level    |

Log output is written to `logs/inventory.log` (rolling, 10 MB, 5 backups).  
Log level is configured in `src/main/resources/log4j.properties`.

---

## Database

The application uses H2 in file-persistence mode. The schema is 
created manually on first deployment using the SQL script in 
`src/main/resources/schema.sql` (run via the H2 console or 
`mvn sql:execute`).

Key tables:

| Table         | Description                         |
|---------------|-------------------------------------|
| `products`    | Product catalog and stock levels    |
| `stock_movements` | Adjustment transaction history  |
| `suppliers`   | Supplier reference data             |

---

## Application Structure

```
src/main/
  java/com/acme/inventory/
    controller/     Spring MVC controllers
    dao/            JDBC data access objects
    model/          Domain objects
    service/        Business logic layer
    util/           Shared utilities
  resources/
    application.properties
    log4j.properties
    schema.sql
  webapp/
    WEB-INF/
      spring/       Spring XML configuration
      views/        JSP templates
    css/            Stylesheets
```

---

## Deployment Notes

- The WAR must be deployed at context path `/inventory`.
- The `./data/` directory (relative to Tomcat's working directory) 
  must be writable by the Tomcat process user.
- The `./logs/` directory must also be writable.
- No external services are required. All data is local.

**Tomcat JNDI is not used.** The data source is configured in 
`applicationContext.xml` using commons-dbcp directly.

---

## Known Issues

- The category selector on the product form does not persist a newly 
  typed category across page reloads if save fails validation. 
  Workaround: add the category to an existing product first, then 
  use it in the new product form.
- The CSV export for large catalogs (> 5,000 rows) can be slow on 
  the first request after a Tomcat restart due to connection pool 
  warm-up. Subsequent exports are fast.
- Session timeout is set to 30 minutes. Users working through a 
  long batch entry may need to log in again if they pause.

---

## Contact

For issues or access requests, open a ticket in the internal helpdesk 
under **Category: Internal Tools > Inventory Tracker**.

Do not make schema changes without coordinating with the Infrastructure 
team — other internal reporting scripts read from the same H2 file.
