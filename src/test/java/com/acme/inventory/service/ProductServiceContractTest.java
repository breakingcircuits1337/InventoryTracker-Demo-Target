package com.acme.inventory.service;

import com.acme.inventory.dao.ProductDao;
import com.acme.inventory.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Contract tests for {@link ProductService} — Module 3, L2L[VERIFY].
 *
 * Uses Mockito to isolate the service from the DAO.
 * Covers contracts PS-01 through PS-07.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceContractTest {

    @Mock
    private ProductDao productDao;

    @InjectMocks
    private ProductService service;

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private Product validProduct(Long id) {
        Product p = new Product();
        p.setId(id);
        p.setSku("SKU-001");
        p.setName("Test Widget");
        p.setUnitPrice(1.50);
        p.setQuantityOnHand(10);
        p.setReorderPoint(5);
        p.setActive(true);
        return p;
    }

    private Product productWithLowStock(int qty, int reorder) {
        Product p = new Product();
        p.setSku("S1");
        p.setName("Low");
        p.setUnitPrice(1.0);
        p.setQuantityOnHand(qty);
        p.setReorderPoint(reorder);
        p.setActive(true);
        return p;
    }

    // ------------------------------------------------------------------
    // PS-01  saveProduct throws when SKU is blank
    // ------------------------------------------------------------------

    @Test
    void saveProduct_blankSku_throwsIllegalArgumentException() {
        Product p = validProduct(null);
        p.setSku("");
        assertThrows(IllegalArgumentException.class, () -> service.saveProduct(p));
        verify(productDao, never()).save(any());
    }

    @Test
    void saveProduct_nullSku_throwsIllegalArgumentException() {
        Product p = validProduct(null);
        p.setSku(null);
        assertThrows(IllegalArgumentException.class, () -> service.saveProduct(p));
    }

    @Test
    void saveProduct_whitespaceSku_throwsIllegalArgumentException() {
        Product p = validProduct(null);
        p.setSku("   ");
        assertThrows(IllegalArgumentException.class, () -> service.saveProduct(p));
    }

    // ------------------------------------------------------------------
    // PS-02  saveProduct throws when name is blank
    // ------------------------------------------------------------------

    @Test
    void saveProduct_blankName_throwsIllegalArgumentException() {
        Product p = validProduct(null);
        p.setName("");
        assertThrows(IllegalArgumentException.class, () -> service.saveProduct(p));
        verify(productDao, never()).save(any());
    }

    @Test
    void saveProduct_nullName_throwsIllegalArgumentException() {
        Product p = validProduct(null);
        p.setName(null);
        assertThrows(IllegalArgumentException.class, () -> service.saveProduct(p));
    }

    // ------------------------------------------------------------------
    // PS-03  saveProduct throws when unitPrice < 0
    // ------------------------------------------------------------------

    @Test
    void saveProduct_negativePrice_throwsIllegalArgumentException() {
        Product p = validProduct(null);
        p.setUnitPrice(-0.01);
        assertThrows(IllegalArgumentException.class, () -> service.saveProduct(p));
        verify(productDao, never()).save(any());
    }

    @Test
    void saveProduct_zeroPriceIsAllowed() {
        Product p = validProduct(null);
        p.setUnitPrice(0.0);
        assertDoesNotThrow(() -> service.saveProduct(p));
        verify(productDao).save(p);
    }

    // ------------------------------------------------------------------
    // PS-04  saveProduct delegates to productDao.save when valid
    // ------------------------------------------------------------------

    @Test
    void saveProduct_validProduct_delegatesToDao() {
        Product p = validProduct(null);
        service.saveProduct(p);
        verify(productDao, times(1)).save(p);
    }

    // ------------------------------------------------------------------
    // PS-05  deleteProduct delegates to productDao.deactivate
    // ------------------------------------------------------------------

    @Test
    void deleteProduct_delegatesToDaoDeactivate() {
        service.deleteProduct(42L);
        verify(productDao, times(1)).deactivate(42L);
    }

    // ------------------------------------------------------------------
    // PS-06  getLowStockProducts returns only qty <= reorderPoint
    // ------------------------------------------------------------------

    @Test
    void getLowStockProducts_returnsOnlyLowStockItems() {
        Product low    = productWithLowStock(3, 5);   // 3 <= 5 → low stock
        Product ok     = productWithLowStock(10, 5);  // 10 > 5 → not low
        Product border = productWithLowStock(5, 5);   // 5 <= 5 → low stock (equal counts)
        when(productDao.findAll()).thenReturn(Arrays.asList(low, ok, border));

        List<Product> result = service.getLowStockProducts();

        assertEquals(2, result.size());
        assertTrue(result.contains(low));
        assertTrue(result.contains(border));
        assertFalse(result.contains(ok));
    }

    @Test
    void getLowStockProducts_allHealthy_returnsEmptyList() {
        Product ok = productWithLowStock(100, 10);
        when(productDao.findAll()).thenReturn(Collections.singletonList(ok));

        assertTrue(service.getLowStockProducts().isEmpty());
    }

    // ------------------------------------------------------------------
    // PS-07  getProductsByCategory falls back to findAll for blank input
    // ------------------------------------------------------------------

    @Test
    void getProductsByCategory_nullCategory_returnsAllProducts() {
        when(productDao.findAll()).thenReturn(Collections.emptyList());
        service.getProductsByCategory(null);
        verify(productDao).findAll();
        verify(productDao, never()).findByCategory(any());
    }

    @Test
    void getProductsByCategory_emptyString_returnsAllProducts() {
        when(productDao.findAll()).thenReturn(Collections.emptyList());
        service.getProductsByCategory("");
        verify(productDao).findAll();
        verify(productDao, never()).findByCategory(any());
    }

    @Test
    void getProductsByCategory_validCategory_delegatesToFindByCategory() {
        when(productDao.findByCategory("Tools")).thenReturn(Collections.emptyList());
        service.getProductsByCategory("Tools");
        verify(productDao).findByCategory("Tools");
        verify(productDao, never()).findAll();
    }

    @Test
    void getProductsByCategory_whitespaceCategory_returnsAllProducts() {
        when(productDao.findAll()).thenReturn(Collections.emptyList());
        service.getProductsByCategory("   ");
        verify(productDao).findAll();
    }
}
