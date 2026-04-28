---
name: e-PDS Project Architecture
description: Package layout, file locations, naming conventions, servlet mappings, and JSP view paths for the e-PDS codebase
type: project
---

## Stack
- Tomcat 10.1, Jakarta EE 6 (jakarta.* namespace), Java 21, PostgreSQL via JDBC, jbcrypt-0.4
- No Maven — manual JARs in `src/main/webapp/WEB-INF/lib/`
- JARs present (as of 2026-04-28): `jakarta.servlet.jsp.jstl-2.0.0.jar`, `jbcrypt-0.4.jar`, `postgresql-42.7.4.jar`
- JSTL API JAR only (no implementation JAR) — do NOT use `<c:if>` or other JSTL tags without verifying impl JAR is present; use JSP scriptlets for conditionals instead

## Package Layout
```
com.ration.controller   — Servlets (LoginServlet, RegisterServlet, ResetPasswordServlet, LogoutServlet)
com.ration.service      — AuthService
com.ration.dao          — UserDAO, TransactionDAO
com.ration.model        — User, Stock
com.ration.filter       — AuthFilter
com.ration.util         — DBConnection, CSRFUtil, AuditUtil, PasswordUtil
```

## JSP Views
All views live under `/WEB-INF/views/` — they are NOT directly accessible by URL.
- `index.jsp`            — Login form (served via LoginServlet GET)
- `register.jsp`         — Registration form (served via RegisterServlet GET)
- `reset-password.jsp`   — Password reset form (served via ResetPasswordServlet GET)
- `register-success.jsp` — Post-registration success page
- `admin-dashboard.jsp`  — Admin dashboard (RBAC-gated by AuthFilter + scriptlet guard)
- `citizen-dashboard.jsp`— Citizen dashboard (session guard scriptlet)
- `error.jsp`            — Error page for 403/404/500 (created 2026-04-28)

## Root webapp JSPs (stubs only — redirect to servlets)
- `index.jsp`          → redirects to `/LoginServlet`
- `register.jsp`       → redirects to `/RegisterServlet`
- `reset-password.jsp` → redirects to `/ResetPasswordServlet`
- `admin-dashboard.jsp`    → forwards to `/WEB-INF/views/admin-dashboard.jsp`
- `citizen-dashboard.jsp`  → forwards to `/WEB-INF/views/citizen-dashboard.jsp`

## Servlet URL Mappings
- `/LoginServlet`         — login GET (renders form) + POST (authenticate)
- `/RegisterServlet`      — register GET (renders form) + POST (create user)
- `/ResetPasswordServlet` — reset GET (renders form) + POST (update password)
- `/LogoutServlet`        — POST only (GET returns 405)

## Welcome file
`web.xml` welcome-file-list: `LoginServlet`

## DBConnection
`com.ration.util.DBConnection.getConnection()` — reads from `db.properties`; env var override for DB password.

**Why:** Keeps credentials out of source control.
**How to apply:** Always use `DBConnection.getConnection()` — never hardcode JDBC URL.
