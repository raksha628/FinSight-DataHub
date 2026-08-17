package com.finsight.datahub.service;

import com.finsight.datahub.FinSightDataHubApplication;
import com.finsight.datahub.dto.response.MovingAverageDto;
import com.finsight.datahub.dto.response.SectorAvgPriceDto;
import com.finsight.datahub.dto.response.StockPerformanceDto;
import com.finsight.datahub.dto.response.UploadResponseDto;
import com.finsight.datahub.entity.AssetType;
import com.finsight.datahub.entity.Company;
import com.finsight.datahub.entity.Stock;
import com.finsight.datahub.entity.User;
import com.finsight.datahub.entity.User.Role;
import com.finsight.datahub.repository.CompanyRepository;
import com.finsight.datahub.repository.StockRepository;
import com.finsight.datahub.repository.UploadHistoryRepository;
import com.finsight.datahub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(
    classes = {FinSightDataHubApplication.class},
    properties = {
        "spring.datasource.url=jdbc:h2:mem:analyticsdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "spring.main.allow-bean-definition-overriding=true"
    }
)
@EnableAutoConfiguration
class AnalyticsServiceIntegrationTest {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private UploadService uploadService;

    @Autowired
    private CompanyRepository companyRepository;

    @SpyBean
    private StockRepository stockRepository;

    @Autowired
    private UploadHistoryRepository uploadHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Company testCompany;

    @BeforeEach
    void setUp() {
        stockRepository.deleteAll();
        uploadHistoryRepository.deleteAll();
        companyRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("test_analyst");
        testUser.setEmail("test_analyst@example.com");
        testUser.setPassword("hashed_pwd");
        testUser.setRole(Role.ANALYST);
        testUser = userRepository.saveAndFlush(testUser);

        testCompany = new Company("AAPL", "Apple Inc.", "Technology", "Consumer Electronics", "USA", "NASDAQ");
        testCompany = companyRepository.saveAndFlush(testCompany);
    }

    @Test
    void testAnalyticsCalculations() {
        // Seed historical stock records for AAPL
        seedStock(testCompany, LocalDate.of(2026, 8, 11), "100.00", 1000L, "0.00");
        seedStock(testCompany, LocalDate.of(2026, 8, 12), "110.00", 2000L, "0.10");
        seedStock(testCompany, LocalDate.of(2026, 8, 13), "120.00", 3000L, "0.0909");

        // Verify Moving Averages (SMA-20) – paginated, page 0 size 10
        Pageable maPageable = PageRequest.of(0, 10);
        Page<MovingAverageDto> movingAveragesPage = analyticsService.getMovingAverages("AAPL", LocalDate.of(2026, 8, 11), LocalDate.of(2026, 8, 13), maPageable);
        assertEquals(3, movingAveragesPage.getTotalElements());
        assertEquals(0, new BigDecimal("100.0000").compareTo(movingAveragesPage.getContent().get(0).getSma20()));
        assertEquals(0, new BigDecimal("105.0000").compareTo(movingAveragesPage.getContent().get(1).getSma20())); // (100+110)/2
        assertEquals(0, new BigDecimal("110.0000").compareTo(movingAveragesPage.getContent().get(2).getSma20())); // (100+110+120)/3

        // Verify Top Gainers
        Pageable pageable = PageRequest.of(0, 10);
        Page<StockPerformanceDto> topGainers = analyticsService.getTopGainers(LocalDate.of(2026, 8, 13), null, null, pageable);
        assertEquals(1, topGainers.getTotalElements());
        assertEquals("AAPL", topGainers.getContent().get(0).getSymbol());
        assertEquals(0, new BigDecimal("120.00").compareTo(topGainers.getContent().get(0).getClosePrice()));

        // Verify Sector Average Prices
        List<SectorAvgPriceDto> sectorAverages = analyticsService.getAveragePriceBySector(LocalDate.of(2026, 8, 13));
        assertEquals(1, sectorAverages.size());
        assertEquals("Technology", sectorAverages.get(0).getSector());
        assertEquals(0, new BigDecimal("120.0000").compareTo(sectorAverages.get(0).getAvgClosePrice()));
    }

    private void seedStock(Company company, LocalDate date, String closePrice, Long volume, String dailyReturn) {
        Stock stock = new Stock();
        stock.setCompany(company);
        stock.setTradeDate(date);
        stock.setOpenPrice(new BigDecimal(closePrice));
        stock.setHighPrice(new BigDecimal(closePrice));
        stock.setLowPrice(new BigDecimal(closePrice));
        stock.setClosePrice(new BigDecimal(closePrice));
        stock.setVolume(volume);
        stock.setDailyReturn(new BigDecimal(dailyReturn));
        stockRepository.saveAndFlush(stock);
    }
}
