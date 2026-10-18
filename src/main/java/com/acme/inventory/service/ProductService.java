package com.acme.inventory.service;

import com.acme.inventory.dao.ProductDao;
import com.acme.inventory.model.Product;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private static final Logger log = Logger.getLogger(ProductService.class);

    @Autowired
    private ProductDao productDao;

    public List<Product> getAllProducts() {
        return productDao.findAll();
    }

    public Product getProductById(Long id) {
        return productDao.findById(id);
    }

    public List<Product> getProductsByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return productDao.findAll();
        }
        return productDao.findByCategory(category.trim());
    }

    public void saveProduct(Product product) {
        if (product.getSku() == null || product.getSku().trim().isEmpty()) {
            throw new IllegalArgumentException("SKU is required");
        }
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (product.getUnitPrice() < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        productDao.save(product);
        log.info("Saved product: " + product.getSku());
    }

    public void deleteProduct(Long id) {
        productDao.deactivate(id);
        log.info("Deactivated product id=" + id);
    }

    public List<Product> getLowStockProducts() {
        List<Product> all = productDao.findAll();
        List<Product> low = new ArrayList<Product>();
        for (Product p : all) {
            if (p.isLowStock()) {
                low.add(p);
            }
        }
        return low;
    }

    public List<String> getCategories() {
        return productDao.findAllCategories();
    }
}
