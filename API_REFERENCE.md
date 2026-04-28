# API Reference — e-PDS Servlets

This document describes all servlet endpoints, their request/response formats, and authentication requirements.

---

## Authentication & Authorization

All routes require a valid session (except public routes). Session is created by successful login.

**Headers:**
- `Content-Type: application/x-www-form-urlencoded` (POST forms)
- Cookies: `JSESSIONID` (set automatically by Tomcat)

**CSRF Protection:**
- All POST requests must include a hidden `csrfToken` input (seeded on GET, validated on POST)
- Invalid token → HTTP 403 Forbidden

**Role-based Access:**
- `/citizen*` routes require `CITIZEN` role
- `/admin*` routes require `ADMIN` role
- Unauthorized role → HTTP 403 Forbidden

---

## Public Routes (No Login Required)

### POST /LoginServlet

**Purpose:** Authenticate a user and create a session.

**Request Parameters:**
```
username     [string, required]     User's login name
password     [string, required]     Plain-text password
csrfToken    [string, required]     CSRF token from GET
```

**Response:**
- **On success (HTTP 302):** Redirect to dashboard
  - Citizen → `/citizen-dashboard.jsp`
  - Admin → `/admin-dashboard.jsp`
- **On failure (HTTP 200):** Re-render login form with error message

**Session attributes set:**
```
user          com.ration.model.User object
userId        int (user_id)
username      String
fullName      String
role          String (ADMIN or CITIZEN)
csrfToken     String (new token)
```

**Example:**
```bash
curl -X POST http://localhost:8080/e-PDS/LoginServlet \
  -d "username=admin&password=Admin@2024&csrfToken=abc123..."
```

---

### GET /LoginServlet

**Purpose:** Display login form.

**Response:** HTML login page with seeded CSRF token.

---

### POST /RegisterServlet

**Purpose:** Register a new citizen account.

**Request Parameters:**
```
username         [string, required]   3-20 chars, alphanumeric + underscore
email            [string, required]   Valid email format
fullName         [string, required]   Full legal name
phone            [string, required]   10-digit phone number
password         [string, required]   Min 6 characters
confirmPassword  [string, required]   Must match password
annualIncome     [long, optional]     Annual household income in rupees
csrfToken        [string, required]   CSRF token
```

**Validation:**
- Username unique (checked against DB)
- Email valid and unique
- Password ≥ 6 chars and matches confirmation
- Annual income optional, defaults to 0

**Response:**
- **On success (HTTP 302):** Redirect to `/register-success.jsp`
- **On validation failure (HTTP 200):** Re-render form with error and previous values

**Database changes:**
- New row inserted into `users` table with role = `CITIZEN`
- `annual_income` recorded (used for auto-classification when family is created)

---

### GET /RegisterServlet

**Purpose:** Display registration form.

**Response:** HTML multi-step registration form with seeded CSRF token.

---

### POST /ResetPasswordServlet

**Purpose:** Reset a user's password.

**Request Parameters:**
```
usernameOrEmail      [string, required]   Username or email to reset
currentPassword      [string, required]   Current password (for verification)
newPassword          [string, required]   New password (min 6 chars)
confirmNewPassword   [string, required]   Must match newPassword
csrfToken            [string, required]   CSRF token
```

**Validation:**
- User exists (by username or email)
- Current password is correct
- New password ≥ 6 chars
- New password != current password

**Response:**
- **On success (HTTP 302):** Redirect to login with success message
- **On failure (HTTP 200):** Re-render form with error

**Database changes:**
- `users.password_hash` updated with new BCrypt hash
- `audit_logs` entry created: action = `PASSWORD_RESET`, user_id, timestamp, IP

---

### POST /LogoutServlet

**Purpose:** Destroy the user session.

**Request:**
- Must be POST (GET returns 405 Method Not Allowed)
- No parameters required
- Session must be valid

**Response:** HTTP 302 redirect to `/LoginServlet`

**Session effect:** Session invalidated; `JSESSIONID` cookie cleared.

---

## Citizen Routes (Login + `CITIZEN` Role Required)

### GET /FamilyMemberServlet

**Purpose:** Display the citizen's family members and add-member form.

**Request:** No parameters.

**Response:** HTML page with:
- Summary stats: total members, verified count, pending count
- Table of family members (name, relation, Aadhaar last 4 digits, age, gender, status)
- Modal form to add a new member

**Attributes in request:**
```
members          List<Map<String, Object>>  Citizen's family members
totalCount       int                        Total members
verifiedCount    long                       Members with Aadhaar
pendingCount     long                       Members without Aadhaar
successMsg       String (optional)          "Family member added successfully"
```

