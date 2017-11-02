package javaclient;

import java.sql.SQLException;
import javaclient.Class.Command;
import javaclient.Control.UserControl;
import javaclient.DA.DBConnection;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

/**
 *
 * @author ahxin_000
 */
public class JavaClient implements MqttCallbackExtended {
    
    private MqttClient client;
    
    public static void main(String[] args) {
        Settings.parseSetting();
        new pingDB().tryToPingDB();
        new JavaClient().connectMqtt();
    }
    
    public void connectMqtt() {
        try {
            System.out.println(TimeStamp.get() + " [INFO] Connecting To MQTT...");
            client = new MqttClient(Settings.mqttServerUrl, MqttClient.generateClientId());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setKeepAliveInterval(10);
            connOpts.setCleanSession(true);
            connOpts.setAutomaticReconnect(true);
            client.setCallback(this);
            client.connect(connOpts);

            //when ctrl+c command received
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        System.out.println();
                        System.out.println(TimeStamp.get() + " [INFO] Closing Connections... Please Wait...");
                        DBConnection.getInstance().closeConnection();
                        client.disconnect();
                        client.close();
                        for (int i = 5; i >= 1; i--) {
                            try {
                                System.out.println(TimeStamp.get() + " [INFO] Exit in " + i + " Seconds");
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                            }
                        }
                    } catch (MqttException ex) {
                        System.out.println(TimeStamp.get() + " [ERROR] " + ex.getMessage());
                    } catch (SQLException ex) {
                        System.out.println(TimeStamp.get() + " [ERROR] " + ex.getMessage());
                    }
                }
            });
        } catch (MqttException e) {
            System.out.println(TimeStamp.get() + " [ERROR] " + e.getMessage());
        } catch (IllegalArgumentException ex) {
            System.out.println(TimeStamp.get() + " [ERROR] " + ex.getMessage());
        }
    }
    
    @Override
    public void connectionLost(Throwable thrwbl) {
        thrwbl.printStackTrace();
        System.out.println("Error : " + thrwbl.getMessage());
        System.out.println(TimeStamp.get() + " [WARNING] Connection to MQTT lost. Reconnecting...");
    }
    
    @Override
    public void connectComplete(boolean bln, String string) {
        try {
            //System.out.println(TimeStamp.get() + " [INFO] Reconnected to MQTT.");
            client.subscribe(Settings.mqttTopic);
            System.out.println(TimeStamp.get() + " [INFO] Subscribed To Topic [" + Settings.mqttTopic + "]");
            System.out.println(TimeStamp.get() + " [INFO] Connected. Ready To Received Payload.");
        } catch (MqttException ex) {
            System.out.println(TimeStamp.get() + " [ERROR] [SUBSCRIBE] " + ex.getMessage());
        }
    }
    
    @Override
    public void messageArrived(String string, MqttMessage mm) throws Exception {
        System.out.println();
        System.out.println();
        //received payload
        System.out.println(TimeStamp.get() + " [INFO] [RECEIVED] Payload Received. Finding Command...");
        
        JSONObject jsonMsg = new JSONObject(mm.toString());
        String cmdPayload = jsonMsg.getString(KeyData.KEY_CMD);
        Command cmd = Action.findCommandByPayload(cmdPayload);
        if (cmd.getName() != null) {
            //cmd found
            System.out.println(TimeStamp.get() + " [INFO] Command Found (" + cmd.getName() + "), Processing...");
            System.out.println(TimeStamp.get() + " [INFO] [Recipient] " + jsonMsg.getString(KeyData.KEY_RECIPIENT));
            //process command
            if (cmd.getName().equalsIgnoreCase("RAT_USER_LOGIN")) {
                System.out.println(TimeStamp.get() + "User login");
                String username = jsonMsg.getString(KeyData.KEY_USER_USERNAME);
                String password = jsonMsg.getString(KeyData.KEY_USER_PASSWORD);
                JSONObject obj = new UserControl().retrieveUserInfo(Action.hexToAscii(username), Action.hexToAscii(password));
                
                System.out.println(obj.toString());
                MqttMessage mTemp = new MqttMessage(sendLoginAck(jsonMsg, obj).getBytes());
                client.publish(Settings.mqttTopic, mTemp);
                
            } else if (cmd.getName().equalsIgnoreCase("RAT_USER_AUTHREQUEST")) {
                System.out.println(TimeStamp.get() + "Verify 2FA code");
                
                String username = jsonMsg.getString(KeyData.KEY_USER_USERNAME);
                String veriCode = jsonMsg.getString(KeyData.KEY_USER_VERICODE);
                System.out.println(Action.hexToAscii(username) + " : " + Action.hexToAscii(veriCode));
                
                JSONObject obj = new UserControl().compare2FACode(Action.hexToAscii(username), Action.hexToAscii(veriCode));
                
                System.out.println(obj.toString());
                MqttMessage mTemp = new MqttMessage(send2FACodeAck(jsonMsg, obj).getBytes());
                client.publish(Settings.mqttTopic, mTemp);
                
            } else if (cmd.getName().equalsIgnoreCase("RAT_USER_AUTHREQUEST_RETRY")) {
                System.out.println(TimeStamp.get() + "Resend 2FA code");
                
                String username = jsonMsg.getString(KeyData.KEY_USER_USERNAME);
                
                JSONObject obj = new UserControl().resend2FACode(Action.hexToAscii(username));
                
                System.out.println(obj.toString());
                MqttMessage mTemp = new MqttMessage(send2FACodeAck(jsonMsg, obj).getBytes());
                client.publish(Settings.mqttTopic, mTemp);
                
            } else if (cmd.getName().equalsIgnoreCase("RAT_USER_RETRIEVEPASS")) {
                System.out.println(TimeStamp.get() + "Retrieve password");
                
                String username = jsonMsg.getString(KeyData.KEY_USER_USERNAME);
                
                JSONObject obj = new UserControl().retrievePassword(Action.hexToAscii(username));
                
                System.out.println(obj.toString());
                MqttMessage mTemp = new MqttMessage(retrievePassAck(jsonMsg, obj).getBytes());
                client.publish(Settings.mqttTopic, mTemp);
            } else if (cmd.getName().equalsIgnoreCase("RAT_USER_CHGPASS")) {
                System.out.println(TimeStamp.get() + "Change password");
                
                String username = jsonMsg.getString(KeyData.KEY_USER_USERNAME);
                String oldPass = jsonMsg.getString(KeyData.KEY_USER_PASSWORD);
                String newPass = jsonMsg.getString(KeyData.KEY_USER_NEWPASS);
                
                JSONObject obj = new UserControl().changePassword(Action.hexToAscii(username), Action.hexToAscii(oldPass), Action.hexToAscii(newPass));
                System.out.println(obj.toString());
                MqttMessage mTemp = new MqttMessage(changePassAck(jsonMsg, obj).getBytes());
                client.publish(Settings.mqttTopic, mTemp);
            }
        } else {
            //cmd not found
            System.out.println(TimeStamp.get() + " [WARNING] Command Not found. Ignoring.");
        }
    }
    
    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt) {
        
    }
    
    private String sendLoginAck(JSONObject jsonMsg, JSONObject data) {
        //set general encoding
        Command cmd = Action.findCommandByName("RAT_USER_AUTHREQUEST_RESPONSE");
        if (!jsonMsg.getString(KeyData.KEY_RECIPIENT).equalsIgnoreCase("")) {
            cmd.setRecipient(jsonMsg.getString(KeyData.KEY_RECIPIENT));
        } else {
            cmd.setRecipient("");
        }
        JSONObject obj = Action.encodePublicCmd(cmd);
        
        obj.put(KeyData.KEY_DATA, data);
        return obj.toString();
    }
    
    private String send2FACodeAck(JSONObject jsonMsg, JSONObject data) {
        //set general encoding
        Command cmd = Action.findCommandByName("RAT_USER_AUTHREQUEST_ACK");
        if (!jsonMsg.getString(KeyData.KEY_RECIPIENT).equalsIgnoreCase("")) {
            cmd.setRecipient(jsonMsg.getString(KeyData.KEY_RECIPIENT));
        } else {
            cmd.setRecipient("");
        }
        JSONObject obj = Action.encodePublicCmd(cmd);
        
        obj.put(KeyData.KEY_DATA, data);
        return obj.toString();
    }
    
    private String retrievePassAck(JSONObject jsonMsg, JSONObject data) {
        //set general encoding
        Command cmd = Action.findCommandByName("RAT_USER_RETRIEVEPASS_VERIFICATION");
        if (!jsonMsg.getString(KeyData.KEY_RECIPIENT).equalsIgnoreCase("")) {
            cmd.setRecipient(jsonMsg.getString(KeyData.KEY_RECIPIENT));
        } else {
            cmd.setRecipient("");
        }
        JSONObject obj = Action.encodePublicCmd(cmd);
        
        obj.put(KeyData.KEY_DATA, data);
        return obj.toString();
    }
    
    private String changePassAck(JSONObject jsonMsg, JSONObject data) {
        //set general encoding
        Command cmd = Action.findCommandByName("RAT_USER_CHGPASS_ACK");
        if (!jsonMsg.getString(KeyData.KEY_RECIPIENT).equalsIgnoreCase("")) {
            cmd.setRecipient(jsonMsg.getString(KeyData.KEY_RECIPIENT));
        } else {
            cmd.setRecipient("");
        }
        JSONObject obj = Action.encodePublicCmd(cmd);
        
        obj.put(KeyData.KEY_DATA, data);
        return obj.toString();
    }
    
}
