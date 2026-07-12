package com.dwp.employeecatalog.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Employee request/response model.
 *
 * <p>Used both as a create/update request body and to deserialise responses.
 * The API is inconsistent about {@code dateOfBirth} (it echoes back only the
 * year on create, and a full ISO date-time on GET-by-id), so the field is kept
 * as a raw {@link String} and asserted on explicitly in the tests rather than
 * being bound to a strict date type.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Employee {

    private String employeeId;
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private ContactInfo contactInfo;

    public Employee() {
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    /** Fluent builder for readable test data construction. */
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Employee employee = new Employee();

        public Builder firstName(String firstName) {
            employee.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            employee.lastName = lastName;
            return this;
        }

        public Builder dateOfBirth(String dateOfBirth) {
            employee.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder contactInfo(ContactInfo contactInfo) {
            employee.contactInfo = contactInfo;
            return this;
        }

        public Employee build() {
            return employee;
        }
    }
}