---

### POST /FamilyMemberServlet

**Purpose:** Add a new family member to the citizen's ration card.

**Request Parameters:**
```
memberName   [string, required]   Full name of family member
relation     [string, required]   Relation (Wife, Son, Daughter, etc.)
aadhaar      [string, required]   12-digit Aadhaar number
dob          [date, required]     Date of birth (YYYY-MM-DD)
gender       [string, required]   Male, Female, or Other
```

**Validation:**
- All fields required and non-blank
- Aadhaar 12 chars

**Response:**
- **On success (HTTP 302):** Redirect to `/FamilyMemberServlet?success=1`
- **On validation error (HTTP 302):** Redirect to `/FamilyMemberServlet?error=missing_fields`
- **On DB error (HTTP 302):** Redirect to `/FamilyMemberServlet?error=db_error`

**Database changes:**
- Auto-create ration card if citizen has none (with `card_type` derived from `annual_income`)
- Insert row into `family_members` table
- Age auto-calculated from DOB

---

### GET /ComplaintServlet

**Purpose:** Display the citizen's complaints and file-complaint form.

**Request:** No parameters.

**Response:** HTML page with:
- Summary stats: total filed, pending count, resolved count
- Table of complaints (type, month, description, status, created date)
- Modal form to file a new complaint

**Attributes in request:**
```
complaints       List<Map<String, Object>>  Citizen's complaints
totalCount       int                        Total complaints filed
pendingCount     long                       Unresolved complaints
resolvedCount    long                       Resolved complaints
successMsg       String (optional)          "Complaint filed successfully"
```

---

### POST /ComplaintServlet

**Purpose:** File a new complaint.

**Request Parameters:**
```
type         [string, required]   Complaint type (Short Supply, Wrong Quality, etc.)
month        [string, optional]   Related month (October 2023, etc.)
description  [string, required]   Complaint details (min 1 char)
```

**Validation:**
- `type` required and non-blank
- `description` required and non-blank
- `month` optional

**Response:**
- **On success (HTTP 302):** Redirect to `/ComplaintServlet?success=1`
- **On validation error (HTTP 302):** Redirect to `/ComplaintServlet?error=missing_fields`
- **On DB error (HTTP 302):** Redirect to `/ComplaintServlet?error=db_error`

**Database changes:**
- Insert row into `complaints` table with status = `PENDING`, created_at = CURRENT_TIMESTAMP

---

## Admin Routes (Login + `ADMIN` Role Required)

### GET /AdminFamilyServlet

**Purpose:** Display all registered citizen families with ability to edit income and card type.

**Request Parameters (optional):**
```
cardId   [int, optional]   If provided, load family members for this card
msg      [string, optional]   Success message (card_updated, income_updated, member_deleted)
```

**Response:** HTML page with:
- Summary stats: total families, BPL count, APL count, AAY count
- Table of all families with:
  - Head of household name, username, email, phone
  - Editable annual income field (inline form, submits on change)
  - Dropdown to change card type (BPL/APL/AAY)
  - Member count
  - Card number and status
- If `cardId` provided: panel showing family members with delete buttons

**Attributes in request:**
```
families         List<Map<String, Object>>   All citizen families with card details
selectedCardId   Integer (optional)          Card ID being detailed
selectedMembers  List<Map<String, Object>>   Members of selected card
msg              String (optional)           Success message
```

**Example response body (families list):**
```json
{
  "userId": 2,
  "fullName": "John Doe",
  "username": "johndoe",
  "email": "john@example.com",
  "mobile": "9876543210",
  "annualIncome": 75000,
  "cardId": 5,
  "cardNumber": "RC-2024-2-1234",
  "cardType": "BPL",
  "cardStatus": "ACTIVE",
  "memberCount": 3
}
```

---

### POST /AdminFamilyServlet (action=updateIncome)

**Purpose:** Update a citizen's annual income and optionally auto-reclassify card type.

**Request Parameters:**
```
action         [string, required]     Must be "updateIncome"
userId         [int, required]        Citizen to update
annualIncome   [string, required]     Annual income (numeric, commas OK)
```

**Response:** HTTP 302 redirect to `/AdminFamilyServlet?msg=income_updated`

**Database changes:**
- `users.annual_income` updated
- *Note:* Card type is NOT auto-updated here; use `updateCardType` action for that

---

### POST /AdminFamilyServlet (action=updateCardType)

**Purpose:** Manually change a ration card's classification (BPL/APL/AAY).

