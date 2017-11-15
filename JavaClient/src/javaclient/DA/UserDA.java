  package javaclient.DA;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javaclient.Class.User;
import javaclient.Settings;
import javaclient.TimeStamp;
import javaclient.pingDB;

public class UserDA {

    private Connection conn;
    private PreparedStatement stmt;
    private String tableName = "user";
    private ResultSet rs;

    public UserDA() {
        try {
            conn = DBConnection.getInstance().getConnection();
        } catch (SQLException ex) {
            System.out.println(TimeStamp.get() + " [ERROR] [MYSQL] [" + UserDA.class.getSimpleName() + "] " + ex.getMessage());
        }
    }
    
    //retrieve pass by username
    public User retrieveUserInfo(String username) {
        String queryStr = "SELECT * FROM " + tableName + " WHERE username = ?";
        User userInfo = new User();

        try {
            stmt = conn.prepareStatement(queryStr);
            stmt.setString(1, username);

            rs = stmt.executeQuery();

            if (rs.next()) {
                userInfo.setUsername(rs.getString("Username"));
                userInfo.setPassword(rs.getString("Password"));
                userInfo.setEmail(rs.getString("Email"));
                userInfo.setEmailVerificationStatus(rs.getString("EmailVerificationStatus"));
                userInfo.setForgetPassStatus(rs.getString("ForgetPassStatus"));
            }

        } catch (SQLException se) {
            System.out.println(TimeStamp.get() + " [ERROR] [MYSQL] [" + UserDA.class.getSimpleName() + "] " + se.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ex) {
                System.out.println(TimeStamp.get() + " [ERROR] [MYSQL] [" + UserDA.class.getSimpleName() + "] " + ex.getMessage());
            }
        }

        return userInfo;

    }
    
    
    //retrieve(arraylist)
    public ArrayList<User> retrieveAllUser() {
        String queryStr = "SELECT * FROM " + tableName;
        ArrayList<User> allUser = new ArrayList<>();

        try {
            stmt = conn.prepareStatement(queryStr);

            rs = stmt.executeQuery();

            while (rs.next()) {
                User t = new User();
                t.setUsername(rs.getString("username"));
                t.setPassword(rs.getString("password"));
                t.setEmail(rs.getString("email"));
                t.setEmailVerificationStatus(rs.getString("emailVerificationStatus"));
                t.setForgetPassStatus(rs.getString("ForgetPassStatus"));
                allUser.add(t);
            }
        } catch (SQLException se) {
            System.out.println(TimeStamp.get() + " [ERROR] [MYSQL] [" + UserDA.class.getSimpleName() + "] " + se.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ex) {
                System.out.println(TimeStamp.get() + " [ERROR] [MYSQL] [" + UserDA.class.getSimpleName() + "] " + ex.getMessage());
            }
        }

        return allUser;
    }

    //update
    public void updateUser(User user) {
        String queryStr = "UPDATE " + tableName + " SET Password = ?, Email = ?, EmailVerificationStatus = ?, ForgetPassStatus = ? WHERE Username = ?";

        try {
            stmt = conn.prepareStatement(queryStr);
            stmt.setString(1, user.getPassword());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getEmailVerificationStatus());
            stmt.setString(4, user.getForgetPassStatus());
            stmt.setString(5, user.getUsername());
            stmt.executeUpdate();
        } catch (SQLException se) {
            System.out.println(TimeStamp.get() + " [ERROR] [MYSQL] [" + UserDA.class.getSimpleName() + "] " + se.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ex) {
                System.out.println(TimeStamp.get() + " [ERROR] [MYSQL] [" + UserDA.class.getSimpleName() + "] " + ex.getMessage());
            }
        }
    }


//        public static void main(String[] args) {
//            Settings.parseSetting();
//            
//            System.out.println(new UserDA().retrieveUserInfo("jiaxin123").getPassword());
//    }
}
