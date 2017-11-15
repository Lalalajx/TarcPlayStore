package javaclient.Class;

import java.util.Date;

public class UserLog {

    private int userLogId;
    private String username;
    private String deviceId;
    private Date lastLoginDateTime;

    public UserLog() {
    }

    public UserLog(int userLogId, String username, String deviceId, Date lastLoginDateTime) {
        this.userLogId = userLogId;
        this.username = username;
        this.deviceId = deviceId;
        this.lastLoginDateTime = lastLoginDateTime;
    }

    public int getUserLogId() {
        return userLogId;
    }

    public String getUsername() {
        return username;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public Date getLastLoginDateTime() {
        return lastLoginDateTime;
    }

    public void setUserLogId(int userLogId) {
        this.userLogId = userLogId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setLastLoginDateTime(Date lastLoginDateTime) {
        this.lastLoginDateTime = lastLoginDateTime;
    }

    @Override
    public String toString() {
        return "UserLog{" + "username=" + username + ", deviceId=" + deviceId + ", lastLoginDateTime=" + lastLoginDateTime + '}';
    }

}
