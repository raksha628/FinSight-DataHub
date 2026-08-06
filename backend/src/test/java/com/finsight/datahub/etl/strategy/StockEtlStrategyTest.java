package com.finsight.datahub.etl.strategy;

import com.finsight.datahub.entity.Company;
import com.finsight.datahub.entity.Stock;
import com.finsight.datahub.entity.UploadHistory;
import com.finsight.datahub.etl.EtlResult;
import com.finsight.datahub.repository.CompanyRepository;
import com.finsight.datahub.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class StockEtlStrategyTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private CompanyRepository companyRepository;

    private StockEtlStrategy stockEtlStrategy;

    @BeforeEach
    void setUp() {
        stockEtlStrategy = new StockEtlStrategy(stockRepository, companyRepository);
    }

    @Test
    void testProcessValidStockCsv() throws Exception {
        String csvData = "date,symbol,open,high,low,close,volume,adj_close\n" +
                         "2026-01-02,AAPL,183.50,185.20,182.10,184.75,52341000,184.75\n" +
                         "2026-01-02,MSFT,374.20,378.50,372.80,377.10,21543000,377.10\n";

        org.mockito.Mockito.when(companyRepository.findBySymbol(any()))
                .thenAnswer(invocation -> {
                    String sym = invocation.getArgument(0);
                    return Optional.of(new Company(sym, sym + " Corp", "Tech", "Software", "USA", "NASDAQ"));
                });

        org.mockito.Mockito.when(stockRepository.findByCompanyAndTradeDate(any(), any()))
                .thenReturn(Optional.empty());

        InputStream is = new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));
        UploadHistory uploadHistory = new UploadHistory();

        EtlResult result = stockEtlStrategy.process(is, uploadHistory);

        assertEquals(2, result.getTotalRows());
        assertEquals(2, result.getAcceptedRows());
        assertEquals(0, result.getRejectedRows());
    }

    @Test
    void testProcessInvalidRowRejection() throws Exception {
        String csvData = "date,symbol,open,high,low,close,volume\n" +
                         "2026-01-02,AAPL,183.50,180.00,182.10,184.75,52341000\n"; // Invalid: High < Low

        InputStream is = new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));
        UploadHistory uploadHistory = new UploadHistory();

        EtlResult result = stockEtlStrategy.process(is, uploadHistory);

        assertEquals(1, result.getTotalRows());
        assertEquals(0, result.getAcceptedRows());
        assertEquals(1, result.getRejectedRows());
        assertEquals(1, result.getValidationReport().size());
        assertTrue(result.getValidationReport().get(0).getReason().contains("High price cannot be lower than Low price"));
    }
}
