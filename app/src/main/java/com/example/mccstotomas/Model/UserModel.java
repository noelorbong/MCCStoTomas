package com.example.mccstotomas.Model;

public class UserModel {

    private int Id;
    private String Name="";
    private String Address="";
    private String Dob = "";
    private String Photo = "";
    private String MobileNumber = "";
    private String Email="";
    private String Password="";

    public int getId() {
        return Id;
    }
    public void setId(int id) {
        this.Id = id;
    }

    public String getName() {
        return Name;
    }
    public void setName(String name) { this.Name = name; }

    public String getAddress() {
        return Address;
    }
    public void setAddress(String address) { this.Address = address;}

    public String getDob() {
        return Dob;
    }
    public void setDob(String dob) { this.Dob = dob;}

    public String getPhoto() {
        return Photo;
    }
    public void setPhoto(String photo) { this.Photo = photo;}

    public String getEmail() {
        return Email;
    }
    public void setEmail(String email) { this.Email = email;}

    public String getMobileNumber() {
        return MobileNumber;
    }
    public void setMobileNumber(String mobileNumber) { this.MobileNumber = mobileNumber;}


    public String getPassword() {
        return Password;
    }
    public void setPassword(String password) { this.Password = password;}

    }
