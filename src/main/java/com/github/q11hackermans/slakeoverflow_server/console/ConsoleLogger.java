package com.github.q11hackermans.slakeoverflow_server.console;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConsoleLogger {
    private JSONArray log;

    public ConsoleLogger() {
        this.log = new JSONArray();
    }

    // LOGGING
    /**
     * Log info message and print it to console.
     * @param module Module that creates the log (e.g. console)
     * @param text Logging text
     */
    public void info(String module, String text) {
        this.createLogEntry("INFO", module, text, true);
    }

    /**
     * Log warning and print it to console.
     * @param module Module that creates the log (e.g. console)
     * @param text Logging text
     */
    public void warning(String module, String text) {
        this.createLogEntry("WARNING", module, text, true);
    }

    /**
     * Log debug message and print it NOT to console.
     * @param module Module that creates the log (e.g. console)
     * @param text Logging text
     */
    public void debug(String module, String text) {
        this.createLogEntry("DEBUG", module, text, false);
    }

    // GET AND SAVE LOG
    /**
     * Get the log as JSONArray
     * @return Log (as JSONArray)
     */
    public JSONArray getLog() {
        return new JSONArray(this.log);
    }

    /**
     * Save the log as a json (text) file
     * @param file The file the log should be written in
     * @throws IOException IOException
     */
    public void saveLog(File file, boolean overwriteExistingFile) throws IOException {
        if(!file.exists()) {
            file.createNewFile();
        } else if(!overwriteExistingFile) {
            return;
        }
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(this.log.toString());
        fileWriter.flush();
        fileWriter.close();
    }

    // PRIVATE
    private void createLogEntry(String type, String module, String text, boolean print) {
        JSONObject logEntry = new JSONObject();
        logEntry.put("time", this.getTimeString());
        logEntry.put("type", type);
        logEntry.put("module", module);
        logEntry.put("text", text);
        this.log.put(logEntry);

        if(print) {
            System.out.println("[" + logEntry.getString("time") + "] [" + logEntry.getString("type") + "] [" + logEntry.getString("module") + "] " + logEntry.getString("text"));
        }
    }

    private String getTimeString() {
        LocalDateTime time = LocalDateTime.now();
        return time.format(DateTimeFormatter.ISO_DATE_TIME);
    }
}