package com.acme.inventory.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Landing page — summary dashboard showing key inventory metrics.
 */
@Controller
public class DashboardController {

    private static final Logger log = Logger.getLogger(DashboardController.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/")
    public String index(Model model) {
        try {
            Integer total    = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM products WHERE active=1", Integer.class);
            Integer lowStock = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM products WHERE active=1 AND quantity_on_hand <= reorder_point",
                    Integer.class);
            Integer outOfStock = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM products WHERE active=1 AND quantity_on_hand = 0",
                    Integer.class);
            Double  totalValue = jdbcTemplate.queryForObject(
                    "SELECT SUM(unit_price * quantity_on_hand) FROM products WHERE active=1",
                    Double.class);

            List<Map<String, Object>> byCategory = jdbcTemplate.queryForList(
                    "SELECT category, COUNT(*) AS cnt FROM products WHERE active=1 " +
                    "GROUP BY category ORDER BY cnt DESC");

            Map<String, Integer> categoryMap = new HashMap<String, Integer>();
            for (Map<String, Object> row : byCategory) {
                categoryMap.put((String) row.get("category"),
                        ((Number) row.get("cnt")).intValue());
            }

            model.addAttribute("totalProducts",  total        != null ? total        : 0);
            model.addAttribute("lowStockCount",  lowStock     != null ? lowStock     : 0);
            model.addAttribute("outOfStockCount",outOfStock   != null ? outOfStock   : 0);
            model.addAttribute("totalValue",     totalValue   != null ? totalValue   : 0.0);
            model.addAttribute("byCategory",     categoryMap);

        } catch (Exception e) {
            log.error("Dashboard query failed", e);
            model.addAttribute("dashboardError", e.getMessage());
        }

        return "index";
    }
}
