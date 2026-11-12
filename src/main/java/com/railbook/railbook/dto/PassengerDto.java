package com.railbook.railbook.dto;

import com.railbook.railbook.model.BerthPreference;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PassengerDto {

    @NotBlank(message = "Passenger name is required")
    private String fullName;

    @NotNull(message = "Age is required")
    @Min(value = 1, message = "Age must be at least 1")
    private Integer age;

    @NotBlank(message = "Gender is required")
    private String gender = "MALE";

    @NotNull(message = "Berth preference is required")
    private BerthPreference berthPreference = BerthPreference.NO_PREFERENCE;

    public PassengerDto() {
    }

    public PassengerDto(String fullName, Integer age, String gender, BerthPreference berthPreference) {
        this.fullName = fullName;
        this.age = age;
        this.gender = gender;
        this.berthPreference = berthPreference != null ? berthPreference : BerthPreference.NO_PREFERENCE;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public BerthPreference getBerthPreference() {
        return berthPreference;
    }

    public void setBerthPreference(BerthPreference berthPreference) {
        this.berthPreference = berthPreference;
    }
}
