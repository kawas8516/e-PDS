<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Reset Password</title>
  <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-100 min-h-screen flex items-center justify-center p-6">
  <div class="w-full max-w-md bg-white rounded-2xl shadow p-8">
    <h1 class="text-xl font-bold text-slate-800 mb-6">Reset Password</h1>

    <c:if test="${not empty error}">
      <div class="text-red-600 text-sm mb-3">${error}</div>
    </c:if>
    <c:if test="${not empty success}">
      <div class="text-green-600 text-sm mb-3">${success}</div>
    </c:if>

    <form action="${pageContext.request.contextPath}/ResetPasswordServlet" method="post" class="space-y-4">
      <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">

      <div>
        <label class="block text-sm font-semibold text-slate-700 mb-1">Username or Email</label>
        <input type="text" name="identifier" value="${identifier}" class="w-full border border-slate-300 rounded-lg px-3 py-2" />
      </div>

      <div>
        <label class="block text-sm font-semibold text-slate-700 mb-1">New Password</label>
        <input type="password" name="newPassword" class="w-full border border-slate-300 rounded-lg px-3 py-2" />
      </div>

      <div>
        <label class="block text-sm font-semibold text-slate-700 mb-1">Confirm Password</label>
        <input type="password" name="confirmPassword" class="w-full border border-slate-300 rounded-lg px-3 py-2" />
      </div>

      <button type="submit" class="w-full bg-blue-600 text-white font-semibold rounded-lg py-2">Reset Password</button>
    </form>

    <a href="${pageContext.request.contextPath}/index.jsp" class="block mt-4 text-sm text-blue-600">Back to Login</a>
  </div>
</body>
</html>
