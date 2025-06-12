package com.satsure.cache.service;

import com.satsure.cache.model.CacheEntry;
import com.satsure.cache.model.CacheStats;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class CacheService {
	private final HashMap<String, CacheEntry<String>> map = new HashMap<>();
	private final int maxSize;
	private final long defaultTtl;
	private final CacheStats stats = new CacheStats();
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private CacheEntry<String> head, tail;

	public CacheService(@Value("${cache.max-size:1000}") int maxSize,
			@Value("${cache.default-ttl:300}") long defaultTtl) {
		this.maxSize = maxSize;
		this.defaultTtl = defaultTtl * 1000;
	}

	public void put(String key, String value, Long ttlSeconds) {
		lock.writeLock().lock();
		try {
			long ttl = ttlSeconds != null ? ttlSeconds * 1000 : defaultTtl;
			long expiry = System.currentTimeMillis() + ttl;

			if (map.containsKey(key)) {
				removeNode(map.get(key));
			}

			CacheEntry<String> newNode = new CacheEntry<>(key, value, expiry);
			addNodeToFront(newNode);
			map.put(key, newNode);

			if (map.size() > maxSize) {
				CacheEntry<String> lru = tail;
				if (lru != null) {
					map.remove(lru.getKey());
					removeNode(lru);
					stats.recordEviction();
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	public String get(String key) {
		lock.writeLock().lock();
		try {
			CacheEntry<String> node = map.get(key);
			if (node == null) {
				stats.recordMiss();
				return null;
			}
			if (System.currentTimeMillis() > node.getExpiryTime()) {
				map.remove(key);
				removeNode(node);
				stats.recordExpired();
				stats.recordMiss();
				return null;
			}
			moveToFront(node);
			stats.recordHit();
			return node.getValue();
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void delete(String key) {
		lock.writeLock().lock();
		try {
			CacheEntry<String> node = map.remove(key);
			if (node != null) {
				removeNode(node);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void clear() {
		lock.writeLock().lock();
		try {
			map.clear();
			head = tail = null;
		} finally {
			lock.writeLock().unlock();
		}
	}

	public String getStats() {
		return stats.snapshot(map.size());
	}

	public void cleanupExpiredEntries() {
		lock.writeLock().lock();
		try {
			map.entrySet().removeIf(e -> {
				boolean expired = System.currentTimeMillis() > e.getValue().getExpiryTime();
				if (expired) {
					removeNode(e.getValue());
					stats.recordExpired();
				}
				return expired;
			});
		} finally {
			lock.writeLock().unlock();
		}
	}

	private void addNodeToFront(CacheEntry<String> node) {
		node.setPrev(null);
		node.setNext(head);
		if (head != null)
			head.setPrev(node);
		head = node;
		if (tail == null)
			tail = node;
	}

	private void moveToFront(CacheEntry<String> node) {
		if (node == head)
			return;
		removeNode(node);
		addNodeToFront(node);
	}

	private void removeNode(CacheEntry<String> node) {
		if (node.getPrev() != null)
			node.getPrev().setNext(node.getNext());
		else
			head = node.getNext();

		if (node.getNext() != null)
			node.getNext().setPrev(node.getPrev());
		else
			tail = node.getPrev();
	}
}