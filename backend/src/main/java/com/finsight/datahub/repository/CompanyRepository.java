package com.finsight.datahub.repository;

import com.finsight.datahub.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findBySymbol(String symbol);
    boolean existsBySymbol(String symbol);
}
