package com.example.demo.Dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.demo.Entities.Game;

import java.util.List;

@Dao
public interface GameDao {

    /**
     * Query to return List of games owned by specific user
     * @param user String userEmail
     * @return List<Game>
     */
    @Query("SELECT * FROM Games WHERE user = :user")
    List<Game> getGameList(String user);

    /**
     * Inserts Game into table
     * @param game
     */
    @Insert
    void insertGame(Game game);

    /**
     * Updates Game in the table
     * @param game
     */
    @Update
    void updateGame(Game game);

    /**
     * Deletes Game from the database
     * @param game
     */
    @Delete
    void deleteGame(Game game);

    /**
     * Gets specific Game from user library
     * @param user String userEmail
     * @param title String titleOfTheGame
     * @return Game
     */
    @Query("SELECT * FROM Games WHERE user = :user AND title = :title")
    Game getGame(String user,String title);

    /**
     * Updates hours_played column in Game table of specific user
     * @param user String userEmail
     * @param title String title of the game
     * @param hoursPlayed String hoursPlayed
     */
    @Query("UPDATE Games SET hours_played = :hoursPlayed WHERE user = :user AND title = :title")
    void updateHoursPlayed(String user, String title, String hoursPlayed);

    /**
     * Updates completed column value of the Game of specific user
     * @param user String userEmail
     * @param title String title of the game
     * @param completed String completed status
     */
    @Query("UPDATE Games SET completed = :completed WHERE user = :user AND title = :title")
    void updateCompletedStatus(String user,String title,String completed);

    /**
     * Deletes everything in Games table
     */
    @Query("DELETE FROM Games")
    void nukeTableGame();

    /**
     * Deletes a game from specific user
     * @param user String userEmail
     * @param title String title of the game
     */
    @Query("DELETE FROM Games WHERE user = :user AND title = :title")
    void deleteGameFromUser(String user,String title);


}
