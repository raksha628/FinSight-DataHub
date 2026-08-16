package com.finsight.datahub.ai.prompt;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PromptBuilder {

    private static final String SCHEMA_CONTEXT = """
            Database PostgreSQL Tables and Columns:
            1. companies (id, symbol, name, sector, industry, country, exchange, market_cap)
            2. stocks (id, company_id, trade_date, open_price, high_price, low_price, close_price, adj_close, volume, daily_return)
            3. etfs (id, symbol, name, trade_date, nav, open_price, high_price, low_price, close_price, volume, aum, expense_ratio, category)
            4. mutual_funds (id, symbol, name, nav_date, nav, category, fund_house, aum, expense_ratio)
            5. crypto (id, symbol, name, trade_date, open_price, high_price, low_price, close_price, volume, market_cap, daily_return)
            6. forex (id, base_currency, quote_currency, trade_date, open_rate, high_rate, low_rate, close_rate, daily_change)
            7. sector_performance (id, sector_name, performance_date, daily_return_pct, weekly_return_pct, monthly_return_pct, ytd_return_pct, total_market_cap, total_volume)
            """;

    public String buildSqlGenerationPrompt(String userQuestion) {
        return String.format("""
                You are an expert PostgreSQL DBA and Financial Data Analyst.
                Given the following database schema:
                %s

                Convert this natural language question into a valid, efficient PostgreSQL SELECT query:
                "%s"

                RULES:
                1. Return ONLY the raw SQL query. Do NOT include markdown blocks, ```sql tags, explanations, or commentary.
                2. Use JOINs between stocks and companies using company_id = companies.id when symbol or sector is needed.
                3. Sorting and returns rules:
                   - For 'top', 'highest', 'best', or 'gainers', use: ORDER BY s.daily_return DESC
                   - For 'least', 'lowest', 'bottom', 'losers', or 'worst', use: ORDER BY s.daily_return ASC
                4. Dynamic LIMITs: Extract explicit numbers from user prompts (e.g., 'top 5' -> LIMIT 5). Default to LIMIT 10 if unspecified.
                5. Filtering: When sectors are mentioned, use case-insensitive filtering like: WHERE LOWER(c.sector) LIKE '%%sector_name%%'
                6. Only use SELECT queries.
                """, SCHEMA_CONTEXT, userQuestion);
    }

    public String buildMarketSummaryPrompt(String marketMetricsJson) {
        return String.format("""
                You are a Chief Market Strategist. Analyze these financial market metrics:
                %s

                Generate a concise Executive Market Brief containing:
                1. Overall Market Health Score (0-100)
                2. Market Sentiment (BULLISH, BEARISH, or NEUTRAL)
                3. A 2-sentence executive summary of market conditions and key sector drivers.
                """, marketMetricsJson);
    }

    public String buildExplanationPrompt(String contextName, Map<String, Object> metrics) {
        return String.format("""
                You are a senior Wall Street Financial Analyst explaining market chart metrics to C-suite executives.
                Context / Section: "%s"
                Metrics Data: %s

                Write a compelling, professional 2-sentence executive narrative explaining what these figures mean, what drove the movement, and the strategic takeaway.
                """, contextName, metrics != null ? metrics.toString() : "{}");
    }
}
