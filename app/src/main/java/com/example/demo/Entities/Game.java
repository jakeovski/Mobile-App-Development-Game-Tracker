package com.example.demo.Entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "Games",foreignKeys = @ForeignKey(entity = User.class,
parentColumns = "user",
childColumns = "user",
onDelete = ForeignKey.CASCADE))
public class Game{

    /**
     * Represents Id - Primary Key
     */
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;

    /**
     * Represents user - Foreign Key
     */
    @ColumnInfo(name = "user")
    private String userEmail;

    /**
     * Represents title
     */
    @ColumnInfo(name = "title")
    private String title;

    /**
     * Represents imageUrl
     */
    @ColumnInfo(name = "imageUrl")
    private String imageUrl;

    /**
     * Represents hours played
     */
    @ColumnInfo(name = "hours_played")
    private String hoursPlayed;

    /**
     * Represents completed
     */
    @ColumnInfo(name = "completed")
    private String completed;

    /**
     * Constructor for the table
     * @param id
     * @param title
     * @param imageUrl
     * @param userEmail
     */
    public Game(int id,String title,String imageUrl,String userEmail) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.hoursPlayed = "00:00";
        this.completed = "N";
        this.userEmail = userEmail;
    }

    /**
     * Alternative constructor
     * @param title
     * @param imageUrl
     * @param userEmail
     */
    @Ignore
    public Game(String title,String imageUrl,String userEmail) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.hoursPlayed = "00:00";
        this.completed = "N";
        this.userEmail = userEmail;
    }

    public int getId(){
        return id;
    }

    public String getTitle(){
        return title;
    }
    public String getImageUrl(){
        return imageUrl;
    }
    public String getHoursPlayed(){
        return hoursPlayed;
    }
    public String getCompleted(){
        return completed;
    }

    public String getUserEmail(){
        return userEmail;
    }

    public void setUserEmail(String user){
        userEmail = user;
    }

    public void setHoursPlayed(String hours){
        hoursPlayed = hours;
    }

    public void setCompleted(String comp) {
        completed = comp;
    }
}
