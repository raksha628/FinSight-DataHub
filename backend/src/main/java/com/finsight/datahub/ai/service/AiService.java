package com.finsight.datahub.ai.service;

import com.finsight.datahub.ai.model.AiQueryResponse;
import com.finsight.datahub.ai.model.ExecutiveInsightDto;
import com.finsight.datahub.ai.model.MarketBriefDto;
import com.finsight.datahub.entity.AiQueryHistory;
import com.finsight.datahub.entity.User;

import java.util.List;
import java.util.Map;

public interface AiService {

    AiQueryResponse processNaturalLanguageQuery(String question, User user);

    MarketBriefDto generateMarketSummary();

    List<ExecutiveInsightDto> getExecutiveInsights();

    String explainChartData(String contextName, Map<String, Object> metrics);

    List<AiQueryHistory> getQueryHistory(User user);
}
