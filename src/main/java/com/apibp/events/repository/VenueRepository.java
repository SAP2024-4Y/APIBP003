package com.apibp.events.repository;

import com.apibp.events.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VenueRepository extends JpaRepository<Venue, Long> {

    // radius in metres — geography type uses metres natively
    @Query(value = """
        SELECT v.* FROM venue v
        WHERE ST_DWithin(v.location,
              ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
              :radiusMetres)
        """, nativeQuery = true)
    List<Venue> findNearby(@Param("lat") double lat,
                           @Param("lon") double lon,
                           @Param("radiusMetres") double radiusMetres);

    @Query(value = """
        SELECT v.* FROM venue v
        WHERE ST_Within(v.location::geometry,
              ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326))
        """, nativeQuery = true)
    List<Venue> findWithin(@Param("minLat") double minLat,
                           @Param("minLon") double minLon,
                           @Param("maxLat") double maxLat,
                           @Param("maxLon") double maxLon);
}
