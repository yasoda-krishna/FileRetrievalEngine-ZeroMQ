package org.yannapu.server;

public class ServerMain {
    public static void main(String[] args) {
        ProcessingEngine processingEngine = new ProcessingEngine();
        processingEngine.start();
        Runtime.getRuntime().addShutdownHook(new Thread(processingEngine::stop));
    }
}
