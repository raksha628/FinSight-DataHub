package com.finsight.datahub.dto.response;

import java.math.BigDecimal;

public class SectorAvgPriceDto {
    private String sector;
    private BigDecimal avgClosePrice;
    private Long totalVolume;
    private Long companyCount;

    public SectorAvgPriceDto() {}

    public SectorAvgPriceDto(String sector, BigDecimal avgClosePrice, Long totalVolume, Long companyCount) {
        this.sector = sector;
        this.avgClosePrice = avgClosePrice;
        this.totalVolume = totalVolume;
        this.companyCount = companyCount;
    }

    public SectorAvgPriceDto(String sector, Double avgClosePrice, Long totalVolume, Long companyCount) {
        this.sector = sector;
        this.avgClosePrice = avgClosePrice != null ? BigDecimal.valueOf(avgClosePrice) : null;
        this.totalVolume = totalVolume;
        this.companyCount = companyCount;
    }

    public SectorAvgPriceDto(String sector, Number avgClosePrice, Number totalVolume, Number companyCount) {
        this.sector = sector;
        this.avgClosePrice = avgClosePrice != null ? BigDecimal.valueOf(avgClosePrice.doubleValue()) : null;
        this.totalVolume = totalVolume != null ? totalVolume.longValue() : null;
        this.companyCount = companyCount != null ? companyCount.longValue() : null;
    }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public BigDecimal getAvgClosePrice() { return avgClosePrice; }
    public void setAvgClosePrice(BigDecimal avgClosePrice) { this.avgClosePrice = avgClosePrice; }

    public Long getTotalVolume() { return totalVolume; }
    public void setTotalVolume(Long totalVolume) { this.totalVolume = totalVolume; }

    public Long getCompanyCount() { return companyCount; }
    public void setCompanyCount(Long companyCount) { this.companyCount = companyCount; }
}
