package com.finsight.datahub.ai.model;

import java.util.Map;

public class ExplanationRequestDto {
    private String contextName; // e.g. "Technology Sector", "Daily Return Distribution"
    private Map<String, Object> metrics;

    public ExplanationRequestDto() {}

    public ExplanationRequestDto(String contextName, Map<String, Object> metrics) {
        this.contextName = contextName;
        this.metrics = metrics;
    }

    public String getContextName() { return contextName; }
    public void setContextName(String contextName) { this.contextName = contextName; }

    public Map<String, Object> getMetrics() { return metrics; }
    public void setMetrics(Map<String, Object> metrics) { this.metrics = metrics; }
}
