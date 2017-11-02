package com.example.rongfu.tarucplaystore.Configuration;

import android.content.Context;

import com.example.rongfu.tarucplaystore.KeyData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;



/**
 * @author lamkailoon Function : To read settings.json file
 */
public class Settings {

    public static String mqttServerUrl;
    public static String mqttTopic;

    public static String dbServerUrl;
    public static String dbUser;
    public static String dbPassword;
    public static String dbName;

    public static void parseSetting(Context context) {
        JSONObject obj = null;
        try {
            obj = new JSONObject(loadJSONFromAsset(context));
            mqttServerUrl = obj.getString(KeyData.KEY_MQTT_SERVER_URL);
            mqttTopic = obj.getString(KeyData.KEY_MQTT_TOPIC);
            dbServerUrl = obj.getString(KeyData.KEY_DB_SERVER_URL);
            dbUser = obj.getString(KeyData.KEY_DB_USER);
            dbPassword = obj.getString(KeyData.KEY_DB_PASSWORD);
            dbName = obj.getString(KeyData.KEY_DB_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("settings.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }
}
