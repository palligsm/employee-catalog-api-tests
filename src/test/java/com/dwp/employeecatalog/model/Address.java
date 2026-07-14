package com.dwp.employeecatalog.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Data model (POJO) for an employee's postal address.
 *
 * <p><b>Purpose:</b> mirrors the {@code contactInfo.address} object in the API's
 * employee JSON. Jackson serialises it into request bodies and deserialises it
 * from responses. Nested inside {@link ContactInfo}.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {

    private String street;
    private String town;
    private String postCode;

    public Address() {
    }

    public Address(String street, String town, String postCode) {
        this.street = street;
        this.town = town;
        this.postCode = postCode;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }
}
