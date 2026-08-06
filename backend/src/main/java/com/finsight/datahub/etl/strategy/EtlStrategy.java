package com.finsight.datahub.etl.strategy;

import com.finsight.datahub.entity.AssetType;
import com.finsight.datahub.entity.UploadHistory;
import com.finsight.datahub.etl.EtlResult;

import java.io.InputStream;

public interface EtlStrategy {

    /**
     * Returns the asset type handled by this strategy.
     */
    AssetType getAssetType();

    /**
     * Process CSV input stream and save valid records to the database.
     *
     * @param inputStream CSV input stream
     * @param uploadHistory associated upload history audit entity
     * @return EtlResult summary of accepted/rejected records
     * @throws Exception if stream reading fails
     */
    EtlResult process(InputStream inputStream, UploadHistory uploadHistory) throws Exception;
}
