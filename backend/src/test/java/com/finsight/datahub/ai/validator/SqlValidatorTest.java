package com.finsight.datahub.ai.validator;

import com.finsight.datahub.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SqlValidatorTest {

    private SqlValidator sqlValidator;

    @BeforeEach
    void setUp() {
        sqlValidator = new SqlValidator();
    }

    @Test
    void testValidSelectQueryPasses() {
        String validSql = "SELECT c.symbol, c.name, s.close_price FROM stocks s JOIN companies c ON s.company_id = c.id ORDER BY s.daily_return DESC LIMIT 10;";
        assertDoesNotThrow(() -> sqlValidator.validate(validSql));
    }

    @Test
    void testRejectDropTableQuery() {
        String dropSql = "DROP TABLE users;";
        BadRequestException ex = assertThrows(BadRequestException.class, () -> sqlValidator.validate(dropSql));
        assertTrue(ex.getMessage().contains("Only read-only SELECT statements are allowed"));
    }

    @Test
    void testRejectDeleteQuery() {
        String deleteSql = "DELETE FROM stocks WHERE volume < 100;";
        BadRequestException ex = assertThrows(BadRequestException.class, () -> sqlValidator.validate(deleteSql));
        assertTrue(ex.getMessage().contains("Only read-only SELECT statements are allowed"));
    }

    @Test
    void testRejectCommentInjection() {
        String commentSql = "SELECT * FROM stocks -- comment injection";
        BadRequestException ex = assertThrows(BadRequestException.class, () -> sqlValidator.validate(commentSql));
        assertTrue(ex.getMessage().contains("Comments are disallowed"));
    }

    @Test
    void testRejectNonWhitelistedTable() {
        String invalidTableSql = "SELECT * FROM users;";
        BadRequestException ex = assertThrows(BadRequestException.class, () -> sqlValidator.validate(invalidTableSql));
        assertTrue(ex.getMessage().contains("unauthorized tables"));
    }
}
