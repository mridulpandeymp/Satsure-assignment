package com.satsure.cache.component;

import com.satsure.cache.service.CacheService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CacheCleaner {
    private final CacheService cacheService;

    public CacheCleaner(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Scheduled(fixedRate = 10000) // every 10 seconds
    public void cleanup() {
        cacheService.cleanupExpiredEntries();
    }
}
