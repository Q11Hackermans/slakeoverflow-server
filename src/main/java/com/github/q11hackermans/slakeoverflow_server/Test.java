package com.github.q11hackermans.slakeoverflow_server;

import org.json.JSONArray;
import org.json.JSONObject;

public class Test {
    public static void main(String[] args){
        int posX = (int) ((Math.random() * ((10 - 1) - 1)) + 1);
        int posY = (int) ((Math.random() * ((10 - 1) - 1)) + 1);

        System.out.println(posX + " " + posY);

        Thread stopwatch = new Thread(() -> {
            long time = 0;
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    if(time <= 60000) {
                        time++;
                        Thread.sleep(1);
                    } else {
                        Thread.currentThread().interrupt();
                        System.out.println("Max stopwatch size reached");
                    }
                } catch(Exception e) {
                    Thread.currentThread().interrupt();
                }
            }
            System.out.println("Time: " + time + "ms");
        });
        stopwatch.start();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("cmd", "playerdata");

        JSONArray fields = new JSONArray();

        for(int i = 0; i < 200; i++) {
            JSONArray fieldsx = new JSONArray();

            for(int i2 = 0; i2 < 200; i2++) {
                fieldsx.put(i2);
            }

            fields.put(fieldsx);
        }

        jsonObject.put("fields", fields);

        stopwatch.interrupt();

        System.out.println(jsonObject.toString());
    }
}
