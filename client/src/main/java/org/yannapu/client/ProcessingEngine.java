package org.yannapu.client;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;
import org.zeromq.ZContext;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

public class ProcessingEngine {

    private ZContext context;
    private ZMQ.Socket socket;

    public ProcessingEngine() {
        this.context = new ZContext();
    }

    public void connect(String endpoint) {
        socket = context.createSocket(SocketType.REQ);
        socket.connect(endpoint);
        System.out.println("Connected to server at " + endpoint);
    }

    public void indexFiles(String directoryPath) {
        try {
            long startTime = System.currentTimeMillis();
            Files.walkFileTree(Paths.get(directoryPath), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (!attrs.isDirectory() && file.toString().endsWith(".txt")) {
                        try {
                            String content = new String(Files.readAllBytes(file));
                            Map<String, Integer> wordCounts = Arrays.stream(content.split("\\W+"))
                                    .filter(word -> !word.isEmpty())
                                    .map(String::toLowerCase)
                                    .collect(Collectors.toMap(word -> word, word -> 1, Integer::sum));

                            StringBuilder messageBuilder = new StringBuilder("index|");
                            messageBuilder.append(file.toString());
                            wordCounts.forEach((word, count) ->
                                    messageBuilder.append("|").append(word).append(",").append(count));


                            socket.send(messageBuilder.toString());
                            String response = socket.recvStr();
                            System.out.println("Server response: \n" + response);
                            long endTime = System.currentTimeMillis();
                            System.out.println("Indexing completed in " + (endTime - startTime) + " ms");
                        } catch (IOException e) {
                            System.err.println("Failed to read file: " + file);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            System.err.println("Error walking the file tree: " + directoryPath);
        }
    }

    public void searchFiles(String query) {
        String searchMessage = "search|" + query;
        long startTime = System.currentTimeMillis();
        socket.send(searchMessage);
        String response = socket.recvStr();
        System.out.println("Search Results: " + response);
        long endTime = System.currentTimeMillis();
        System.out.println("Search completed in " + (endTime - startTime) + " ms");
    }

    public void listActiveConnections() {
        try {
            socket.send("list");
            String response = socket.recvStr();
            System.out.println("Active connections: \n" + response);
        } catch (ZMQException e) {
            System.out.println("Failed to list active connections. Error: " + e.getMessage());
        }
    }


    public void disconnect() {
        if (socket != null) {
            socket.close();
        }
        context.close();
        System.out.println("Disconnected from server.");
    }
}
