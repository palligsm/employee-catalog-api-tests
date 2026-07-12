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
 * Tests for {@code PUT /employees/{id}}.
 */
@DisplayName("PUT /employees/{id} - update employee")
class UpdateEmployeeTests extends BaseTest {

    @Test
    @DisplayName("updating an existing employee returns 200 and persists the change")
    void updateExisting_persistsChange() {
        String id = api.createEmployeeAndReturnId(token, TestDataFactory.validEmployee());
        try {
            Employee update = Employee.builder()
                    .firstName("Amended")
                    .lastName("Surname")
                    .build();

            Response response = api.updateEmployee(token, id, update);
            assertThat(response.statusCode(), is(200));

            // Confirm the change actually persisted by reading the record back.
            Response fetched = api.getEmployeeById(token, id);
            assertThat(fetched.statusCode(), is(200));
            assertThat("first name should be updated",
                    fetched.jsonPath().getString("firstName"), equalTo("Amended"));
            assertThat("last name should be updated",
                    fetched.jsonPath().getString("lastName"), equalTo("Surname"));
        } finally {
            api.deleteEmployee(token, id);
        }
    }

    @Test
    @DisplayName("updating a non-existent employee returns HTTP 404")
    void updateUnknown_returns404() {
        Employee update = Employee.builder().firstName("Ghost").build();

        Response response = api.updateEmployee(token, TestDataFactory.nonExistentEmployeeId(), update);

        assertThat("updating a missing employee should yield 404",
                response.statusCode(), is(404));
    }
}
