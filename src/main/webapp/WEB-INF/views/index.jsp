<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>ACME Inventory Tracker</title>
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

    <h2>Dashboard</h2>

    <c:if test="${not empty dashboardError}">
        <div class="alert alert-error">Dashboard data unavailable: ${dashboardError}</div>
    </c:if>

    <div class="stats-row">
        <div class="stat-box">
            <div class="stat-number">${totalProducts}</div>
            <div class="stat-label">Active Products</div>
        </div>
        <div class="stat-box warn">
            <div class="stat-number">${lowStockCount}</div>
            <div class="stat-label">Low Stock</div>
        </div>
        <div class="stat-box danger">
            <div class="stat-number">${outOfStockCount}</div>
            <div class="stat-label">Out of Stock</div>
        </div>
        <div class="stat-box">
            <div class="stat-number">
                <fmt:formatNumber value="${totalValue}" type="currency" currencySymbol="$" maxFractionDigits="0"/>
            </div>
            <div class="stat-label">Inventory Value</div>
        </div>
    </div>

    <c:if test="${not empty byCategory}">
        <h3>By Category</h3>
        <table class="data-table">
            <thead><tr><th>Category</th><th>Products</th></tr></thead>
            <tbody>
            <c:forEach var="entry" items="${byCategory}">
                <tr>
                    <td><a href="${pageContext.request.contextPath}/products?category=${entry.key}">${entry.key}</a></td>
                    <td>${entry.value}</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:if>

</div>

<div class="footer">ACME Internal Systems &mdash; Inventory Tracker v2.4.1</div>
</body>
</html>
