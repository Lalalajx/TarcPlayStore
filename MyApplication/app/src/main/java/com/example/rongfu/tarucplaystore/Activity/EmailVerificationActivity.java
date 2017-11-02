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

public class EmailVerificationActivity extends AppCompatActivity {

    private final String TAG = EmailVerificationActivity.class.getSimpleName();
    private ProgressDialog loading;
    private MqttHelper mqttHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        final EditText veriCode = (EditText) findViewById(R.id.veriCode);
        Button btnResend = (Button) findViewById(R.id.btn_resend);
        Button btnEnter = (Button) findViewById(R.id.btn_enter2FACode);

        final String username = getIntent().getStringExtra(KeyData.KEY_USER_USERNAME);

        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyCode(username, veriCode.getText().toString());
            }
        });

        btnResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resendCode(username);
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
                verifyCodeAck(mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            }
        });
    }

    private void verifyCode(String username, String veriCode) {
        try {
            //build general command
            Command cmd = Action.findCommandByName("RAT_USER_AUTHREQUEST", this);
            cmd.setRecipient(Action.asciiToHex(mqttHelper.mqttClientId));
            JSONObject obj = Action.encodePublicCmd(cmd);

            obj.put(KeyData.KEY_USER_USERNAME, Action.asciiToHex("" + username));
            obj.put(KeyData.KEY_USER_VERICODE, Action.asciiToHex("" + veriCode));


            mqttHelper.publish(obj.toString());


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }


    private void verifyCodeAck(String msg) throws Exception {
        Log.d(TAG, msg);
        //process payload
        JSONObject jsonMsg = new JSONObject(msg.toString());
        String cmdPayload = jsonMsg.getString(KeyData.KEY_CMD);
        Command cmd = Action.findCommandByPayload(cmdPayload, this);
        if (cmd.getName() != null) {
            if (cmd.getName().equalsIgnoreCase("RAT_USER_AUTHREQUEST_ACK")) {
                //only looking for 2fa command
                if (mqttHelper.mqttClientId.equals(Action.hexToAscii(jsonMsg.getString(KeyData.KEY_RECIPIENT)))) {
                    //check whether object returned
                    JSONObject dataReturned = jsonMsg.getJSONObject(KeyData.KEY_DATA);
                    if (dataReturned.getInt("emailVeriStatus") == 1){
                        //verification correct
                        JSONObject rtnObj = jsonMsg.getJSONObject(KeyData.KEY_DATA);
                        User user = new User();
                        user.setUserId(Integer.parseInt(Action.hexToAscii("" + rtnObj.getInt(KeyData.KEY_USER_ID))));
                        user.setUsername(Action.hexToAscii(rtnObj.getString(KeyData.KEY_USER_USERNAME)));
                        user.setPassword(Action.hexToAscii(rtnObj.getString(KeyData.KEY_USER_PASSWORD)));
                        user.setEmail(Action.hexToAscii(rtnObj.getString(KeyData.KEY_USER_EMAIL)));
                        user.setEmailVerificationStatus(Action.hexToAscii(rtnObj.getString(KeyData.KEY_USER_EMAILVERIFICATIONSTATUS)));
                        user.setForgetPassStatus(Action.hexToAscii(rtnObj.getString(KeyData.KEY_USER_FORGETPASSSTATUS)));

                        //login user
                        LoggedInUser.Login(user);

                        //redirect to
                        finish();
                        startActivity(new Intent(this, MainPageActivity.class));
                    }else {
                        if (dataReturned.getInt("errorCode") == 2) {
                            //incorrect 2faCode
                            AlertDialog alertDialog = new AlertDialog.Builder(EmailVerificationActivity.this).create();
                            alertDialog.setTitle("Error");
                            alertDialog.setMessage("Wrong Verification Code! Please enter again!");
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

    private void resendCode(String username){
        try {
            //build general command
            Command cmd = Action.findCommandByName("RAT_USER_AUTHREQUEST_RETRY", this);
            cmd.setRecipient(Action.asciiToHex(mqttHelper.mqttClientId));
            JSONObject obj = Action.encodePublicCmd(cmd);

            obj.put(KeyData.KEY_USER_USERNAME, Action.asciiToHex("" + username));

            mqttHelper.publish(obj.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mqttHelper.disconnect();
    }
}

