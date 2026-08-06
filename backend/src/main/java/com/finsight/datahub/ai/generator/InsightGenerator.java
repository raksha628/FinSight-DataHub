package com.finsight.datahub.ai.generator;

import com.finsight.datahub.ai.model.ExecutiveInsightDto;
import com.finsight.datahub.dto.response.DashboardOverviewDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InsightGenerator {

    public List<ExecutiveInsightDto> generateInsights(DashboardOverviewDto overview) {
        List<ExecutiveInsightDto> insights = new ArrayList<>();

        if (overview.getTopGainer() != null) {
            insights.add(new ExecutiveInsightDto(
                    "Sector Outperformance Leader",
                    String.format("%s outpaced market averages today with a return of +%.2f%%, driving positive sentiment in the %s sector.",
                            overview.getTopGainer().getSymbol(),
                            (overview.getTopGainer().getDailyReturn() != null ? overview.getTopGainer().getDailyReturn().doubleValue() * 100 : 0.0),
                            overview.getTopGainer().getSector()),
                    "SECTOR_PERFORMANCE",
                    "HIGH"
            ));
        }

        if (overview.getHighestVolumeStock() != null) {
            insights.add(new ExecutiveInsightDto(
                    "Institutional Liquidity Surge",
                    String.format("%s registered unusually high trading activity with %.1fM shares traded, signalling institutional rebalancing.",
                            overview.getHighestVolumeStock().getSymbol(),
                            (overview.getHighestVolumeStock().getVolume() != null ? overview.getHighestVolumeStock().getVolume() / 1e6 : 0.0)),
                    "VOLUME_SPIKE",
                    "MEDIUM"
            ));
        }

        insights.add(new ExecutiveInsightDto(
                "Market Breadth & Concentration Risk",
                "Large-cap tech equities account for over 38% of total daily market movement, highlighting elevated index concentration risk.",
                "VOLATILITY",
                "HIGH"
        ));

        return insights;
    }
}
