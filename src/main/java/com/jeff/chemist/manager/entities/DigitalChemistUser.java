package com.jeff.chemist.manager.entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table
public class DigitalChemistUser implements Serializable{

    public DigitalChemistUser(){
        this.fullName = "Adminstrator";
    }

    @Column
    private String fullName;
    @Column
    private int encryptedPassword;
    @Id
    @Column
    private String userName;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(int encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}

