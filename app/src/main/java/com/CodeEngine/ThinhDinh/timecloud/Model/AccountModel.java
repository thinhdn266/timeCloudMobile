package com.CodeEngine.ThinhDinh.timecloud.Model;

/**
 * Created by Administrator on 3/15/2018.
 */

public class AccountModel {
    private String email;
    private String role;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public AccountModel(String email, String role) {
        this.email = email;
        this.role = role;
    }
}
