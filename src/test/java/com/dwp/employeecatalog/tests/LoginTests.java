package com.dwp.employeecatalog.tests;

import com.dwp.employeecatalog.util.ConfigManager;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Tests for {@code POST /hr/login}.
 *
 * <p>Business requirement: only authorised HR administrators may authenticate,
 * and a successful login returns an authorisation token.</p>
 */
@DisplayName("POST /hr/login - authentication")
class LoginTests extends BaseTest {

    @Test
    @Order(1)
    @DisplayName("valid admin credentials return HTTP 200 and a token")
    void validCredentials_returnToken() {
        Response response = api.login(ConfigManager.adminUsername(), ConfigManager.adminPassword());

        assertThat("valid login should return 200", response.statusCode(), is(200));

        String jwt = response.jsonPath().getString("token");
        assertThat("a token must be issued on success", jwt, is(not(emptyOrNullString())));
        // The service issues a JWT (three dot-separated segments).
        assertThat("token should be a JWT with 3 segments",
                jwt.split("\\.").length, is(3));
    }

    @Test
    @Order(2)
    @DisplayName("wrong password is rejected with HTTP 401 and no token")
    void wrongPassword_isRejected() {
        Response response = api.login(ConfigManager.adminUsername(), "definitely-wrong-password");

        assertThat("bad credentials must not authenticate", response.statusCode(), is(401));
        assertThat("no token should be leaked on failure",
                response.jsonPath().getString("token"), is(emptyOrNullString()));
    }

    @Test
    @Order(3)
    @DisplayName("unknown username is rejected with HTTP 401")
    void unknownUsername_isRejected() {
        Response response = api.login("no-such-admin", ConfigManager.adminPassword());

        assertThat("unknown user must not authenticate", response.statusCode(), is(401));
    }

    @Test
    @Order(4)
    @DisplayName("empty credentials do not authenticate")
    void emptyCredentials_areRejected() {
        Response response = api.login("", "");

        assertThat("empty credentials must not yield a 200 + token",
                response.statusCode(), is(not(200)));
    }

    @Test
    @Order(5)
    @DisplayName("case-sensitivity: correct password with wrong case is rejected")
    void wrongCasePassword_isRejected() {
        Response response = api.login(ConfigManager.adminUsername(),
                ConfigManager.adminPassword().toUpperCase());

        assertThat("password check should be case-sensitive",
                response.statusCode(), is(401));
    }
}
