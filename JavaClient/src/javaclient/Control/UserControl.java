package javaclient.Control;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import javaclient.Action;
import javaclient.Class.User;
import javaclient.Class.UserLog;
import javaclient.DA.UserDA;
import javaclient.DA.UserLogDA;
import javaclient.KeyData;
import javaclient.Verification;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.json.JSONArray;
import org.json.JSONObject;

public class UserControl {

    private UserDA userInfoDA;
    private UserLogDA userLogInfoDA;

    public UserControl() {
        userInfoDA = new UserDA();
        userLogInfoDA = new UserLogDA();
    }

    public JSONObject retrieveUserInfo(String username, String password, String deviceId) {
        User user = userInfoDA.retrieveUserInfo(username);
        UserLog userLog = userLogInfoDA.retrieveUserLogInfo(username);

        System.out.println(username);
        System.out.println(user.getPassword());
        System.out.println(Action.hexToAscii(deviceId));
        System.out.println(userLog.getDeviceId());

        if (validatePass(password, user.getPassword())) {
            //correct pass
            //forget pass status false
            if (validateEmail(user.getEmailVerificationStatus())) {
                //email verified
                if (Action.hexToAscii(deviceId).equalsIgnoreCase(userLog.getDeviceId())) {
                    //got deviceid
                    //perform encoding
                    //set data encoding
                    JSONObject obj = new JSONObject();
                    obj.put(KeyData.KEY_USER_USERNAME, Action.asciiToHex(user.getUsername()));
                    obj.put(KeyData.KEY_USER_PASSWORD, Action.asciiToHex(user.getPassword()));
                    obj.put(KeyData.KEY_USER_EMAIL, Action.asciiToHex(user.getEmail()));
                    obj.put(KeyData.KEY_USER_EMAILVERIFICATIONSTATUS, Action.asciiToHex(user.getEmailVerificationStatus()));
                    obj.put(KeyData.KEY_USER_FORGETPASSSTATUS, Action.asciiToHex(user.getForgetPassStatus()));
                    obj.put(KeyData.KEY_USERLOG_LASTLOGINDATETIME, Action.asciiToHex(userLog.getLastLoginDateTime() + ""));

                    //update last login time
                    userLog.setLastLoginDateTime(new Date());
                    updateUserLog(userLog);

                    obj.put("loginStatus", 1); //1 success login
                    return obj;
                } else {
                    //device not verified
                    JSONObject obj = new JSONObject();
                    obj.put("loginStatus", 0);//0 failed login
                    obj.put("errorCode", 5);//5 device not verified 

                    new Thread(() -> {
                        String code = Verification.generateCode(5);
                        user.setVeriCode(code);
                        String subject = "TARUC Play Store - Verify Device";
                        String body = "Code: " + code;

                        Verification.SendEmail(subject, body, "leejx-wa14@student.tarc.edu.my");
                        Verification.veriList.add(user);
                    }).start();
                    return obj;
                }
            } else {
                //email not verified
                JSONObject obj = new JSONObject();
                obj.put("loginStatus", 0);//0 failed login
                obj.put("errorCode", 1);//1 not verified

                new Thread(() -> {
                    String code = Verification.generateCode(5);
                    user.setVeriCode(code);
                    String subject = "TARUC Play Store - Verify Email";
                    String body = "Code: " + code;

                    Verification.SendEmail(subject, body, "leejx-wa14@student.tarc.edu.my");
                    Verification.veriList.add(user);
                }).start();

                return obj;
            }
        } else {
            //wrong pass
            JSONObject obj = new JSONObject();
            obj.put("loginStatus", 0);//0 failed login
            obj.put("errorCode", 0);//0 wrong password

            return obj;
        }
    }

    public JSONArray retrieveAllUser() {
        ArrayList<User> allUser = userInfoDA.retrieveAllUser();

        //perform encoding
        //set data encoding
        JSONArray jsonArr = new JSONArray();

        for (int i = 0; i < allUser.size(); i++) {
            JSONObject t = new JSONObject();
            t.put(KeyData.KEY_USER_USERNAME, Action.asciiToHex("" + allUser.get(i).getUsername()));
            t.put(KeyData.KEY_USER_PASSWORD, Action.asciiToHex("" + allUser.get(i).getPassword()));
            t.put(KeyData.KEY_USER_EMAIL, Action.asciiToHex("" + allUser.get(i).getEmail()));
            t.put(KeyData.KEY_USER_EMAILVERIFICATIONSTATUS, Action.asciiToHex("" + allUser.get(i).getEmailVerificationStatus()));
            t.put(KeyData.KEY_USER_FORGETPASSSTATUS, Action.asciiToHex(allUser.get(i).getForgetPassStatus()));

            jsonArr.put(t);
        }
        return jsonArr;
    }

