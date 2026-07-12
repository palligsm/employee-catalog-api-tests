package com.dwp.employeecatalog.tests;

import com.dwp.employeecatalog.util.TestDataFactory;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.oneOf;

/**
 * Cross-cutting authorisation checks.
 *
 * <p>Business requirement: "All employee endpoints must operate using a
 * specified authorization token, formatted as Bearer 'token'." Therefore every
 * employee endpoint must reject requests that carry no / an invalid token.</p>
 */
@DisplayName("Authorization - employee endpoints require a valid bearer token")
class AuthorizationTests extends BaseTest {

    @Test
    @DisplayName("GET /employees without a token is rejected (401)")
    void getAll_withoutToken_isUnauthorized() {
        Response response = api.getAllEmployeesNoAuth();
        assertThat(response.statusCode(), is(401));
    }

    @Test
    @DisplayName("POST /employees without a token is rejected (401)")
    void create_withoutToken_isUnauthorized() {
        Response response = api.createEmployeeNoAuth(TestDataFactory.validEmployee());
        assertThat(response.statusCode(), is(401));
    }

    @Test
    @DisplayName("PUT /employees/{id} without a token is rejected (401)")
    void update_withoutToken_isUnauthorized() {
        Response response = api.updateEmployeeNoAuth(
                TestDataFactory.nonExistentEmployeeId(), TestDataFactory.validEmployee());
        assertThat(response.statusCode(), is(401));
    }

    @Test
    @DisplayName("DELETE /employees/{id} without a token is rejected (401)")
    void delete_withoutToken_isUnauthorized() {
        Response response = api.deleteEmployeeNoAuth(TestDataFactory.nonExistentEmployeeId());
        assertThat(response.statusCode(), is(401));
    }

    @Test
    @DisplayName("a malformed / garbage token is rejected (401/403)")
    void garbageToken_isRejected() {
        Response response = api.getAllEmployees("not-a-real-jwt-token");
        assertThat("a bogus token must not be accepted",
                response.statusCode(), is(oneOf(401, 403)));
    }
}
