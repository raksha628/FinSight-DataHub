package com.finsight.datahub.ai.model;

import java.util.List;
import java.util.Map;

public class AiQueryResponse {
    private String question;
    private String generatedSql;
    private List<Map<String, Object>> results;
    private int rowCount;
    private long executionTimeMs;
    private String explanation;

    public AiQueryResponse() {}

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getGeneratedSql() { return generatedSql; }
    public void setGeneratedSql(String generatedSql) { this.generatedSql = generatedSql; }

    public List<Map<String, Object>> getResults() { return results; }
    public void setResults(List<Map<String, Object>> results) { this.results = results; }

    public int getRowCount() { return rowCount; }
    public void setRowCount(int rowCount) { this.rowCount = rowCount; }

    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
