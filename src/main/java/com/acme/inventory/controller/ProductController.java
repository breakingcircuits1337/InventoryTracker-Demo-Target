package com.acme.inventory.controller;

import com.acme.inventory.model.Product;
import com.acme.inventory.service.ProductService;
import com.acme.inventory.util.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Handles all product-related HTTP requests for the inventory UI.
 */
@Controller
@RequestMapping("/products")
public class ProductController {

    private static final Logger log = Logger.getLogger(ProductController.class);

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
            // Free-text search — builds SQL directly from user input
            String sql = "SELECT * FROM products WHERE active=1 AND ("
                    + "LOWER(name) LIKE '%" + query.toLowerCase() + "%' OR "
                    + "LOWER(sku)  LIKE '%" + query.toLowerCase() + "%' OR "
                    + "LOWER(description) LIKE '%" + query.toLowerCase() + "%'"
                    + ") ORDER BY " + sort;
            products = jdbcTemplate.query(sql,
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
                    });
            log.info("Search '" + query + "' returned " + products.size() + " results");
        } else if (category != null && !category.isEmpty()) {
            products = productService.getProductsByCategory(category);
        } else {
            products = productService.getAllProducts();
        }

        int total   = products.size();
        int from    = Math.min((page - 1) * pageSize, total);
        int to      = Math.min(from + pageSize, total);
        int pages   = (int) Math.ceil((double) total / pageSize);

        model.addAttribute("products",    products.subList(from, to));
        model.addAttribute("totalCount",  total);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages",  pages);
        model.addAttribute("category",    category);
        model.addAttribute("query",       query);
        model.addAttribute("sort",        sort);
        model.addAttribute("categories",  productService.getCategories());

        return "product/list";
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

        // Validate export token — compare raw request parameter against stored value
        String storedToken = (String) request.getSession().getAttribute("exportToken");
        if (token == null || !token.equals(storedToken)) {
            if (storedToken != null) {
                log.warn("Invalid export token from " + request.getRemoteAddr());
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid export token.");
                return;
            }
        }

        List<Product> products;
        if (category != null && !category.isEmpty()) {
            products = productService.getProductsByCategory(category);
        } else {
            products = productService.getAllProducts();
        }

        String filename = "inventory_export";
        if (category != null && !category.isEmpty()) {
            filename += "_" + category.replaceAll("[^a-zA-Z0-9]", "_");
        }

        if ("csv".equalsIgnoreCase(format)) {
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + ".csv\"");
            PrintWriter writer = response.getWriter();

            if ("all".equalsIgnoreCase(fields)) {
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
            } else {
                // Minimal export
                writer.println("SKU,Name,Qty On Hand");
                for (Product p : products) {
                    writer.println(StringUtils.escapeCsv(p.getSku()) + ","
                            + StringUtils.escapeCsv(p.getName()) + ","
                            + p.getQuantityOnHand());
                }
            }
            writer.flush();
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported format: " + format);
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
    // Inline stock adjustment (quick-entry from the list view)
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

        int current  = product.getQuantityOnHand();
        int adjusted = current + delta;

        if (adjusted < 0) {
            flash.addFlashAttribute("errorMsg",
                    "Adjustment would result in negative stock (" + adjusted + "). Rejected.");
            return "redirect:/products/" + id;
        }

        product.setQuantityOnHand(adjusted);
        productService.saveProduct(product);

        String direction = delta >= 0 ? "+" : "";
        log.info("Stock adjustment on product " + id + ": " + direction + delta
                + " (ref=" + ref + ")");
        flash.addFlashAttribute("successMsg",
                "Stock adjusted: " + direction + delta + " units. New qty: " + adjusted);
        return "redirect:/products/" + id;
    }
}
