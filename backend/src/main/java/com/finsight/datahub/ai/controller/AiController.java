package com.finsight.datahub.ai.controller;

import com.finsight.datahub.ai.model.*;
import com.finsight.datahub.ai.service.AiService;
import com.finsight.datahub.dto.response.ApiResponse;
import com.finsight.datahub.entity.AiQueryHistory;
import com.finsight.datahub.entity.User;
import com.finsight.datahub.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI Market Copilot", description = "Natural Language to SQL, Executive Brief, Chart Explanations & AI Insights Endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AiController {

    private static final Logger log = LoggerFactory.getLogger(AiController.class);

    private final AiService aiService;
    private final UserRepository userRepository;

    public AiController(AiService aiService, UserRepository userRepository) {
        this.aiService = aiService;
        this.userRepository = userRepository;
    }

    @PostMapping("/query")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    @Operation(
        summary = "Execute Natural Language to SQL query",
        description = "Translates natural language questions into secure PostgreSQL SELECT queries, executes them, and returns tabular data. Roles: ANALYST, ADMIN."
    )
    public ResponseEntity<ApiResponse<AiQueryResponse>> executeNaturalLanguageQuery(
            @Valid @RequestBody AiQueryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("AI Query Request: '{}'", request.getQuestion());
        User user = userDetails != null ? userRepository.findByUsername(userDetails.getUsername()).orElse(null) : null;

        AiQueryResponse result = aiService.processNaturalLanguageQuery(request.getQuestion(), user);
        return ResponseEntity.ok(ApiResponse.success("AI Query executed successfully", result));
    }

    @PostMapping("/market-summary")
    @Operation(
        summary = "Generate Executive Market Brief",
        description = "Analyzes platform data warehouse metrics to output Market Health Score, Overall Sentiment, and key risk/activity trends."
    )
    public ResponseEntity<ApiResponse<MarketBriefDto>> generateMarketSummary() {
        MarketBriefDto brief = aiService.generateMarketSummary();
        return ResponseEntity.ok(ApiResponse.success("Executive Market Brief generated successfully", brief));
    }

    @PostMapping("/explain")
    @Operation(
        summary = "Generate AI Chart & Sector Explanation",
        description = "Translates structured chart data and sector metrics into C-suite natural language narrative explanations."
    )
    public ResponseEntity<ApiResponse<String>> explainChartData(
            @RequestBody ExplanationRequestDto request) {

        String narrative = aiService.explainChartData(request.getContextName(), request.getMetrics());
        return ResponseEntity.ok(ApiResponse.success("Chart explanation generated successfully", narrative));
    }

    @GetMapping("/executive-insights")
    @Operation(
        summary = "Get AI Executive Insights",
        description = "Retrieves explainable AI narrative insights regarding market concentration, volatility, and sector outperformance."
    )
    public ResponseEntity<ApiResponse<List<ExecutiveInsightDto>>> getExecutiveInsights() {
        List<ExecutiveInsightDto> insights = aiService.getExecutiveInsights();
        return ResponseEntity.ok(ApiResponse.success("Executive insights retrieved successfully", insights));
    }

    @GetMapping("/history")
    @Operation(
        summary = "Get user AI query history",
        description = "Retrieves audit log of all previous natural language AI queries executed by the user."
    )
    public ResponseEntity<ApiResponse<List<AiQueryHistory>>> getQueryHistory(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userDetails != null ? userRepository.findByUsername(userDetails.getUsername()).orElse(null) : null;
        List<AiQueryHistory> history = aiService.getQueryHistory(user);
        return ResponseEntity.ok(ApiResponse.success("AI Query history retrieved successfully", history));
    }
}
