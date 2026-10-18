<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Internal Error &mdash; ACME Inventory Tracker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
</head>
<body>
<div class="header">
    <span class="logo">ACME Inventory Tracker</span>
    <nav>
        <a href="${pageContext.request.contextPath}/">Dashboard</a>
        <a href="${pageContext.request.contextPath}/products">Products</a>
    </nav>
</div>
<div class="content">
    <h2>500 &mdash; Internal Server Error</h2>
    <p>An unexpected error occurred. Please contact the system administrator if the problem persists.</p>
    <c:if test="${not empty errorMsg}">
        <p class="error-detail">${errorMsg}</p>
    </c:if>
    <p><a href="${pageContext.request.contextPath}/">&larr; Return to Dashboard</a></p>
</div>
<div class="footer">ACME Internal Systems &mdash; Inventory Tracker v2.4.1</div>
</body>
</html>
