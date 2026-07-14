package com.dwp.employeecatalog.tests;

import com.dwp.employeecatalog.model.Employee;
import com.dwp.employeecatalog.util.TestDataFactory;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Verifies the <b>list employees</b> endpoint, {@code GET /employees}.
 *
 * <p><b>Purpose:</b> confirm that an authenticated request returns 200 with a
 * JSON array, that every entry carries an {@code employeeId}, and that a freshly
 * created employee appears in the full catalog.</p>
 */
@DisplayName("GET /employees - list all employees")
class GetAllEmployeesTests extends BaseTest {

    @Test
    @DisplayName("returns HTTP 200 and a JSON array")
    void returnsOkAndArray() {
        Response response = api.getAllEmployees(token);

        assertThat(response.statusCode(), is(200));
        assertThat("body should be a JSON array",
                response.jsonPath().getList("$"), is(notNullValue()));
    }

    @Test
    @DisplayName("every listed employee carries an employeeId")
    void everyEmployeeHasId() {
        Response response = api.getAllEmployees(token);
        assertThat(response.statusCode(), is(200));

        List<Object> ids = response.jsonPath().getList("employeeId");
        assertThat("no employee should have a null id", ids, everyItem(is(notNullValue())));
    }

    @Test
    @DisplayName("a newly created employee appears in the full list")
    void newlyCreatedEmployeeIsListed() {
        Employee employee = TestDataFactory.validEmployee();
        String id = api.createEmployeeAndReturnId(token, employee);
        try {
            Response response = api.getAllEmployees(token);
            assertThat(response.statusCode(), is(200));

            List<String> ids = response.jsonPath().getList("employeeId");
            assertThat("the created employee id should be present in the list",
                    ids, hasItem(id));
            assertThat("list size should be at least 1", ids.size(), greaterThanOrEqualTo(1));
        } finally {
            api.deleteEmployee(token, id); // keep the shared catalog tidy
        }
    }
}
