<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title><c:choose>
        <c:when test="${not empty product.id}">Edit ${product.name}</c:when>
        <c:otherwise>New Product</c:otherwise>
    </c:choose> &mdash; ACME Inventory Tracker</title>
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

    <c:if test="${not empty errorMsg}">
        <div class="alert alert-error">${errorMsg}</div>
    </c:if>

    <h2><c:choose>
        <c:when test="${formAction == 'update'}">Edit Product</c:when>
        <c:otherwise>New Product</c:otherwise>
    </c:choose></h2>

    <c:set var="actionUrl">
        <c:choose>
            <c:when test="${formAction == 'update'}">${pageContext.request.contextPath}/products/${product.id}/update</c:when>
            <c:otherwise>${pageContext.request.contextPath}/products/create</c:otherwise>
        </c:choose>
    </c:set>

    <form method="post" action="${actionUrl}" class="edit-form">

        <div class="form-row">
            <label>SKU <span class="required">*</span><br>
                <input type="text" name="sku" value="${product.sku}" required maxlength="50"/>
            </label>
            <label>Name <span class="required">*</span><br>
                <input type="text" name="name" value="${product.name}" required maxlength="200"/>
            </label>
        </div>

        <div class="form-row">
            <label>Category<br>
                <select name="category">
                    <option value="">-- Select --</option>
                    <c:forEach var="cat" items="${categories}">
                        <option value="${cat}" ${cat == product.category ? 'selected' : ''}>${cat}</option>
                    </c:forEach>
                    <option value="__new__">+ New category…</option>
                </select>
            </label>
            <label>Supplier Code<br>
                <input type="text" name="supplierCode" value="${product.supplierCode}" maxlength="50"/>
            </label>
        </div>

        <div class="form-row">
            <label>Unit Price <span class="required">*</span><br>
                <input type="number" name="unitPrice" value="${product.unitPrice}" step="0.01" min="0" required/>
            </label>
            <label>Qty On Hand<br>
                <input type="number" name="quantityOnHand" value="${product.quantityOnHand}" min="0"/>
            </label>
            <label>Reorder Point<br>
                <input type="number" name="reorderPoint" value="${product.reorderPoint}" min="0"/>
            </label>
        </div>

        <div class="form-row">
            <label style="width:100%">Description<br>
                <textarea name="description" rows="4" maxlength="2000">${product.description}</textarea>
            </label>
        </div>

        <div class="form-actions">
            <button type="submit" class="btn-primary">
                <c:choose>
                    <c:when test="${formAction == 'update'}">Save Changes</c:when>
                    <c:otherwise>Create Product</c:otherwise>
                </c:choose>
            </button>
            <a href="${pageContext.request.contextPath}/products" class="btn-secondary">Cancel</a>
        </div>

    </form>
</div>

<div class="footer">ACME Internal Systems &mdash; Inventory Tracker v2.4.1</div>
</body>
</html>
