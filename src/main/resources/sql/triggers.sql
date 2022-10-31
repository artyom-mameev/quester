-- noinspection SqlResolveForFile @ table/"REVIEW"

CREATE TRIGGER calculate_game_rating_trigger_insert
    AFTER INSERT
    ON REVIEW
    FOR EACH ROW
CALL "com.artyommameev.quester.database.CalculateGameRatingTrigger";
CREATE TRIGGER calculate_game_rating_trigger_update
    AFTER UPDATE
    ON REVIEW
    FOR EACH ROW
CALL "com.artyommameev.quester.database.CalculateGameRatingTrigger";
CREATE TRIGGER calculate_game_rating_trigger_delete
    AFTER DELETE
    ON REVIEW
    FOR EACH ROW
CALL "com.artyommameev.quester.database.CalculateGameRatingTrigger";