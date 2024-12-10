package com.example.application.repository;

import com.example.application.data.DataRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DataRecordRepository extends JpaRepository<DataRecord, Long> {

    List<DataRecord> findByRatingCount(int ratingCount);
}
