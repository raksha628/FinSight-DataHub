package com.finsight.datahub.service.impl;

import com.finsight.datahub.dto.response.DashboardOverviewDto;
import com.finsight.datahub.dto.response.StockPerformanceDto;
import com.finsight.datahub.repository.CompanyRepository;
import com.finsight.datahub.repository.StockRepository;
import com.finsight.datahub.service.AnalyticsService;
import com.finsight.datahub.service.DashboardService;
import com.finsight.datahub.service.UploadService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final StockRepository stockRepository;
    private final CompanyRepository companyRepository;
    private final AnalyticsService analyticsService;
    private final UploadService uploadService;

    public DashboardServiceImpl(StockRepository stockRepository,
                                CompanyRepository companyRepository,
                                AnalyticsService analyticsService,
                                UploadService uploadService) {
        this.stockRepository = stockRepository;
        this.companyRepository = companyRepository;
        this.analyticsService = analyticsService;
        this.uploadService = uploadService;
    }

    @Override
    public DashboardOverviewDto getDashboardOverview() {
        DashboardOverviewDto dto = new DashboardOverviewDto();

        dto.setTotalStocks(stockRepository.count());
        dto.setTotalCompanies(companyRepository.count());

        BigDecimal avgPrice = stockRepository.findAverageMarketPrice();
        dto.setAvgMarketPrice(avgPrice != null ? avgPrice.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        PageRequest singleRow = PageRequest.of(0, 1);

        StockPerformanceDto topGainer = analyticsService.getTopGainers(null, null, null, singleRow)
                .getContent().stream().findFirst().orElse(null);
        StockPerformanceDto topLoser = analyticsService.getTopLosers(null, null, null, singleRow)
                .getContent().stream().findFirst().orElse(null);
        StockPerformanceDto highestVolume = analyticsService.getHighestVolume(null, null, null, singleRow)
                .getContent().stream().findFirst().orElse(null);

        dto.setTopGainer(topGainer);
        dto.setTopLoser(topLoser);
        dto.setHighestVolumeStock(highestVolume);
        dto.setSectorDistribution(analyticsService.getAveragePriceBySector(null));
        dto.setRecentUploads(uploadService.getUploadHistory().stream().limit(5).collect(Collectors.toList()));

        return dto;
    }
}
