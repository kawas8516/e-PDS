<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, java.util.Map, java.sql.Timestamp, java.text.SimpleDateFormat" %>
<%
    if (session.getAttribute("user") == null) {
        response.sendRedirect(request.getContextPath() + "/LoginServlet");
        return;
    }
    List<Map<String, Object>> complaints = (List<Map<String, Object>>) request.getAttribute("complaints");
    int total    = complaints == null ? 0 : complaints.size();
    long pending  = request.getAttribute("pendingCount")  != null ? (Long) request.getAttribute("pendingCount")  : 0;
    long resolved = request.getAttribute("resolvedCount") != null ? (Long) request.getAttribute("resolvedCount") : 0;
    String successMsg = (String) request.getAttribute("successMsg");
    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Complaints – Digital Ration Card System</title>
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
  </style>
</head>
<body class="bg-slate-50 h-screen overflow-hidden flex">

  <!-- Sidebar -->
  <aside class="w-64 bg-slate-900 text-slate-300 h-full flex flex-col p-4 shadow-xl shrink-0">
    <div class="flex items-center gap-3 mb-10 px-2 pt-2">
      <div class="bg-blue-600 p-2.5 rounded-xl text-white"><i data-lucide="shield-check" class="w-6 h-6"></i></div>
      <div>
        <h1 class="font-bold text-white text-base leading-none">Digital Ration</h1>
        <span class="text-blue-400 text-[10px] uppercase font-bold tracking-widest">Citizen Portal</span>
      </div>
    </div>
    <nav class="flex-1 space-y-1">
      <a href="${pageContext.request.contextPath}/WEB-INF/views/citizen-dashboard.jsp" class="sidebar-btn w-full flex items-center gap-3 px-4 py-3 rounded-xl"><i data-lucide="layout-dashboard" class="w-5 h-5"></i><span class="font-medium text-sm">Dashboard</span></a>
      <a href="${pageContext.request.contextPath}/FamilyMemberServlet" class="sidebar-btn w-full flex items-center gap-3 px-4 py-3 rounded-xl"><i data-lucide="users" class="w-5 h-5"></i><span class="font-medium text-sm">Family Members</span></a>
      <a href="${pageContext.request.contextPath}/allocation.html" class="sidebar-btn w-full flex items-center gap-3 px-4 py-3 rounded-xl"><i data-lucide="clipboard-list" class="w-5 h-5"></i><span class="font-medium text-sm">Monthly Quota</span></a>
      <a href="${pageContext.request.contextPath}/ComplaintServlet" class="sidebar-btn active w-full flex items-center gap-3 px-4 py-3 rounded-xl"><i data-lucide="alert-circle" class="w-5 h-5"></i><span class="font-medium text-sm">Complaints</span></a>
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
        <h2 class="text-2xl font-bold text-slate-800 tracking-tight">Complaints &amp; Feedback</h2>
        <p class="text-slate-500 text-sm mt-1">File a grievance or track existing complaints</p>
      </div>
      <button onclick="openModal()" class="bg-red-500 text-white font-bold px-5 py-3 rounded-xl hover:bg-red-600 transition-all shadow-lg shadow-red-100 flex items-center gap-2 text-sm">
        <i data-lucide="plus" class="w-4 h-4"></i> New Complaint
      </button>
    </div>

    <% if (successMsg != null) { %>
    <div class="mb-6 p-4 bg-green-50 border border-green-200 text-green-700 rounded-2xl text-sm font-medium">
      <%= successMsg %>
    </div>
    <% } %>

    <!-- Stats -->
    <div class="grid grid-cols-3 gap-4 mb-8">
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-slate-200 flex items-center gap-4">
        <div class="w-12 h-12 bg-slate-50 text-slate-500 rounded-xl flex items-center justify-center"><i data-lucide="inbox" class="w-6 h-6"></i></div>
        <div><p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Total Filed</p><p class="text-2xl font-black text-slate-800"><%= total %></p></div>
      </div>
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-slate-200 flex items-center gap-4">
        <div class="w-12 h-12 bg-orange-50 text-orange-500 rounded-xl flex items-center justify-center"><i data-lucide="clock" class="w-6 h-6"></i></div>
        <div><p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Pending</p><p class="text-2xl font-black text-slate-800"><%= pending %></p></div>
      </div>
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-slate-200 flex items-center gap-4">
        <div class="w-12 h-12 bg-green-50 text-green-600 rounded-xl flex items-center justify-center"><i data-lucide="check-circle" class="w-6 h-6"></i></div>
        <div><p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Resolved</p><p class="text-2xl font-black text-slate-800"><%= resolved %></p></div>
      </div>
    </div>

    <!-- Complaint List -->
    <div class="bg-white rounded-3xl shadow-sm border border-slate-200 overflow-hidden">
      <div class="p-6 border-b border-slate-100"><h3 class="font-bold text-slate-800">My Complaints</h3></div>
      <% if (complaints == null || complaints.isEmpty()) { %>
      <div class="p-12 text-center text-slate-400">
        <i data-lucide="inbox" class="w-12 h-12 mx-auto mb-3 opacity-40"></i>
        <p class="font-medium">No complaints filed yet.</p>
      </div>
      <% } else { %>
      <div class="divide-y divide-slate-50">
        <% for (Map<String, Object> c : complaints) {
            String status = String.valueOf(c.get("status"));
            boolean isPending  = "PENDING".equals(status);
            boolean isResolved = "RESOLVED".equals(status);
            String badgeClass  = isPending  ? "bg-orange-50 text-orange-500" : isResolved ? "bg-green-50 text-green-600" : "bg-slate-50 text-slate-500";
            String iconName    = isResolved ? "check-circle" : "clock";
            String iconClass   = isResolved ? "bg-green-50 text-green-600" : "bg-orange-50 text-orange-500";
            Timestamp ts = (Timestamp) c.get("createdAt");
            String dateStr = ts != null ? sdf.format(ts) : "";
        %>
        <div class="p-6 hover:bg-slate-50 transition-all">
          <div class="flex justify-between items-start mb-2">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 <%= iconClass %> rounded-xl flex items-center justify-center"><i data-lucide="<%= iconName %>" class="w-5 h-5"></i></div>
              <div>
                <p class="font-bold text-slate-800 text-sm"><%= c.get("type") %><% if (c.get("month") != null && !c.get("month").toString().isEmpty()) { %> – <%= c.get("month") %><% } %></p>
                <p class="text-xs text-slate-500 mt-0.5">Filed: <%= dateStr %> · ID: CMP-<%= c.get("id") %></p>
              </div>
            </div>
            <span class="px-3 py-1 <%= badgeClass %> text-[10px] font-black uppercase rounded-lg"><%= status %></span>
          </div>
          <p class="text-sm text-slate-600" style="padding-left:52px"><%= c.get("description") %></p>
        </div>
        <% } %>
      </div>
      <% } %>
    </div>
  </main>

  <!-- New Complaint Modal -->
  <div id="complaint-modal" class="modal fixed inset-0 bg-black/50 backdrop-blur-sm z-50 items-center justify-center p-6">
    <div class="bg-white rounded-3xl shadow-2xl w-full max-w-lg border border-slate-200">
      <div class="flex justify-between items-center p-7 border-b border-slate-100">
        <h3 class="font-bold text-slate-800 text-lg">File New Complaint</h3>
        <button onclick="closeModal()" class="text-slate-400 hover:text-slate-600 transition-colors"><i data-lucide="x" class="w-6 h-6"></i></button>
      </div>
      <form action="${pageContext.request.contextPath}/ComplaintServlet" method="POST" class="p-7 space-y-4">
        <div>
          <label class="block text-xs font-bold text-slate-500 uppercase tracking-widest mb-1">Complaint Type *</label>
          <select name="type" required class="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none text-sm transition-all">
            <option value="">Select Type</option>
            <option>Short Supply</option>
            <option>Wrong Quality</option>
            <option>FPS Shop Closed</option>
            <option>Overcharging</option>
            <option>Rude Behaviour</option>
            <option>Card Not Updated</option>
            <option>Other</option>
          </select>
        </div>
        <div>
          <label class="block text-xs font-bold text-slate-500 uppercase tracking-widest mb-1">Related Month</label>
          <select name="month" class="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none text-sm transition-all">
            <option>October 2023</option>
            <option>September 2023</option>
            <option>August 2023</option>
          </select>
        </div>
        <div>
          <label class="block text-xs font-bold text-slate-500 uppercase tracking-widest mb-1">Description *</label>
          <textarea name="description" rows="4" placeholder="Describe your complaint in detail..." required
            class="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none text-sm transition-all resize-none"></textarea>
        </div>
        <div class="flex gap-3 pt-2">
          <button type="button" onclick="closeModal()" class="flex-1 py-3 rounded-xl border border-slate-200 text-slate-600 font-bold text-sm hover:bg-slate-50 transition-all">Cancel</button>
          <button type="submit" class="flex-1 py-3 rounded-xl bg-red-500 text-white font-bold text-sm hover:bg-red-600 transition-all shadow-lg shadow-red-100">Submit Complaint</button>
        </div>
      </form>
    </div>
  </div>

  <script>
    lucide.createIcons();
    function openModal()  { document.getElementById('complaint-modal').classList.add('open'); }
    function closeModal() { document.getElementById('complaint-modal').classList.remove('open'); }
    <% if (successMsg != null) { %>
    window.addEventListener('load', () => { /* already shown in banner */ });
    <% } %>
    <% if ("missing_fields".equals(request.getParameter("error"))) { %>
    window.addEventListener('load', () => openModal());
    <% } %>
  </script>
</body>
</html>
