/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaclient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONObject;

public class Settings {

    public static String mqttServerUrl;
    public static String mqttTopic;
    
    public static String dbServerUrl;
    public static String dbUser;
    public static String dbPassword;
    public static String dbName;

    public static void parseSetting() {
        try {
            System.out.println(TimeStamp.get() + " [INFO] Reading Settings...");
            String text = new String(Files.readAllBytes(Paths.get("settings.json")), StandardCharsets.UTF_8);
            JSONObject obj = new JSONObject(text);
            mqttServerUrl = obj.getString(KeyData.KEY_MQTT_SERVER_URL);
            mqttTopic = obj.getString(KeyData.KEY_MQTT_TOPIC);
            dbServerUrl = obj.getString(KeyData.KEY_DB_SERVER_URL);
            dbUser = obj.getString(KeyData.KEY_DB_USER);
            dbPassword = obj.getString(KeyData.KEY_DB_PASSWORD);
            dbName = obj.getString(KeyData.KEY_DB_NAME);
        } catch (IOException ex) {
            System.out.println(TimeStamp.get() + " [ERROR] " + ex.getMessage() + " not Found! Check Path.");
            //exit
            for (int i = 5; i >= 1; i--) {
                try {
                    System.out.println(TimeStamp.get() + " [ERROR] Exit in " + i + " Seconds");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
            System.exit(0);
        }
    }
}
