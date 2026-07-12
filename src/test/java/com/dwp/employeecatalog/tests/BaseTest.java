package com.dwp.employeecatalog.tests;

import com.dwp.employeecatalog.api.EmployeeCatalogClient;
import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import org.junit.jupiter.api.BeforeAll;

import static io.restassured.config.HttpClientConfig.httpClientConfig;

/**
 * Shared setup for every test class:
 *
 * <ul>
 *   <li>generous HTTP timeouts so the first (cold-start) request doesn't fail;</li>
 *   <li>a single authenticated token, obtained once and reused across the suite
 *       so we don't hammer the login endpoint before every test.</li>
 * </ul>
 */
public abstract class BaseTest {

    protected static final EmployeeCatalogClient api = new EmployeeCatalogClient();

    /** Bearer token shared by all authenticated tests. */
    protected static String token;

    @BeforeAll
    static void authenticateOnce() {
        // Timeouts sized for the free-tier cold start (up to ~60s on first hit).
        RestAssured.config = RestAssuredConfig.config().httpClient(
                httpClientConfig()
                        .setParam("http.connection.timeout", 90_000)
                        .setParam("http.socket.timeout", 90_000));

        if (token == null) {
            token = api.warmUpAndLogin();
        }
    }
}
