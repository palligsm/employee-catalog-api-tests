package com.dwp.employeecatalog.tests;

import com.dwp.employeecatalog.model.ContactInfo;
import com.dwp.employeecatalog.model.Employee;
import com.dwp.employeecatalog.util.TestDataFactory;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.startsWith;

/**
 * Tests for {@code POST /employees}.
 */
@DisplayName("POST /employees - create employee")
class CreateEmployeeTests extends BaseTest {

    private final List<String> createdIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        createdIds.forEach(id -> api.deleteEmployee(token, id));
        createdIds.clear();
    }

    /**
     * Safely reads {@code employeeId} from a response that may not be JSON (a
     * rejected create can return a plain-text / HTML error body). Returns empty
     * rather than throwing, so negative tests never fail on body parsing.
     */
    private static Optional<String> extractEmployeeId(Response response) {
        String contentType = response.getContentType();
        if (contentType == null || !contentType.contains("json")) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(response.jsonPath().getString("employeeId"));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    @Test
    @DisplayName("valid payload creates an employee and returns 201 with an id")
    void validPayload_creates201() {
        Employee employee = TestDataFactory.validEmployee();

        Response response = api.createEmployee(token, employee);

        assertThat(response.statusCode(), is(201));
        String id = response.jsonPath().getString("employeeId");
        assertThat("a generated employeeId must be returned", id, is(not(emptyOrNullString())));
        assertThat("employeeId should use the documented 'Emp-' prefix", id, startsWith("Emp-"));
        createdIds.add(id);

        // The response should echo the data we sent.
        assertThat(response.jsonPath().getString("firstName"), equalTo(employee.getFirstName()));
        assertThat(response.jsonPath().getString("lastName"), equalTo(employee.getLastName()));
    }

    @Test
    @DisplayName("minimal payload (required fields only) is accepted")
    void minimalPayload_isAccepted() {
        Employee employee = TestDataFactory.minimalValidEmployee();

        Response response = api.createEmployee(token, employee);

        assertThat(response.statusCode(), is(201));
        createdIds.add(response.jsonPath().getString("employeeId"));
    }

    @Test
    @DisplayName("duplicate email is rejected with HTTP 400")
    void duplicateEmail_isRejected() {
        Employee first = TestDataFactory.validEmployee();
        Response created = api.createEmployee(token, first);
        assertThat("precondition: first create succeeds", created.statusCode(), is(201));
        createdIds.add(created.jsonPath().getString("employeeId"));

        // Second employee re-using the same email address.
        Employee duplicate = TestDataFactory.validEmployee();
        duplicate.getContactInfo().setEmail(first.getContactInfo().getEmail());

        Response response = api.createEmployee(token, duplicate);

        assertThat("re-using an email must be rejected", response.statusCode(), is(400));
        // The API reports the duplicated field as "contactInfo.email".
        assertThat("error should identify the offending field as the email",
                response.jsonPath().getString("error.field"), containsString("email"));
    }

    @Test
    @DisplayName("missing required field (lastName) is rejected, not 201")
    void missingRequiredField_isRejected() {
        // Build a body deliberately missing the required lastName.
        Employee employee = Employee.builder()
                .firstName("NoLastName")
                .dateOfBirth("1990-01-01")
                .contactInfo(new ContactInfo(
                        TestDataFactory.uniqueEmail("no", "last"), null, null))
                .build();

        Response response = api.createEmployee(token, employee);

        // A well-behaved API should reject this with a 4xx (ideally 400).
        assertThat("creating without a required field must not succeed with 201",
                response.statusCode(), is(not(201)));

        // Guard against silent success: if it somehow created, clean it up.
        extractEmployeeId(response).ifPresent(createdIds::add);
    }

    @Test
    @DisplayName("missing contactInfo.email is rejected, not 201")
    void missingEmail_isRejected() {
        Employee employee = Employee.builder()
                .firstName("NoEmail")
                .lastName("Person")
                .dateOfBirth("1990-01-01")
                .contactInfo(new ContactInfo(null, "+441234567890", null))
                .build();

        Response response = api.createEmployee(token, employee);

        assertThat("creating without the required email must not succeed with 201",
                response.statusCode(), is(not(201)));
        extractEmployeeId(response).ifPresent(createdIds::add);
    }

    @Test
    @DisplayName("empty JSON body is rejected, not 201")
    void emptyBody_isRejected() {
        Response response = api.createEmployee(token, Map.of());

        assertThat("an empty body must not create an employee",
                response.statusCode(), is(not(201)));
    }
}
