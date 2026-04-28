<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, java.util.Map, java.math.BigDecimal" %>
<%
    com.ration.model.User __u = (com.ration.model.User) session.getAttribute("user");
    if (__u == null || !__u.isAdmin()) {
        response.sendRedirect(request.getContextPath() + "/LoginServlet");
        return;
    }
    List<Map<String, Object>> stocks = (List<Map<String, Object>>) request.getAttribute("stocks");
    int totalItems = stocks != null ? stocks.size() : 0;
    long lowStockCount = request.getAttribute("lowStockCount") != null ? (Long) request.getAttribute("lowStockCount") : 0;
    String msg = (String) request.getAttribute("msg");
    if (stocks == null) stocks = new java.util.ArrayList<>();
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Stock Management – Admin Portal</title>
  <script src="https://cdn.tailwindcss.com"></script>
  <script src="https://unpkg.com/lucide@latest"></script>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet"/>
  <style>
    body { font-family: 'Inter', sans-serif; }
    .sidebar-btn { transition: all 0.2s; color: #94a3b8; }
    .sidebar-btn:hover { background: #1e293b; color: #fff; }
    .sidebar-btn.active { background: #2563eb; color: #fff; }
    .modal { display: none; }
    .modal.open { display: flex; }
    ::-webkit-scrollbar { width: 5px; }
    ::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
  </style>
</head>
<body class="bg-slate-50 h-screen overflow-hidden flex">

  <!-- Sidebar -->
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
      <a href="${pageContext.request.contextPath}/admin-dashboard.jsp" class="sidebar-btn w-full flex items-center gap-3 px-4 py-3 rounded-xl">
        <i data-lucide="bar-chart-3" class="w-5 h-5"></i><span class="font-medium text-sm">System Stats</span>
      </a>
      <a href="${pageContext.request.contextPath}/approvals.html" class="sidebar-btn w-full flex items-center gap-3 px-4 py-3 rounded-xl">
        <i data-lucide="clock" class="w-5 h-5"></i><span class="font-medium text-sm">New Applications</span>
      </a>
      <a href="${pageContext.request.contextPath}/StockServlet" class="sidebar-btn active w-full flex items-center gap-3 px-4 py-3 rounded-xl">
        <i data-lucide="package" class="w-5 h-5"></i><span class="font-medium text-sm">Stock Management</span>
      </a>
      <a href="${pageContext.request.contextPath}/reports.html" class="sidebar-btn w-full flex items-center gap-3 px-4 py-3 rounded-xl">
        <i data-lucide="file-text" class="w-5 h-5"></i><span class="font-medium text-sm">Reports</span>
      </a>
      <a href="${pageContext.request.contextPath}/AdminFamilyServlet" class="sidebar-btn w-full flex items-center gap-3 px-4 py-3 rounded-xl">
        <i data-lucide="users" class="w-5 h-5"></i><span class="font-medium text-sm">Family Management</span>
      </a>
    </nav>
    <div class="mt-auto pt-6 border-t border-slate-800">
      <form method="post" action="${pageContext.request.contextPath}/LogoutServlet">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <button type="submit" class="w-full flex items-center gap-3 px-4 py-3 text-slate-400 hover:text-red-400 hover:bg-red-400/10 rounded-xl transition-all">
          <i data-lucide="log-out" class="w-5 h-5"></i><span class="font-medium text-sm">Logout</span>
        </button>
      </form>
    </div>
  </aside>

  <!-- Main -->
  <main class="flex-1 overflow-y-auto p-8">
    <div class="flex justify-between items-center mb-8">
      <div>
        <h2 class="text-2xl font-bold text-slate-800 tracking-tight">Stock Management</h2>
        <p class="text-slate-500 text-sm mt-1">Monitor and update commodity inventory levels</p>
      </div>
      <button onclick="openAddModal()" class="bg-blue-600 text-white font-bold px-5 py-3 rounded-xl hover:bg-blue-700 transition-all shadow-lg shadow-blue-100 flex items-center gap-2 text-sm">
        <i data-lucide="plus" class="w-4 h-4"></i> Add Item
      </button>
    </div>

    <% if (msg != null) { %>
    <div class="mb-6 p-4 bg-green-50 border border-green-200 text-green-700 rounded-2xl text-sm font-medium flex items-center gap-2">
      <i data-lucide="check-circle" class="w-4 h-4"></i>
      <% if ("item_added".equals(msg)) { %>Added stock item successfully.
      <% } else if ("qty_updated".equals(msg)) { %>Updated quantity successfully.
      <% } else if ("price_updated".equals(msg)) { %>Updated price successfully.
      <% } else if ("threshold_updated".equals(msg)) { %>Updated threshold successfully.
      <% } else if ("item_deleted".equals(msg)) { %>Deleted item successfully.
      <% } else { %><%= msg %><% } %>
    </div>
    <% } %>

    <% if (lowStockCount > 0) { %>
    <div class="bg-red-50 border border-red-200 rounded-2xl p-4 mb-6 flex items-center gap-3">
      <div class="w-9 h-9 bg-red-100 text-red-500 rounded-xl flex items-center justify-center shrink-0">
        <i data-lucide="alert-triangle" class="w-5 h-5"></i>
      </div>
      <div class="flex-1">
        <p class="font-bold text-red-700 text-sm">Low Stock Alert</p>
        <p class="text-xs text-red-600"><%= lowStockCount %> item(s) below threshold. Immediate replenishment required.</p>
      </div>
    </div>
    <% } %>

    <!-- Summary Cards -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-slate-200 flex items-center gap-4">
        <div class="w-12 h-12 bg-blue-50 text-blue-600 rounded-xl flex items-center justify-center">
          <i data-lucide="package" class="w-6 h-6"></i>
        </div>
        <div>
          <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Total Items</p>
          <p class="text-2xl font-black text-slate-800"><%= totalItems %></p>
        </div>
      </div>
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-slate-200 flex items-center gap-4">
        <div class="w-12 h-12 bg-red-50 text-red-500 rounded-xl flex items-center justify-center">
          <i data-lucide="alert-circle" class="w-6 h-6"></i>
        </div>
        <div>
          <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Low Stock</p>
          <p class="text-2xl font-black text-slate-800"><%= lowStockCount %></p>
        </div>
      </div>
    </div>

    <!-- Stock Table -->
    <div class="bg-white rounded-3xl shadow-sm border border-slate-200 overflow-hidden">
      <div class="p-6 border-b border-slate-100">
        <h3 class="font-bold text-slate-800">Inventory Items</h3>
      </div>

      <% if (stocks.isEmpty()) { %>
      <div class="p-12 text-center text-slate-400">
        <i data-lucide="package" class="w-12 h-12 mx-auto mb-3 opacity-40"></i>
        <p class="font-medium">No stock items yet. Add one to get started.</p>
      </div>
      <% } else { %>
      <div class="overflow-x-auto">
        <table class="w-full text-sm text-left">
          <thead>
            <tr class="text-[10px] font-bold text-slate-400 uppercase tracking-widest bg-slate-50 border-b border-slate-100">
              <th class="px-6 py-4">Item Name</th>
              <th class="px-6 py-4">Quantity</th>
              <th class="px-6 py-4">Unit Price</th>
              <th class="px-6 py-4">Threshold</th>
              <th class="px-6 py-4">Status</th>
              <th class="px-6 py-4">Actions</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-50">
          <% for (Map<String, Object> stock : stocks) {
              int qty = stock.get("quantity") != null ? (Integer) stock.get("quantity") : 0;
              int threshold = stock.get("thresholdLimit") != null ? (Integer) stock.get("thresholdLimit") : 0;
              boolean isLow = threshold > 0 && qty <= threshold;
              String statusBadge = isLow ? "bg-red-50 text-red-600" : "bg-green-50 text-green-600";
              String statusText = isLow ? "Low Stock" : "Good";
          %>
          <tr class="hover:bg-slate-50 transition-all">
            <td class="px-6 py-4 font-bold text-slate-700"><%= stock.get("itemName") %></td>
            <td class="px-6 py-4">
              <form method="post" action="<%= request.getContextPath() %>/StockServlet" class="flex items-center gap-1">
                <input type="hidden" name="action" value="updateQty"/>
                <input type="hidden" name="itemId" value="<%= stock.get("itemId") %>"/>
                <input type="number" name="quantity" value="<%= qty %>" min="0"
                       class="w-20 px-2 py-1 rounded-lg bg-slate-50 border border-slate-200 text-xs font-mono focus:ring-2 focus:ring-blue-400 outline-none"/>
                <button type="submit" class="p-1 text-blue-500 hover:text-blue-700">
                  <i data-lucide="save" class="w-3 h-3"></i>
                </button>
              </form>
            </td>
            <td class="px-6 py-4">
              <form method="post" action="<%= request.getContextPath() %>/StockServlet" class="flex items-center gap-1">
                <input type="hidden" name="action" value="updatePrice"/>
                <input type="hidden" name="itemId" value="<%= stock.get("itemId") %>"/>
                <input type="number" name="unitPrice" value="<%= stock.get("unitPrice") %>" min="0" step="0.01"
                       class="w-20 px-2 py-1 rounded-lg bg-slate-50 border border-slate-200 text-xs font-mono focus:ring-2 focus:ring-blue-400 outline-none"/>
                <button type="submit" class="p-1 text-blue-500 hover:text-blue-700">
                  <i data-lucide="save" class="w-3 h-3"></i>
                </button>
              </form>
            </td>
            <td class="px-6 py-4">
              <form method="post" action="<%= request.getContextPath() %>/StockServlet" class="flex items-center gap-1">
                <input type="hidden" name="action" value="updateThreshold"/>
                <input type="hidden" name="itemId" value="<%= stock.get("itemId") %>"/>
                <input type="number" name="thresholdLimit" value="<%= threshold %>" min="0"
                       class="w-20 px-2 py-1 rounded-lg bg-slate-50 border border-slate-200 text-xs font-mono focus:ring-2 focus:ring-blue-400 outline-none"/>
                <button type="submit" class="p-1 text-blue-500 hover:text-blue-700">
                  <i data-lucide="save" class="w-3 h-3"></i>
                </button>
              </form>
            </td>
            <td class="px-6 py-4">
              <span class="px-3 py-1 <%= statusBadge %> text-[10px] font-black uppercase rounded-lg"><%= statusText %></span>
            </td>
            <td class="px-6 py-4">
              <form method="post" action="<%= request.getContextPath() %>/StockServlet" style="display: inline;"
                    onsubmit="return confirm('Delete this item?');">
                <input type="hidden" name="action" value="delete"/>
                <input type="hidden" name="itemId" value="<%= stock.get("itemId") %>"/>
                <button type="submit" class="text-red-400 hover:text-red-600 transition-colors">
                  <i data-lucide="trash-2" class="w-4 h-4"></i>
                </button>
              </form>
            </td>
          </tr>
          <% } %>
          </tbody>
        </table>
      </div>
      <% } %>
    </div>
  </main>

  <!-- Add Stock Modal -->
  <div id="add-modal" class="modal fixed inset-0 bg-black/50 backdrop-blur-sm z-50 items-center justify-center p-6">
    <div class="bg-white rounded-3xl shadow-2xl w-full max-w-lg border border-slate-200">
      <div class="flex justify-between items-center p-7 border-b border-slate-100">
        <h3 class="font-bold text-slate-800 text-lg">Add Stock Item</h3>
        <button onclick="closeAddModal()" class="text-slate-400 hover:text-slate-600 transition-colors">
          <i data-lucide="x" class="w-6 h-6"></i>
        </button>
      </div>
      <form action="${pageContext.request.contextPath}/StockServlet" method="POST" class="p-7 space-y-4">
        <input type="hidden" name="action" value="add"/>
        <div>
          <label class="block text-xs font-bold text-slate-500 uppercase tracking-widest mb-1">Item Name *</label>
          <input type="text" name="itemName" placeholder="e.g., Rice, Wheat, Sugar" required
            class="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none text-sm"/>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-xs font-bold text-slate-500 uppercase tracking-widest mb-1">Quantity *</label>
            <input type="number" name="quantity" min="0" placeholder="0" required
              class="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none text-sm"/>
          </div>
          <div>
            <label class="block text-xs font-bold text-slate-500 uppercase tracking-widest mb-1">Unit Price</label>
            <input type="number" name="unitPrice" min="0" step="0.01" placeholder="0.00"
              class="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none text-sm"/>
          </div>
        </div>
        <div>
          <label class="block text-xs font-bold text-slate-500 uppercase tracking-widest mb-1">Alert Threshold</label>
          <input type="number" name="thresholdLimit" min="0" placeholder="0"
            class="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none text-sm"/>
          <p class="text-[10px] text-slate-400 mt-1">Alert when quantity drops below this value.</p>
        </div>
        <div class="flex gap-3 pt-2">
          <button type="button" onclick="closeAddModal()" class="flex-1 py-3 rounded-xl border border-slate-200 text-slate-600 font-bold text-sm hover:bg-slate-50 transition-all">Cancel</button>
          <button type="submit" class="flex-1 py-3 rounded-xl bg-blue-600 text-white font-bold text-sm hover:bg-blue-700 transition-all shadow-lg shadow-blue-100">Add Item</button>
        </div>
      </form>
    </div>
  </div>

  <script>
    lucide.createIcons();
    function openAddModal()  { document.getElementById('add-modal').classList.add('open'); }
    function closeAddModal() { document.getElementById('add-modal').classList.remove('open'); }
  </script>
</body>
</html>
