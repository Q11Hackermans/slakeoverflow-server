package com.github.q11hackermans.slakeoverflow_server.console;

import com.github.q11hackermans.slakeoverflow_server.SlakeoverflowServer;

import java.util.Scanner;

public class ServerConsole implements Runnable {
    private Thread thread;
    private final ConsoleLogger logger;

    public ServerConsole() {
        this.logger = SlakeoverflowServer.getServer().getLogger();
    }

    public ServerConsole(ConsoleLogger logger) {
        this.logger = logger;
    }

    /**
     * Start the console
     */
    public void start() {
        if(this.thread != null) {
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
        int stopcount = 60;
        while(this.thread.isAlive()) {
            if(stopcount > 1) {
                stopcount--;
                try {
                    Thread.sleep(1000);
                } catch(Exception ignored) {}
            } else {
                this.thread.stop();
            }
        }
    }

    /**
     * Returns true if the console is running.
     * @return boolean (true = console is running)
     */
    public boolean isRunning() {
        if(this.thread != null) {
            return this.thread.isAlive();
        } else {
            return false;
        }
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted() && thread != null) {
            Scanner scanner = new Scanner(System.in);
            this.logger.info("CONSOLE", ConsoleCommands.run(scanner.nextLine().split(" ")));
        }
    }
}
