<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Error – Digital Ration Card System</title>
  <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="min-h-screen bg-slate-100 flex items-center justify-center p-6">
  <div class="max-w-md w-full bg-white rounded-2xl shadow p-8 text-center">
    <div class="text-6xl font-black text-slate-300 mb-4">
      <%= request.getAttribute("jakarta.servlet.error.status_code") != null
          ? request.getAttribute("jakarta.servlet.error.status_code")
          : "Error" %>
    </div>
    <h1 class="text-xl font-bold text-slate-800 mb-2">Something went wrong</h1>
    <p class="text-slate-500 text-sm mb-6">
      <%= request.getAttribute("jakarta.servlet.error.message") != null
          ? request.getAttribute("jakarta.servlet.error.message")
          : "An unexpected error occurred." %>
    </p>
    <a href="${pageContext.request.contextPath}/LoginServlet"
       class="inline-block bg-blue-600 text-white font-semibold rounded-lg px-6 py-2 hover:bg-blue-700 transition-all">
      Back to Login
    </a>
  </div>
</body>
</html>
