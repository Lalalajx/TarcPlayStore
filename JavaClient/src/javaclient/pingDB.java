package javaclient;

import java.sql.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import static java.util.concurrent.TimeUnit.*;
import javaclient.DA.DBConnection;

public class pingDB {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private int count = 0;

    public void tryToPingDB() {
        final Runnable beeper = new Runnable() {
            public void run() {
                try {
                    Connection conn = DBConnection.getInstance().getConnection();
                    String queryStr = "SELECT * FROM user";
                    PreparedStatement stmt = conn.prepareStatement(queryStr);
                    stmt.executeQuery();
                    if (count == 50) {//5 mininutes
                        System.out.println(TimeStamp.get() + " [INFO] [SQL] PING Success.");
                        count = 0;
                    }
                    count++;
                } catch (SQLException ex) {
                    System.out.println(TimeStamp.get() + " [ERROR] " + ex.getMessage());
                }
            }
        };
        final ScheduledFuture<?> beeperHandle = scheduler.scheduleAtFixedRate(beeper, 60, 10, SECONDS);
        scheduler.schedule(new Runnable() {
            public void run() {
                beeperHandle.cancel(true);
            }
        }, 315400000, SECONDS);
    }

}
