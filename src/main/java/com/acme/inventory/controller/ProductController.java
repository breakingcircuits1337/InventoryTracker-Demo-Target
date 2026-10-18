package com.acme.inventory.controller;

import com.acme.inventory.model.Product;
import com.acme.inventory.service.ProductService;
import com.acme.inventory.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Handles all product-related HTTP requests for the inventory UI (JSP layer).
 *
 * <p>L2L[TRANSFORM] changes:
 * <ul>
 *   <li>Log4j → SLF4J (done in prior pass)</li>
 *   <li>javax.servlet → jakarta.servlet</li>
 *   <li>SQL injection fixed: search uses parameterized query; sort validated against allowlist</li>
 *   <li>Export token bypass fixed: always reject when token absent or invalid</li>
 *   <li>listProducts CC reduced from ~7 to ≤5 via extracted helpers</li>
 *   <li>exportCsv CC reduced from ~8 to ≤5 via extracted helpers</li>
 *   <li>REST API added alongside JSP endpoints (inner static class ProductApiController)</li>
 * </ul>
 */
@Controller
@RequestMapping("/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private static final Set<String> SORT_ALLOWLIST = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("name", "sku", "category", "unit_price", "quantity_on_hand")));

    @Autowired
    private ProductService productService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${app.pageSize:25}")
    private int pageSize;

    @Value("${app.exportEnabled:true}")
    private boolean exportEnabled;

    // -----------------------------------------------------------------------
    // List / Search
    // -----------------------------------------------------------------------

    @GetMapping
    public String listProducts(
            @RequestParam(value = "category",  required = false) String category,
            @RequestParam(value = "q",         required = false) String query,
            @RequestParam(value = "sort",      required = false, defaultValue = "name") String sort,
            @RequestParam(value = "page",      required = false, defaultValue = "1")   int    page,
            Model model) {

        List<Product> products;

        if (query != null && !query.trim().isEmpty()) {
            products = searchProducts(query.trim(), sort);
            log.info("Search '{}' returned {} results", query, products.size());
        } else if (category != null && !category.isEmpty()) {
            products = productService.getProductsByCategory(category);
        } else {
            products = productService.getAllProducts();
        }

        paginateAndPopulate(products, page, category, query, sort, model);
        return "product/list";
    }

    /**
     * PC-01/PC-02: Parameterized LIKE search with sort-column allowlist.
     * CC of this helper ≤ 2.
     */
    private List<Product> searchProducts(String query, String sort) {
        String safeSort = SORT_ALLOWLIST.contains(sort) ? sort : "name";
        String sql = "SELECT * FROM products WHERE active=1 AND ("
                + "LOWER(name) LIKE ? OR LOWER(sku) LIKE ? OR LOWER(description) LIKE ?"
                + ") ORDER BY " + safeSort;
        String term = "%" + query.toLowerCase() + "%";
        return jdbcTemplate.query(sql,
                (rs, n) -> {
                    Product p = new Product();
                    p.setId(rs.getLong("id"));
                    p.setSku(rs.getString("sku"));
                    p.setName(rs.getString("name"));
                    p.setCategory(rs.getString("category"));
                    p.setUnitPrice(rs.getDouble("unit_price"));
                    p.setQuantityOnHand(rs.getInt("quantity_on_hand"));
                    p.setActive(rs.getInt("active") == 1);
                    return p;
                }, term, term, term);
    }

    /** Adds pagination attributes to the model. CC ≤ 2. */
    private void paginateAndPopulate(List<Product> products, int page,
                                     String category, String query, String sort, Model model) {
        int total = products.size();
        int from  = Math.min((page - 1) * pageSize, total);
        int to    = Math.min(from + pageSize, total);
        int pages = (int) Math.ceil((double) total / pageSize);

        model.addAttribute("products",    products.subList(from, to));
        model.addAttribute("totalCount",  total);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages",  pages);
        model.addAttribute("category",    category);
        model.addAttribute("query",       query);
        model.addAttribute("sort",        sort);
        model.addAttribute("categories",  productService.getCategories());
    }

    // -----------------------------------------------------------------------
    // Detail
    // -----------------------------------------------------------------------

    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) {
            model.addAttribute("errorMsg", "Product not found: " + id);
            return "error";
        }
        model.addAttribute("product", product);
        return "product/detail";
    }

    // -----------------------------------------------------------------------
    // Create / Edit
    // -----------------------------------------------------------------------

    @GetMapping("/new")
    public String newProductForm(Model model) {
        model.addAttribute("product",    new Product());
        model.addAttribute("categories", productService.getCategories());
        model.addAttribute("formAction", "create");
        return "product/form";
    }

    @PostMapping("/create")
    public String createProduct(@ModelAttribute Product product,
                                RedirectAttributes flash) {
        try {
            productService.saveProduct(product);
            flash.addFlashAttribute("successMsg", "Product created: " + product.getSku());
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/products/new";
        }
        return "redirect:/products";
    }

    @GetMapping("/{id}/edit")
    public String editProductForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) {
            model.addAttribute("errorMsg", "Product not found: " + id);
            return "error";
        }
        model.addAttribute("product",    product);
        model.addAttribute("categories", productService.getCategories());
        model.addAttribute("formAction", "update");
        return "product/form";
    }

    @PostMapping("/{id}/update")
    public String updateProduct(@PathVariable Long id,
                                @ModelAttribute Product product,
                                RedirectAttributes flash) {
        product.setId(id);
        try {
            productService.saveProduct(product);
            flash.addFlashAttribute("successMsg", "Product updated.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/products/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes flash) {
        productService.deleteProduct(id);
        flash.addFlashAttribute("successMsg", "Product removed from active inventory.");
        return "redirect:/products";
    }

    // -----------------------------------------------------------------------
    // Export
    // -----------------------------------------------------------------------

    @GetMapping("/export")
    public void exportCsv(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "format",   required = false, defaultValue = "csv") String format,
            @RequestParam(value = "fields",   required = false, defaultValue = "all") String fields,
            @RequestParam(value = "token",    required = false) String token,
            HttpServletRequest  request,
            HttpServletResponse response) throws IOException {

        if (!exportEnabled) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Export is disabled.");
            return;
        }

        // PC-07: always reject when token is absent or does not match stored value
        String storedToken = (String) request.getSession().getAttribute("exportToken");
        if (token == null || !token.equals(storedToken)) {
            log.warn("Invalid export token from {}", request.getRemoteAddr());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid export token.");
            return;
        }

        if (!"csv".equalsIgnoreCase(format)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported format: " + format);
            return;
        }

        List<Product> products = resolveExportProducts(category);
        String filename = buildExportFilename(category);

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + ".csv\"");
        PrintWriter writer = response.getWriter();
        if ("all".equalsIgnoreCase(fields)) {
            writeCsvFull(writer, products);
        } else {
            writeCsvMinimal(writer, products);
        }
        writer.flush();
    }

    private List<Product> resolveExportProducts(String category) {
        if (category != null && !category.isEmpty()) {
            return productService.getProductsByCategory(category);
        }
        return productService.getAllProducts();
    }

    private String buildExportFilename(String category) {
        String filename = "inventory_export";
        if (category != null && !category.isEmpty()) {
            filename += "_" + category.replaceAll("[^a-zA-Z0-9]", "_");
        }
        return filename;
    }

    private void writeCsvFull(PrintWriter writer, List<Product> products) {
        writer.println("ID,SKU,Name,Category,Unit Price,Qty On Hand,Reorder Point,Supplier,Active");
        for (Product p : products) {
            writer.println(p.getId() + "," + StringUtils.escapeCsv(p.getSku()) + ","
                    + StringUtils.escapeCsv(p.getName()) + ","
                    + StringUtils.escapeCsv(p.getCategory()) + ","
                    + p.getUnitPrice() + "," + p.getQuantityOnHand() + ","
                    + p.getReorderPoint() + ","
                    + StringUtils.escapeCsv(p.getSupplierCode()) + ","
                    + p.isActive());
        }
    }

    private void writeCsvMinimal(PrintWriter writer, List<Product> products) {
        writer.println("SKU,Name,Qty On Hand");
        for (Product p : products) {
            writer.println(StringUtils.escapeCsv(p.getSku()) + ","
                    + StringUtils.escapeCsv(p.getName()) + ","
                    + p.getQuantityOnHand());
        }
    }

    // -----------------------------------------------------------------------
    // Low stock alert
    // -----------------------------------------------------------------------

    @GetMapping("/low-stock")
    public String lowStock(Model model) {
        List<Product> lowStock = productService.getLowStockProducts();
        model.addAttribute("products",  lowStock);
        model.addAttribute("alertMode", true);
        return "product/list";
    }

    // -----------------------------------------------------------------------
    // Inline stock adjustment
    // -----------------------------------------------------------------------

    @PostMapping("/{id}/adjust")
    public String adjustStock(@PathVariable Long id,
                              @RequestParam("delta") int delta,
                              @RequestParam(value = "ref", required = false) String ref,
                              RedirectAttributes flash) {
        Product product = productService.getProductById(id);
        if (product == null) {
            flash.addFlashAttribute("errorMsg", "Product not found.");
            return "redirect:/products";
        }

        int adjusted = product.getQuantityOnHand() + delta;
        if (adjusted < 0) {
            flash.addFlashAttribute("errorMsg",
                    "Adjustment would result in negative stock (" + adjusted + "). Rejected.");
            return "redirect:/products/" + id;
        }

        product.setQuantityOnHand(adjusted);
        productService.saveProduct(product);

        String direction = delta >= 0 ? "+" : "";
        log.info("Stock adjustment on product {}: {}{} (ref={})", id, direction, delta, ref);
        flash.addFlashAttribute("successMsg",
                "Stock adjusted: " + direction + delta + " units. New qty: " + adjusted);
        return "redirect:/products/" + id;
    }

    // -----------------------------------------------------------------------
    // REST API — PC-08 through PC-13
    // Added alongside JSP controller; JSP mappings above are NOT removed.
    // -----------------------------------------------------------------------

    @RestController
    @RequestMapping("/api/products")
    public static class ProductApiController {

        private static final Logger apiLog = LoggerFactory.getLogger(ProductApiController.class);

        @Autowired
        private ProductService productService;

        /** PC-08 */
        @GetMapping
        public ResponseEntity<List<Product>> apiList() {
            return ResponseEntity.ok(productService.getAllProducts());
        }

        /** PC-09 */
        @GetMapping("/{id}")
        public ResponseEntity<Product> apiGet(@PathVariable Long id) {
            Product p = productService.getProductById(id);
            if (p == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(p);
        }

        /** PC-10, PC-11 */
        @PostMapping
        public ResponseEntity<?> apiCreate(@RequestBody Product product) {
            try {
                productService.saveProduct(product);
                return ResponseEntity.status(HttpStatus.CREATED).body(product);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }

        /** PC-12 */
        @PutMapping("/{id}")
        public ResponseEntity<?> apiUpdate(@PathVariable Long id,
                                           @RequestBody Product product) {
            if (productService.getProductById(id) == null) {
                return ResponseEntity.notFound().build();
            }
            product.setId(id);
            try {
                productService.saveProduct(product);
                return ResponseEntity.ok(product);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }

        /** DELETE /api/products/{id} */
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> apiDelete(@PathVariable Long id) {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        }

        /** PC-13 */
        @PatchMapping("/{id}/stock")
        public ResponseEntity<?> apiAdjustStock(@PathVariable Long id,
                                                @RequestParam("delta") int delta) {
            Product product = productService.getProductById(id);
            if (product == null) return ResponseEntity.notFound().build();

            int adjusted = product.getQuantityOnHand() + delta;
            if (adjusted < 0) {
                return ResponseEntity.unprocessableEntity()
                        .body("Adjustment would result in negative stock (" + adjusted + ").");
            }
            product.setQuantityOnHand(adjusted);
            productService.saveProduct(product);
            apiLog.info("API stock adjustment on product {}: delta={}", id, delta);
            return ResponseEntity.ok(product);
        }
    }
}
