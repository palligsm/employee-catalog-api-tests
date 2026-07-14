package com.dwp.employeecatalog.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Data model (POJO) for an employee's contact details: {@code email},
 * {@code phone} and {@code address}.
 *
 * <p><b>Purpose:</b> mirrors the {@code contactInfo} block of the API's employee
 * JSON. Only {@code email} is required by the API; the optional fields are
 * omitted from the request when {@code null}. Nested inside {@link Employee}.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContactInfo {

    private String email;
    private String phone;
    private Address address;

    public ContactInfo() {
    }

    public ContactInfo(String email, String phone, Address address) {
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
