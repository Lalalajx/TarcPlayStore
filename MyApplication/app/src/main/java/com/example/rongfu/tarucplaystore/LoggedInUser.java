package com.example.rongfu.tarucplaystore;


import com.example.rongfu.tarucplaystore.Class.User;

public class LoggedInUser {
    private static User user = new User();

    public static void Login (User user){
        LoggedInUser.user = user;
    }

    public static User getLoggedInUser(){
        return LoggedInUser.user;
    }

    public static void Logout(){
        user = new User();
    }

}
