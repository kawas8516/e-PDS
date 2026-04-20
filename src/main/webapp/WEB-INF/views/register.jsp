<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Citizen Registration - Digital Ration System</title>
    <!-- Tailwind CSS -->
    <script src="https://cdn.tailwindcss.com"></script>
    <!-- Lucide Icons -->
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');
        body { font-family: 'Inter', sans-serif; }

        .step-content { display: none; animation: slideIn 0.3s ease-out; }
        .step-content.active { display: block; }

        @keyframes slideIn {
            from { opacity: 0; transform: translateX(20px); }
            to { opacity: 1; transform: translateX(0); }
        }

        .progress-bar { transition: width 0.4s ease-in-out; }
    </style>
</head>
<body class="bg-slate-50 min-h-screen flex items-center justify-center p-6">

    <div class="max-w-2xl w-full bg-white rounded-3xl shadow-2xl overflow-hidden border border-slate-200">
        <!-- Header & Progress -->
        <div class="bg-blue-600 p-8 text-white">
            <div class="flex items-center gap-3 mb-4">
                <i data-lucide="user-plus" class="w-8 h-8"></i>
                <h1 class="text-2xl font-bold">New Citizen Registration</h1>
            </div>
            <p class="text-blue-100 text-sm mb-6">Complete the registration to access your digital ration profile.</p>

            <div class="w-full bg-blue-800/50 h-2 rounded-full overflow-hidden">
                <div id="progress" class="progress-bar bg-white h-full w-1/2"></div>
            </div>
            <div class="flex justify-between mt-3 text-[10px] font-bold uppercase tracking-widest text-blue-200">
                <span>Account</span>
                <span>Verification</span>
            </div>
        </div>

        <!-- Registration Form -->
        <form action="${pageContext.request.contextPath}/RegisterServlet" method="POST" id="regForm" class="p-8">

            <!-- Error Message -->
            <% if (request.getAttribute("error") != null && !request.getAttribute("error").toString().isEmpty()) { %>
            <div class="mb-6 p-4 bg-red-50 border border-red-100 text-red-600 rounded-2xl flex items-center gap-3">
                <i data-lucide="alert-circle" class="w-5 h-5"></i>
                <span class="text-sm font-medium">${error}</span>
            </div>
            <% } %>

            <!-- Step 1: Account Credentials -->
            <div id="step-1" class="step-content active space-y-5">
                <h2 class="text-lg font-bold text-slate-800">Step 1: Account Credentials</h2>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div class="space-y-1">
                        <label class="text-xs font-bold text-slate-500 uppercase">Username</label>
                        <input type="text" name="username" value="${prevUsername}" required class="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none">
                    </div>
                    <div class="space-y-1">
                        <label class="text-xs font-bold text-slate-500 uppercase">Email Address</label>
                        <input type="email" name="email" value="${prevEmail}" required class="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none">
                    </div>
                    <div class="space-y-1">
                        <label class="text-xs font-bold text-slate-500 uppercase">Password</label>
                        <input type="password" name="password" required class="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none">
                    </div>
                    <div class="space-y-1">
                        <label class="text-xs font-bold text-slate-500 uppercase">Confirm Password</label>
                        <input type="password" name="confirmPassword" required class="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none">
                    </div>
                </div>
            </div>

            <!-- Step 2: Identity Verification -->
            <div id="step-2" class="step-content space-y-5">
                <h2 class="text-lg font-bold text-slate-800">Step 2: Identity Verification</h2>
                <div class="space-y-4">
                    <div class="space-y-1">
                        <label class="text-xs font-bold text-slate-500 uppercase">Full Name</label>
                        <input type="text" name="fullName" value="${prevFullName}" required class="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none">
                    </div>
                    <div class="space-y-1">
                        <label class="text-xs font-bold text-slate-500 uppercase">Phone Number</label>
                        <input type="tel" name="phone" value="${prevPhone}" required class="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none">
                    </div>
                </div>
                <div class="flex items-center gap-2 pt-4">
                    <input type="checkbox" id="terms" required class="w-4 h-4 rounded">
                    <label for="terms" class="text-xs text-slate-500 font-medium">I hereby declare that the information provided is true to my knowledge.</label>
                </div>
            </div>

            <!-- Navigation Buttons -->
            <div class="flex justify-between items-center mt-10 pt-6 border-t border-slate-100">
                <button type="button" id="prevBtn" onclick="nextStep(-1)" class="invisible text-slate-400 font-bold text-sm hover:text-slate-600 flex items-center gap-2">
                    <i data-lucide="arrow-left" class="w-4 h-4"></i> Previous
                </button>
                <div class="flex gap-3">
                    <a href="${pageContext.request.contextPath}/index.jsp" class="px-6 py-3 text-slate-400 font-bold text-sm">Cancel</a>
                    <button type="button" id="nextBtn" onclick="nextStep(1)" class="bg-blue-600 text-white px-8 py-3 rounded-xl font-bold text-sm shadow-lg shadow-blue-200 flex items-center gap-2">
                        Next <i data-lucide="arrow-right" class="w-4 h-4"></i>
                    </button>
                </div>
            </div>
        </form>
    </div>

    <script>
        lucide.createIcons();
        let currentStep = 1;
        const totalSteps = 2;

        function nextStep(n) {
            const steps = document.querySelectorAll('.step-content');

            if (n > 0) {
                const inputs = steps[currentStep-1].querySelectorAll('input[required]');
                let valid = true;
                inputs.forEach(input => {
                    if (!input.value) {
                        input.classList.add('border-red-500');
                        valid = false;
                    } else {
                        input.classList.remove('border-red-500');
                    }
                });
                if (!valid) return;
            }

            steps[currentStep - 1].classList.remove('active');
            currentStep += n;

            if (currentStep > totalSteps) {
                document.getElementById('regForm').submit();
                return;
            }

            steps[currentStep - 1].classList.add('active');
            updateUI();
        }

        function updateUI() {
            const progress = (currentStep / totalSteps) * 100;
            document.getElementById('progress').style.width = progress + '%';

            const prevBtn = document.getElementById('prevBtn');
            prevBtn.style.visibility = (currentStep === 1) ? 'hidden' : 'visible';

            const nextBtn = document.getElementById('nextBtn');
            if (currentStep === totalSteps) {
                nextBtn.innerHTML = 'Register <i data-lucide="check" class="w-4 h-4"></i>';
                nextBtn.classList.replace('bg-blue-600', 'bg-green-600');
            } else {
                nextBtn.innerHTML = 'Next <i data-lucide="arrow-right" class="w-4 h-4"></i>';
                nextBtn.classList.replace('bg-green-600', 'bg-blue-600');
            }
            lucide.createIcons();
        }
    </script>
</body>
</html>
