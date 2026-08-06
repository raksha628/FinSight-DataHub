package com.finsight.datahub.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "forex",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_forex_pair_date", columnNames = {"base_currency", "quote_currency", "trade_date"})
    },
    indexes = {
        @Index(name = "idx_forex_pair", columnList = "base_currency, quote_currency"),
        @Index(name = "idx_forex_trade_date", columnList = "trade_date DESC")
    })
public class Forex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency;

    @Column(name = "quote_currency", nullable = false, length = 3)
    private String quoteCurrency;

    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    @Column(name = "open_rate", nullable = false, precision = 15, scale = 6)
    private BigDecimal openRate;

    @Column(name = "high_rate", nullable = false, precision = 15, scale = 6)
    private BigDecimal highRate;

    @Column(name = "low_rate", nullable = false, precision = 15, scale = 6)
    private BigDecimal lowRate;

    @Column(name = "close_rate", nullable = false, precision = 15, scale = 6)
    private BigDecimal closeRate;

    @Column(name = "daily_change", precision = 10, scale = 6)
    private BigDecimal dailyChange;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upload_id")
    private UploadHistory upload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Forex() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBaseCurrency() { return baseCurrency; }
    public void setBaseCurrency(String baseCurrency) { this.baseCurrency = baseCurrency; }

    public String getQuoteCurrency() { return quoteCurrency; }
    public void setQuoteCurrency(String quoteCurrency) { this.quoteCurrency = quoteCurrency; }

    public LocalDate getTradeDate() { return tradeDate; }
    public void setTradeDate(LocalDate tradeDate) { this.tradeDate = tradeDate; }

    public BigDecimal getOpenRate() { return openRate; }
    public void setOpenRate(BigDecimal openRate) { this.openRate = openRate; }

    public BigDecimal getHighRate() { return highRate; }
    public void setHighRate(BigDecimal highRate) { this.highRate = highRate; }

    public BigDecimal getLowRate() { return lowRate; }
    public void setLowRate(BigDecimal lowRate) { this.lowRate = lowRate; }

    public BigDecimal getCloseRate() { return closeRate; }
    public void setCloseRate(BigDecimal closeRate) { this.closeRate = closeRate; }

    public BigDecimal getDailyChange() { return dailyChange; }
    public void setDailyChange(BigDecimal dailyChange) { this.dailyChange = dailyChange; }

    public UploadHistory getUpload() { return upload; }
    public void setUpload(UploadHistory upload) { this.upload = upload; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
