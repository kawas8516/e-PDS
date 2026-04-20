<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Login – Digital Ration Card System</title>
  <script src="https://cdn.tailwindcss.com"></script>
  <script src="https://unpkg.com/lucide@latest"></script>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet"/>
  <style>
    body { font-family: 'Inter', sans-serif; }
    input:focus { outline: none; }
    .error-box { display: none; }
    .error-box.show { display: block; }
  </style>
</head>
<body class="min-h-screen bg-slate-100 flex items-center justify-center p-6">

  <div class="max-w-4xl w-full bg-white rounded-3xl shadow-2xl overflow-hidden flex flex-col md:flex-row border border-slate-200">
    <div class="md:w-1/2 bg-blue-600 p-12 text-white flex flex-col justify-center">
      <i data-lucide="shield-check" class="w-16 h-16 mb-6"></i>
      <h2 class="text-4xl font-bold mb-4 leading-tight">Securing Food,<br/>Empowering Citizens.</h2>
      <p class="text-blue-100 text-base">Official Government Portal for Ration Allocation and Family Management.</p>
    </div>

    <div class="md:w-1/2 p-12 bg-white">
      <div class="mb-8">
        <h3 class="text-2xl font-bold text-slate-800 mb-1">Welcome Back</h3>
        <p class="text-slate-500 text-sm">Log in to manage your digital card profile</p>
      </div>

      <div id="login-error" class="error-box mb-4 p-3 bg-red-50 border border-red-200 text-red-700 rounded-xl text-sm font-medium">${error}</div>
      <div class="mb-4 p-3 bg-green-50 border border-green-200 text-green-700 rounded-xl text-sm font-medium">${success}</div>

      <form id="login-form" action="${pageContext.request.contextPath}/LoginServlet" method="post" class="space-y-5" novalidate>
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <div>
          <label class="block text-sm font-semibold text-slate-700 mb-1">Aadhaar / Username</label>
          <div class="relative">
            <span class="absolute inset-y-0 left-3 flex items-center text-slate-400">
              <i data-lucide="user" class="w-5 h-5"></i>
            </span>
            <input id="username" name="username" type="text"
                   value="${lastUsername}"
                   placeholder="Enter 12-digit Aadhaar or Username"
                   class="w-full pl-11 pr-4 py-3 rounded-xl bg-slate-50 border border-slate-200 focus:ring-2 focus:ring-blue-500 transition-all text-sm"/>
          </div>
        </div>

        <div>
          <label class="block text-sm font-semibold text-slate-700 mb-1">Password</label>
          <div class="relative">
            <span class="absolute inset-y-0 left-3 flex items-center text-slate-400">
              <i data-lucide="lock" class="w-5 h-5"></i>
            </span>
            <input id="password" name="password" type="password"
                   placeholder="••••••••"
                   class="w-full pl-11 pr-4 py-3 rounded-xl bg-slate-50 border border-slate-200 focus:ring-2 focus:ring-blue-500 transition-all text-sm"/>
          </div>
        </div>

        <div class="pt-2">
          <button type="submit"
                  class="w-full bg-blue-600 text-white font-bold py-3 rounded-xl hover:bg-blue-700 transition-all shadow-lg shadow-blue-200 flex items-center justify-center gap-2 text-sm">
            <i data-lucide="log-in" class="w-4 h-4"></i> Login
          </button>
        </div>
      </form>
      <div class="mt-4 text-sm">
        <a href="${pageContext.request.contextPath}/ResetPasswordServlet" class="text-blue-600">Forgot password?</a>
      </div>
    </div>
  </div>

  <script>
    lucide.createIcons();

    const errorBox = document.getElementById('login-error');
    if (errorBox.textContent.trim().length > 0) {
      errorBox.classList.add('show');
    }

    document.getElementById('login-form').addEventListener('submit', function(e) {
      const username = document.getElementById('username').value.trim();
      const password = document.getElementById('password').value.trim();

      errorBox.classList.remove('show');

      if (!username) {
        e.preventDefault();
        errorBox.textContent = 'Please enter your Aadhaar number or username.';
        errorBox.classList.add('show');
        return;
      }

      if (!password) {
        e.preventDefault();
        errorBox.textContent = 'Please enter your password.';
        errorBox.classList.add('show');
      }
    });
  </script>
</body>
</html>
