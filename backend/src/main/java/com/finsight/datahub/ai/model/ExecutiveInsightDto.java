package com.finsight.datahub.ai.model;

public class ExecutiveInsightDto {
    private String title;
    private String insightText;
    private String category; // e.g. SECTOR_PERFORMANCE, VOLUME_SPIKE, VOLATILITY
    private String impactLevel; // HIGH, MEDIUM, LOW

    public ExecutiveInsightDto() {}

    public ExecutiveInsightDto(String title, String insightText, String category, String impactLevel) {
        this.title = title;
        this.insightText = insightText;
        this.category = category;
        this.impactLevel = impactLevel;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getInsightText() { return insightText; }
    public void setInsightText(String insightText) { this.insightText = insightText; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImpactLevel() { return impactLevel; }
    public void setImpactLevel(String impactLevel) { this.impactLevel = impactLevel; }
}
