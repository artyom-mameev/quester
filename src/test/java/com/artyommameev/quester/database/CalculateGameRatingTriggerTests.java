package com.artyommameev.quester.database;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CalculateGameRatingTriggerTests {

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    @Mock
    private Connection connection;
    @Mock
    private Statement statement;
    @Mock
    private ResultSet newRow;
    @Mock
    private ResultSet oldRow;

    @Test
    public void executesSqlInstructionWhenNewRow() throws Exception {
        when(connection.createStatement()).thenReturn(statement);
        when(newRow.getLong("game_id")).thenReturn(1L);

        val calculateGameRatingTrigger = new CalculateGameRatingTrigger();
        calculateGameRatingTrigger.fire(connection, null, newRow);

        verify(statement, times(2)).executeUpdate(
                stringArgumentCaptor.capture());

        assertEquals("UPDATE GAME " +
                "SET RATING = " +
                "(SELECT CASE WHEN (AVG(RATING) IS NOT NULL) " +
                "THEN AVG(RATING) ELSE 0.0 END " +
                "FROM REVIEW " +
                "WHERE GAME_ID = 1) " +
                "WHERE ID = 1", stringArgumentCaptor.getAllValues().get(0));

        assertEquals("UPDATE USER " +
                        "SET RATING = " +
                        "(SELECT CASE WHEN (AVG(RATING) IS NOT NULL) " +
                        "THEN AVG(RATING) ELSE 0.0 END " +
                        "FROM REVIEW " +
                        "WHERE GAME_ID IN " +
                        "(SELECT ID FROM GAME WHERE USER_ID = " +
                        "(SELECT USER_ID FROM GAME WHERE ID = 1))) " +
                        "WHERE ID = " +
                        "(SELECT USER_ID FROM GAME WHERE ID = 1)",
                stringArgumentCaptor.getAllValues().get(1));
    }

    @Test
    public void executesSqlInstructionWhenOldRow() throws Exception {
        when(connection.createStatement()).thenReturn(statement);
        when(oldRow.getLong("game_id")).thenReturn(2L);

        val calculateGameRatingTrigger = new CalculateGameRatingTrigger();

        calculateGameRatingTrigger.fire(connection, oldRow, null);

        verify(statement, times(2)).executeUpdate(
                stringArgumentCaptor.capture());

        assertEquals("UPDATE GAME " +
                "SET RATING = " +
                "(SELECT CASE WHEN (AVG(RATING) IS NOT NULL) " +
                "THEN AVG(RATING) ELSE 0.0 END " +
                "FROM REVIEW " +
                "WHERE GAME_ID = 2) " +
                "WHERE ID = 2", stringArgumentCaptor.getAllValues().get(0));

        assertEquals("UPDATE USER " +
                        "SET RATING = " +
                        "(SELECT CASE WHEN (AVG(RATING) IS NOT NULL) " +
                        "THEN AVG(RATING) ELSE 0.0 END " +
                        "FROM REVIEW " +
                        "WHERE GAME_ID IN " +
                        "(SELECT ID FROM GAME WHERE USER_ID = " +
                        "(SELECT USER_ID FROM GAME WHERE ID = 2))) " +
                        "WHERE ID = " +
                        "(SELECT USER_ID FROM GAME WHERE ID = 2)",
                stringArgumentCaptor.getAllValues().get(1));
    }
}
