/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaclient.DA;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import javaclient.Settings;
import javaclient.TimeStamp;

/**
 *
 * @author lamkailoon
 */
public class DBConnection {

    private static DBConnection instance;
    private Connection connection;

    private DBConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            this.connection = DriverManager.getConnection(Settings.dbServerUrl, Settings.dbUser, Settings.dbPassword);
            Statement stmt = this.connection.createStatement();
            stmt.execute("USE " + Settings.dbName);
        } catch (ClassNotFoundException | SQLException ex) {
            System.out.println(TimeStamp.get() + " [ERROR] [MYSQL] " + ex.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public static DBConnection getInstance() throws SQLException {
        if (instance == null) {
            instance = new DBConnection();
        } else if (instance.getConnection().isClosed()) {
            instance = new DBConnection();
        }

        return instance;
    }

    public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ex) {
            System.out.println(TimeStamp.get() + " [ERROR] [MYSQL] " + ex.getMessage());
        }
    }
}
