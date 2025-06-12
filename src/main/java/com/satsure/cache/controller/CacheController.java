package com.satsure.cache.controller;

import com.satsure.cache.service.CacheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cache")
public class CacheController {

	private final CacheService cacheService;

	public CacheController(CacheService cacheService) {
		this.cacheService = cacheService;
	}

	@PutMapping
	public ResponseEntity<String> put(@RequestParam String key, @RequestParam String value,
			@RequestParam(required = false) Long ttl) {
		cacheService.put(key, value, ttl);
		return ResponseEntity.ok("Cached successfully");
	}

	@GetMapping("/{key}")
	public ResponseEntity<String> get(@PathVariable String key) {
		String value = cacheService.get(key);
		return value != null ? ResponseEntity.ok(value) : ResponseEntity.notFound().build();
	}

	@DeleteMapping("/{key}")
	public ResponseEntity<String> delete(@PathVariable String key) {
		cacheService.delete(key);
		return ResponseEntity.ok("Deleted if existed");
	}

	@DeleteMapping
	public ResponseEntity<String> clear() {
		cacheService.clear();
		return ResponseEntity.ok("Cache cleared");
	}

	@GetMapping("/stats")
	public ResponseEntity<String> stats() {
		return ResponseEntity.ok(cacheService.getStats());
	}
}
