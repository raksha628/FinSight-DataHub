package com.finsight.datahub.ai.executor;

import com.finsight.datahub.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class QueryExecutor {

    private static final Logger log = LoggerFactory.getLogger(QueryExecutor.class);

    private final JdbcTemplate jdbcTemplate;

    public QueryExecutor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> executeSelectQuery(String sql) {
        log.info("Executing AI-generated SQL query: {}", sql);

        try {
            JdbcTemplate safeTemplate = new JdbcTemplate(jdbcTemplate.getDataSource());
            safeTemplate.setMaxRows(100);
            safeTemplate.setQueryTimeout(5); // 5-second timeout safeguard

            return safeTemplate.queryForList(sql);
        } catch (Exception e) {
            log.error("Failed to execute AI SQL query: {}", sql, e);
            throw new BadRequestException("Database Query Execution Failed: " + e.getMessage());
        }
    }
}
