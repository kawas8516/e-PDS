<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, java.util.Map" %>
<%
    if (session.getAttribute("user") == null) {
        response.sendRedirect(request.getContextPath() + "/LoginServlet");
        return;
    }
    List<Map<String, Object>> members = (List<Map<String, Object>>) request.getAttribute("members");
    int total    = members == null ? 0 : members.size();
    long verified = request.getAttribute("verifiedCount") != null ? (Long) request.getAttribute("verifiedCount") : 0;
    long pending  = request.getAttribute("pendingCount")  != null ? (Long) request.getAttribute("pendingCount")  : 0;
    String successMsg = (String) request.getAttribute("successMsg");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Family Members – Digital Ration Card System</title>
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
      <a href="${pageContext.request.contextPath}/FamilyMemberServlet" class="sidebar-btn active w-full flex items-center gap-3 px-4 py-3 rounded-xl"><i data-lucide="users" class="w-5 h-5"></i><span class="font-medium text-sm">Family Members</span></a>
      <a href="${pageContext.request.contextPath}/allocation.html" class="sidebar-btn w-full flex items-center gap-3 px-4 py-3 rounded-xl"><i data-lucide="clipboard-list" class="w-5 h-5"></i><span class="font-medium text-sm">Monthly Quota</span></a>
      <a href="${pageContext.request.contextPath}/ComplaintServlet" class="sidebar-btn w-full flex items-center gap-3 px-4 py-3 rounded-xl"><i data-lucide="alert-circle" class="w-5 h-5"></i><span class="font-medium text-sm">Complaints</span></a>
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
        <h2 class="text-2xl font-bold text-slate-800 tracking-tight">Family Members</h2>
        <p class="text-slate-500 text-sm mt-1">Manage members linked to your ration card</p>
      </div>
      <button onclick="openModal()" class="bg-blue-600 text-white font-bold px-5 py-3 rounded-xl hover:bg-blue-700 transition-all shadow-lg shadow-blue-100 flex items-center gap-2 text-sm">
        <i data-lucide="user-plus" class="w-4 h-4"></i> Add Member
      </button>
    </div>

    <% if (successMsg != null) { %>
    <div class="mb-6 p-4 bg-green-50 border border-green-200 text-green-700 rounded-2xl text-sm font-medium">
      <%= successMsg %>
    </div>
    <% } %>

    <!-- Summary Bar -->
    <div class="grid grid-cols-3 gap-4 mb-8">
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-slate-200 flex items-center gap-4">
        <div class="w-12 h-12 bg-blue-50 text-blue-600 rounded-xl flex items-center justify-center"><i data-lucide="users" class="w-6 h-6"></i></div>
        <div><p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Total Members</p><p class="text-2xl font-black text-slate-800"><%= total %></p></div>
      </div>
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-slate-200 flex items-center gap-4">
        <div class="w-12 h-12 bg-green-50 text-green-600 rounded-xl flex items-center justify-center"><i data-lucide="check-circle" class="w-6 h-6"></i></div>
        <div><p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Verified</p><p class="text-2xl font-black text-slate-800"><%= verified %></p></div>
      </div>
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-slate-200 flex items-center gap-4">
        <div class="w-12 h-12 bg-amber-50 text-amber-600 rounded-xl flex items-center justify-center"><i data-lucide="clock" class="w-6 h-6"></i></div>
        <div><p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Pending</p><p class="text-2xl font-black text-slate-800"><%= pending %></p></div>
      </div>
    </div>

    <!-- Member Table -->
    <div class="bg-white rounded-3xl shadow-sm border border-slate-200 overflow-hidden">
      <div class="p-6 border-b border-slate-100 flex items-center justify-between">
        <h3 class="font-bold text-slate-800">Member List</h3>
      </div>
      <% if (members == null || members.isEmpty()) { %>
      <div class="p-12 text-center text-slate-400">
        <i data-lucide="users" class="w-12 h-12 mx-auto mb-3 opacity-40"></i>
        <p class="font-medium">No family members added yet.</p>
      </div>
      <% } else { %>
      <table class="w-full text-sm text-left">
        <thead>
          <tr class="text-[10px] font-bold text-slate-400 uppercase tracking-widest bg-slate-50 border-b border-slate-100">
            <th class="px-6 py-4">Name</th>
            <th class="px-6 py-4">Relation</th>
            <th class="px-6 py-4">Aadhaar</th>
            <th class="px-6 py-4">Age</th>
            <th class="px-6 py-4">Gender</th>
            <th class="px-6 py-4">Status</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-50">
          <% for (Map<String, Object> m : members) {
              String aadhaar = m.get("aadhaar") != null ? m.get("aadhaar").toString() : "";
              String maskedAadhaar = aadhaar.length() >= 4
                  ? "XXXX-XXXX-" + aadhaar.substring(aadhaar.length() - 4)
                  : aadhaar;
              String initials = "";
              String n = m.get("name") != null ? m.get("name").toString() : "";
              for (String part : n.split(" ")) if (!part.isEmpty()) initials += part.charAt(0);
              if (initials.length() > 2) initials = initials.substring(0, 2);
          %>
          <tr class="hover:bg-slate-50 transition-all">
            <td class="px-6 py-4">
              <div class="flex items-center gap-3">
                <div class="w-9 h-9 bg-gradient-to-br from-blue-500 to-blue-700 rounded-xl flex items-center justify-center text-white font-bold text-xs"><%= initials.toUpperCase() %></div>
                <p class="font-bold text-slate-700"><%= n %></p>
              </div>
            </td>
            <td class="px-6 py-4 text-slate-600"><%= m.get("relation") %></td>
            <td class="px-6 py-4 font-mono text-slate-600 text-xs"><%= maskedAadhaar %></td>
            <td class="px-6 py-4 text-slate-600"><%= m.get("age") %></td>
            <td class="px-6 py-4 text-slate-600"><%= m.get("gender") %></td>
            <td class="px-6 py-4"><span class="px-3 py-1 bg-green-50 text-green-600 text-[10px] font-black uppercase rounded-full">Verified</span></td>
          </tr>
          <% } %>
        </tbody>
      </table>
      <% } %>
    </div>
  </main>

  <!-- Add Member Modal -->
  <div id="add-modal" class="modal fixed inset-0 bg-black/50 backdrop-blur-sm z-50 items-center justify-center p-6">
    <div class="bg-white rounded-3xl shadow-2xl w-full max-w-lg border border-slate-200">
      <div class="flex justify-between items-center p-7 border-b border-slate-100">
        <h3 class="font-bold text-slate-800 text-lg">Add Family Member</h3>
        <button onclick="closeModal()" class="text-slate-400 hover:text-slate-600 transition-colors"><i data-lucide="x" class="w-6 h-6"></i></button>
      </div>
      <form action="${pageContext.request.contextPath}/FamilyMemberServlet" method="POST" class="p-7 space-y-4">
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-xs font-bold text-slate-500 uppercase tracking-widest mb-1">Full Name *</label>
            <input name="memberName" type="text" placeholder="Full name" required
              class="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none text-sm transition-all"/>
          </div>
          <div>
            <label class="block text-xs font-bold text-slate-500 uppercase tracking-widest mb-1">Relation *</label>
            <select name="relation" required class="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none text-sm transition-all">
              <option value="">Select</option>
              <option>Wife</option>
              <option>Husband</option>
              <option>Son</option>
              <option>Daughter</option>
              <option>Father</option>
              <option>Mother</option>
              <option>Other</option>
            </select>
          </div>
          <div>
            <label class="block text-xs font-bold text-slate-500 uppercase tracking-widest mb-1">Aadhaar *</label>
            <input name="aadhaar" type="text" placeholder="12-digit Aadhaar" maxlength="12" required
              class="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none text-sm font-mono transition-all"/>
          </div>
          <div>
            <label class="block text-xs font-bold text-slate-500 uppercase tracking-widest mb-1">Date of Birth *</label>
            <input name="dob" type="date" required
              class="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none text-sm transition-all"/>
          </div>
          <div class="col-span-2">
            <label class="block text-xs font-bold text-slate-500 uppercase tracking-widest mb-1">Gender *</label>
            <div class="flex gap-4">
              <label class="flex items-center gap-2 cursor-pointer"><input type="radio" name="gender" value="Male" class="accent-blue-600"/> <span class="text-sm text-slate-700">Male</span></label>
              <label class="flex items-center gap-2 cursor-pointer"><input type="radio" name="gender" value="Female" class="accent-blue-600"/> <span class="text-sm text-slate-700">Female</span></label>
              <label class="flex items-center gap-2 cursor-pointer"><input type="radio" name="gender" value="Other" class="accent-blue-600"/> <span class="text-sm text-slate-700">Other</span></label>
            </div>
          </div>
        </div>
        <div class="flex gap-3 pt-4">
          <button type="button" onclick="closeModal()" class="flex-1 py-3 rounded-xl border border-slate-200 text-slate-600 font-bold text-sm hover:bg-slate-50 transition-all">Cancel</button>
          <button type="submit" class="flex-1 py-3 rounded-xl bg-blue-600 text-white font-bold text-sm hover:bg-blue-700 transition-all shadow-lg shadow-blue-100">Add Member</button>
        </div>
      </form>
    </div>
  </div>

  <script>
    lucide.createIcons();
    function openModal()  { document.getElementById('add-modal').classList.add('open'); }
    function closeModal() { document.getElementById('add-modal').classList.remove('open'); }
    <% if ("missing_fields".equals(request.getParameter("error"))) { %>
    window.addEventListener('load', () => openModal());
    <% } %>
  </script>
</body>
</html>
