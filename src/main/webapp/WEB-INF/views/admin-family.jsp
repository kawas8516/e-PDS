<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, java.util.Map" %>
<%
    com.ration.model.User __u = (com.ration.model.User) session.getAttribute("user");
    if (__u == null || !__u.isAdmin()) {
        response.sendRedirect(request.getContextPath() + "/LoginServlet");
        return;
    }
    List<Map<String, Object>> families = (List<Map<String, Object>>) request.getAttribute("families");
    List<Map<String, Object>> selectedMembers = (List<Map<String, Object>>) request.getAttribute("selectedMembers");
    Integer selectedCardId = (Integer) request.getAttribute("selectedCardId");
    String msg = (String) request.getAttribute("msg");
    if (families == null) families = new java.util.ArrayList<>();

    long bplCount = families.stream().filter(f -> "BPL".equals(f.get("cardType"))).count();
    long aplCount = families.stream().filter(f -> "APL".equals(f.get("cardType"))).count();
    long aayCount = families.stream().filter(f -> "AAY".equals(f.get("cardType"))).count();
    long noCardCount = families.stream().filter(f -> f.get("cardId") == null).count();
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Family Management – Admin Portal</title>
  <script src="https://cdn.tailwindcss.com"></script>
  <script src="https://unpkg.com/lucide@latest"></script>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet"/>
  <style>
    body { font-family: 'Inter', sans-serif; }
    .sidebar-btn { transition: all 0.2s; color: #94a3b8; }
    .sidebar-btn:hover { background: #1e293b; color: #fff; }
    .sidebar-btn.active { background: #2563eb; color: #fff; box-shadow: 0 10px 15px -3px rgba(37,99,235,0.25); }
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
      <a href="${pageContext.request.contextPath}/stock.html" class="sidebar-btn w-full flex items-center gap-3 px-4 py-3 rounded-xl">
        <i data-lucide="package" class="w-5 h-5"></i><span class="font-medium text-sm">Stock Management</span>
      </a>
      <a href="${pageContext.request.contextPath}/reports.html" class="sidebar-btn w-full flex items-center gap-3 px-4 py-3 rounded-xl">
        <i data-lucide="file-text" class="w-5 h-5"></i><span class="font-medium text-sm">Reports</span>
      </a>
      <a href="${pageContext.request.contextPath}/AdminFamilyServlet" class="sidebar-btn active w-full flex items-center gap-3 px-4 py-3 rounded-xl">
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

    <header class="flex justify-between items-center mb-8">
      <div>
        <h2 class="text-2xl font-bold text-slate-800 tracking-tight">Family Management</h2>
        <p class="text-slate-500 text-sm mt-1">View all registered families, update BPL/APL classification and income.</p>
      </div>
      <div class="flex items-center gap-3 bg-white px-4 py-2 rounded-2xl shadow-sm border border-slate-200">
        <div class="w-9 h-9 bg-gradient-to-br from-slate-600 to-slate-800 rounded-xl flex items-center justify-center text-white font-bold text-sm">AD</div>
        <div>
          <p class="text-sm font-bold text-slate-800 leading-none">${sessionScope.fullName != null ? sessionScope.fullName : 'Administrator'}</p>
          <span class="text-[10px] text-slate-500 font-bold uppercase tracking-wider">Admin</span>
        </div>
      </div>
    </header>

    <% if (msg != null) { %>
    <div class="mb-6 p-4 bg-green-50 border border-green-200 text-green-700 rounded-2xl text-sm font-medium flex items-center gap-2">
      <i data-lucide="check-circle" class="w-4 h-4"></i>
      <% if ("card_updated".equals(msg)) { %>Updated card classification successfully.
      <% } else if ("income_updated".equals(msg)) { %>Updated annual income successfully.
      <% } else if ("member_deleted".equals(msg)) { %>Family member removed successfully.
      <% } else { %><%= msg %><% } %>
    </div>
    <% } %>

    <!-- Summary Stats -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-slate-200 flex items-center gap-4">
        <div class="w-12 h-12 bg-blue-50 text-blue-600 rounded-xl flex items-center justify-center"><i data-lucide="users" class="w-6 h-6"></i></div>
        <div><p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Total Families</p><p class="text-2xl font-black text-slate-800"><%= families.size() %></p></div>
      </div>
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-slate-200 flex items-center gap-4">
        <div class="w-12 h-12 bg-red-50 text-red-500 rounded-xl flex items-center justify-center"><i data-lucide="heart" class="w-6 h-6"></i></div>
        <div><p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">BPL</p><p class="text-2xl font-black text-slate-800"><%= bplCount %></p></div>
      </div>
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-slate-200 flex items-center gap-4">
        <div class="w-12 h-12 bg-blue-50 text-blue-600 rounded-xl flex items-center justify-center"><i data-lucide="trending-up" class="w-6 h-6"></i></div>
        <div><p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">APL</p><p class="text-2xl font-black text-slate-800"><%= aplCount %></p></div>
      </div>
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-slate-200 flex items-center gap-4">
        <div class="w-12 h-12 bg-amber-50 text-amber-600 rounded-xl flex items-center justify-center"><i data-lucide="star" class="w-6 h-6"></i></div>
        <div><p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">AAY</p><p class="text-2xl font-black text-slate-800"><%= aayCount %></p></div>
      </div>
    </div>

    <!-- Family Table -->
    <div class="bg-white rounded-3xl shadow-sm border border-slate-200 overflow-hidden mb-6">
      <div class="p-6 border-b border-slate-100 flex items-center justify-between">
        <h3 class="font-bold text-slate-800">All Registered Families</h3>
        <span class="text-xs text-slate-400">Click a row to view/edit family members</span>
      </div>

      <% if (families.isEmpty()) { %>
      <div class="p-12 text-center text-slate-400">
        <i data-lucide="users" class="w-12 h-12 mx-auto mb-3 opacity-40"></i>
        <p class="font-medium">No citizen families registered yet.</p>
      </div>
      <% } else { %>
      <div class="overflow-x-auto">
        <table class="w-full text-sm text-left">
          <thead>
            <tr class="text-[10px] font-bold text-slate-400 uppercase tracking-widest bg-slate-50 border-b border-slate-100">
              <th class="px-6 py-4">Family (Head)</th>
              <th class="px-6 py-4">Contact</th>
              <th class="px-6 py-4">Annual Income</th>
              <th class="px-6 py-4">Members</th>
              <th class="px-6 py-4">Card</th>
              <th class="px-6 py-4">Category</th>
              <th class="px-6 py-4">Actions</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-50">
          <% for (Map<String, Object> f : families) {
              String ct = f.get("cardType") != null ? f.get("cardType").toString() : "—";
              String badgeClass = "BPL".equals(ct) ? "bg-red-50 text-red-600"
                                : "APL".equals(ct) ? "bg-blue-50 text-blue-600"
                                : "AAY".equals(ct) ? "bg-amber-50 text-amber-600"
                                : "bg-slate-100 text-slate-400";
              String name = f.get("fullName") != null ? f.get("fullName").toString() : "";
              String initials = "";
              for (String part : name.split(" ")) if (!part.isEmpty()) initials += part.charAt(0);
              if (initials.length() > 2) initials = initials.substring(0, 2);
              long income = f.get("annualIncome") != null ? (Long) f.get("annualIncome") : 0;
              boolean hasCard = f.get("cardId") != null;
              int cardId = hasCard ? (Integer) f.get("cardId") : 0;
              boolean isSelected = selectedCardId != null && selectedCardId == cardId;
          %>
          <tr class="hover:bg-slate-50 transition-all cursor-pointer <%= isSelected ? "bg-blue-50" : "" %>"
              onclick="window.location='<%= request.getContextPath() %>/AdminFamilyServlet?cardId=<%= cardId %>'">
            <td class="px-6 py-4">
              <div class="flex items-center gap-3">
                <div class="w-9 h-9 bg-gradient-to-br from-slate-500 to-slate-700 rounded-xl flex items-center justify-center text-white font-bold text-xs"><%= initials.toUpperCase() %></div>
                <div>
                  <p class="font-bold text-slate-700"><%= name %></p>
                  <p class="text-xs text-slate-400">@<%= f.get("username") %></p>
                </div>
              </div>
            </td>
            <td class="px-6 py-4">
              <p class="text-slate-600 text-xs"><%= f.get("email") != null ? f.get("email") : "—" %></p>
              <p class="text-slate-400 text-xs"><%= f.get("mobile") != null ? f.get("mobile") : "" %></p>
            </td>
            <td class="px-6 py-4">
              <div class="flex items-center gap-2" onclick="event.stopPropagation()">
                <form method="post" action="<%= request.getContextPath() %>/AdminFamilyServlet"
                      class="flex items-center gap-1">
                  <input type="hidden" name="action" value="updateIncome"/>
                  <input type="hidden" name="userId" value="<%= f.get("userId") %>"/>
                  <input type="number" name="annualIncome" value="<%= income %>" min="0"
                         class="w-28 px-2 py-1 rounded-lg bg-slate-50 border border-slate-200 text-xs font-mono focus:ring-2 focus:ring-blue-400 outline-none"/>
                  <button type="submit" class="p-1 text-blue-500 hover:text-blue-700" title="Save income">
                    <i data-lucide="save" class="w-4 h-4"></i>
                  </button>
                </form>
              </div>
            </td>
            <td class="px-6 py-4 text-slate-600 font-bold"><%= f.get("memberCount") %></td>
            <td class="px-6 py-4">
              <% if (hasCard) { %>
              <span class="font-mono text-xs text-slate-500"><%= f.get("cardNumber") %></span>
              <% } else { %>
              <span class="text-xs text-slate-400 italic">No card yet</span>
              <% } %>
            </td>
            <td class="px-6 py-4" onclick="event.stopPropagation()">
              <% if (hasCard) { %>
              <form method="post" action="<%= request.getContextPath() %>/AdminFamilyServlet"
                    class="flex items-center gap-1">
                <input type="hidden" name="action" value="updateCardType"/>
                <input type="hidden" name="cardId" value="<%= cardId %>"/>
                <select name="cardType" onchange="this.form.submit()"
                        class="px-2 py-1 rounded-lg border text-xs font-bold outline-none cursor-pointer
                               <%= "BPL".equals(ct) ? "bg-red-50 border-red-200 text-red-600"
                                 : "APL".equals(ct) ? "bg-blue-50 border-blue-200 text-blue-600"
                                 : "bg-amber-50 border-amber-200 text-amber-600" %>">
                  <option value="BPL" <%= "BPL".equals(ct) ? "selected" : "" %>>BPL</option>
                  <option value="APL" <%= "APL".equals(ct) ? "selected" : "" %>>APL</option>
                  <option value="AAY" <%= "AAY".equals(ct) ? "selected" : "" %>>AAY</option>
                </select>
              </form>
              <% } else { %>
              <span class="text-xs text-slate-400">—</span>
              <% } %>
            </td>
            <td class="px-6 py-4">
              <a href="<%= request.getContextPath() %>/AdminFamilyServlet?cardId=<%= cardId %>"
                 class="text-blue-600 text-xs font-bold hover:underline flex items-center gap-1">
                <i data-lucide="eye" class="w-3 h-3"></i> Members
              </a>
            </td>
          </tr>
          <% } %>
          </tbody>
        </table>
      </div>
      <% } %>
    </div>

    <!-- Selected Family Members Panel -->
    <% if (selectedMembers != null && selectedCardId != null) { %>
    <div class="bg-white rounded-3xl shadow-sm border border-blue-200 overflow-hidden">
      <div class="p-6 border-b border-slate-100 flex items-center justify-between bg-blue-50">
        <h3 class="font-bold text-slate-800 flex items-center gap-2">
          <i data-lucide="users" class="w-5 h-5 text-blue-600"></i>
          Family Members — Card #<%= selectedCardId %>
        </h3>
        <a href="<%= request.getContextPath() %>/AdminFamilyServlet" class="text-slate-400 hover:text-slate-600 text-xs font-bold">
          Clear ✕
        </a>
      </div>
      <% if (selectedMembers.isEmpty()) { %>
      <div class="p-8 text-center text-slate-400 text-sm">No members added to this card yet.</div>
      <% } else { %>
      <table class="w-full text-sm text-left">
        <thead>
          <tr class="text-[10px] font-bold text-slate-400 uppercase tracking-widest bg-slate-50 border-b border-slate-100">
            <th class="px-6 py-3">Name</th>
            <th class="px-6 py-3">Relation</th>
            <th class="px-6 py-3">Aadhaar</th>
            <th class="px-6 py-3">DOB</th>
            <th class="px-6 py-3">Gender</th>
            <th class="px-6 py-3">Age</th>
            <th class="px-6 py-3">Remove</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-50">
        <% for (Map<String, Object> m : selectedMembers) {
            String aadhaar = m.get("aadhaar") != null ? m.get("aadhaar").toString() : "";
            String maskedAadhaar = aadhaar.length() >= 4
                ? "XXXX-XXXX-" + aadhaar.substring(aadhaar.length() - 4) : aadhaar;
        %>
        <tr class="hover:bg-slate-50">
          <td class="px-6 py-3 font-bold text-slate-700"><%= m.get("name") %></td>
          <td class="px-6 py-3 text-slate-600"><%= m.get("relation") %></td>
          <td class="px-6 py-3 font-mono text-slate-500 text-xs"><%= maskedAadhaar %></td>
          <td class="px-6 py-3 text-slate-600 text-xs"><%= m.get("dob") != null ? m.get("dob").toString() : "—" %></td>
          <td class="px-6 py-3 text-slate-600"><%= m.get("gender") %></td>
          <td class="px-6 py-3 text-slate-600"><%= m.get("age") %></td>
          <td class="px-6 py-3">
            <form method="post" action="<%= request.getContextPath() %>/AdminFamilyServlet"
                  onsubmit="return confirm('Remove this member?')">
              <input type="hidden" name="action"   value="deleteMember"/>
              <input type="hidden" name="memberId" value="<%= m.get("id") %>"/>
              <input type="hidden" name="cardId"   value="<%= selectedCardId %>"/>
              <button type="submit" class="text-red-400 hover:text-red-600 transition-colors">
                <i data-lucide="trash-2" class="w-4 h-4"></i>
              </button>
            </form>
          </td>
        </tr>
        <% } %>
        </tbody>
      </table>
      <% } %>
    </div>
    <% } %>

  </main>

  <script>lucide.createIcons();</script>
</body>
</html>
