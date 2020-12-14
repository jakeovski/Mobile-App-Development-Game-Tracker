package com.example.demo.Entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "User")
public class User {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "user")
    private String user;

    @ColumnInfo(name = "password")
    private String password;

    @ColumnInfo(name = "username")
    private String username;

    @ColumnInfo(name = "total_games")
    private int totalGames;

    @ColumnInfo(name = "completed_games")
    private int completedGames;

    @ColumnInfo(name = "profile_image")
    private String profile_image;

    public User(String user, String username, String password) {
        this.user = user;
        this.username = username;
        this.password = password;
        this.totalGames = 0;
        this.completedGames = 0;
    }

    public String getProfile_image(){
        return profile_image;
    }
    public String getUser(){
        return user;
    }

    public String getPassword(){
        return password;
    }

    public String getUsername(){
        return username;
    }

    public int getTotalGames(){
        return totalGames;
    }

    public int getCompletedGames(){
        return completedGames;
    }

    public void setTotalGames(int count){
        totalGames = count;
    }

    public void setCompletedGames(int count) {
        completedGames = count;
    }

    public void setPassword(String pass) {
        password = pass;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setProfile_image(String uri) {
        profile_image = uri;
    }
}
