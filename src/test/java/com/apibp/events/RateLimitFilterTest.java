package com.apibp.events;

import com.apibp.events.ratelimit.RateLimitFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "app.rate-limit.enabled=true",
    "app.rate-limit.capacity=3",
    "app.rate-limit.refill-tokens=3",
    "app.rate-limit.refill-seconds=60"
})
@AutoConfigureMockMvc
class RateLimitFilterTest {

    @Autowired MockMvc mvc;
    @Autowired RateLimitFilter rateLimitFilter;

    @BeforeEach
    void resetBuckets() {
        rateLimitFilter.resetBuckets();
    }

    @Test
    void rateLimitHeaders_presentOnEveryResponse() throws Exception {
        mvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists("RateLimit-Limit"))
                .andExpect(header().exists("RateLimit-Remaining"))
                .andExpect(header().exists("RateLimit-Reset"));
    }

    @Test
    void returns429_afterCapacityExhausted() throws Exception {
        // capacity=3 in test yml — exhaust the bucket then expect 429
        for (int i = 0; i < 3; i++) {
            mvc.perform(get("/health")).andExpect(status().isOk());
        }
        mvc.perform(get("/health"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("RateLimit-Remaining", "0"))
                .andExpect(header().exists("Retry-After"));
    }
}
