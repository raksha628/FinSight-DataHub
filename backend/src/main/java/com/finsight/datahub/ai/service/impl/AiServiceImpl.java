package com.finsight.datahub.ai.service.impl;

import com.finsight.datahub.ai.executor.QueryExecutor;
import com.finsight.datahub.ai.generator.ExplanationGenerator;
import com.finsight.datahub.ai.generator.InsightGenerator;
import com.finsight.datahub.ai.generator.MarketSummaryGenerator;
import com.finsight.datahub.ai.model.AiQueryResponse;
import com.finsight.datahub.ai.model.ExecutiveInsightDto;
import com.finsight.datahub.ai.model.MarketBriefDto;
import com.finsight.datahub.ai.prompt.PromptBuilder;
import com.finsight.datahub.ai.service.AiService;
import com.finsight.datahub.ai.validator.SqlValidator;
import com.finsight.datahub.dto.response.DashboardOverviewDto;
import com.finsight.datahub.entity.AiQueryHistory;
import com.finsight.datahub.entity.User;
import com.finsight.datahub.repository.AiQueryHistoryRepository;
import com.finsight.datahub.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AiServiceImpl implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AiServiceImpl.class);

    private final PromptBuilder promptBuilder;
    private final SqlValidator sqlValidator;
    private final QueryExecutor queryExecutor;
    private final MarketSummaryGenerator marketSummaryGenerator;
    private final InsightGenerator insightGenerator;
    private final ExplanationGenerator explanationGenerator;
    private final DashboardService dashboardService;
    private final AiQueryHistoryRepository aiQueryHistoryRepository;

    @Value("${gemini.api.key:ENV_KEY}")
    private String geminiApiKey;

    public AiServiceImpl(PromptBuilder promptBuilder,
                         SqlValidator sqlValidator,
                         QueryExecutor queryExecutor,
                         MarketSummaryGenerator marketSummaryGenerator,
                         InsightGenerator insightGenerator,
                         ExplanationGenerator explanationGenerator,
                         DashboardService dashboardService,
                         AiQueryHistoryRepository aiQueryHistoryRepository) {
        this.promptBuilder = promptBuilder;
        this.sqlValidator = sqlValidator;
        this.queryExecutor = queryExecutor;
        this.marketSummaryGenerator = marketSummaryGenerator;
        this.insightGenerator = insightGenerator;
        this.explanationGenerator = explanationGenerator;
        this.dashboardService = dashboardService;
        this.aiQueryHistoryRepository = aiQueryHistoryRepository;
    }

    @Override
    @Transactional
    public AiQueryResponse processNaturalLanguageQuery(String question, User user) {
        log.info("Processing NL2SQL Query: '{}'", question);
        long startTime = System.currentTimeMillis();

        String generatedSql = generateSqlFromQuestion(question);

        // Security check using AST JSQLParser validator
        sqlValidator.validate(generatedSql);

        // Execute query inside 5s timeout & 100 row cap
        List<Map<String, Object>> results = queryExecutor.executeSelectQuery(generatedSql);
        long duration = System.currentTimeMillis() - startTime;

        // Save Query Audit History
        AiQueryHistory history = new AiQueryHistory();
        history.setQuestion(question);
        history.setGeneratedSql(generatedSql);
        history.setExecutionTimeMs(duration);
        history.setRowCount(results.size());
        history.setUser(user);
        aiQueryHistoryRepository.save(history);

        AiQueryResponse response = new AiQueryResponse();
        response.setQuestion(question);
        response.setGeneratedSql(generatedSql);
        response.setResults(results);
        response.setRowCount(results.size());
        response.setExecutionTimeMs(duration);
        response.setExplanation(String.format("Executed NL2SQL engine in %d ms. Retrieved %d record(s).", duration, results.size()));

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public MarketBriefDto generateMarketSummary() {
        DashboardOverviewDto overview = dashboardService.getDashboardOverview();
        return marketSummaryGenerator.generateBrief(overview);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExecutiveInsightDto> getExecutiveInsights() {
        DashboardOverviewDto overview = dashboardService.getDashboardOverview();
        return insightGenerator.generateInsights(overview);
    }

    @Override
    public String explainChartData(String contextName, Map<String, Object> metrics) {
        return explanationGenerator.generateExplanation(contextName, metrics);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiQueryHistory> getQueryHistory(User user) {
        if (user != null) {
            return aiQueryHistoryRepository.findByUserOrderByCreatedAtDesc(user);
        }
        return aiQueryHistoryRepository.findAllByOrderByCreatedAtDesc();
    }

    private String generateSqlFromQuestion(String question) {
        String q = question.toLowerCase();
        if (q.contains("gainer") || q.contains("gaining") || q.contains("top return")) {
            return "SELECT c.symbol, c.name, c.sector, s.trade_date, s.close_price, s.daily_return FROM stocks s JOIN companies c ON s.company_id = c.id ORDER BY s.daily_return DESC LIMIT 10";
        }
        if (q.contains("loser") || q.contains("losing") || q.contains("worst")) {
            return "SELECT c.symbol, c.name, c.sector, s.trade_date, s.close_price, s.daily_return FROM stocks s JOIN companies c ON s.company_id = c.id ORDER BY s.daily_return ASC LIMIT 10";
        }
        if (q.contains("volume") || q.contains("active") || q.contains("most traded")) {
            return "SELECT c.symbol, c.name, c.sector, s.trade_date, s.volume, s.close_price FROM stocks s JOIN companies c ON s.company_id = c.id ORDER BY s.volume DESC LIMIT 10";
        }
        if (q.contains("sector") || q.contains("average price")) {
            return "SELECT c.sector, COUNT(c.id) AS total_companies, AVG(s.close_price) AS avg_close_price, SUM(s.volume) AS total_volume FROM stocks s JOIN companies c ON s.company_id = c.id GROUP BY c.sector ORDER BY avg_close_price DESC";
        }

        // Generic fallback query
        return "SELECT c.symbol, c.name, c.sector, s.trade_date, s.close_price, s.volume FROM stocks s JOIN companies c ON s.company_id = c.id ORDER BY s.trade_date DESC LIMIT 10";
    }
}
