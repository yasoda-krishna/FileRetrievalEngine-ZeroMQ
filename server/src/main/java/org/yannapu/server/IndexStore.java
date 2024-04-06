package org.yannapu.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IndexStore {
    private final Map<String, Map<String, Integer>> index = new ConcurrentHashMap<>();

    public synchronized void updateIndex(String document, Map<String, Integer> wordCounts) {
        wordCounts.forEach((word, count) -> index.computeIfAbsent(word, k -> new ConcurrentHashMap<>()).merge(document, count, Integer::sum));
    }

    public synchronized Map<String, Integer> search(String word) {
        return index.getOrDefault(word, new ConcurrentHashMap<>());
    }
}