package com.example.thefirststep;

public class Beneficiary {

    public String key, name, contact, status, history, side, level, photoUrl;
    public String gender, birthDate, occupation, address, yearOfAmputation, nameKin, contactKin;
    public Beneficiary() {
    }

    public Beneficiary(String key, String name, String contact, String level, String side, String status, String history,
                       String photoUrl, String gender, String birthDate, String occupation, String address,
                       String yearOfAmputation, String nameKin, String contactKin) {
        this.key = key;
        this.name = name;
        this.contact = contact;
        this.side = side;
        this.level = level;
        this.status = status;
        this.history = history;
        this.photoUrl = photoUrl;
        this.gender = gender;
        this.birthDate = birthDate;
        this.occupation = occupation;
        this.address = address;
        this.yearOfAmputation = yearOfAmputation;
        this.nameKin = nameKin;
        this.contactKin = contactKin;
    }

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

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
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

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getBirthDate() { return birthDate; }
    public void getBirthDate(String birthDate) { this.birthDate = birthDate; }
    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getYearOfAmputation() { return yearOfAmputation; }
    public void setYearOfAmputation(String yearOfAmputation) { this.yearOfAmputation = yearOfAmputation; }
    public String getNameKin() { return nameKin; }
    public void setNameKin(String nameKin) { this.nameKin = nameKin; }
    public String getContactKin() { return contactKin; }
    public void setContactKin(String contactKin) { this.contactKin = contactKin; }

}