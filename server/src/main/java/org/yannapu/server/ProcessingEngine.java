package org.yannapu.server;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ProcessingEngine {

    private ZContext context = new ZContext();
    private boolean running = true;
    private final Set<Thread> workerThreads = Collections.synchronizedSet(new HashSet<>());

    public void start() {
        ZMQ.Socket socket = context.createSocket(SocketType.REP);
        socket.bind("tcp://*:5555");
        System.out.println("Server started, listening on port 5555...");

        while (running) {
            byte[] requestBytes = socket.recv(0);
            String message = new String(requestBytes, ZMQ.CHARSET);


            if ("quit".equals(message)) {
                stop();
                break;
            }

            else if ("list".equals(message)) {
                System.out.println("Active connections: " + workerThreads.size());
                socket.send("Active connections: " + workerThreads.size());
            } else {
                Thread worker = new Thread(() -> {
                    new WorkerThread(message, socket, this).run();
                });
                workerThreads.add(worker);
                worker.start();
            }
        }
        socket.close();
    }

    public void removeWorker(Thread worker) {
        workerThreads.remove(worker);
    }

    public void stop() {
        running = false;
        workerThreads.forEach(Thread::interrupt);
        for (Thread worker : workerThreads) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        context.destroy();
        System.out.println("Server stopped.");
    }

    public static void main(String[] args) {
        new ProcessingEngine().start();
    }
}

