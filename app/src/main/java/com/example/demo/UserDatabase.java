package com.example.demo;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.demo.Dao.GameDao;
import com.example.demo.Dao.UserDao;
import com.example.demo.Entities.Game;
import com.example.demo.Entities.User;

@Database(entities = {User.class, Game.class},version = 12,exportSchema = false)
public abstract class UserDatabase extends RoomDatabase {
    private static final String DB_NAME = "user_db";
    private static UserDatabase instance;

    public static synchronized UserDatabase getInstance(Context context) {
        if(instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), UserDatabase.class,DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
    public abstract UserDao getUserDao();

    public abstract GameDao getGameDao();
}
