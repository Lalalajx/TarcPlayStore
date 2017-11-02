package javaclient;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import javaclient.Class.User;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class Verification {

    public static ArrayList<User> veriList = new ArrayList<User>();

    public static void SendEmail(String subject, String body, String recipient) {
        String USER_NAME = "leejx-wa14@student.tarc.edu.my";  // GMail user name (just the part before "@gmail.com")
        String PASSWORD = "1996born"; // GMail password

        Properties props = System.getProperties();
        String host = "smtp.gmail.com";
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.user", USER_NAME);
        props.put("mail.smtp.password", PASSWORD);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(props);
        MimeMessage message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(USER_NAME));

            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));

            message.setSubject(subject);
            message.setText(body);
            Transport transport = session.getTransport("smtp");
            transport.connect(host, USER_NAME, PASSWORD);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
            System.out.println("email sent");
        } catch (AddressException ae) {
            ae.printStackTrace();
        } catch (MessagingException me) {
            me.printStackTrace();
        }
    }

    public static String generateCode(int length) {
        char[] chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        System.out.print(sb.toString());
        return sb.toString();
    }

    public static boolean compare2FA(String username, String code) {
        for (User u : veriList) {
            if (u.getUsername().equals(username)) {
                //check code
                if (u.getVeriCode().equals(code)) {
                    return true;
                }
                break;
            }
        }
        return false;
    }

    public static String renew2FA(String username) {
        String code = generateCode(5);
        for (User u : veriList) {
            if (u.getUsername().equals(username)) {
                u.setVeriCode(code);
                return code;
            }
        }
        return null;
    }

}
