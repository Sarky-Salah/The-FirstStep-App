package com.example.thefirststep;

public class Beneficiary {

    public String key, name, contact, prostheticType, status, history, photoUrl;
    public Beneficiary() {
    }

    public Beneficiary(String key, String name, String contact, String prostheticType, String status, String history, String photoUrl) {
        this.key = key;
        this.name = name;
        this.contact = contact;
        this.prostheticType = prostheticType;
        this.status = status;
        this.history = history;
        this.photoUrl = photoUrl;
    }

    // Getters and setters

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getProstheticType() {
        return prostheticType;
    }

    public void setProstheticType(String prostheticType) {
        this.prostheticType = prostheticType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }
}
