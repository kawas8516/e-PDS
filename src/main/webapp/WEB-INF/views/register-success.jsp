<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registration Successful - Digital Ration System</title>
    <!-- Tailwind CSS -->
    <script src="https://cdn.tailwindcss.com"></script>
    <!-- Lucide Icons -->
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');
        body { font-family: 'Inter', sans-serif; }
    </style>
</head>
<body class="bg-slate-50 min-h-screen flex items-center justify-center p-6">

    <div class="max-w-md w-full bg-white rounded-3xl shadow-2xl p-10 border border-slate-200 text-center">
        <div class="w-20 h-20 bg-green-100 text-green-600 rounded-full flex items-center justify-center mx-auto mb-6">
            <i data-lucide="check-circle" class="w-10 h-10"></i>
        </div>
        
        <h1 class="text-2xl font-bold text-slate-800 mb-3">Registration Successful!</h1>
        <p class="text-slate-500 mb-8">${successMessage}</p>

        <noscript>
            <a href="${pageContext.request.contextPath}/index.jsp" class="w-full bg-blue-600 text-white px-8 py-4 rounded-2xl font-bold shadow-lg shadow-blue-200 hover:bg-blue-700 transition-all flex items-center justify-center gap-3">
                Go to Login <i data-lucide="arrow-right" class="w-5 h-5"></i>
            </a>
        </noscript>
    </div>

    <script>
        lucide.createIcons();

        window.onload = function () {
            alert("Registration successful! Click OK to go to the login page.");
            window.location.href = "${pageContext.request.contextPath}/index.jsp";
        };
    </script>
</body>
</html>
