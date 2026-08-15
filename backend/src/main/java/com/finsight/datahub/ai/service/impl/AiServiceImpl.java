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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

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
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key:ENV_KEY}")
    private String geminiApiKey;

    public AiServiceImpl(PromptBuilder promptBuilder,
                         SqlValidator sqlValidator,
                         QueryExecutor queryExecutor,
                         MarketSummaryGenerator marketSummaryGenerator,
                         InsightGenerator insightGenerator,
                         ExplanationGenerator explanationGenerator,
                         DashboardService dashboardService,
                         AiQueryHistoryRepository aiQueryHistoryRepository,
                         RestTemplate restTemplate) {
        this.promptBuilder = promptBuilder;
        this.sqlValidator = sqlValidator;
        this.queryExecutor = queryExecutor;
        this.marketSummaryGenerator = marketSummaryGenerator;
        this.insightGenerator = insightGenerator;
        this.explanationGenerator = explanationGenerator;
        this.dashboardService = dashboardService;
        this.aiQueryHistoryRepository = aiQueryHistoryRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public AiQueryResponse processNaturalLanguageQuery(String question, User user) {
        log.info("Processing NL2SQL Query: '{}'", question);
        long startTime = System.currentTimeMillis();

        String generatedSql = null;
        try {
            generatedSql = generateSqlFromQuestion(question);

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
            history.setIsSuccessful(true);
            aiQueryHistoryRepository.save(history);

            AiQueryResponse response = new AiQueryResponse();
            response.setQuestion(question);
            response.setGeneratedSql(generatedSql);
            response.setResults(results);
            response.setRowCount(results.size());
            response.setExecutionTimeMs(duration);
            response.setExplanation(String.format("Executed NL2SQL engine in %d ms. Retrieved %d record(s).", duration, results.size()));

            return response;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;

            AiQueryHistory history = new AiQueryHistory();
            history.setQuestion(question);
            history.setGeneratedSql(generatedSql != null ? generatedSql : "ERROR");
            history.setExecutionTimeMs(duration);
            history.setRowCount(0);
            history.setUser(user);
            history.setIsSuccessful(false);
            history.setErrorMessage(e.getMessage());
            aiQueryHistoryRepository.save(history);

            throw new com.finsight.datahub.exception.ExternalApiException("AI Service encountered an error: " + e.getMessage(), e);
        }
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
        try {
            String prompt = promptBuilder.buildSqlGenerationPrompt(question);
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + geminiApiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestBody = String.format(
                    "{\"contents\": [{\"parts\":[{\"text\": %s}]}]}",
                    objectMapper.writeValueAsString(prompt)
            );

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject(url, request, String.class);

            JsonNode rootNode = objectMapper.readTree(response);
            String rawSql = rootNode.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
            
            // Strip markdown blocks if Gemini includes them despite instructions
            rawSql = rawSql.trim();
            if (rawSql.startsWith("```sql")) {
                rawSql = rawSql.substring(6);
            } else if (rawSql.startsWith("```")) {
                rawSql = rawSql.substring(3);
            }
            if (rawSql.endsWith("```")) {
                rawSql = rawSql.substring(0, rawSql.length() - 3);
            }
            
            return rawSql.trim();
        } catch (Exception e) {
            log.error("Failed to generate SQL from Gemini: ", e);
            throw new RuntimeException("AI SQL Generation failed: " + e.getMessage(), e);
        }
    }
}
