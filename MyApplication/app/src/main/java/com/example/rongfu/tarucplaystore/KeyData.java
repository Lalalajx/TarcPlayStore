package com.example.rongfu.tarucplaystore;

/**
 * Created by Rongfu on 31-Oct-17.
 */

public class KeyData {
    //setting command key
    public static String KEY_MQTT_SERVER_URL = "MQTTServerUrl";
    public static String KEY_MQTT_TOPIC = "MQTTTopic";
    public static String KEY_DB_SERVER_URL = "DatabaseServerUrl";
    public static String KEY_DB_USER = "DatabaseUser";
    public static String KEY_DB_PASSWORD = "DatabasePassword";
    public static String KEY_DB_NAME = "DatabaseName";

    //global command key
    public static String KEY_CMD = "command";
    public static String KEY_RESERVE = "reserve";
    public static String KEY_RECIPIENT = "recipient";
    public static String KEY_DATA = "data";

    //data key
    //For User Table
    public static String KEY_USER_OBJECT = "user";
    public static String KEY_USER_ID = "userId";
    public static String KEY_USER_USERNAME = "username";
    public static String KEY_USER_PASSWORD = "password";
    public static String KEY_USER_EMAIL = "email";
    public static String KEY_USER_EMAILVERIFICATIONSTATUS = "emailVerificationStatus";
    public static String KEY_USER_FORGETPASSSTATUS = "forgetPassStatus";
    public static String KEY_USER_VERICODE = "code";
    public static String KEY_USER_NEWPASSCODE = "newPassCode";
    public static String KEY_USER_NEWPASS = "newPass";
    public static String KEY_USER_VERITYPE = "veriType";

    //For UserLog Table
    public static String KEY_USERLOG_OBJECT = "userLog";
    public static String KEY_USERLOG_ID = "userLogId";
    public static String KEY_USERLOG_USERNAME = "username";
    public static String KEY_USERLOG_DEVICEID = "deviceId";
    public static String KEY_USERLOG_LASTLOGINDATETIME = "lastLoginDateTime";

}
