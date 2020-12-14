package com.example.demo.Dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.demo.Entities.User;

import java.util.List;

@Dao
public interface UserDao {

    /**
     * Gets all users from the table
     * @return List<User>
     */
    @Query("SELECT * FROM user")
    List<User> getAllUsers();

    /**
     * Finds a user from the email specified
     * @param email String userEmail
     * @return User
     */
    @Query("SELECT * FROM user WHERE user LIKE :email LIMIT 1")
    User findByEmail(String email);

    /**
     * Inserts user into the table
     * @param user User
     */
    @Insert
    void insertUser(User user);

    /**
     * Deletes a specific user from the table
     * @param user User
     */
    @Delete
    void delete(User user);

    /**
     * Deletes everything from User table
     */
    @Query("DELETE FROM User")
    void nukeTableUser();

    /**
     * Updates profile_image column of specified user
     * @param email String userEmail
     * @param profileImage String profileImage
     */
    @Query("UPDATE User SET profile_image = :profileImage WHERE user = :email")
    void updateProfileImage(String email,String profileImage);

    /**
     * Updates total_games count of specific user
     * @param email String userEmail
     * @param totalGames int totalGames
     */
    @Query("UPDATE User SET total_games = :totalGames WHERE user = :email")
    void updateTotalGames(String email,int totalGames);

    /**
     * Updates completed_games count of specific user
     * @param email String userEmail
     * @param completedGames int completedGames
     */
    @Query("UPDATE User SET completed_games = :completedGames WHERE user = :email")
    void updateCompletedGames(String email,int completedGames);
}
