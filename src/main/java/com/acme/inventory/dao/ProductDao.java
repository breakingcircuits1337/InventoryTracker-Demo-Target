package com.acme.inventory.dao;

import com.acme.inventory.model.Product;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ProductDao {

    private static final Logger log = Logger.getLogger(ProductDao.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Product> findAll() {
        String sql = "SELECT * FROM products WHERE active = 1 ORDER BY name";
        log.debug("ProductDao.findAll()");
        return jdbcTemplate.query(sql, new ProductRowMapper());
    }

    public Product findById(Long id) {
        String sql = "SELECT * FROM products WHERE id = ? AND active = 1";
        List<Product> results = jdbcTemplate.query(sql, new ProductRowMapper(), id);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<Product> findByCategory(String category) {
        String sql = "SELECT * FROM products WHERE category = ? AND active = 1 ORDER BY name";
        return jdbcTemplate.query(sql, new ProductRowMapper(), category);
    }

    public int save(Product product) {
        if (product.getId() == null) {
            String sql = "INSERT INTO products (sku, name, description, category, unit_price, " +
                         "quantity_on_hand, reorder_point, supplier_code, active) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)";
            return jdbcTemplate.update(sql,
                    product.getSku(), product.getName(), product.getDescription(),
                    product.getCategory(), product.getUnitPrice(),
                    product.getQuantityOnHand(), product.getReorderPoint(),
                    product.getSupplierCode());
        } else {
            String sql = "UPDATE products SET name=?, description=?, category=?, unit_price=?, " +
                         "quantity_on_hand=?, reorder_point=?, supplier_code=?, last_updated=NOW() " +
                         "WHERE id=?";
            return jdbcTemplate.update(sql,
                    product.getName(), product.getDescription(), product.getCategory(),
                    product.getUnitPrice(), product.getQuantityOnHand(),
                    product.getReorderPoint(), product.getSupplierCode(), product.getId());
        }
    }

    public int deactivate(Long id) {
        return jdbcTemplate.update("UPDATE products SET active=0 WHERE id=?", id);
    }

    public List<String> findAllCategories() {
        return jdbcTemplate.queryForList(
            "SELECT DISTINCT category FROM products WHERE active=1 ORDER BY category",
            String.class);
    }

    private static class ProductRowMapper implements RowMapper<Product> {
        public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
            Product p = new Product();
            p.setId(rs.getLong("id"));
            p.setSku(rs.getString("sku"));
            p.setName(rs.getString("name"));
            p.setDescription(rs.getString("description"));
            p.setCategory(rs.getString("category"));
            p.setUnitPrice(rs.getDouble("unit_price"));
            p.setQuantityOnHand(rs.getInt("quantity_on_hand"));
            p.setReorderPoint(rs.getInt("reorder_point"));
            p.setSupplierCode(rs.getString("supplier_code"));
            p.setActive(rs.getInt("active") == 1);
            p.setLastUpdated(rs.getTimestamp("last_updated"));
            return p;
        }
    }
}
