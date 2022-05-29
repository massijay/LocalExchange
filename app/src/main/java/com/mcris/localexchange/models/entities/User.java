package com.mcris.localexchange.models.entities;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class User {
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("ID")
    @Expose
    private String id;
    @SerializedName("Phone")
    @Expose
    private String phoneNumber;
    @SerializedName("Mail")
    @Expose
    private String emailAddress;
    @SerializedName("Register Date")
    @Expose(serialize = false)
    private String signUpDateString;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getSignUpDateString() {
        return signUpDateString;
    }

    public LocalDate getDate() {
        try {
            return LocalDate.parse(signUpDateString, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setSignUpDateString(String signUpDateString) {
        this.signUpDateString = signUpDateString;
    }
}
