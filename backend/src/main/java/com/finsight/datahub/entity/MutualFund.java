package com.finsight.datahub.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "mutual_funds",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_mutual_funds_symbol_date", columnNames = {"symbol", "nav_date"})
    },
    indexes = {
        @Index(name = "idx_mutual_funds_symbol", columnList = "symbol"),
        @Index(name = "idx_mutual_funds_nav_date", columnList = "nav_date DESC"),
        @Index(name = "idx_mutual_funds_category", columnList = "category")
    })
public class MutualFund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false)
    private String name;

    @Column(name = "nav_date", nullable = false)
    private LocalDate navDate;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal nav;

    @Column(length = 100)
    private String category;

    @Column(name = "fund_house")
    private String fundHouse;

    @Column(precision = 20, scale = 2)
    private BigDecimal aum;

    @Column(name = "expense_ratio", precision = 6, scale = 4)
    private BigDecimal expenseRatio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upload_id")
    private UploadHistory upload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public MutualFund() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getNavDate() { return navDate; }
    public void setNavDate(LocalDate navDate) { this.navDate = navDate; }

    public BigDecimal getNav() { return nav; }
    public void setNav(BigDecimal nav) { this.nav = nav; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getFundHouse() { return fundHouse; }
    public void setFundHouse(String fundHouse) { this.fundHouse = fundHouse; }

    public BigDecimal getAum() { return aum; }
    public void setAum(BigDecimal aum) { this.aum = aum; }

    public BigDecimal getExpenseRatio() { return expenseRatio; }
    public void setExpenseRatio(BigDecimal expenseRatio) { this.expenseRatio = expenseRatio; }

    public UploadHistory getUpload() { return upload; }
    public void setUpload(UploadHistory upload) { this.upload = upload; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
