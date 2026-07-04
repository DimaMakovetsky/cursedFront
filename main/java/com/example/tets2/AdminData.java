package com.example.tets2;

public class AdminData {
    private String login;
    private String password;

    public AdminData(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
