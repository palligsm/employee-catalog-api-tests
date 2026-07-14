package com.dwp.employeecatalog.api;

/**
 * Central registry of the API's URL paths for the Employee Catalog Management
 * System.
 *
 * <p><b>Purpose:</b> keep every endpoint path in one place so the tests and the
 * client read declaratively (e.g. {@code Endpoints.EMPLOYEE_BY_ID}) and any path
 * change is a single edit. Not instantiable — constants only.</p>
 */
public final class Endpoints {

    private Endpoints() {
    }

    public static final String LOGIN = "/hr/login";
    public static final String EMPLOYEES = "/employees";
    public static final String EMPLOYEE_BY_ID = "/employees/{id}";
}
