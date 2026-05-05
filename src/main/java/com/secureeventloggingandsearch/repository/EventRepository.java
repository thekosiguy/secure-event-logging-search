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

    @Query("SELECT e FROM Event e WHERE " +
           "(:type IS NULL OR e.type = :type) AND " +
           "(:from IS NULL OR e.timestamp >= :from) AND " +
           "(:to IS NULL OR e.timestamp <= :to)")
    Page<Event> findByFilters(
            @Param("type") String type,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable);
}
