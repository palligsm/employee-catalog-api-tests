package com.dwp.employeecatalog.api;

/**
 * API paths for the Employee Catalog Management System, kept in one place so the
 * tests read declaratively and any path change is a single edit.
 */
public final class Endpoints {

    private Endpoints() {
    }

    public static final String LOGIN = "/hr/login";
    public static final String EMPLOYEES = "/employees";
    public static final String EMPLOYEE_BY_ID = "/employees/{id}";
}
