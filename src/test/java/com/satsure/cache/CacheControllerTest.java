package com.satsure.cache;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static java.lang.Thread.sleep;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CacheControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testPutAndGetCacheEntry() throws Exception {
        mockMvc.perform(put("/cache")
                        .param("key", "test-key")
                        .param("value", "test-value"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cached successfully")));

        mockMvc.perform(get("/cache/test-key"))
                .andExpect(status().isOk())
                .andExpect(content().string("test-value"));
    }

    @Test
    public void testDeleteCacheKey() throws Exception {
        mockMvc.perform(put("/cache")
                        .param("key", "to-delete")
                        .param("value", "some-data"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/cache/to-delete"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Deleted if existed")));

        mockMvc.perform(get("/cache/to-delete"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testClearCache() throws Exception {
        mockMvc.perform(put("/cache")
                        .param("key", "clear-key")
                        .param("value", "clear-value"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/cache"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cache cleared")));

        mockMvc.perform(get("/cache/clear-key"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testStatsEndpoint() throws Exception {
        mockMvc.perform(get("/cache/stats"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("hits")))
                .andExpect(content().string(containsString("misses")));
    }

    @Test
    public void testTTLExpiration() throws Exception {
        mockMvc.perform(put("/cache")
                        .param("key", "ttl-key")
                        .param("value", "short-lived")
                        .param("ttl", "2"))
                .andExpect(status().isOk());

        sleep(3000); // wait for TTL to expire

        mockMvc.perform(get("/cache/ttl-key"))
                .andExpect(status().isNotFound());
    }
}

