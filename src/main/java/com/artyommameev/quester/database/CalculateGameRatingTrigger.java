package com.artyommameev.quester.database;

import com.artyommameev.quester.entity.Game;
import com.artyommameev.quester.entity.Review;
import com.artyommameev.quester.entity.User;
import lombok.val;
import org.h2.tools.TriggerAdapter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A database trigger that executes a SQL to update rating of {@link Game}
 * and {@link User} who created that {@link Game} after a new game
 * {@link Review} is added to the database.
 *
 * @author Artyom Mameev
 */
@SuppressWarnings("ALL")
public class CalculateGameRatingTrigger extends TriggerAdapter {

    /**
     * Executes SQL to update the corresponding entities of the 'GAME' and 'USER'
     * tables when the 'REVIEW' table is updated.
     * <p>
     * The new game rating is equals to the arithmetic average of all rating
     * values of the game, or '0.0' if no reviews related to the game exist.
     * <p>
     * The new user rating is equals to to the arithmetic average of
     * all ratings of the games created by the user, or '0.0' if no reviews
     * related to games that were created by the user exist.
     *
     * @param connection the database connection.
     * @param newRow     the new row that was added in the 'REVIEW' table.
     * @param oldRow     the old row from the 'REVIEW' table that needs
     *                   to be updated.
     */
    @Override
    public void fire(Connection connection, ResultSet oldRow, ResultSet newRow)
            throws SQLException {
        val actualRow = newRow != null ? newRow : oldRow;

        long gameId = actualRow.getLong("game_id");

        val statement = connection.createStatement();

        statement.executeUpdate( // update game rating
                "UPDATE GAME " +
                        "SET RATING = " +
                        "(SELECT CASE WHEN (AVG(RATING) IS NOT NULL) " +
                        "THEN AVG(RATING) ELSE 0.0 END " +
                        "FROM REVIEW " +
                        "WHERE GAME_ID = " + gameId + ") " +
                        "WHERE ID = " + gameId);

        statement.executeUpdate( // update user rating
                "UPDATE USER " +
                        "SET RATING = " +
                        "(SELECT CASE WHEN (AVG(RATING) IS NOT NULL) " +
                        "THEN AVG(RATING) ELSE 0.0 END " +
                        "FROM REVIEW " +
                        "WHERE GAME_ID IN " +
                        "(SELECT ID FROM GAME WHERE USER_ID = " +
                        "(SELECT USER_ID FROM GAME WHERE ID = " + gameId + "))) " +
                        "WHERE ID = " +
                        "(SELECT USER_ID FROM GAME WHERE ID = " + gameId + ")");
    }
}