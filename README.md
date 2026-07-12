# Employee Catalog Management API — Automated Test Suite

Automated API tests for the **Employee Catalog Management System**, written for
the DWP back-end test exercise.

- **Language / build:** Java 21, Maven
- **Test stack:** JUnit 5 (Jupiter) · REST-assured · Hamcrest · Jackson · Datafaker
- **Service under test:** `https://apisforemployeecatalogmanagementsystem.onrender.com`

The suite evaluates the API against its stated business requirements: only
authenticated HR administrators may manage employees, every employee endpoint
requires a `Bearer` token, and the service must support full CRUD over employees.

---

## How to run

```bash
# Run the whole suite against the live service
mvn clean test
```

The service is hosted on a free tier that **cold-starts** — the first request can
take up to ~60 seconds and may briefly return `502/503` while waking. The suite
handles this automatically (see `EmployeeCatalogClient#warmUpAndLogin`), so no
manual warm-up is needed; just allow the first run some time.

### Useful overrides

Any value in `src/test/resources/config.properties` can be overridden on the CLI:

```bash
mvn test -Dbase.uri=http://localhost:3000            # point at a local instance
mvn test -Dadmin.username=admin2 -Dadmin.password=securePassword
mvn test -Dtest=EmployeeLifecycleJourneyTest         # run just the E2E journey
```

---

## What is covered

| Area | Test class | Highlights |
|------|-----------|-----------|
| Authentication | `LoginTests` | valid login returns a JWT; wrong password / unknown user / empty / wrong-case credentials are rejected with 401 |
| Authorization | `AuthorizationTests` | every employee endpoint rejects missing / garbage tokens |
| Create | `CreateEmployeeTests` | happy path (201), minimal payload, duplicate-email (400), missing required fields, empty body |
| Read (all) | `GetAllEmployeesTests` | 200 + array shape, ids present, a created employee is listed |
| Read (by id) | `GetEmployeeByIdTests` | existing id returns the record; unknown id returns 404 |
| Update | `UpdateEmployeeTests` | update persists and is read back; unknown id returns 404 |
| Delete | `DeleteEmployeeTests` | delete removes the record; unknown id returns 404 |
| **End-to-end journey** | `EmployeeLifecycleJourneyTest` | the required happy path: **login → add → verify → amend → remove → confirm gone** |

Every test contains explicit assertions, and all test data is **fake / synthetic**
(via Datafaker) with unique emails so runs are repeatable. Created employees are
cleaned up after each test to keep the shared catalog tidy.

---

## Project layout

```
src/test/java/com/dwp/employeecatalog/
├── api/          EmployeeCatalogClient, Endpoints   (REST-assured plumbing)
├── model/        Employee, ContactInfo, Address, LoginRequest (POJOs)
├── util/         ConfigManager, TestDataFactory      (config + fake data)
└── tests/        BaseTest + one test class per endpoint + the E2E journey
src/test/resources/
├── config.properties          (base URI, credentials, retry tuning)
├── junit-platform.properties
└── simplelogger.properties
```

---

## Findings / potential gaps

The exercise asks the tester to identify implementation gaps. Observations made
while building the suite are documented in **[FINDINGS.md](FINDINGS.md)**.
Notable ones include the API storing only the **year** of `dateOfBirth`, and the
login error field being `error` rather than the documented `message`.

Per the exercise guidelines, no performance or security (non-functional) tests
are included.
