# Setup and Deployment Guide for e-PDS

## Table of Contents
1. [Development Environment Setup](#development-environment-setup)
2. [Database Setup](#database-setup)
3. [Eclipse Configuration](#eclipse-configuration)
4. [Running Locally](#running-locally)
5. [Deployment to Production](#deployment-to-production)
6. [Troubleshooting](#troubleshooting)

---

## Development Environment Setup

### Prerequisites

- **JDK 21** or later
- **Apache Tomcat 10.1** (Jakarta EE compatible)
- **Eclipse IDE** (with WTP — Web Tools Platform)
- **PostgreSQL client tools** (optional, for local testing)
- **Git** for version control

### Install JDK 21

```bash
# Windows (using chocolatey)
choco install openjdk21

# Or download from https://jdk.java.net/21/
```

Verify:
```bash
java -version
javac -version
```

### Install Tomcat 10.1

1. Download from [apache.org/tomcat](https://tomcat.apache.org/)
2. Extract to a folder (e.g., `C:\apache-tomcat-10.1.x`)
3. Note the path — you'll configure it in Eclipse

### Install Eclipse with WTP

1. Download **Eclipse IDE for Enterprise Java Developers** from [eclipse.org](https://www.eclipse.org/downloads/packages/)
2. Extract and launch
3. In **Help → Install New Software**, search for and install **Web Tools Platform** if not already included

---

## Database Setup

### Create Supabase Project

1. Go to [supabase.com](https://supabase.com/) and sign up
2. Create a new project in your organization
3. Wait for it to initialize — you'll get:
   - **Host:** `db.xxxxx.supabase.co`
   - **Port:** `5432`
   - **Database:** `postgres`
   - **Username:** `postgres`
   - **Password:** *(auto-generated, shown once)*

### Create Database Schema

Run these migrations in the **Supabase SQL Editor**:

```sql
-- 1. Users table
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    full_name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    mobile VARCHAR(20),
    role VARCHAR(20) DEFAULT 'CITIZEN' CHECK (role IN ('ADMIN', 'CITIZEN')),
    is_active BOOLEAN DEFAULT TRUE,
    annual_income BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- 2. Ration Cards
CREATE TABLE ration_cards (
    card_id SERIAL PRIMARY KEY,
    card_number VARCHAR(50) UNIQUE NOT NULL,
    user_id INTEGER REFERENCES users(user_id),
    card_type VARCHAR(10) DEFAULT 'BPL' CHECK (card_type IN ('BPL', 'APL', 'AAY')),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    issue_date DATE DEFAULT CURRENT_DATE
);

-- 3. Family Members
CREATE TABLE family_members (
    member_id SERIAL PRIMARY KEY,
    card_id INTEGER REFERENCES ration_cards(card_id),
    name VARCHAR(255) NOT NULL,
    relation VARCHAR(50),
    aadhaar VARCHAR(12),
    dob DATE,
    gender VARCHAR(20),
    age INTEGER
);

-- 4. Complaints
CREATE TABLE complaints (
    complaint_id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(user_id),
    complaint_type VARCHAR(100),
    month VARCHAR(20),
    description TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Transactions
CREATE TABLE transactions (
    tx_id SERIAL PRIMARY KEY,
    card_id INTEGER REFERENCES ration_cards(card_id),
    item_id INTEGER,
    quantity INTEGER,
    amount DECIMAL(10,2),
    tx_date DATE DEFAULT CURRENT_DATE
);

-- 6. Stock Inventory
CREATE TABLE stock_inventory (
    item_id SERIAL PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL,
    quantity INTEGER DEFAULT 0,
    unit_price DECIMAL(10,2),
    threshold_limit INTEGER,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. Audit Logs
CREATE TABLE audit_logs (
    id SERIAL PRIMARY KEY,
    action TEXT,
    username TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45)
);
```

### Insert Test Admin Credentials

```sql
INSERT INTO users (username, password_hash, full_name, email, mobile, role, is_active, annual_income)
VALUES
  ('admin',      '$2a$12$3le1rO4jK7sn6y9RQ2El7e.QrrPadeirufSVBpDucMN9Omh5ISKP.', 'Admin Officer',     'admin@ration.gov.in',      '9000000001', 'ADMIN', TRUE, 0),
  ('superadmin', '$2a$12$/D36nplY0lWgtWSCYh99N.HkISduHjzLAOsO639CeCijENsgSB5bu', 'Super Administrator','superadmin@ration.gov.in', '9000000002', 'ADMIN', TRUE, 0)
ON CONFLICT (username) DO NOTHING;
```

**Credentials:**
- Admin: `admin` / `Admin@2024`
- Super Admin: `superadmin` / `Super@2024`

---

## Eclipse Configuration

### 1. Import Project

1. Open Eclipse → **File → Import → Existing Projects into Workspace**
2. Browse to your `e-PDS` folder
3. Click **Finish**

### 2. Add Tomcat Runtime

1. Go to **Eclipse Preferences** → **Server → Runtime Environments**
2. Click **Add...**
3. Select **Apache Tomcat v10.1**
4. Browse to your Tomcat installation folder
5. Click **Finish** and **Apply and Close**

### 3. Configure Project Targeted Runtime

1. Right-click the project → **Properties**
2. Go to **Project Facets**
3. Check **Dynamic Web Module** (version 6.0 or later)
4. Set **Runtimes** tab → select the Tomcat runtime you just added
5. Apply and close

### 4. Update Database Credentials

Edit `src/main/resources/db.properties`:

```properties
db.url=jdbc:postgresql://db.xxxxx.supabase.co:5432/postgres
db.user=postgres
db.password=YOUR_SUPABASE_PASSWORD_HERE
```

**OR** (preferred for security — set environment variables before starting Tomcat):

```bash
# Set in your shell/IDE launcher
export DB_URL=jdbc:postgresql://db.xxxxx.supabase.co:5432/postgres
export DB_USER=postgres
export DB_PASSWORD=YOUR_PASSWORD
```

### 5. Verify JARs in WEB-INF/lib

The following JARs should be present. **Remove** any servlet-api.jar or jsp-api.jar (provided by Tomcat):

```
src/main/webapp/WEB-INF/lib/
  ├─ postgresql-42.7.4.jar       ✓ (JDBC driver)
  ├─ jbcrypt-0.4.jar             ✓ (Password hashing)
  ├─ jakarta.servlet.jsp.jstl-api-2.0.0.jar  ✓ (JSTL API)
  └─ (NO servlet-api.jar, NO jsp-api.jar)
```

---

## Running Locally

### 1. Start Tomcat in Eclipse

1. In Eclipse, go to **Windows → Show View → Servers**
2. In the Servers panel, right-click → **New → Server**
3. Select **Apache Tomcat v10.1** → Next
4. Add the project to the server → Finish
5. Right-click the server → **Start**

Watch the **Console** tab for startup messages. If you see errors about DB connection, verify `db.properties` and the Supabase credentials.

### 2. Test the Application

Open your browser:

- **Login page:** http://localhost:8080/e-PDS/LoginServlet
- **Register:** Click "Register" link on login page
- **Admin login:** Use `admin` / `Admin@2024`
- **Test citizen:** Register a new account

### 3. Monitor Logs

- **Tomcat console:** In Eclipse Console tab
- **Errors:** Check `DB_PASSWORD`, firewall access to Supabase, and that tables exist

---

## Deployment to Production

### 1. Build the WAR

In Eclipse:
1. Right-click project → **Export → WAR file**
2. Set destination to `e-PDS.war`
3. Click **Finish**

### 2. Set Environment Variables on Server

Before starting Tomcat, set these (e.g., in `catalina.sh` or your container startup):

```bash
export DB_URL=jdbc:postgresql://db.xxxxx.supabase.co:5432/postgres
export DB_USER=postgres
export DB_PASSWORD=your_production_password
```

### 3. Configure Tomcat context.xml

Edit `conf/context.xml` and add SameSite cookie support:

```xml
<Context>
  <CookieProcessor sameSiteCookies="strict" />
</Context>
```

### 4. Enable HTTPS

In `conf/server.xml`, configure the SSL connector:

```xml
<Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol"
           maxThreads="150" SSLEnabled="true" scheme="https" secure="true"
           clientAuth="false" sslProtocol="TLSv1.2"
           keystoreFile="path/to/keystore.jks" keystorePass="password" />
```

### 5. Deploy WAR

Copy `e-PDS.war` to `$CATALINA_HOME/webapps/` and restart Tomcat:

```bash
./bin/catalina.sh stop
./bin/catalina.sh start
```

The application will be available at `https://your-domain.com/e-PDS/LoginServlet`.

---

## Troubleshooting

### Issue: "JDBC Driver not found"

**Fix:** Verify `postgresql-42.7.4.jar` is in `WEB-INF/lib` and rebuild the project:
- **Project → Clean → Full Build**

### Issue: "Connection refused" or "Database is down"

**Fix:** Test the connection string directly:
```bash
psql -h db.xxxxx.supabase.co -U postgres -d postgres
```
Enter the password when prompted. If it fails, check:
- Supabase project is active
- Network allows outbound port 5432
- `DB_PASSWORD` environment variable is set

### Issue: "Cannot execute query: Schema already exists"

**Fix:** If a migration table already exists, clear it or append `IF NOT EXISTS` to migration queries.

### Issue: Forms submit but don't update the database

**Fix:** Check that Tomcat has redeployed the latest code:
1. **Project → Clean**
2. **Right-click Tomcat server → Clean**
3. **Right-click server → Publish**
4. Restart the server

Check the **Tomcat Console** for any stack traces related to SQL errors.

### Issue: "Unauthorized (403)" on admin pages

**Fix:** Verify your login role is `ADMIN`:
1. Log in as `admin` (not `superadmin`)
2. Check the `users` table: `SELECT user_id, username, role FROM users WHERE username = 'admin';`

### Issue: CSRF token validation fails

**Fix:** Ensure:
1. `web.xml` session timeout is set (default 30 minutes)
2. The hidden `csrfToken` input is present in your HTML form
3. The token is being regenerated after failed submissions

---

## Security Checklist

- [ ] Use strong passwords for DB user and admin accounts
- [ ] Never commit DB credentials to git (use `db.properties` in `.gitignore` and set env vars)
- [ ] Enable HTTPS on production (Tomcat SSL connector + valid certificate)
- [ ] Set `Secure` cookie flag in `web.xml` (only sent over HTTPS)
- [ ] Test CSRF token flow (submit invalid token, verify rejection)
- [ ] Verify RLS policies if using Supabase (currently disabled)
- [ ] Enable audit logging review (check `audit_logs` table regularly)
- [ ] Keep JDK and Tomcat updated
- [ ] Restrict admin panel access by IP (reverse proxy + firewall rules)

---

## Additional Resources

- [Jakarta EE Documentation](https://jakarta.ee/)
- [Apache Tomcat 10.1 Docs](https://tomcat.apache.org/tomcat-10.1-doc/)
- [Supabase Documentation](https://supabase.com/docs/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [jBCrypt Documentation](https://www.mindrot.org/projects/jbcrypt/)

