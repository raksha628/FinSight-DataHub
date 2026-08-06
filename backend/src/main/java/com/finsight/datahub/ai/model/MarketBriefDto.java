package com.finsight.datahub.ai.model;

import java.util.List;

public class MarketBriefDto {
    private int marketHealthScore; // 0 - 100
    private String overallSentiment; // BULLISH, BEARISH, NEUTRAL
    private String topPerformingSector;
    private String weakestSector;
    private List<String> mostActiveStocks;
    private List<String> highestRiskStocks;
    private List<String> unusualTradingVolume;
    private String marketSummary;
    private double confidenceScore; // e.g. 0.95

    public MarketBriefDto() {}

    public int getMarketHealthScore() { return marketHealthScore; }
    public void setMarketHealthScore(int marketHealthScore) { this.marketHealthScore = marketHealthScore; }

    public String getOverallSentiment() { return overallSentiment; }
    public void setOverallSentiment(String overallSentiment) { this.overallSentiment = overallSentiment; }

    public String getTopPerformingSector() { return topPerformingSector; }
    public void setTopPerformingSector(String topPerformingSector) { this.topPerformingSector = topPerformingSector; }

    public String getWeakestSector() { return weakestSector; }
    public void setWeakestSector(String weakestSector) { this.weakestSector = weakestSector; }

    public List<String> getMostActiveStocks() { return mostActiveStocks; }
    public void setMostActiveStocks(List<String> mostActiveStocks) { this.mostActiveStocks = mostActiveStocks; }

    public List<String> getHighestRiskStocks() { return highestRiskStocks; }
    public void setHighestRiskStocks(List<String> highestRiskStocks) { this.highestRiskStocks = highestRiskStocks; }

    public List<String> getUnusualTradingVolume() { return unusualTradingVolume; }
    public void setUnusualTradingVolume(List<String> unusualTradingVolume) { this.unusualTradingVolume = unusualTradingVolume; }

    public String getMarketSummary() { return marketSummary; }
    public void setMarketSummary(String marketSummary) { this.marketSummary = marketSummary; }

    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
}
