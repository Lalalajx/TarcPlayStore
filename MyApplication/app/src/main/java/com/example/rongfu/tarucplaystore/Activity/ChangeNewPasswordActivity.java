package com.example.rongfu.tarucplaystore.Activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.rongfu.tarucplaystore.Action;
import com.example.rongfu.tarucplaystore.Class.Command;
import com.example.rongfu.tarucplaystore.Class.User;
import com.example.rongfu.tarucplaystore.KeyData;
import com.example.rongfu.tarucplaystore.LoggedInUser;
import com.example.rongfu.tarucplaystore.MQTT.MqttHelper;
import com.example.rongfu.tarucplaystore.R;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import static android.R.attr.elegantTextHeight;
import static android.R.attr.password;

public class ChangeNewPasswordActivity extends AppCompatActivity {

    private final String TAG = ChangeNewPasswordActivity.class.getSimpleName();
    private ProgressDialog loading;
    private MqttHelper mqttHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_new_password);

        final EditText oldPass = (EditText) findViewById(R.id.chgpass_password);
        final EditText newPass = (EditText) findViewById(R.id.chgpass_newpass);
        final EditText newPass2 = (EditText) findViewById(R.id.chgpass_newpass2);
        Button btnConfirm = (Button) findViewById(R.id.btn_confirm);

        final String username = getIntent().getStringExtra(KeyData.KEY_USER_USERNAME);

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chgPass(username, oldPass.getText().toString(), newPass.getText().toString(), newPass2.getText().toString());
            }
        });

        startMqtt();
    }

    private void startMqtt() {
        mqttHelper = new MqttHelper(getApplicationContext());
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.d(TAG, "connected");
            }

            @Override
            public void connectionLost(Throwable throwable) {
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w(TAG, "payload -> " + mqttMessage.toString());
                chgPassAck(mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            }
        });
    }

    private void chgPass(String username, String oldPass, String newPass, String newPass2){
        try {
            if(newPass.equals(newPass2)){
                //build general command
                Command cmd = Action.findCommandByName("RAT_USER_CHGPASS", this);
                cmd.setRecipient(Action.asciiToHex(mqttHelper.mqttClientId));
                JSONObject obj = Action.encodePublicCmd(cmd);

                obj.put(KeyData.KEY_USER_USERNAME, Action.asciiToHex("" + username));
                obj.put(KeyData.KEY_USER_PASSWORD, Action.asciiToHex("" + oldPass));
                obj.put(KeyData.KEY_USER_NEWPASS, Action.asciiToHex("" + newPass));

                mqttHelper.publish(obj.toString());
            }else{
                AlertDialog alertDialog = new AlertDialog.Builder(ChangeNewPasswordActivity.this).create();
                alertDialog.setTitle("Error");
                alertDialog.setMessage("New password not match! Please enter again!");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alertDialog.show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void chgPassAck(String msg) throws Exception {
        Log.d(TAG, msg);
        //process payload
        JSONObject jsonMsg = new JSONObject(msg.toString());
        String cmdPayload = jsonMsg.getString(KeyData.KEY_CMD);
        Command cmd = Action.findCommandByPayload(cmdPayload, this);
        if (cmd.getName() != null) {
            if (cmd.getName().equalsIgnoreCase("RAT_USER_CHGPASS_ACK")) {
                //only looking for chg pass command
                if (mqttHelper.mqttClientId.equals(Action.hexToAscii(jsonMsg.getString(KeyData.KEY_RECIPIENT)))) {
                    //check whether object returned
                    JSONObject dataReturned = jsonMsg.getJSONObject(KeyData.KEY_DATA);
                    if (dataReturned.getInt("chgPassStatus") == 1){
                        //change password correct
                        JSONObject rtnObj = jsonMsg.getJSONObject(KeyData.KEY_DATA);
                        User user = new User();
                        user.setUserId(Integer.parseInt(Action.hexToAscii("" + rtnObj.getInt(KeyData.KEY_USER_ID))));
                        user.setUsername(Action.hexToAscii(rtnObj.getString(KeyData.KEY_USER_USERNAME)));
                        user.setPassword(Action.hexToAscii(rtnObj.getString(KeyData.KEY_USER_PASSWORD)));
                        user.setEmail(Action.hexToAscii(rtnObj.getString(KeyData.KEY_USER_EMAIL)));
                        user.setEmailVerificationStatus(Action.hexToAscii(rtnObj.getString(KeyData.KEY_USER_EMAILVERIFICATIONSTATUS)));
                        user.setForgetPassStatus(Action.hexToAscii(rtnObj.getString(KeyData.KEY_USER_FORGETPASSSTATUS)));

                        //login user //need log out ???
                        LoggedInUser.Login(user);

                        //redirect to
                        finish();
                        startActivity(new Intent(this, MainPageActivity.class));
                    }else {
                        if (dataReturned.getInt("errorCode") == 4) {
                            //incorrect password
                            AlertDialog alertDialog = new AlertDialog.Builder(ChangeNewPasswordActivity.this).create();
                            alertDialog.setTitle("Error");
                            alertDialog.setMessage("Current password is invalid! Please enter again!");
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            alertDialog.show();
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mqttHelper.disconnect();
    }
}
