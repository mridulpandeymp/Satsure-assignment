package com.satsure.cache.model;

public class CacheStats {
	public long hits;
	public long misses;
	public long totalRequests;
	public long evictions;
	public long expiredRemovals;

	public synchronized void recordHit() {
		hits++;
		totalRequests++;
	}

	public synchronized void recordMiss() {
		misses++;
		totalRequests++;
	}

	public synchronized void recordEviction() {
		evictions++;
	}

	public synchronized void recordExpired() {
		expiredRemovals++;
	}

	public synchronized String snapshot(long currentSize) {
		double hitRate = totalRequests == 0 ? 0.0 : (double) hits / totalRequests;
		return String.format(
				"{ \"hits\": %d, \"misses\": %d, \"hit_rate\": %.3f, \"total_requests\": %d, \"current_size\": %d, \"evictions\": %d, \"expired_removals\": %d }",
				hits, misses, hitRate, totalRequests, currentSize, evictions, expiredRemovals);
	}
}