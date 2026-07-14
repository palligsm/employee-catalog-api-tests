# Requirements — Employee Catalog Management API

The requirements the API is expected to meet, derived **directly from the
published API documentation** at
`https://apisforemployeecatalogmanagementsystem.onrender.com/api-docs/`
(OpenAPI `Exercise2: REST API`, v1.0.0).

This document is the *specification* the tester validates against; the
counterpart **[FINDINGS.md](FINDINGS.md)** records where the live implementation
deviates from it. Priority is a rough judgement of business importance.

---

## 1. Business requirements

Taken from the API's **Introduction / Business Requirement** description.

| # | Priority | Requirement |
|---|----------|-------------|
| BR-1 | **High** | HR administrators are the **sole authorised users**; all employee actions are performed by them. |
| BR-2 | **High** | Only authorised HR administrators can create employees in the catalog. |
| BR-3 | **High** | On **successful login**, an HR administrator receives an **authorisation token**. |
| BR-4 | **High** | **Every** employee endpoint must operate using that token, supplied as `Authorization: Bearer <token>` (no quotes). |
| BR-5 | Medium | The service must let admins **retrieve a specific employee**, **get all employees**, **create**, **amend**, and **delete** employees. |
| BR-6 | Medium | When an employee leaves the organisation, their record is **deleted** from the system. |

---

## 2. Functional requirements — per endpoint

Six operations. `Auth` = whether a valid bearer token is required. Response codes
are those the documentation declares for each operation.

| # | Priority | Endpoint | Auth | Documented behaviour & responses |
|---|----------|----------|:----:|----------------------------------|
| FR-1 | **High** | `POST /hr/login` | ❌ | Authenticate an admin from `{username, password}`. **200** → `{ token }`; **401** → invalid credentials; **500** → server error. |
| FR-2 | **High** | `POST /employees` | ✅ | Add a new employee. **201** → `{ message, employeeId, firstName, lastName, email }`; **400** → duplicate key (e.g. email already in use); **500** → server error. |
| FR-3 | **High** | `GET /employees` | ✅ | List all employees as a JSON **array** of `{ employeeId, firstName, lastName, email, address }`. **200** → list; **401** → token missing/invalid; **500** → server error. |
| FR-4 | Medium | `GET /employees/{id}` | ✅ | Get one employee by id, incl. full `contactInfo` and `dateOfBirth`. **200** → employee; **404** → not found; **500** → server error. |
| FR-5 | Medium | `PUT /employees/{id}` | ✅ | Amend an existing employee (change of circumstances). **200** → updated employee; **404** → not found; **500** → server error. |
| FR-6 | **High** | `DELETE /employees/{id}` | ✅ | Remove an employee (they left the org). **200** → `{ message: "Employee deleted successfully!" }`; **404** → not found; **500** → server error. |

**Derived (implied) requirements** — not spelled out per-operation in the docs
but implied by BR-4 and REST conventions, and therefore tested:

| # | Priority | Requirement |
|---|----------|-------------|
| FR-7 | **High** | `POST`, `GET /employees`, `GET/PUT/DELETE /employees/{id}` must reject a **missing or invalid** token with **401** (BR-4). |
| FR-8 | Medium | After a successful `DELETE`, a subsequent `GET /employees/{id}` for the same id must return **404** (BR-6 — the record is gone). |
| FR-9 | Medium | An employee created via `POST` must subsequently appear in `GET /employees` and be retrievable via `GET /employees/{id}`. |
| FR-10 | **High** | The **unique-email** constraint must also hold on **update**. A `PUT /employees/{id}` that changes `contactInfo.email` to a value already used by **another** employee must be rejected — ideally **409 Conflict**, or **400** for consistency with `POST` (FR-2) — and must **not** modify data or crash the service. Re-sending an employee's **own** existing email (unchanged) must still succeed with **200**. |
| FR-11 | Medium | `firstName` / `lastName` are **not** unique keys. Two employees may share the same name, so creating or updating an employee to a name that already exists is **valid** and must succeed (not treated as a duplicate/conflict). Only `contactInfo.email` is unique. |

### 2.1 `POST /employees` — response code matrix

The expected status code for each request scenario. **Note:** `POST` creates a
resource, so success is **`201 Created`, not `200`** — the only `200` in this API
is for `POST /hr/login`, `GET`, `PUT` and `DELETE`.

