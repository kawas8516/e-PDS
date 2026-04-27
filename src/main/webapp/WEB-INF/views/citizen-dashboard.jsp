<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    if (session.getAttribute("user") == null) {
        response.sendRedirect(request.getContextPath() + "/index.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Citizen Dashboard – Digital Ration Card System</title>
  <script src="https://cdn.tailwindcss.com"></script>
  <script src="https://unpkg.com/lucide@latest"></script>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet"/>
  <style>
    body { font-family: 'Inter', sans-serif; }
    .sidebar-btn { transition: all 0.2s; color: #94a3b8; }
    .sidebar-btn:hover { background: #1e293b; color: #fff; }
    .sidebar-btn.active { background: #2563eb; color: #fff; box-shadow: 0 10px 15px -3px rgba(37,99,235,0.25); }
    ::-webkit-scrollbar { width: 5px; }
    ::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
  </style>
</head>
<body class="bg-slate-50 h-screen overflow-hidden flex" style="font-family:'Inter',sans-serif">

  <!-- ══ SIDEBAR ══ -->
  <aside class="w-64 bg-slate-900 text-slate-300 h-full flex flex-col p-4 shadow-xl shrink-0">
    <div class="flex items-center gap-3 mb-10 px-2 pt-2">
      <div class="bg-blue-600 p-2.5 rounded-xl text-white shadow-lg shadow-blue-900/40">
        <i data-lucide="shield-check" class="w-6 h-6"></i>
      </div>
      <div>
        <h1 class="font-bold text-white text-base leading-none">Digital Ration</h1>
        <span class="text-blue-400 text-[10px] uppercase font-bold tracking-widest">Citizen Portal</span>
      </div>
    </div>

    <nav class="flex-1 space-y-1">
      <a href="${pageContext.request.contextPath}/citizen-dashboard.jsp" class="sidebar-btn active w-full flex items-center gap-3 px-4 py-3 rounded-xl">
        <i data-lucide="layout-dashboard" class="w-5 h-5"></i><span class="font-medium text-sm">Dashboard</span>
      </a>
      <a href="${pageContext.request.contextPath}/family.html" class="sidebar-btn w-full flex items-center gap-3 px-4 py-3 rounded-xl">
        <i data-lucide="users" class="w-5 h-5"></i><span class="font-medium text-sm">Family Members</span>
      </a>
      <a href="${pageContext.request.contextPath}/allocation.html" class="sidebar-btn w-full flex items-center gap-3 px-4 py-3 rounded-xl">
        <i data-lucide="clipboard-list" class="w-5 h-5"></i><span class="font-medium text-sm">Monthly Quota</span>
      </a>
      <a href="${pageContext.request.contextPath}/complaints.html" class="sidebar-btn w-full flex items-center gap-3 px-4 py-3 rounded-xl">
        <i data-lucide="alert-circle" class="w-5 h-5"></i><span class="font-medium text-sm">Complaints</span>
      </a>
    </nav>

    <div class="mt-auto pt-6 border-t border-slate-800">
      <!-- Logout must be POST to prevent CSRF logout (GET logout was disabled). -->
      <form method="post" action="${pageContext.request.contextPath}/LogoutServlet" id="logoutForm">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <button type="submit" class="w-full flex items-center gap-3 px-4 py-3 text-slate-400 hover:text-red-400 hover:bg-red-400/10 rounded-xl transition-all">
          <i data-lucide="log-out" class="w-5 h-5"></i><span class="font-medium text-sm">Logout</span>
        </button>
      </form>
    </div>
  </aside>

  <!-- ══ MAIN ══ -->
  <main class="flex-1 overflow-y-auto p-8">

    <!-- Header -->
    <header class="flex justify-between items-center mb-8">
      <div>
        <h2 class="text-2xl font-bold text-slate-800 tracking-tight">Dashboard Overview</h2>
        <p class="text-slate-500 text-sm flex items-center gap-1 mt-1">
          <i data-lucide="map-pin" class="w-4 h-4"></i> Dept. of Food &amp; Public Distribution • Region 04
        </p>
      </div>
      <div class="flex items-center gap-4">
        <button class="relative p-2 bg-white rounded-xl border border-slate-200 shadow-sm text-slate-400 hover:text-blue-600 transition-all">
          <i data-lucide="bell" class="w-5 h-5"></i>
          <span class="absolute top-1.5 right-1.5 w-2 h-2 bg-red-500 rounded-full border-2 border-white"></span>
        </button>
        <div class="flex items-center gap-3 bg-white px-4 py-2 rounded-2xl shadow-sm border border-slate-200">
          <div class="w-9 h-9 bg-gradient-to-br from-blue-500 to-blue-700 rounded-xl flex items-center justify-center text-white font-bold text-sm">JD</div>
          <div>
            <p class="text-sm font-bold text-slate-800 leading-none">${sessionScope.fullName != null ? sessionScope.fullName : 'Citizen'}</p>
            <div class="flex items-center gap-1 mt-1">
              <span class="w-1.5 h-1.5 bg-green-500 rounded-full"></span>
              <span class="text-[10px] text-slate-500 font-bold uppercase tracking-wider">Active</span>
            </div>
          </div>
        </div>
      </div>
    </header>

    <!-- Content Grid -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">

      <!-- Smart Ration Card -->
      <div class="md:col-span-2 bg-white rounded-3xl p-8 shadow-sm border border-slate-200 relative overflow-hidden">
        <div class="absolute top-0 right-0 w-40 h-40 bg-blue-50 rounded-full -mr-20 -mt-20 opacity-60"></div>
        <div class="flex justify-between items-start mb-8 relative z-10">
          <div>
            <h3 class="text-lg font-bold text-slate-800">Smart Ration Card</h3>
            <p class="text-slate-500 text-sm">Valid until Dec 2028</p>
          </div>
          <span class="px-4 py-2 bg-green-100 text-green-700 rounded-xl text-xs font-bold flex items-center gap-2">
            <i data-lucide="check-circle" class="w-4 h-4"></i> VERIFIED BPL
          </span>
        </div>
        <div class="grid grid-cols-2 gap-5 p-6 bg-slate-50 rounded-2xl border border-slate-100">
          <div>
            <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1">Card Holder</p>
            <p class="font-bold text-slate-700">${sessionScope.fullName != null ? sessionScope.fullName : 'Citizen'}</p>
          </div>
          <div>
            <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1">Ration Card ID</p>
            <p class="font-bold text-slate-700 font-mono text-sm">RC-2023-9844-1029</p>
          </div>
          <div>
            <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1">FPS Shop ID</p>
            <p class="font-bold text-slate-700 text-sm">FPS-7721-REGION-04</p>
          </div>
          <div>
            <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1">Mobile Linked</p>
            <p class="font-bold text-slate-700 text-sm">+91 ••••••4421</p>
          </div>
        </div>
      </div>

      <!-- Family Members Widget -->
      <div class="bg-white rounded-3xl p-8 shadow-sm border border-slate-200 flex flex-col items-center justify-center text-center">
        <div class="w-20 h-20 bg-blue-50 rounded-full flex items-center justify-center text-blue-600 mb-4">
          <i data-lucide="users" class="w-10 h-10"></i>
        </div>
        <h3 class="text-base font-bold text-slate-800">Family Members</h3>
        <div class="text-6xl font-black text-slate-800 my-3">03</div>
        <p class="text-[10px] text-slate-400 uppercase font-bold tracking-widest">Verified Units</p>
        <a href="${pageContext.request.contextPath}/family.html" class="mt-6 text-blue-600 text-sm font-bold hover:underline flex items-center gap-1">
          Manage Members <i data-lucide="arrow-right" class="w-4 h-4"></i>
        </a>
      </div>

      <!-- Quick Links -->
      <div class="md:col-span-3 grid grid-cols-2 md:grid-cols-4 gap-4">
        <a href="${pageContext.request.contextPath}/allocation.html" class="bg-white rounded-2xl p-5 shadow-sm border border-slate-200 hover:border-blue-300 hover:shadow-md transition-all text-center group">
          <div class="w-12 h-12 bg-amber-50 text-amber-600 rounded-xl flex items-center justify-center mx-auto mb-3 group-hover:bg-amber-100 transition-all">
            <i data-lucide="wheat" class="w-6 h-6"></i>
          </div>
          <p class="text-sm font-bold text-slate-700">Monthly Quota</p>
          <p class="text-xs text-slate-400 mt-0.5">View entitlement</p>
        </a>
        <a href="${pageContext.request.contextPath}/family.html" class="bg-white rounded-2xl p-5 shadow-sm border border-slate-200 hover:border-blue-300 hover:shadow-md transition-all text-center group">
          <div class="w-12 h-12 bg-blue-50 text-blue-600 rounded-xl flex items-center justify-center mx-auto mb-3 group-hover:bg-blue-100 transition-all">
            <i data-lucide="user-plus" class="w-6 h-6"></i>
          </div>
          <p class="text-sm font-bold text-slate-700">Add Member</p>
          <p class="text-xs text-slate-400 mt-0.5">Update family</p>
        </a>
        <a href="${pageContext.request.contextPath}/complaints.html" class="bg-white rounded-2xl p-5 shadow-sm border border-slate-200 hover:border-blue-300 hover:shadow-md transition-all text-center group">
          <div class="w-12 h-12 bg-red-50 text-red-500 rounded-xl flex items-center justify-center mx-auto mb-3 group-hover:bg-red-100 transition-all">
            <i data-lucide="message-square-warning" class="w-6 h-6"></i>
          </div>
          <p class="text-sm font-bold text-slate-700">Complaint</p>
          <p class="text-xs text-slate-400 mt-0.5">File a grievance</p>
        </a>
        <a href="#" class="bg-white rounded-2xl p-5 shadow-sm border border-slate-200 hover:border-blue-300 hover:shadow-md transition-all text-center group">
          <div class="w-12 h-12 bg-green-50 text-green-600 rounded-xl flex items-center justify-center mx-auto mb-3 group-hover:bg-green-100 transition-all">
            <i data-lucide="search" class="w-6 h-6"></i>
          </div>
          <p class="text-sm font-bold text-slate-700">Track Status</p>
          <p class="text-xs text-slate-400 mt-0.5">Application status</p>
        </a>
      </div>

      <!-- Recent Transactions -->
      <div class="md:col-span-3 bg-white rounded-3xl p-8 shadow-sm border border-slate-200">
        <div class="flex justify-between items-center mb-6">
          <h3 class="text-base font-bold text-slate-800">Recent Ration Collection</h3>
          <a href="${pageContext.request.contextPath}/allocation.html" class="text-blue-600 text-sm font-bold hover:underline">View All</a>
        </div>
        <div class="overflow-x-auto">
          <table class="w-full text-left text-sm">
            <thead>
              <tr class="text-[10px] font-bold text-slate-400 uppercase tracking-widest border-b border-slate-100">
                <th class="pb-4">Date</th>
                <th class="pb-4">Commodity</th>
                <th class="pb-4">Quantity</th>
                <th class="pb-4">Amount Paid</th>
                <th class="pb-4">Status</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-50">
              <tr>
                <td class="py-4 text-slate-600">Oct 12, 2023</td>
                <td class="py-4 font-bold text-slate-700">Wheat</td>
                <td class="py-4 text-slate-600">15 KG</td>
                <td class="py-4 font-bold text-slate-700">₹ 30.00</td>
                <td class="py-4"><span class="px-3 py-1 bg-green-50 text-green-600 rounded-full text-[10px] font-black uppercase">Collected</span></td>
              </tr>
              <tr>
                <td class="py-4 text-slate-600">Oct 12, 2023</td>
                <td class="py-4 font-bold text-slate-700">Rice</td>
                <td class="py-4 text-slate-600">10 KG</td>
                <td class="py-4 font-bold text-slate-700">₹ 30.00</td>
                <td class="py-4"><span class="px-3 py-1 bg-green-50 text-green-600 rounded-full text-[10px] font-black uppercase">Collected</span></td>
              </tr>
              <tr>
                <td class="py-4 text-slate-600">Sep 08, 2023</td>
                <td class="py-4 font-bold text-slate-700">Wheat</td>
                <td class="py-4 text-slate-600">15 KG</td>
                <td class="py-4 font-bold text-slate-700">₹ 30.00</td>
                <td class="py-4"><span class="px-3 py-1 bg-green-50 text-green-600 rounded-full text-[10px] font-black uppercase">Collected</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </main>

  <script>lucide.createIcons();</script>
</body>
</html>