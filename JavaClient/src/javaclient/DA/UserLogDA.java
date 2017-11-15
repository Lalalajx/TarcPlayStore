package javaclient.DA;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import javaclient.Class.User;
import javaclient.Class.UserLog;
import javaclient.TimeStamp;

public class UserLogDA {

    private Connection conn;
    private PreparedStatement stmt;
    private String tableName = "userLog";
    private ResultSet rs;

    public UserLogDA() {
        try {
            conn = DBConnection.getInstance().getConnection();
        } catch (SQLException ex) {
            System.out.println(TimeStamp.get() + " [ERROR] [MYSQL] [" + UserLogDA.class.getSimpleName() + "] " + ex.getMessage());
        }
    }

    //add
    public void createUserLogInfo(UserLog userLog) {
        String queryStr = "INSERT INTO " + tableName + " (username, deviceId, lastLoginDateTime)" + " VALUES (?,?,?)";

        try {
            stmt = conn.prepareStatement(queryStr);
            stmt.setString(1, userLog.getUsername());
            stmt.setString(2, userLog.getDeviceId());
            stmt.setTimestamp(3, new Timestamp(userLog.getLastLoginDateTime().getTime()));
            stmt.executeUpdate();
            
        } catch (SQLException se) {
            System.out.println(TimeStamp.get() + " [ERROR] [MYSQL] [" + UserLogDA.class.getSimpleName() + "] " + se.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ex) {
                System.out.println(TimeStamp.get() + " [ERROR] [MYSQL] [" + UserLogDA.class.getSimpleName() + "] " + ex.getMessage());
            }
        }
    }

    //retrieve by username
    public UserLog retrieveUserLogInfo(String username) {
        String queryStr = "SELECT * FROM " + tableName + " WHERE username = ?";
        UserLog userLogInfo = new UserLog();

        try {
            stmt = conn.prepareStatement(queryStr);
            stmt.setString(1, username);

            rs = stmt.executeQuery();

            if (rs.next()) {
                userLogInfo.setUserLogId(rs.getInt("UserLogId"));
                userLogInfo.setUsername(rs.getString("Username"));
                userLogInfo.setDeviceId(rs.getString("DeviceId"));
                userLogInfo.setLastLoginDateTime(rs.getTimestamp("LastLoginDateTime"));
            }

        } catch (SQLException se) {
            System.out.println(TimeStamp.get() + " [ERROR] [MYSQL] [" + UserLogDA.class.getSimpleName() + "] " + se.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ex) {
                System.out.println(TimeStamp.get() + " [ERROR] [MYSQL] [" + UserLogDA.class.getSimpleName() + "] " + ex.getMessage());
            }
        }
        return userLogInfo;
    }

    //update 
    public void updateUser(UserLog userLog) {
        String queryStr = "UPDATE " + tableName + " SET Username = ?, deviceId = ?, lastLoginDateTime = ? WHERE UserLogID = ?";
        
        try {
            stmt = conn.prepareStatement(queryStr);
            stmt.setString(1, userLog.getUsername());
            stmt.setString(2, userLog.getDeviceId());
            stmt.setTimestamp(3, new Timestamp(userLog.getLastLoginDateTime().getTime()));
            stmt.setInt(4, userLog.getUserLogId());
            stmt.executeUpdate();
        } catch (SQLException se) {
            System.out.println(TimeStamp.get() + " [ERROR] [MYSQL] [" + UserLogDA.class.getSimpleName() + "] " + se.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ex) {
                System.out.println(TimeStamp.get() + " [ERROR] [MYSQL] [" + UserLogDA.class.getSimpleName() + "] " + ex.getMessage());
            }
        }
    }

}
