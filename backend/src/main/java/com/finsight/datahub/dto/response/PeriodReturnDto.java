package com.finsight.datahub.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PeriodReturnDto {
    private String symbol;
    private String companyName;
    private String sector;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal startPrice;
    private BigDecimal endPrice;
    private BigDecimal absoluteChange;
    private BigDecimal percentageReturn;

    public PeriodReturnDto() {}

    public PeriodReturnDto(String symbol, String companyName, String sector, LocalDate startDate, LocalDate endDate,
                           BigDecimal startPrice, BigDecimal endPrice, BigDecimal absoluteChange, BigDecimal percentageReturn) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.sector = sector;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startPrice = startPrice;
        this.endPrice = endPrice;
        this.absoluteChange = absoluteChange;
        this.percentageReturn = percentageReturn;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public BigDecimal getStartPrice() { return startPrice; }
    public void setStartPrice(BigDecimal startPrice) { this.startPrice = startPrice; }

    public BigDecimal getEndPrice() { return endPrice; }
    public void setEndPrice(BigDecimal endPrice) { this.endPrice = endPrice; }

    public BigDecimal getAbsoluteChange() { return absoluteChange; }
    public void setAbsoluteChange(BigDecimal absoluteChange) { this.absoluteChange = absoluteChange; }

    public BigDecimal getPercentageReturn() { return percentageReturn; }
    public void setPercentageReturn(BigDecimal percentageReturn) { this.percentageReturn = percentageReturn; }
}
