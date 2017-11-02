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

public class LoginActivity extends AppCompatActivity {

    private final String TAG = LoginActivity.class.getSimpleName();
    private ProgressDialog loading;
    private MqttHelper mqttHelper;
    private EditText username;
    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //login button
        Button btnLogin = (Button) findViewById(R.id.button_login);
        username = (EditText) findViewById(R.id.login_username);
        password = (EditText) findViewById(R.id.login_password);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        //forgetpassword button
        Button btnForgetPass = (Button) findViewById(R.id.btn_forgetPass);
        btnForgetPass.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                forgetPass();
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
                msgAck(mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            }
        });
    }

    private void login() {
        try {
            //build general command
            Command cmd = Action.findCommandByName("RAT_USER_LOGIN", this);
            cmd.setRecipient(Action.asciiToHex(mqttHelper.mqttClientId));
            JSONObject obj = Action.encodePublicCmd(cmd);

            obj.put(KeyData.KEY_USER_USERNAME, Action.asciiToHex("" + username.getText().toString()));
            obj.put(KeyData.KEY_USER_PASSWORD, Action.asciiToHex("" + password.getText().toString()));


            mqttHelper.publish(obj.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void msgAck(String msg) throws Exception {
        Log.d(TAG, msg);
        //process payload
        JSONObject jsonMsg = new JSONObject(msg.toString());
        String cmdPayload = jsonMsg.getString(KeyData.KEY_CMD);
        Command cmd = Action.findCommandByPayload(cmdPayload, this);
        if (cmd.getName() != null) {
            if (cmd.getName().equalsIgnoreCase("RAT_USER_AUTHREQUEST_RESPONSE")) {
                //only looking for login command
                if (mqttHelper.mqttClientId.equals(Action.hexToAscii(jsonMsg.getString(KeyData.KEY_RECIPIENT)))) {
                    //check whether object returned
                    JSONObject dataReturned = jsonMsg.getJSONObject(KeyData.KEY_DATA);

                    if (dataReturned.getInt("loginStatus") == 1) {
                        //login success
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

                        if (dataReturned.getString("forgetPassStatus") == "false") {
                            //redirect to
                            finish();
                            startActivity(new Intent(this, MainPageActivity.class));
                        } else {
                            //redirect to change password activity

                            Intent intent = new Intent(LoginActivity.this, ChangeNewPasswordActivity.class);
                            intent.putExtra(KeyData.KEY_USER_USERNAME, username.getText().toString());
                            startActivity(intent);
                        }

                    } else {
                        //get failed reason
                        if (dataReturned.getInt("errorCode") == 0) {
                            //incorrect pass
                            AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                            alertDialog.setTitle("Error");
                            alertDialog.setMessage("Wrong username or password!");
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            alertDialog.show();
                        } else if (dataReturned.getInt("errorCode") == 1) {
                            //email not verified
                            AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                            alertDialog.setTitle("Error");
                            alertDialog.setMessage("Email Not Verified! Please Check Your Email!");
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    //redirect to (enter 2fa code)
                                    Intent intent = new Intent(LoginActivity.this, EmailVerificationActivity.class);
                                    intent.putExtra(KeyData.KEY_USER_USERNAME, username.getText().toString());
                                    startActivity(intent);
                                }
                            });
                            alertDialog.show();
                        }

                    }

                }
            } else if (cmd.getName().equalsIgnoreCase("RAT_USER_RETRIEVEPASS_VERIFICATION")) {
                //only looking for forgetpass command
                if (mqttHelper.mqttClientId.equals(Action.hexToAscii(jsonMsg.getString(KeyData.KEY_RECIPIENT)))) {
                    //check whether object returned
                    JSONObject dataReturned = jsonMsg.getJSONObject(KeyData.KEY_DATA);
                    if (dataReturned.getInt("retrievePassStatus") == 1) {
                        //success send new password
                        //redirect to login
                        finish();
                        startActivity(new Intent(this, ForgetPasswordActivity.class));
                    } else if (dataReturned.getInt("errorCode") == 3) {
                        //failed to send new password
                        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                        alertDialog.setTitle("Error");
                        alertDialog.setMessage("Username is not exist");
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

    private void forgetPass() {
        try {
            //build general command
            Command cmd = Action.findCommandByName("RAT_USER_RETRIEVEPASS", this);
            cmd.setRecipient(Action.asciiToHex(mqttHelper.mqttClientId));
            JSONObject obj = Action.encodePublicCmd(cmd);

            obj.put(KeyData.KEY_USER_USERNAME, Action.asciiToHex("" + username.getText().toString()));

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
