package com.finsight.datahub.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class DashboardOverviewDto {
    private long totalStocks;
    private long totalCompanies;
    private BigDecimal avgMarketPrice;
    private StockPerformanceDto topGainer;
    private StockPerformanceDto topLoser;
    private StockPerformanceDto highestVolumeStock;
    private List<SectorAvgPriceDto> sectorDistribution;
    private List<UploadHistoryDto> recentUploads;

    public DashboardOverviewDto() {}

    public long getTotalStocks() { return totalStocks; }
    public void setTotalStocks(long totalStocks) { this.totalStocks = totalStocks; }

    public long getTotalCompanies() { return totalCompanies; }
    public void setTotalCompanies(long totalCompanies) { this.totalCompanies = totalCompanies; }

    public BigDecimal getAvgMarketPrice() { return avgMarketPrice; }
    public void setAvgMarketPrice(BigDecimal avgMarketPrice) { this.avgMarketPrice = avgMarketPrice; }

    public StockPerformanceDto getTopGainer() { return topGainer; }
    public void setTopGainer(StockPerformanceDto topGainer) { this.topGainer = topGainer; }

    public StockPerformanceDto getTopLoser() { return topLoser; }
    public void setTopLoser(StockPerformanceDto topLoser) { this.topLoser = topLoser; }

    public StockPerformanceDto getHighestVolumeStock() { return highestVolumeStock; }
    public void setHighestVolumeStock(StockPerformanceDto highestVolumeStock) { this.highestVolumeStock = highestVolumeStock; }

    public List<SectorAvgPriceDto> getSectorDistribution() { return sectorDistribution; }
    public void setSectorDistribution(List<SectorAvgPriceDto> sectorDistribution) { this.sectorDistribution = sectorDistribution; }

    public List<UploadHistoryDto> getRecentUploads() { return recentUploads; }
    public void setRecentUploads(List<UploadHistoryDto> recentUploads) { this.recentUploads = recentUploads; }
}
