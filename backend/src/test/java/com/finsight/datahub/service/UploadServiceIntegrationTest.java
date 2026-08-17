package com.finsight.datahub.service;

import com.finsight.datahub.FinSightDataHubApplication;
import com.finsight.datahub.dto.response.UploadResponseDto;
import com.finsight.datahub.entity.AssetType;
import com.finsight.datahub.entity.Company;
import com.finsight.datahub.entity.User.Role;
import com.finsight.datahub.entity.Stock;
import com.finsight.datahub.entity.UploadStatus;
import com.finsight.datahub.entity.User;
import com.finsight.datahub.exception.BadRequestException;
import com.finsight.datahub.repository.CompanyRepository;
import com.finsight.datahub.repository.StockRepository;
import com.finsight.datahub.repository.UploadHistoryRepository;
import com.finsight.datahub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    classes = {FinSightDataHubApplication.class},
    properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
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
class UploadServiceIntegrationTest {

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
    }

    @Test
    void testUploadCsvSuccess() {
        // Seed Company Apple Inc.
        Company company = new Company("AAPL", "Apple Inc.", "Technology", "Consumer Electronics", "USA", "NASDAQ");
        companyRepository.saveAndFlush(company);

        String csvData = "date,symbol,open,high,low,close,volume,adj_close\n" +
                         "2026-08-15,AAPL,180.00,185.00,179.00,184.00,50000,184.00\n";
        
        MockMultipartFile file = new MockMultipartFile(
                "file", "stocks_valid.csv", "text/csv",
                csvData.getBytes(StandardCharsets.UTF_8)
        );

        UploadResponseDto response = uploadService.uploadCsv(file, AssetType.STOCK, testUser);

        assertNotNull(response);
        assertEquals(UploadStatus.SUCCESS, response.getStatus());
        assertEquals(1, response.getAcceptedRows());
        assertEquals(0, response.getRejectedRows());

        List<Stock> stocks = stockRepository.findAll();
        assertEquals(1, stocks.size());
        Stock savedStock = stocks.get(0);
        assertEquals(company.getId(), savedStock.getCompany().getId());
        assertEquals(LocalDate.of(2026, 8, 15), savedStock.getTradeDate());
        assertEquals(0, new BigDecimal("184.00").compareTo(savedStock.getClosePrice()));
    }

    @Test
    void testUploadInvalidFileFormat() {
        // Test an invalid file format (validation rejection: negative close price)
        Company company = new Company("AAPL", "Apple Inc.", "Technology", "Consumer Electronics", "USA", "NASDAQ");
        companyRepository.saveAndFlush(company);

        // Apple row has a negative close price which violates validations
        String csvData = "date,symbol,open,high,low,close,volume,adj_close\n" +
                         "2026-08-15,AAPL,180.00,185.00,179.00,-184.00,50000,184.00\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "stocks_invalid.csv", "text/csv",
                csvData.getBytes(StandardCharsets.UTF_8)
        );

        // Verify that BadRequestException is thrown
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            uploadService.uploadCsv(file, AssetType.STOCK, testUser);
        });

        assertTrue(exception.getMessage().contains("CSV validation failed"));

        // Ensure database remains empty (transaction was rolled back)
        List<Stock> stocks = stockRepository.findAll();
        assertTrue(stocks.isEmpty());
    }

    @Test
    void testUploadDatabaseConstraintViolation() {
        // Test unique constraint violation (duplicate records for same company and date in same upload)
        Company company = new Company("AAPL", "Apple Inc.", "Technology", "Consumer Electronics", "USA", "NASDAQ");
        companyRepository.saveAndFlush(company);

        // Spy on stockRepository to throw unique constraint violation when saving the stock
        Mockito.doThrow(new DataIntegrityViolationException("Unique constraint violation uq_stocks_company_date"))
                .when(stockRepository).saveAndFlush(Mockito.any(Stock.class));

        // Duplicate rows for AAPL on 2026-08-15
        String csvData = "date,symbol,open,high,low,close,volume,adj_close\n" +
                         "2026-08-15,AAPL,180.00,185.00,179.00,184.00,50000,184.00\n" +
                         "2026-08-15,AAPL,180.00,185.00,179.00,184.00,50000,184.00\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "stocks_duplicate.csv", "text/csv",
                csvData.getBytes(StandardCharsets.UTF_8)
        );

        // Verify that BadRequestException is thrown wrapping the database constraint violation
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            uploadService.uploadCsv(file, AssetType.STOCK, testUser);
        });

        assertTrue(exception.getMessage().contains("ETL Processing Failed") || exception.getMessage().contains("Constraint violation") || exception.getMessage().contains("constraint") || exception.getMessage().contains("violation"));

        // Ensure database remains empty (transaction was rolled back)
        List<Stock> stocks = stockRepository.findAll();
        assertTrue(stocks.isEmpty());
    }
}
