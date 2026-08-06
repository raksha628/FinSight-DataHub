package com.finsight.datahub.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MovingAverageDto {
    private String symbol;
    private String companyName;
    private LocalDate tradeDate;
    private BigDecimal closePrice;
    private BigDecimal sma20;
    private BigDecimal sma50;

    public MovingAverageDto() {}

    public MovingAverageDto(String symbol, String companyName, LocalDate tradeDate, BigDecimal closePrice, BigDecimal sma20, BigDecimal sma50) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.tradeDate = tradeDate;
        this.closePrice = closePrice;
        this.sma20 = sma20;
        this.sma50 = sma50;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public LocalDate getTradeDate() { return tradeDate; }
    public void setTradeDate(LocalDate tradeDate) { this.tradeDate = tradeDate; }

    public BigDecimal getClosePrice() { return closePrice; }
    public void setClosePrice(BigDecimal closePrice) { this.closePrice = closePrice; }

    public BigDecimal getSma20() { return sma20; }
    public void setSma20(BigDecimal sma20) { this.sma20 = sma20; }

    public BigDecimal getSma50() { return sma50; }
    public void setSma50(BigDecimal sma50) { this.sma50 = sma50; }
}
