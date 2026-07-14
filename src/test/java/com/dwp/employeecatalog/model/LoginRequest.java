package com.dwp.employeecatalog.model;

/**
 * Data model (POJO) for the {@code POST /hr/login} request body:
 * {@code username} + {@code password}.
 *
 * <p><b>Purpose:</b> serialised by Jackson into the JSON sent when an HR admin
 * authenticates. Consumed by {@code EmployeeCatalogClient#login}.</p>
 */
public class LoginRequest {

    private String username;
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
