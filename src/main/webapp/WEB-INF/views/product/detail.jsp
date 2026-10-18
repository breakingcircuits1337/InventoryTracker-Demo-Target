<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>${product.name} &mdash; ACME Inventory Tracker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
</head>
<body>
<div class="header">
    <span class="logo">ACME Inventory Tracker</span>
    <nav>
        <a href="${pageContext.request.contextPath}/">Dashboard</a>
        <a href="${pageContext.request.contextPath}/products">Products</a>
        <a href="${pageContext.request.contextPath}/products/low-stock">Low Stock</a>
        <a href="${pageContext.request.contextPath}/products/new">Add Product</a>
    </nav>
</div>

<div class="content">

    <c:if test="${not empty successMsg}">
        <div class="alert alert-success">${successMsg}</div>
    </c:if>
    <c:if test="${not empty errorMsg}">
        <div class="alert alert-error">${errorMsg}</div>
    </c:if>

    <div class="detail-header">
        <h2>${product.name}</h2>
        <span class="sku-badge">${product.sku}</span>
        <a href="${pageContext.request.contextPath}/products/${product.id}/edit" class="btn">Edit</a>
    </div>

    <table class="detail-table">
        <tr><th>SKU</th>         <td><code>${product.sku}</code></td></tr>
        <tr><th>Category</th>    <td>${product.category}</td></tr>
        <tr><th>Description</th> <td>${product.description}</td></tr>
        <tr><th>Unit Price</th>
            <td><fmt:formatNumber value="${product.unitPrice}" type="currency" currencySymbol="$"/></td></tr>
        <tr><th>Qty On Hand</th> <td>${product.quantityOnHand}
            <c:if test="${product.lowStock}"> <span class="badge warn">LOW STOCK</span></c:if>
            <c:if test="${product.quantityOnHand == 0}"> <span class="badge danger">OUT OF STOCK</span></c:if>
        </td></tr>
        <tr><th>Reorder Point</th><td>${product.reorderPoint}</td></tr>
        <tr><th>Supplier</th>    <td>${product.supplierCode}</td></tr>
        <tr><th>Last Updated</th>
            <td><fmt:formatDate value="${product.lastUpdated}" pattern="yyyy-MM-dd HH:mm"/></td></tr>
    </table>

    <h3>Quick Stock Adjustment</h3>
    <form method="post" action="${pageContext.request.contextPath}/products/${product.id}/adjust"
          class="adjust-form">
        <label>Delta (positive = receive, negative = issue):
            <input type="number" name="delta" value="0" required />
        </label>
        <label>Reference #:
            <input type="text" name="ref" placeholder="PO / work order" />
        </label>
        <button type="submit">Apply Adjustment</button>
    </form>

    <p><a href="${pageContext.request.contextPath}/products">&larr; Back to Products</a></p>

</div>

<div class="footer">ACME Internal Systems &mdash; Inventory Tracker v2.4.1</div>
</body>
</html>
