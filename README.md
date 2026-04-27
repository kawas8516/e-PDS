# Digital Ration Card Management System (e-PDS)

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/kawas8516/e-PDS)

A Jakarta EE web application for managing digital ration card operations. Citizens can register and view their ration profile; administrators manage stock, applications, allocations, and complaints. Deployed on Apache Tomcat 10.1 with a PostgreSQL backend.

---

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Runtime | Java 21 |
| Servlet container | Apache Tomcat 10.1 |
| API namespace | Jakarta EE 6 (`jakarta.*`) |
| Database | PostgreSQL (JDBC via `postgresql-42.7.4.jar`) |
| Password hashing | jBCrypt (`jbcrypt-0.4.jar`) |
| Build system | None — manual JARs in `WEB-INF/lib` |
| IDE | Eclipse (Dynamic Web Project / WTP) |

---

## Architecture

The project follows a strict three-layer pattern inside a single WAR:

```
Request
  └─ AuthFilter              (authentication + RBAC gate)
       └─ Servlet (controller)
            └─ Service        (business logic)
                 └─ DAO       (SQL via PreparedStatement)
                      └─ PostgreSQL
```

Views are JSP files stored exclusively under `WEB-INF/views/` and are never accessible by direct URL. All view rendering goes through a servlet.

### Package layout

```
com.ration.controller   LoginServlet, RegisterServlet, ResetPasswordServlet, LogoutServlet
com.ration.service      AuthService
com.ration.dao          UserDAO, TransactionDAO
com.ration.model        User, Stock
com.ration.filter       AuthFilter
com.ration.util         DBConnection, CSRFUtil, AuditUtil, PasswordUtil
```

---

## User Roles

### Citizen (`CITIZEN`)
- Register for an account
- Log in and view their ration card profile
- View family members, monthly quota, and collection history
- Submit complaints

### Administrator (`ADMIN`)
- View system statistics and inventory levels
- Review and action new card applications
- Manage stock
- View reports and open complaints

---

## Security

- **Authentication:** BCrypt password verification; sessions expire after 30 minutes of inactivity
- **Session fixation:** `request.changeSessionId()` rotates the session ID on successful login
- **CSRF protection:** UUID token seeded on every form GET, validated on every POST; regenerated after each failed attempt
- **RBAC:** `AuthFilter` enforces role checks on every request (REQUEST dispatch only); admin and citizen pages are gated to their respective roles
- **Logout:** POST-only; GET returns 405 to prevent cross-site logout
- **Password reset:** Requires the current password before accepting a new one
- **Cookies:** `HttpOnly` and `Secure` flags set in `web.xml`; `SameSite=Strict` requires a Tomcat `context.xml` setting (see Deployment below)
- **SQL injection:** All queries use `PreparedStatement` with bound parameters
- **Audit log:** Login success, login failure, and password reset events are written to the `audit_logs` table

---

## Database Tables

| Table | Purpose |
|-------|---------|
| `users` | Account credentials, role (`ADMIN`/`CITIZEN`), active flag, last login |
| `audit_logs` | Per-event log of `user_id`, action, timestamp, IP address |
| `stock_inventory` | Item stock levels; decremented atomically on ration issuance |
| `transactions` | Ration issuance records (card, item, quantity, amount, date) |

---

## Project Structure

```
src/main/
  java/com/ration/          Java source files (see package layout above)
  webapp/
    WEB-INF/
      lib/                  Third-party JARs (postgresql, jbcrypt, jstl-api)
      views/                JSP views (never served directly)
        index.jsp           Login form
        register.jsp        Registration form
        reset-password.jsp  Password reset form
        register-success.jsp
        admin-dashboard.jsp
        citizen-dashboard.jsp
        error.jsp           Shared error page (403 / 404 / 500)
      web.xml
    *.html                  Static admin/citizen sub-pages (RBAC-gated by AuthFilter)
    index.jsp               Root redirect → /LoginServlet
```

---

## Deployment

### Prerequisites

- JDK 21
- Apache Tomcat 10.1
- PostgreSQL with the schema applied

### Database configuration

Connection parameters are read from `src/main/webapp/WEB-INF/classes/db.properties`. The database password can be overridden at runtime via the `DB_PASSWORD` environment variable so that credentials are never committed to source control.

### SameSite cookie

Add the following to Tomcat's `conf/context.xml` to enable `SameSite=Strict` on the session cookie (this cannot be set inside `web.xml`):

```xml
<CookieProcessor sameSiteCookies="strict" />
```

### Eclipse / WTP

1. Import the project as an existing Eclipse project.
2. Add Tomcat 10.1 as a targeted runtime (Project Properties > Targeted Runtimes).
3. Verify the JARs in `WEB-INF/lib` — do **not** add `servlet-api.jar` or `jsp-api.jar`; those are provided by Tomcat.
4. Deploy to the configured Tomcat server and start.

---

## Known Limitations and Planned Improvements

- **Password reset** currently requires the user's existing password, not an email token. A proper email-token reset flow (with a `password_reset_tokens` table and email gateway) is the intended long-term replacement.
- **JSTL conditionals** in JSPs use scriptlets because only the JSTL API JAR is bundled. Adding the JSTL implementation JAR would allow migration to `<c:if>` expressions.
- **Static sub-pages** (`stock.html`, `approvals.html`, etc.) contain placeholder UI only. Full backend wiring for those modules is pending.
