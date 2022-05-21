package com.github.q11hackermans.slakeoverflow_server;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) throws IOException {
        /*
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

         */

        /*
        String text = "{\"cmd\":\"playerdata\",\"fovx\":50,\"fovy\":50,\"fields\":[[0,58,325,502],[0,62,335,502],[0,83,350,101],[0,83,351,105],[0,83,352,105],[0,75,371,502]]}";
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);

        for(byte b : bytes) {
            System.out.println(Byte.toUnsignedInt(b));
        }

         */

        String text = "0\n" +
                "127\n" +
                "123\n" +
                "34\n" +
                "99\n" +
                "109\n" +
                "100\n" +
                "34\n" +
                "58\n" +
                "34\n" +
                "112\n" +
                "108\n" +
                "97\n" +
                "121\n" +
                "101\n" +
                "114\n" +
                "100\n" +
                "97\n" +
                "116\n" +
                "97\n" +
                "34\n" +
                "44\n" +
                "34\n" +
                "102\n" +
                "111\n" +
                "118\n" +
                "120\n" +
                "34\n" +
                "58\n" +
                "53\n" +
                "48\n" +
                "44\n" +
                "34\n" +
                "102\n" +
                "111\n" +
                "118\n" +
                "121\n" +
                "34\n" +
                "58\n" +
                "53\n" +
                "48\n" +
                "44\n" +
                "34\n" +
                "102\n" +
                "105\n" +
                "101\n" +
                "108\n" +
                "100\n" +
                "115\n" +
                "34\n" +
                "58\n" +
                "91\n" +
                "91\n" +
                "48\n" +
                "44\n" +
                "55\n" +
                "48\n" +
                "55\n" +
                "44\n" +
                "56\n" +
                "49\n" +
                "44\n" +
                "53\n" +
                "48\n" +
                "50\n" +
                "93\n" +
                "44\n" +
                "91\n" +
                "48\n" +
                "44\n" +
                "55\n" +
                "48\n" +
                "52\n" +
                "44\n" +
                "57\n" +
                "54\n" +
                "44\n" +
                "49\n" +
                "48\n" +
                "49\n" +
                "93\n" +
                "44\n" +
                "91\n" +
                "48\n" +
                "44\n" +
                "55\n" +
                "48\n" +
                "52\n" +
                "44\n" +
                "57\n" +
                "55\n" +
                "44\n" +
                "49\n" +
                "48\n" +
                "53\n" +
                "93\n" +
                "44\n" +
                "91\n" +
                "48\n" +
                "44\n" +
                "55\n" +
                "48\n" +
                "52\n" +
                "44\n" +
                "57\n" +
                "56\n" +
                "44\n" +
                "49\n" +
                "48\n" +
                "53\n" +
                "93\n" +
                "44\n" +
                "91\n" +
                "48\n" +
                "44\n" +
                "55\n" +
                "48\n" +
                "53\n" +
                "44\n" +
                "49\n" +
                "48\n" +
                "49\n" +
                "44\n" +
                "53\n" +
                "48\n" +
                "50\n" +
                "93\n" +
                "93\n" +
                "125\n" +
                "0\n" +
                "36\n" +
                "123\n" +
                "34\n" +
                "97\n" +
                "117\n" +
                "116\n" +
                "104\n" +
                "34\n" +
                "58\n" +
                "49\n" +
                "44\n" +
                "34\n" +
                "99\n" +
                "109\n" +
                "100\n" +
                "34\n" +
                "58\n" +
                "34\n" +
                "115\n" +
                "116\n" +
                "97\n" +
                "116\n" +
                "117\n" +
                "115\n" +
                "34\n" +
                "44\n" +
                "34\n" +
                "115\n" +
                "116\n" +
                "97\n" +
                "116\n" +
                "117\n" +
                "115\n" +
                "34\n" +
                "58\n" +
                "50\n" +
                "125\n" +
                "0\n";

        Socket socket = new Socket();
        socket.getOutputStream().write(-114);

        text = text.replace("\n", " ");

        String[] bytes = text.split(" ");

        List<Character> characters = new ArrayList<>();
        for(String byteString : bytes) {
            characters.add((char) Integer.parseInt(byteString));
        }

        System.out.println();
        for(char c : characters) {
            System.out.print(c);
        }
    }
}
