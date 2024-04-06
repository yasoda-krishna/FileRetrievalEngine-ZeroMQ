package org.yannapu.server;

import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class WorkerThread implements Runnable {

    private final String message;
    private final ZMQ.Socket socket;
    private final ProcessingEngine engine;

    private static final IndexStore indexStore = new IndexStore();

    public WorkerThread(String message, ZMQ.Socket socket, ProcessingEngine engine) {
        this.message = message;
        this.socket = socket;
        this.engine = engine;
    }

    @Override
    public void run() {
        try {
            if (message.startsWith("index|")) {

                indexMessage();
            } else if (message.startsWith("search|")) {

                searchMessage();
            }
        } finally {
            engine.removeWorker(Thread.currentThread());
        }
    }

    private void indexMessage() {
        String[] parts = message.split("\\|");
        Map<String, Integer> wordCounts = new HashMap<>();
        for (int i = 2; i < parts.length; i++) {
            String[] wordCount = parts[i].split(",");
            wordCounts.put(wordCount[0], Integer.parseInt(wordCount[1]));
        }
        indexStore.updateIndex(parts[1], wordCounts);
        socket.send("Indexing completed for: " + parts[1]);
    }

    private void searchMessage() {
        String query = message.substring("search|".length());
        String[] keywords = query.split(" and ");

        Set<String> intersection = new HashSet<>(indexStore.search(keywords[0]).keySet());

        for (int i = 1; i < keywords.length; i++) {
            Set<String> results = indexStore.search(keywords[i]).keySet();
            intersection.retainAll(results);
        }

        if (intersection.isEmpty()) {
            socket.send("No matches found");
            return;
        }

        Map<String, Integer> finalResults = new HashMap<>();
        intersection.forEach(doc -> finalResults.put(doc, indexStore.search(doc).values().stream().mapToInt(Integer::intValue).sum()));
        List<Map.Entry<String, Integer>> sortedResults = finalResults.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());

        StringBuilder response = new StringBuilder();
        response.append("\n");
        sortedResults.forEach(entry -> response.append(entry.getKey()).append("\n"));

        socket.send(response.toString());
    }
}