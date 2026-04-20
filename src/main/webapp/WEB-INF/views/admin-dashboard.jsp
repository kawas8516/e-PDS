<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${empty sessionScope.user or not sessionScope.user.admin}">
    <c:redirect url="/index.jsp" />
</c:if>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Admin Dashboard – Digital Ration Card System</title>
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
<body class="bg-slate-50 h-screen overflow-hidden flex">

  <!-- ══ SIDEBAR ══ -->
  <aside class="w-64 bg-slate-900 text-slate-300 h-full flex flex-col p-4 shadow-xl shrink-0">
    <div class="flex items-center gap-3 mb-10 px-2 pt-2">
      <div class="bg-blue-600 p-2.5 rounded-xl text-white shadow-lg shadow-blue-900/40">
        <i data-lucide="shield-check" class="w-6 h-6"></i>
      </div>
      <div>
        <h1 class="font-bold text-white text-base leading-none">Digital Ration</h1>
        <span class="text-orange-400 text-[10px] uppercase font-bold tracking-widest">Admin Portal</span>
      </div>
    </div>

    <nav class="flex-1 space-y-1">
      <a href="${pageContext.request.contextPath}/admin-dashboard.jsp" class="sidebar-btn active w-full flex items-center gap-3 px-4 py-3 rounded-xl">
        <i data-lucide="bar-chart-3" class="w-5 h-5"></i><span class="font-medium text-sm">System Stats</span>
      </a>
      <a href="${pageContext.request.contextPath}/approvals.html" class="sidebar-btn w-full flex items-center gap-3 px-4 py-3 rounded-xl">
        <i data-lucide="clock" class="w-5 h-5"></i><span class="font-medium text-sm">New Applications</span>
      </a>
      <a href="${pageContext.request.contextPath}/stock.html" class="sidebar-btn w-full flex items-center gap-3 px-4 py-3 rounded-xl">
        <i data-lucide="package" class="w-5 h-5"></i><span class="font-medium text-sm">Stock Management</span>
      </a>
      <a href="${pageContext.request.contextPath}/reports.html" class="sidebar-btn w-full flex items-center gap-3 px-4 py-3 rounded-xl">
        <i data-lucide="file-text" class="w-5 h-5"></i><span class="font-medium text-sm">Reports</span>
      </a>
    </nav>

    <div class="mt-auto pt-6 border-t border-slate-800">
      <a href="${pageContext.request.contextPath}/LogoutServlet" class="w-full flex items-center gap-3 px-4 py-3 text-slate-400 hover:text-red-400 hover:bg-red-400/10 rounded-xl transition-all">
        <i data-lucide="log-out" class="w-5 h-5"></i><span class="font-medium text-sm">Logout</span>
      </a>
    </div>
  </aside>

  <!-- ══ MAIN ══ -->
  <main class="flex-1 overflow-y-auto p-8">

    <header class="flex justify-between items-center mb-8">
      <div>
        <h2 class="text-2xl font-bold text-slate-800 tracking-tight">System Dashboard</h2>
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
          <div class="w-9 h-9 bg-gradient-to-br from-slate-600 to-slate-800 rounded-xl flex items-center justify-center text-white font-bold text-sm">AD</div>
          <div>
            <p class="text-sm font-bold text-slate-800 leading-none">${sessionScope.fullName != null ? sessionScope.fullName : 'Administrator'}</p>
            <div class="flex items-center gap-1 mt-1">
              <span class="w-1.5 h-1.5 bg-green-500 rounded-full"></span>
              <span class="text-[10px] text-slate-500 font-bold uppercase tracking-wider">Active</span>
            </div>
          </div>
        </div>
      </div>
    </header>

    <!-- Stat Cards -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-5 mb-8">
      <div class="bg-white rounded-3xl p-6 shadow-sm border border-slate-200 flex items-center gap-4">
        <div class="w-14 h-14 bg-blue-50 text-blue-600 rounded-2xl flex items-center justify-center shrink-0">
          <i data-lucide="credit-card" class="w-7 h-7"></i>
        </div>
        <div>
          <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Total Cards</p>
          <p class="text-2xl font-black text-slate-800 mt-0.5">1,240</p>
        </div>
      </div>
      <div class="bg-white rounded-3xl p-6 shadow-sm border border-slate-200 flex items-center gap-4">
        <div class="w-14 h-14 bg-orange-50 text-orange-500 rounded-2xl flex items-center justify-center shrink-0">
          <i data-lucide="clock" class="w-7 h-7"></i>
        </div>
        <div>
          <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Pending Apps</p>
          <p class="text-2xl font-black text-slate-800 mt-0.5">45</p>
        </div>
      </div>
      <div class="bg-white rounded-3xl p-6 shadow-sm border border-slate-200 flex items-center gap-4">
        <div class="w-14 h-14 bg-red-50 text-red-500 rounded-2xl flex items-center justify-center shrink-0">
          <i data-lucide="package-x" class="w-7 h-7"></i>
        </div>
        <div>
          <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Low Stock</p>
          <p class="text-2xl font-black text-slate-800 mt-0.5">02</p>
        </div>
      </div>
      <div class="bg-white rounded-3xl p-6 shadow-sm border border-slate-200 flex items-center gap-4">
        <div class="w-14 h-14 bg-green-50 text-green-600 rounded-2xl flex items-center justify-center shrink-0">
          <i data-lucide="activity" class="w-7 h-7"></i>
        </div>
        <div>
          <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Efficiency</p>
          <p class="text-2xl font-black text-slate-800 mt-0.5">94.2%</p>
        </div>
      </div>
    </div>

    <!-- Middle Row -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">

      <!-- Stock Inventory -->
      <div class="bg-white rounded-3xl p-8 shadow-sm border border-slate-200">
        <div class="flex justify-between items-center mb-7">
          <h3 class="text-base font-bold text-slate-800">Inventory Levels</h3>
          <a href="${pageContext.request.contextPath}/stock.html" class="text-blue-600 text-sm font-bold hover:underline flex items-center gap-1">
            Manage <i data-lucide="arrow-right" class="w-4 h-4"></i>
          </a>
        </div>
        <div class="space-y-6">
          <div>
            <div class="flex justify-between mb-2">
              <span class="text-sm font-bold text-slate-700">Rice</span>
              <span class="text-xs font-bold text-slate-400 uppercase tracking-widest">82% Full</span>
            </div>
            <div class="w-full h-3 bg-slate-100 rounded-full overflow-hidden">
              <div class="w-[82%] h-full bg-blue-600 rounded-full"></div>
            </div>
          </div>
          <div>
            <div class="flex justify-between mb-2">
              <span class="text-sm font-bold text-slate-700">Wheat <span class="text-red-500 text-xs ml-1">⚠ Critical</span></span>
              <span class="text-xs font-bold text-red-500 uppercase tracking-widest">12% Remaining</span>
            </div>
            <div class="w-full h-3 bg-slate-100 rounded-full overflow-hidden">
              <div class="w-[12%] h-full bg-red-500 rounded-full"></div>
            </div>
          </div>
          <div>
            <div class="flex justify-between mb-2">
              <span class="text-sm font-bold text-slate-700">Sugar</span>
              <span class="text-xs font-bold text-slate-400 uppercase tracking-widest">55% Full</span>
            </div>
            <div class="w-full h-3 bg-slate-100 rounded-full overflow-hidden">
              <div class="w-[55%] h-full bg-blue-600 rounded-full"></div>
            </div>
          </div>
          <div>
            <div class="flex justify-between mb-2">
              <span class="text-sm font-bold text-slate-700">Kerosene</span>
              <span class="text-xs font-bold text-slate-400 uppercase tracking-widest">30% Remaining</span>
            </div>
            <div class="w-full h-3 bg-slate-100 rounded-full overflow-hidden">
              <div class="w-[30%] h-full bg-amber-500 rounded-full"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- Recent Applications -->
      <div class="bg-white rounded-3xl p-8 shadow-sm border border-slate-200">
        <div class="flex justify-between items-center mb-7">
          <h3 class="text-base font-bold text-slate-800">Recent Applications</h3>
          <a href="${pageContext.request.contextPath}/approvals.html" class="text-blue-600 text-sm font-bold hover:underline flex items-center gap-1">
            View All <i data-lucide="arrow-right" class="w-4 h-4"></i>
          </a>
        </div>
        <div class="space-y-3">
          <div class="flex items-center gap-4 p-3 rounded-2xl hover:bg-slate-50 transition-all cursor-pointer">
            <div class="w-10 h-10 bg-blue-100 rounded-xl flex items-center justify-center font-bold text-blue-600 text-sm shrink-0">MK</div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-bold text-slate-800">Mukesh Kumar</p>
              <p class="text-xs text-slate-500 truncate">New Card Application</p>
            </div>
            <span class="px-3 py-1 bg-orange-50 text-orange-500 text-[10px] font-black uppercase rounded-lg shrink-0">Pending</span>
          </div>
          <div class="flex items-center gap-4 p-3 rounded-2xl hover:bg-slate-50 transition-all cursor-pointer">
            <div class="w-10 h-10 bg-purple-100 rounded-xl flex items-center justify-center font-bold text-purple-600 text-sm shrink-0">SR</div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-bold text-slate-800">Suman Rani</p>
              <p class="text-xs text-slate-500 truncate">Address Change Request</p>
            </div>
            <span class="px-3 py-1 bg-green-50 text-green-500 text-[10px] font-black uppercase rounded-lg shrink-0">Approved</span>
          </div>
          <div class="flex items-center gap-4 p-3 rounded-2xl hover:bg-slate-50 transition-all cursor-pointer">
            <div class="w-10 h-10 bg-amber-100 rounded-xl flex items-center justify-center font-bold text-amber-600 text-sm shrink-0">RP</div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-bold text-slate-800">Ravi Patel</p>
              <p class="text-xs text-slate-500 truncate">Member Addition – 2 persons</p>
            </div>
            <span class="px-3 py-1 bg-orange-50 text-orange-500 text-[10px] font-black uppercase rounded-lg shrink-0">Pending</span>
          </div>
          <div class="flex items-center gap-4 p-3 rounded-2xl hover:bg-slate-50 transition-all cursor-pointer">
            <div class="w-10 h-10 bg-red-100 rounded-xl flex items-center justify-center font-bold text-red-600 text-sm shrink-0">AN</div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-bold text-slate-800">Anita Nair</p>
              <p class="text-xs text-slate-500 truncate">New Card Application</p>
            </div>
            <span class="px-3 py-1 bg-red-50 text-red-500 text-[10px] font-black uppercase rounded-lg shrink-0">Rejected</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Complaints Summary -->
    <div class="bg-white rounded-3xl p-8 shadow-sm border border-slate-200">
      <div class="flex justify-between items-center mb-6">
        <h3 class="text-base font-bold text-slate-800">Open Complaints</h3>
        <span class="px-3 py-1 bg-red-50 text-red-500 text-xs font-black uppercase rounded-full">8 Unresolved</span>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full text-sm text-left">
          <thead>
            <tr class="text-[10px] font-bold text-slate-400 uppercase tracking-widest border-b border-slate-100">
              <th class="pb-4">Citizen</th>
              <th class="pb-4">Type</th>
              <th class="pb-4">Filed On</th>
              <th class="pb-4">Priority</th>
              <th class="pb-4">Action</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-50">
            <tr>
              <td class="py-4 font-bold text-slate-700">Priya Sharma</td>
              <td class="py-4 text-slate-600">Short Supply</td>
              <td class="py-4 text-slate-500">Oct 15, 2023</td>
              <td class="py-4"><span class="px-2 py-1 bg-red-50 text-red-500 rounded-lg text-[10px] font-black">HIGH</span></td>
              <td class="py-4"><button class="text-blue-600 text-xs font-bold hover:underline">Review</button></td>
            </tr>
            <tr>
              <td class="py-4 font-bold text-slate-700">Ram Lal</td>
              <td class="py-4 text-slate-600">Wrong Quantity</td>
              <td class="py-4 text-slate-500">Oct 14, 2023</td>
              <td class="py-4"><span class="px-2 py-1 bg-amber-50 text-amber-500 rounded-lg text-[10px] font-black">MED</span></td>
              <td class="py-4"><button class="text-blue-600 text-xs font-bold hover:underline">Review</button></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </main>

  <script>lucide.createIcons();</script>
</body>
</html>