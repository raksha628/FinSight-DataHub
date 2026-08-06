package com.finsight.datahub.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sector_performance",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_sector_date", columnNames = {"sector_name", "performance_date"})
    },
    indexes = {
        @Index(name = "idx_sector_performance_sector", columnList = "sector_name"),
        @Index(name = "idx_sector_performance_date", columnList = "performance_date DESC"),
        @Index(name = "idx_sector_performance_return", columnList = "daily_return_pct DESC")
    })
public class SectorPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sector_name", nullable = false, length = 100)
    private String sectorName;

    @Column(name = "performance_date", nullable = false)
    private LocalDate performanceDate;

    @Column(name = "daily_return_pct", precision = 8, scale = 4)
    private BigDecimal dailyReturnPct;

    @Column(name = "weekly_return_pct", precision = 8, scale = 4)
    private BigDecimal weeklyReturnPct;

    @Column(name = "monthly_return_pct", precision = 8, scale = 4)
    private BigDecimal monthlyReturnPct;

    @Column(name = "ytd_return_pct", precision = 8, scale = 4)
    private BigDecimal ytdReturnPct;

    @Column(name = "total_market_cap", precision = 30, scale = 2)
    private BigDecimal totalMarketCap;

    @Column(name = "total_volume")
    private Long totalVolume;

    @Column(name = "advancing_count")
    private Integer advancingCount = 0;

    @Column(name = "declining_count")
    private Integer decliningCount = 0;

    @Column(name = "unchanged_count")
    private Integer unchangedCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upload_id")
    private UploadHistory upload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public SectorPerformance() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSectorName() { return sectorName; }
    public void setSectorName(String sectorName) { this.sectorName = sectorName; }

    public LocalDate getPerformanceDate() { return performanceDate; }
    public void setPerformanceDate(LocalDate performanceDate) { this.performanceDate = performanceDate; }

    public BigDecimal getDailyReturnPct() { return dailyReturnPct; }
    public void setDailyReturnPct(BigDecimal dailyReturnPct) { this.dailyReturnPct = dailyReturnPct; }

    public BigDecimal getWeeklyReturnPct() { return weeklyReturnPct; }
    public void setWeeklyReturnPct(BigDecimal weeklyReturnPct) { this.weeklyReturnPct = weeklyReturnPct; }

    public BigDecimal getMonthlyReturnPct() { return monthlyReturnPct; }
    public void setMonthlyReturnPct(BigDecimal monthlyReturnPct) { this.monthlyReturnPct = monthlyReturnPct; }

    public BigDecimal getYtdReturnPct() { return ytdReturnPct; }
    public void setYtdReturnPct(BigDecimal ytdReturnPct) { this.ytdReturnPct = ytdReturnPct; }

    public BigDecimal getTotalMarketCap() { return totalMarketCap; }
    public void setTotalMarketCap(BigDecimal totalMarketCap) { this.totalMarketCap = totalMarketCap; }

    public Long getTotalVolume() { return totalVolume; }
    public void setTotalVolume(Long totalVolume) { this.totalVolume = totalVolume; }

    public Integer getAdvancingCount() { return advancingCount; }
    public void setAdvancingCount(Integer advancingCount) { this.advancingCount = advancingCount; }

    public Integer getDecliningCount() { return decliningCount; }
    public void setDecliningCount(Integer decliningCount) { this.decliningCount = decliningCount; }

    public Integer getUnchangedCount() { return unchangedCount; }
    public void setUnchangedCount(Integer unchangedCount) { this.unchangedCount = unchangedCount; }

    public UploadHistory getUpload() { return upload; }
    public void setUpload(UploadHistory upload) { this.upload = upload; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