| Scenario | Expected | Meaning |
|----------|:--------:|---------|
| Valid body, **new unique** email | **201 Created** | Employee created; returns `{ message, employeeId, firstName, lastName, email }`. |
| Valid body, **duplicate** email (already in use) | **400 Bad Request** | Rejected — unique-email constraint; body identifies `contactInfo.email`. *(Ideally `409 Conflict`; the API uses `400`.)* |
| Missing a **required** field (`firstName` / `lastName` / `dateOfBirth` / `contactInfo.email`) | **400 Bad Request** | Validation failure — must be a clean 4xx. *(Live host currently crashes with `5xx` — see [FINDINGS #5](FINDINGS.md).)* |
| Empty body `{}` | **400 Bad Request** | As above. *(Live host currently crashes — FINDINGS #5.)* |
| **No** `Authorization` header | **401 Unauthorized** | Token missing (FR-7 / BR-4). |
| **Invalid / malformed** token | **401 / 403** | Token present but not valid. |

> Success is **`201`**; a duplicate email is a **rejection (`400`)**, not a
> success. A `200` is never a valid outcome of `POST /employees`.

---

## 3. Data model requirements

### 3.1 Login request — `POST /hr/login`
| Field | Type | Required |
|-------|------|:--------:|
| `username` | string | ✅ |
| `password` | string | ✅ |

### 3.2 Employee — `POST /employees` request body
| Field | Type | Required | Notes |
|-------|------|:--------:|-------|
| `firstName` | string | ✅ | |
| `lastName` | string | ✅ | |
| `dateOfBirth` | string (`date`, `YYYY-MM-DD`) | ✅ | |
| `contactInfo` | object | ✅ | |
| `contactInfo.email` | string (`email`) | ✅ | Must be **unique** across all employees, enforced on **both create and update** (duplicate → 400 on `POST` per FR-2; should be 409/400 on `PUT` per FR-10). |
| `contactInfo.phone` | string | ❌ | |
| `contactInfo.address` | object | ❌ | |
| `contactInfo.address.street` | string | ❌ | |
| `contactInfo.address.town` | string | ❌ | |
| `contactInfo.address.postCode` | string | ❌ | |

> Per the docs, only `email` is required inside `contactInfo`; `phone` and
> `address` are optional. (The live service does not honour this — see
> [FINDINGS #8](FINDINGS.md).)

### 3.3 Employee — `PUT /employees/{id}` request body
Same shape as §3.2, but **all fields optional** (a partial amendment). The `{id}`
path parameter is required.

### 3.4 Employee id
Server-generated string, documented in the form `Emp-<uuid>`
(e.g. `Emp-ce2f233c-2af8-458c-9c17-0d262cc828d4`).

---

## 4. Security requirements

| # | Priority | Requirement |
|---|----------|-------------|
| SEC-1 | **High** | Authentication scheme is **HTTP Bearer**, token format **JWT** (`components.securitySchemes.bearerAuth`). |
| SEC-2 | **High** | The token from `POST /hr/login` must be sent on every employee request as `Authorization: Bearer <token>`. |
| SEC-3 | **High** | Requests without a valid token must be rejected (401) — no employee data is returned or modified (BR-4/FR-7). |

**Valid credentials** (supplied with the exercise): `admin1` … `admin10`, all with
password `securePassword`. Any one may be used.

---

## 5. Constraints & test guidelines

From the docs' **Important Guidelines** — these constrain *how* the suite is built,
not the API itself.

| # | Guideline |
|---|-----------|
| G-1 | Implement tests in **Java or JavaScript** (this suite uses Java + Maven). |
| G-2 | **Include assertions** verifying the result of every test case. |
| G-3 | **Use fake data** — no real personal data; create synthetic profiles. |
| G-4 | The **first request may be slow** (cold start); subsequent ones are fast. |
| G-5 | **No non-functional tests** — do not run performance or security tests. |
| G-6 | Cover at least **one successful end-to-end journey** (add → … → remove) and confirm **all endpoints** function as expected. |

---

## 6. Traceability — requirement → test coverage

Where each requirement is exercised in this suite.

| Requirement | Verified by |
|-------------|-------------|
| BR-3, FR-1 (login issues token) | `LoginTests` |
| BR-1/BR-2/BR-4, FR-7, SEC-1..3 (auth enforced) | `AuthorizationTests`, `LoginTests` |
| FR-2 (create), §3.2 data model | `CreateEmployeeTests` |
| FR-3, FR-9 (list + created appears) | `GetAllEmployeesTests` |
| FR-4 (get by id) | `GetEmployeeByIdTests` |
| FR-5 (amend) | `UpdateEmployeeTests` |
| FR-10 (unique email on update) | Documented in FINDINGS — a duplicate-email `PUT` currently **crashes** the live host instead of returning 409/400; an automated test is parked until the API is fixed / run locally (would take the shared host down). |
| FR-11 (names not unique) | Implicitly upheld — create/update tests use employees that may share names and still expect success; observed `PUT` with a duplicate first name returns 200. |
| FR-6, FR-8, BR-6 (delete + gone) | `DeleteEmployeeTests` |
| BR-5, G-6 (end-to-end journey) | `EmployeeLifecycleJourneyTest` |
| G-2 (assertions) | all test classes |
| G-3 (fake data) | `TestDataFactory` |
| G-4 (cold start) | `BaseTest`, `EmployeeCatalogClient` (warm-up/retry) |
| G-5 (no NFT) | scope decision — no perf/security tests included |

---

*Requirements sourced from the OpenAPI description and endpoint contracts. Observed
deviations from these requirements are logged in **[FINDINGS.md](FINDINGS.md)**.*
