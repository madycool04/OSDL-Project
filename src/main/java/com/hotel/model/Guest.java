package com.hotel.model;

import java.io.Serializable;

public class Guest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String guestId;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String idProofType;   // Aadhaar, PAN, Passport, etc.
    private String idProofNumber;
    private String nationality;

    public Guest() {}

    public Guest(String guestId, String name, String phone, String email,
                 String address, String idProofType, String idProofNumber) {
        this.guestId = guestId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.idProofType = idProofType;
        this.idProofNumber = idProofNumber;
        this.nationality = "Indian";
    }

    // Getters & Setters
    public String getGuestId() { return guestId; }
    public void setGuestId(String guestId) { this.guestId = guestId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getIdProofType() { return idProofType; }
    public void setIdProofType(String idProofType) { this.idProofType = idProofType; }

    public String getIdProofNumber() { return idProofNumber; }
    public void setIdProofNumber(String idProofNumber) { this.idProofNumber = idProofNumber; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    @Override
    public String toString() {
        return name + " (" + guestId + ")";
    }
}