    public JSONObject compare2FACode(String username, String code, String type, String deviceId) {
        User user = userInfoDA.retrieveUserInfo(username);
        if (Verification.compare2FA(username, code)) {
            //correct
            //allowed to login
            if (type.equalsIgnoreCase("email")) {
                JSONObject obj = new JSONObject();
                obj.put(KeyData.KEY_USER_USERNAME, Action.asciiToHex(user.getUsername()));
                obj.put(KeyData.KEY_USER_PASSWORD, Action.asciiToHex(user.getPassword()));
                obj.put(KeyData.KEY_USER_EMAIL, Action.asciiToHex(user.getEmail()));
                obj.put(KeyData.KEY_USER_EMAILVERIFICATIONSTATUS, Action.asciiToHex(user.getEmailVerificationStatus()));
                obj.put(KeyData.KEY_USER_FORGETPASSSTATUS, Action.asciiToHex(user.getForgetPassStatus()));
                obj.put(KeyData.KEY_USERLOG_LASTLOGINDATETIME, Action.asciiToHex(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date())));
               
                user.setEmailVerificationStatus("verified");

                UserLog userLog = new UserLog();
                userLog.setUsername(user.getUsername());
                userLog.setDeviceId(Action.hexToAscii(deviceId));
                userLog.setLastLoginDateTime(new Date());

                System.out.println(userLog.toString());

                //update db
                updateUser(user);
                addUserLog(userLog);
                Verification.veriList.remove(user);

                obj.put("emailVeriStatus", 1); //1 success verification
                return obj;
            } else {
                //type = device
                JSONObject obj = new JSONObject();
                obj.put(KeyData.KEY_USER_USERNAME, Action.asciiToHex(user.getUsername()));
                obj.put(KeyData.KEY_USER_PASSWORD, Action.asciiToHex(user.getPassword()));
                obj.put(KeyData.KEY_USER_EMAIL, Action.asciiToHex(user.getEmail()));
                obj.put(KeyData.KEY_USER_EMAILVERIFICATIONSTATUS, Action.asciiToHex(user.getEmailVerificationStatus()));
                obj.put(KeyData.KEY_USER_FORGETPASSSTATUS, Action.asciiToHex(user.getForgetPassStatus()));
                obj.put(KeyData.KEY_USERLOG_LASTLOGINDATETIME, Action.asciiToHex(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date())));

                //set device id
                UserLog userLog = new UserLog();
                userLog.setUsername(user.getUsername());
                userLog.setDeviceId(Action.hexToAscii(deviceId));
                userLog.setLastLoginDateTime(new Date());

                //add new
                addUserLog(userLog);
                Verification.veriList.remove(user);

                obj.put("deviceVeriStatus", 1); //1 success verification
                return obj;
            }

        } else {
            //incorrect
            if (type.equalsIgnoreCase("email")) {
                JSONObject obj = new JSONObject();
                obj.put("emailVeriStatus", 0);//0 failed verification
                obj.put("errorCode", 2); //error code 2 //incorrect 2FA

                return obj;
            } else {
                JSONObject obj = new JSONObject();
                obj.put("deviceVeriStatus", 0);//0 failed verification
                obj.put("errorCode", 2); //error code 2 //incorrect 2FA

                return obj;
            }
        }
    }

    public JSONObject resend2FACode(String username) {
        JSONObject obj = new JSONObject();

        new Thread(() -> {
            String subject = "TARUC Play Store - Verify Email";
            String body = "Code: " + Verification.renew2FA(username);;

            Verification.SendEmail(subject, body, "leejx-wa14@student.tarc.edu.my");
        }).start();

        return obj;
    }

    public JSONObject retrievePassword(String username) {
        User user = userInfoDA.retrieveUserInfo(username);
        if (user.getUsername() != null) {
            String email = user.getEmail();
            JSONObject obj = new JSONObject();

            new Thread(() -> {
                String newPass = Verification.generateCode(10);
                user.setPassword(newPass);
                user.setForgetPassStatus("true");
                //update db
                updateUser(user);
                String subject = "TARUC Play Store - Forgot Password";
                String body = "Please login with new password: " + newPass;

                Verification.SendEmail(subject, body, email);

            }).start();

            obj.put("retrievePassStatus", 1); //1 success retrievePass
            return obj;
        } else {
            //wrong username
            JSONObject obj = new JSONObject();
            obj.put("retrievePassStatus", 0); //0 failed retrievePass
            obj.put("errorCode", 3);//3 username not exist

            return obj;
        }
    }

    public JSONObject changePassword(String username, String oldPass, String newPass) {
        User user = userInfoDA.retrieveUserInfo(username);
        if (oldPass.equals(user.getPassword())) {
            //correct oldpass
            JSONObject obj = new JSONObject();
            user.setPassword(newPass);
            user.setForgetPassStatus("false");
            //update db
            updateUser(user);

            obj.put("chgPassStatus", 1); //1 success change password
            return obj;
        } else {
            //wrong oldpass
            JSONObject obj = new JSONObject();
            obj.put("chgPassStatus", 0); //1 failed change password
            obj.put("errorCode", 4);//4 oldpass not match

            return obj;
        }
    }

    public void updateUser(User user) {
        userInfoDA.updateUser(user);
    }

    public void addUserLog(UserLog userLog) {
        userLogInfoDA.createUserLogInfo(userLog);
    }

    public void updateUserLog(UserLog userLog) {
        userLogInfoDA.updateUser(userLog);
    }

    private boolean validatePass(String pass1, String pass2) {
        return pass1.equals(pass2);
    }

    private boolean validateEmail(String emailStatus) {
        return emailStatus.equalsIgnoreCase("verified");
    }

    private boolean validateForgetPassStatus(String forgetPassStatus) {
        return forgetPassStatus.equalsIgnoreCase("false");
    }
}
