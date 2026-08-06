package com.finsight.datahub.service;

import com.finsight.datahub.ai.executor.QueryExecutor;
import com.finsight.datahub.ai.generator.ExplanationGenerator;
import com.finsight.datahub.ai.generator.InsightGenerator;
import com.finsight.datahub.ai.generator.MarketSummaryGenerator;
import com.finsight.datahub.ai.model.AiQueryResponse;
import com.finsight.datahub.ai.model.MarketBriefDto;
import com.finsight.datahub.ai.prompt.PromptBuilder;
import com.finsight.datahub.ai.service.impl.AiServiceImpl;
import com.finsight.datahub.ai.validator.SqlValidator;
import com.finsight.datahub.dto.response.DashboardOverviewDto;
import com.finsight.datahub.entity.User;
import com.finsight.datahub.repository.AiQueryHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceImplTest {

    @Mock
    private PromptBuilder promptBuilder;
    @Mock
    private SqlValidator sqlValidator;
    @Mock
    private QueryExecutor queryExecutor;
    @Mock
    private MarketSummaryGenerator marketSummaryGenerator;
    @Mock
    private InsightGenerator insightGenerator;
    @Mock
    private ExplanationGenerator explanationGenerator;
    @Mock
    private DashboardService dashboardService;
    @Mock
    private AiQueryHistoryRepository aiQueryHistoryRepository;

    private AiServiceImpl aiService;

    @BeforeEach
    void setUp() {
        aiService = new AiServiceImpl(
                promptBuilder, sqlValidator, queryExecutor,
                marketSummaryGenerator, insightGenerator, explanationGenerator,
                dashboardService, aiQueryHistoryRepository
        );
    }

    @Test
    void testProcessNaturalLanguageQuery() {
        String question = "Show top gainers in Technology";
        User user = new User();
        user.setUsername("analyst");

        when(queryExecutor.executeSelectQuery(any())).thenReturn(List.of(Map.of("symbol", "AAPL", "close_price", 184.75)));

        AiQueryResponse response = aiService.processNaturalLanguageQuery(question, user);

        assertNotNull(response);
        assertEquals(question, response.getQuestion());
        assertEquals(1, response.getRowCount());
        verify(sqlValidator).validate(any());
        verify(aiQueryHistoryRepository).save(any());
    }

    @Test
    void testGenerateMarketSummary() {
        DashboardOverviewDto overview = new DashboardOverviewDto();
        MarketBriefDto mockBrief = new MarketBriefDto();
        mockBrief.setMarketHealthScore(85);

        when(dashboardService.getDashboardOverview()).thenReturn(overview);
        when(marketSummaryGenerator.generateBrief(overview)).thenReturn(mockBrief);

        MarketBriefDto brief = aiService.generateMarketSummary();

        assertNotNull(brief);
        assertEquals(85, brief.getMarketHealthScore());
    }
}
