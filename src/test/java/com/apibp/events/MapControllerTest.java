package com.apibp.events;

import com.apibp.events.dto.GeoJsonFeature;
import com.apibp.events.dto.GeoJsonFeatureCollection;
import com.apibp.events.service.MapService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MapControllerTest {

    @Autowired MockMvc mvc;
    @MockBean  MapService mapService;

    private static final GeoJsonFeatureCollection ONE_VENUE =
            GeoJsonFeatureCollection.of(List.of(
                    GeoJsonFeature.of(1L, "Test Venue", "Test Address", 77.20, 28.61,
                            List.of(new GeoJsonFeature.EventSummary(1L, "Rock Fest", "MUSIC", "ON_SALE")))
            ));

    private static final GeoJsonFeatureCollection EMPTY =
            GeoJsonFeatureCollection.of(List.of());

    @Test
    void nearby_returnsFeatureCollection() throws Exception {
        when(mapService.nearby(28.61, 77.20, 5.0)).thenReturn(ONE_VENUE);

        mvc.perform(get("/api/v1/events/nearby")
                .param("latitude",  "28.61")
                .param("longitude", "77.20")
                .param("radius_km", "5.0"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.type").value("FeatureCollection"))
           .andExpect(jsonPath("$.features[0].type").value("Feature"))
           .andExpect(jsonPath("$.features[0].geometry.type").value("Point"))
           .andExpect(jsonPath("$.features[0].geometry.coordinates[0]").value(77.20))
           .andExpect(jsonPath("$.features[0].geometry.coordinates[1]").value(28.61))
           .andExpect(jsonPath("$.features[0].properties.name").value("Test Venue"))
           .andExpect(jsonPath("$.features[0].properties.events[0].title").value("Rock Fest"));
    }

    @Test
    void nearby_emptyResult_returnsEmptyFeatures() throws Exception {
        when(mapService.nearby(anyDouble(), anyDouble(), anyDouble())).thenReturn(EMPTY);

        mvc.perform(get("/api/v1/events/nearby")
                .param("latitude",  "0.0")
                .param("longitude", "0.0")
                .param("radius_km", "1.0"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.features").isEmpty());
    }

    @Test
    void within_returnsFeatureCollection() throws Exception {
        when(mapService.within(28.5, 77.1, 28.7, 77.3)).thenReturn(ONE_VENUE);

        mvc.perform(get("/api/v1/events/within")
                .param("min_lat", "28.5")
                .param("min_lon", "77.1")
                .param("max_lat", "28.7")
                .param("max_lon", "77.3"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.type").value("FeatureCollection"))
           .andExpect(jsonPath("$.features[0].properties.id").value(1));
    }

    @Test
    void nearby_missingParam_returns400() throws Exception {
        mvc.perform(get("/api/v1/events/nearby")
                .param("latitude", "28.61"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void within_missingParam_returns400() throws Exception {
        mvc.perform(get("/api/v1/events/within")
                .param("min_lat", "28.5"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
