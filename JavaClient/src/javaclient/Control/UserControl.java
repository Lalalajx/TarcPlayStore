package javaclient.Control;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import javaclient.Action;
import javaclient.Class.User;
import javaclient.DA.UserDA;
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

    public UserControl() {
        userInfoDA = new UserDA();
    }

    public JSONObject retrieveUserInfo(Integer userID) {
        User user = userInfoDA.retrieveUserInfo(userID);

        //perform encoding
        //set data encoding
        JSONObject obj = new JSONObject();
        obj.put(KeyData.KEY_USER_ID, Action.asciiToHex("" + user.getUserId()));
        obj.put(KeyData.KEY_USER_USERNAME, Action.asciiToHex(user.getUsername()));
        obj.put(KeyData.KEY_USER_PASSWORD, Action.asciiToHex(user.getPassword()));
        obj.put(KeyData.KEY_USER_EMAIL, Action.asciiToHex(user.getEmail()));
        obj.put(KeyData.KEY_USER_EMAILVERIFICATIONSTATUS, Action.asciiToHex(user.getEmailVerificationStatus()));

        return obj;
    }

    public JSONObject retrieveUserInfo(String username, String password) {
        User user = userInfoDA.retrieveUserInfo(username);
        System.out.println(username);
        System.out.println(user.getPassword());
        if (validatePass(password, user.getPassword())) {
            //correct pass
            //forget pass status false
            if (validateEmail(user.getEmailVerificationStatus())) {
                //email verified
                //perform encoding
                //set data encoding
                JSONObject obj = new JSONObject();
                obj.put(KeyData.KEY_USER_ID, Action.asciiToHex("" + user.getUserId()));
                obj.put(KeyData.KEY_USER_USERNAME, Action.asciiToHex(user.getUsername()));
                obj.put(KeyData.KEY_USER_PASSWORD, Action.asciiToHex(user.getPassword()));
                obj.put(KeyData.KEY_USER_EMAIL, Action.asciiToHex(user.getEmail()));
                obj.put(KeyData.KEY_USER_EMAILVERIFICATIONSTATUS, Action.asciiToHex(user.getEmailVerificationStatus()));
                obj.put(KeyData.KEY_USER_FORGETPASSSTATUS, Action.asciiToHex(user.getForgetPassStatus()));

                obj.put("loginStatus", 1); //1 success login

                return obj;
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
            t.put(KeyData.KEY_USER_ID, Action.asciiToHex("" + allUser.get(i).getUserId()));
            t.put(KeyData.KEY_USER_USERNAME, Action.asciiToHex("" + allUser.get(i).getUsername()));
            t.put(KeyData.KEY_USER_PASSWORD, Action.asciiToHex("" + allUser.get(i).getPassword()));
            t.put(KeyData.KEY_USER_EMAIL, Action.asciiToHex("" + allUser.get(i).getEmail()));
            t.put(KeyData.KEY_USER_EMAILVERIFICATIONSTATUS, Action.asciiToHex("" + allUser.get(i).getEmailVerificationStatus()));
            t.put(KeyData.KEY_USER_FORGETPASSSTATUS, Action.asciiToHex(allUser.get(i).getForgetPassStatus()));

            jsonArr.put(t);
        }
        return jsonArr;
    }

    public JSONObject compare2FACode(String username, String code) {
        User user = userInfoDA.retrieveUserInfo(username);
        if (Verification.compare2FA(username, code)) {
            //correct
            //allowed to login
            JSONObject obj = new JSONObject();
            obj.put(KeyData.KEY_USER_ID, Action.asciiToHex("" + user.getUserId()));
            obj.put(KeyData.KEY_USER_USERNAME, Action.asciiToHex(user.getUsername()));
            obj.put(KeyData.KEY_USER_PASSWORD, Action.asciiToHex(user.getPassword()));
            obj.put(KeyData.KEY_USER_EMAIL, Action.asciiToHex(user.getEmail()));
            obj.put(KeyData.KEY_USER_EMAILVERIFICATIONSTATUS, Action.asciiToHex(user.getEmailVerificationStatus()));
            obj.put(KeyData.KEY_USER_FORGETPASSSTATUS, Action.asciiToHex(user.getForgetPassStatus()));

            user.setEmailVerificationStatus("verified");
            //update db
            updateUser(user);
            Verification.veriList.remove(user);

            obj.put("emailVeriStatus", 1); //1 success verification

            return obj;
        } else {
            //incorrect
            JSONObject obj = new JSONObject();
            obj.put("emailVeriStatus", 0);//0 failed verification
            obj.put("errorCode", 2); //error code 2 //incorrect 2FA

            return obj;
            //retry?
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
