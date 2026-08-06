package com.finsight.datahub.ai.validator;

import com.finsight.datahub.exception.BadRequestException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SqlValidator {

    private static final Logger log = LoggerFactory.getLogger(SqlValidator.class);

    private static final Set<String> ALLOWED_TABLES = Set.of(
            "stocks",
            "companies",
            "etfs",
            "mutual_funds",
            "crypto",
            "forex",
            "sector_performance"
    );

    public void validate(String rawSql) {
        if (rawSql == null || rawSql.isBlank()) {
            throw new BadRequestException("Generated SQL query cannot be empty.");
        }

        String sql = rawSql.trim();

        // Rule 1: Reject comments
        if (sql.contains("--") || sql.contains("/*") || sql.contains("*/")) {
            log.warn("SQL Security Violation: Comments disallowed in generated query: {}", sql);
            throw new BadRequestException("SQL Security Violation: Comments are disallowed in AI generated queries.");
        }

        // Rule 2: Reject multiple statements (semicolons)
        long semicolonCount = sql.chars().filter(ch -> ch == ';').count();
        if (semicolonCount > 1 || (semicolonCount == 1 && !sql.endsWith(";"))) {
            log.warn("SQL Security Violation: Multiple SQL statements disallowed: {}", sql);
            throw new BadRequestException("SQL Security Violation: Multiple SQL statements are strictly disallowed.");
        }

        // Remove trailing semicolon for parsing
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1).trim();
        }

        // Rule 3: AST Parsing & Statement Type Verification via JSQLParser
        Statement statement;
        try {
            statement = CCJSqlParserUtil.parse(sql);
        } catch (Exception e) {
            log.error("JSQLParser failed to parse SQL: {}", sql, e);
            throw new BadRequestException("SQL Syntax Error: The generated query could not be parsed securely. Details: " + e.getMessage());
        }

        if (!(statement instanceof Select)) {
            log.warn("SQL Security Violation: Non-SELECT statement attempt ({})", statement.getClass().getSimpleName());
            throw new BadRequestException("SQL Security Violation: Only read-only SELECT statements are allowed.");
        }

        // Rule 4: Table Whitelist Verification
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(statement);

        Set<String> invalidTables = tableList.stream()
                .map(String::toLowerCase)
                .filter(table -> !ALLOWED_TABLES.contains(table))
                .collect(Collectors.toSet());

        if (!invalidTables.isEmpty()) {
            log.warn("SQL Security Violation: Query references non-whitelisted table(s): {}", invalidTables);
            throw new BadRequestException("SQL Security Violation: Query accesses unauthorized tables: " + invalidTables);
        }

        log.info("SQL Security Validator: Query successfully validated: {}", sql);
    }
}
