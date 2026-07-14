package com.dwp.employeecatalog.tests;

import com.dwp.employeecatalog.model.Employee;
import com.dwp.employeecatalog.util.TestDataFactory;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Verifies the <b>get-one employee</b> endpoint, {@code GET /employees/{id}}.
 *
 * <p><b>Purpose:</b> confirm that an existing id returns 200 with the matching
 * employee (including the email we stored), and that an unknown id returns
 * 404.</p>
 */
@DisplayName("GET /employees/{id} - retrieve one employee")
class GetEmployeeByIdTests extends BaseTest {

    @Test
    @DisplayName("existing id returns HTTP 200 with the matching employee")
    void existingId_returnsEmployee() {
        Employee employee = TestDataFactory.validEmployee();
        String id = api.createEmployeeAndReturnId(token, employee);
        try {
            Response response = api.getEmployeeById(token, id);

            assertThat(response.statusCode(), is(200));
            assertThat("returned id must match the requested id",
                    response.jsonPath().getString("employeeId"), equalTo(id));
            assertThat(response.jsonPath().getString("firstName"),
                    equalTo(employee.getFirstName()));
            assertThat("email should be persisted and returned",
                    response.jsonPath().getString("contactInfo.email"),
                    equalTo(employee.getContactInfo().getEmail()));
        } finally {
            api.deleteEmployee(token, id);
        }
    }

    @Test
    @DisplayName("unknown id returns HTTP 404")
    void unknownId_returns404() {
        Response response = api.getEmployeeById(token, TestDataFactory.nonExistentEmployeeId());

        assertThat("a non-existent employee should yield 404",
                response.statusCode(), is(404));
    }
}
