package com.github.q11hackermans.slakeoverflow_server.console;

import com.github.q11hackermans.slakeoverflow_server.SlakeoverflowServer;

import java.util.Scanner;

public class ServerConsole implements Runnable {

    private final SlakeoverflowServer server;
    private Thread thread;
    private final ConsoleLogger logger;

    public ServerConsole(SlakeoverflowServer server) {
        this(server, server.getLogger());
    }

    public ServerConsole(SlakeoverflowServer server, ConsoleLogger logger) {
        this.server = server;
        this.logger = logger;
    }

    /**
     * Start the console
     */
    public void start() {
        if (this.thread != null) {
            this.thread.stop();
            this.thread = null;
        }
        this.thread = new Thread(this);
        this.thread.setName("SLAKEOVERFLOW-CONSOLE-" + this.toString());
        this.thread.start();
    }

    /**
     * Stop the console (and kill it if the thread does not stop after 60 seconds)
     */
    public void stop() {
        this.thread.interrupt();
        new Thread(() -> {
            int stopcount = 10;
            while (this.thread.isAlive()) {
                if (stopcount > 1) {
                    stopcount--;
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ignored) {
                    }
                } else {
                    this.thread.stop();
                }
            }
        }).start();
    }

    /**
     * Returns true if the console is running.
     *
     * @return boolean (true = console is running)
     */
    public boolean isRunning() {
        if (this.thread != null) {
            return this.thread.isAlive();
        } else {
            return false;
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted() && thread != null) {
            Scanner scanner = new Scanner(System.in);
            this.logger.info("CONSOLE", ConsoleCommands.run(this.getServer(), scanner.nextLine().split(" ")));
        }
    }

    public SlakeoverflowServer getServer() {
        return this.server;
    }
}
