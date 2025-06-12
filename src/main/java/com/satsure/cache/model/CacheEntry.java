package com.satsure.cache.model;

public class CacheEntry<V> {
	private final V value;
	private final long expiryTime;
	private CacheEntry<V> prev;
	private CacheEntry<V> next;
	private final String key;

	public CacheEntry(String key, V value, long expiryTime) {
		this.key = key;
		this.value = value;
		this.expiryTime = expiryTime;
	}

	public V getValue() {
		return value;
	}

	public long getExpiryTime() {
		return expiryTime;
	}

	public CacheEntry<V> getPrev() {
		return prev;
	}

	public void setPrev(CacheEntry<V> prev) {
		this.prev = prev;
	}

	public CacheEntry<V> getNext() {
		return next;
	}

	public void setNext(CacheEntry<V> next) {
		this.next = next;
	}

	public String getKey() {
		return key;
	}
}