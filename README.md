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
| Database | PostgreSQL 17 via Supabase (JDBC `postgresql-42.7.4.jar`) |
| Frontend | HTML5, Tailwind CSS, Lucide Icons, vanilla JavaScript |
| Password hashing | jBCrypt (`jbcrypt-0.4.jar`, 12 rounds) |
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

## Features

### Citizen Workflows (`CITIZEN` role)

1. **Registration**
   - Self-registration with username, email, phone, full name, and annual household income
   - Passwords hashed with BCrypt (12 rounds)
   - Income used to auto-classify ration card: ≤ ₹1,00,000/year = **BPL**, else = **APL**

2. **Dashboard**
   - View ration card details (card number, type, validity)
   - Access quick links to family, quota, and complaints modules

3. **Family Members**
   - Add household members with name, relation, Aadhaar, DOB, and gender
   - Auto-calculate age from DOB
   - View all family members linked to the ration card

4. **Complaints**
   - File complaints (type, month, description)
   - View complaint history with status (Pending/Resolved)
   - Track complaint IDs and creation dates

5. **Monthly Quota** (planned)
   - View entitled monthly ration based on card type and family size
   - Track issued commodities (rice, wheat, sugar, kerosene)

### Admin Workflows (`ADMIN` role)

1. **System Dashboard**
   - View system statistics: total cards, pending applications, low-stock alerts, efficiency metrics
   - Inventory levels with color-coded alerts
   - Recent applications and open complaints summary

2. **Family Management**
   - View all registered families with search/sort
   - Edit annual income (triggers BPL/APL reclassification)
   - Change ration card type (BPL/APL/AAY) manually
   - View and remove family members
   - Summary stats: total families, count by card type (BPL/APL/AAY)

3. **New Applications** (planned)
   - Review citizen applications awaiting approval
   - Approve/reject with optional reason
   - Auto-create ration card on approval

4. **Stock Management** (planned)
   - CRUD operations on inventory items
   - Low-stock threshold alerts
   - Track stock distribution history

5. **Reports** (planned)
   - Distribution analytics by region, card type, commodity
   - Complaint resolution rates
   - Family statistics

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

## Database Schema

### Core Tables

| Table | Columns | Purpose |
|-------|---------|---------|
| `users` | user_id (PK), username, password_hash, full_name, email, mobile, role, is_active, annual_income, created_at, last_login | Citizens and admins; role = CITIZEN \| ADMIN |
| `ration_cards` | card_id (PK), card_number, user_id (FK), card_type, status, issue_date | One card per citizen; type = BPL \| APL \| AAY (auto-set by income) |
| `family_members` | member_id (PK), card_id (FK), name, relation, aadhaar, dob, gender, age | Household members linked to card |
| `complaints` | complaint_id (PK), user_id (FK), complaint_type, month, description, status, created_at | Grievances filed by citizens; status = PENDING \| RESOLVED |
| `transactions` | tx_id (PK), card_id (FK), item_id, quantity, amount, tx_date | Ration issuance records |
| `stock_inventory` | item_id (PK), item_name, quantity, unit_price, threshold_limit, last_updated | Commodity tracking |
| `audit_logs` | id (PK), action, username, timestamp, ip_address | Login/logout/password-reset events |

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

## Test Credentials

| Role | Username | Password |
|------|----------|----------|
| **Admin** | `admin` | `Admin@2024` |
| **Super Admin** | `superadmin` | `Super@2024` |

---

## Servlets and Routes

### Public Routes (no login required)
| Route | Servlet | Purpose |
|-------|---------|---------|
| `/` | `LoginServlet` (GET) | Root redirect |
| `/LoginServlet` | `LoginServlet` | Login form & submission |
| `/RegisterServlet` | `RegisterServlet` | Citizen registration |
| `/ResetPasswordServlet` | `ResetPasswordServlet` | Password reset (requires current password) |
| `/LogoutServlet` | `LogoutServlet` (POST only) | Destroy session |

### Citizen Routes (login + `CITIZEN` role required)
| Route | Servlet/JSP | Purpose |
|-------|-------------|---------|
| `/citizen-dashboard.jsp` | JSP | Citizen home dashboard |
| `/FamilyMemberServlet` | Servlet + JSP | View/add family members |
| `/ComplaintServlet` | Servlet + JSP | View/file complaints |
| `/allocation.html` | Static HTML (RBAC-gated) | Monthly quota (UI only) |

### Admin Routes (login + `ADMIN` role required)
| Route | Servlet/JSP | Purpose |
|-------|-------------|---------|
| `/admin-dashboard.jsp` | JSP | Admin system dashboard |
| `/AdminFamilyServlet` | Servlet + JSP | View/edit all families, change income & card type |
| `/approvals.html` | Static HTML (RBAC-gated) | Applications (UI placeholder) |
| `/stock.html` | Static HTML (RBAC-gated) | Inventory (UI placeholder) |
| `/reports.html` | Static HTML (RBAC-gated) | Analytics (UI placeholder) |

---

## Known Limitations and Planned Improvements

- **Applications approval workflow** (`approvals.html`) needs backend servlet to load pending applications and handle approve/reject logic
- **Stock management** (`stock.html`) needs CRUD operations and low-stock alerts
- **Reports dashboard** (`reports.html`) needs analytics queries
- **Password reset** currently requires the user's existing password. An email-token reset flow (with `password_reset_tokens` table and SMTP gateway) is planned.
- **Transactions/issuance** system (`TransactionDAO` defined but not wired) needs a FPS-facing module to record ration collections
- **RLS (Row-Level Security)** in Supabase is not enabled — any PostgreSQL admin can see all data. Enable `ALTER TABLE ... ENABLE ROW LEVEL SECURITY` and attach policies per role for multi-tenant isolation.
- **Email notifications** (complaints resolved, registration confirmation) are not yet implemented
