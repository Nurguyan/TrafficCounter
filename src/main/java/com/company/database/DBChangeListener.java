package com.company.database;

import com.impossibl.postgres.api.jdbc.PGConnection;
import com.impossibl.postgres.api.jdbc.PGNotificationListener;

import java.sql.*;
import java.util.concurrent.Callable;

public class DBChangeListener {
    public static PGConnection con;

    public DBChangeListener(){
        try{
            con = DriverManager.getConnection(IDataBaseConstants.url, IDataBaseConstants.user, IDataBaseConstants.password).unwrap(PGConnection.class);

        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
    }

    public void createListener(Callable<Void> func) throws SQLException {
        if (con != null){

            PGNotificationListener listener = new PGNotificationListener() {
                public void notification(int processId, String channelName, String payload) {
                    System.out.println("Database has been updated: " + processId + ", " + channelName + ", " + payload);
                    try {
                        func.call();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            Statement statement = con.createStatement();
            statement.executeUpdate("LISTEN test");
            statement.executeUpdate("NOTIFY test");
            statement.close();
            con.addNotificationListener(listener);
            System.out.println("Database change listener created");
        }
    }

}
