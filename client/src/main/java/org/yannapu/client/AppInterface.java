package org.yannapu.client;

import java.util.Scanner;

public class AppInterface {

    private ProcessingEngine processingEngine;
    private boolean running = true;

    public AppInterface() {
        this.processingEngine = new ProcessingEngine();
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the File Retrieval Engine Client");

        while (running) {
            System.out.print("> ");
            String input = scanner.nextLine();
            handleCommand(input);
        }

        scanner.close();
    }

    private void handleCommand(String input) {
        if (input.startsWith("connect ")) {
            String[] parts = input.split(" ");
            if (parts.length == 2) {
                String endpoint = parts[1];
                if (!endpoint.startsWith("tcp://")) {
                    endpoint = "tcp://" + endpoint;
                }
                processingEngine.connect(endpoint);
            } else {
                System.out.println("Invalid command. Usage: connect <IP:Port>");
            }
        } else if (input.startsWith("index ")) {
            processingEngine.indexFiles(input.substring(6));
        } else if (input.startsWith("search ")) {
            processingEngine.searchFiles(input.substring(7));
        }
        else if (input.equals("list")) {
            processingEngine.listActiveConnections();
        } else if (input.equals("quit")) {
            processingEngine.disconnect();
            running = false;
        } else {
            System.out.println("Unknown command.");
        }
    }

}
