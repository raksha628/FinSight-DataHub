package com.finsight.datahub.entity;

/**
 * Status of an ETL ingestion job.
 */
public enum UploadStatus {
    PROCESSING,
    SUCCESS,
    FAILED,
    PARTIAL
}
