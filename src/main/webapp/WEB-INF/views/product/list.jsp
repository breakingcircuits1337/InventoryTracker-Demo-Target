<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Products &mdash; ACME Inventory Tracker</title>
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

    <div class="toolbar">
        <h2><c:choose>
            <c:when test="${alertMode}">Low Stock Alert</c:when>
            <c:when test="${not empty category}">Category: ${category}</c:when>
            <c:otherwise>All Products</c:otherwise>
        </c:choose></h2>

        <form method="get" action="${pageContext.request.contextPath}/products" class="search-form">
            <input type="text" name="q" value="${query}" placeholder="Search SKU, name, description..." />
            <select name="category">
                <option value="">All Categories</option>
                <c:forEach var="cat" items="${categories}">
                    <option value="${cat}" ${cat == category ? 'selected' : ''}>${cat}</option>
                </c:forEach>
            </select>
            <select name="sort">
                <option value="name"          ${sort == 'name'          ? 'selected' : ''}>Name</option>
                <option value="sku"           ${sort == 'sku'           ? 'selected' : ''}>SKU</option>
                <option value="unit_price"    ${sort == 'unit_price'    ? 'selected' : ''}>Price</option>
                <option value="quantity_on_hand" ${sort == 'quantity_on_hand' ? 'selected' : ''}>Qty</option>
            </select>
            <button type="submit">Search</button>
            <a href="${pageContext.request.contextPath}/products/export?category=${category}" class="btn-secondary">Export CSV</a>
        </form>
    </div>

    <p class="record-count">Showing ${products.size()} of ${totalCount} products
        (page ${currentPage} of ${totalPages})</p>

    <table class="data-table">
        <thead>
            <tr>
                <th>SKU</th>
                <th>Name</th>
                <th>Category</th>
                <th>Unit Price</th>
                <th>Qty On Hand</th>
                <th>Status</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
        <c:forEach var="p" items="${products}">
            <tr class="${p.lowStock ? 'row-warn' : ''}">
                <td><code>${p.sku}</code></td>
                <td><a href="${pageContext.request.contextPath}/products/${p.id}">${p.name}</a></td>
                <td>${p.category}</td>
                <td><fmt:formatNumber value="${p.unitPrice}" type="currency" currencySymbol="$"/></td>
                <td>${p.quantityOnHand}</td>
                <td>
                    <c:choose>
                        <c:when test="${p.quantityOnHand == 0}"><span class="badge danger">OUT</span></c:when>
                        <c:when test="${p.lowStock}"><span class="badge warn">LOW</span></c:when>
                        <c:otherwise><span class="badge ok">OK</span></c:otherwise>
                    </c:choose>
                </td>
                <td>
                    <a href="${pageContext.request.contextPath}/products/${p.id}/edit">Edit</a>
                    &nbsp;
                    <form method="post" action="${pageContext.request.contextPath}/products/${p.id}/delete"
                          style="display:inline"
                          onsubmit="return confirm('Remove ${p.name} from active inventory?')">
                        <button type="submit" class="link-btn">Remove</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty products}">
            <tr><td colspan="7" class="empty-row">No products found.</td></tr>
        </c:if>
        </tbody>
    </table>

    <c:if test="${totalPages > 1}">
        <div class="pagination">
            <c:forEach begin="1" end="${totalPages}" var="pg">
                <c:choose>
                    <c:when test="${pg == currentPage}"><span class="page-current">${pg}</span></c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/products?page=${pg}&category=${category}&q=${query}&sort=${sort}">${pg}</a>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </div>
    </c:if>

</div>

<div class="footer">ACME Internal Systems &mdash; Inventory Tracker v2.4.1</div>
</body>
</html>
