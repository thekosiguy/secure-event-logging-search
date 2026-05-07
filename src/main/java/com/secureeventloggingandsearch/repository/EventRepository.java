package com.secureeventloggingandsearch.repository;

import com.secureeventloggingandsearch.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    @Query(value = "SELECT * FROM events e WHERE " +
           "(:type IS NULL OR e.type = :type) AND " +
           "(CAST(:fromTs AS TIMESTAMP) IS NULL OR e.timestamp >= CAST(:fromTs AS TIMESTAMP)) AND " +
           "(CAST(:toTs AS TIMESTAMP) IS NULL OR e.timestamp <= CAST(:toTs AS TIMESTAMP))",
           countQuery = "SELECT COUNT(*) FROM events e WHERE " +
           "(:type IS NULL OR e.type = :type) AND " +
           "(CAST(:fromTs AS TIMESTAMP) IS NULL OR e.timestamp >= CAST(:fromTs AS TIMESTAMP)) AND " +
           "(CAST(:toTs AS TIMESTAMP) IS NULL OR e.timestamp <= CAST(:toTs AS TIMESTAMP))",
           nativeQuery = true)
    Page<Event> findByFilters(
            @Param("type") String type,
            @Param("fromTs") Instant from,
            @Param("toTs") Instant to,
            Pageable pageable);
}
