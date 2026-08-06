package com.finsight.datahub.etl;

import com.finsight.datahub.entity.AssetType;
import com.finsight.datahub.etl.strategy.EtlStrategy;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class EtlStrategyRegistry {

    private final Map<AssetType, EtlStrategy> strategyMap = new EnumMap<>(AssetType.class);

    public EtlStrategyRegistry(List<EtlStrategy> strategies) {
        for (EtlStrategy strategy : strategies) {
            strategyMap.put(strategy.getAssetType(), strategy);
        }
    }

    public Optional<EtlStrategy> getStrategy(AssetType assetType) {
        return Optional.ofNullable(strategyMap.get(assetType));
    }
}