**Request Parameters:**
```
action    [string, required]     Must be "updateCardType"
cardId    [int, required]        Card ID to update
cardType  [string, required]     New type: BPL, APL, or AAY
```

**Validation:**
- `cardType` must be one of: BPL, APL, AAY

**Response:** HTTP 302 redirect to `/AdminFamilyServlet?msg=card_updated&cardId={cardId}`

**Database changes:**
- `ration_cards.card_type` updated

---

### POST /AdminFamilyServlet (action=deleteMember)

**Purpose:** Remove a family member from a ration card.

**Request Parameters:**
```
action    [string, required]     Must be "deleteMember"
memberId  [int, required]        Member ID to delete
cardId    [int, required]        Card ID (for redirect back to panel)
```

**Response:** HTTP 302 redirect to `/AdminFamilyServlet?msg=member_deleted&cardId={cardId}`

**Database changes:**
- Row deleted from `family_members` table

---

## Views (JSP Pages)

### /WEB-INF/views/citizen-dashboard.jsp

**Template:** Rendered by servlet context path. Contains:
- Card details (hardcoded in demo, will fetch from DB)
- Family members summary
- Recent transactions
- Quick-action buttons

**Requires session attributes:**
```
fullName   String   (from session)
role       String   (from session)
csrfToken  String   (for logout POST)
```

---

### /WEB-INF/views/family.jsp

**Rendered by:** `FamilyMemberServlet.doGet()`

**Uses attributes:**
- `members` — List of family members
- `totalCount`, `verifiedCount`, `pendingCount` — Summary stats
- `successMsg` — Optional success banner

---

### /WEB-INF/views/complaints.jsp

**Rendered by:** `ComplaintServlet.doGet()`

**Uses attributes:**
- `complaints` — List of user's complaints
- `totalCount`, `pendingCount`, `resolvedCount` — Summary stats
- `successMsg` — Optional success banner

---

### /WEB-INF/views/admin-family.jsp

**Rendered by:** `AdminFamilyServlet.doGet()`

**Uses attributes:**
- `families` — List of all families
- `selectedCardId` — Card being detailed (optional)
- `selectedMembers` — Members of selected card (optional)
- `msg` — Success message (optional)

---

## Error Codes

| Code | Meaning | Cause |
|------|---------|-------|
| 403 | Forbidden | Invalid CSRF token, or unauthorized role access |
| 404 | Not Found | Route does not exist (e.g., `/AdminFamilyServlet` accessed by citizen) |
| 500 | Internal Server Error | DB connection failure, SQL syntax error, or unexpected exception |
| 302 | Redirect | Form submission successful or validation error |

---

## Example Workflows

### Citizen Registration → Family Addition → Complaint Filing

```bash
# 1. Get login form (seeded with CSRF token)
GET /e-PDS/LoginServlet

# 2. Register new account
POST /e-PDS/RegisterServlet
  username=newuser&email=user@example.com&fullName=Test%20User&phone=9876543210
  &password=MyPass123&confirmPassword=MyPass123&annualIncome=80000
  &csrfToken=<from-GET>

# Redirect: /register-success.jsp

# 3. Login
POST /e-PDS/LoginServlet
  username=newuser&password=MyPass123&csrfToken=<from-form>

# Redirect: /citizen-dashboard.jsp
# Session created with userId, username, fullName, role, csrfToken

# 4. Add family member
POST /e-PDS/FamilyMemberServlet
  memberName=Spouse&relation=Wife&aadhaar=123456789012&dob=1995-06-15
  &gender=Female

# Creates ration_cards entry with card_type=BPL (income ≤ 100k)
# Inserts into family_members

# 5. File complaint
POST /e-PDS/ComplaintServlet
  type=Short%20Supply&month=October%202023&description=Short%20on%20wheat
  &csrfToken=<from-form>

# Inserts into complaints with status=PENDING
```

### Admin: View Families & Change Card Classification

```bash
# 1. Login as admin
POST /e-PDS/LoginServlet
  username=admin&password=Admin@2024&csrfToken=<from-form>

# 2. View all families
GET /e-PDS/AdminFamilyServlet

# 3. Update a family's income
POST /e-PDS/AdminFamilyServlet
  action=updateIncome&userId=2&annualIncome=150000

# 4. Change card type (after reviewing income)
POST /e-PDS/AdminFamilyServlet
  action=updateCardType&cardId=5&cardType=APL

# 5. View members of a card
GET /e-PDS/AdminFamilyServlet?cardId=5

# 6. Delete a member
POST /e-PDS/AdminFamilyServlet
  action=deleteMember&memberId=12&cardId=5
```

