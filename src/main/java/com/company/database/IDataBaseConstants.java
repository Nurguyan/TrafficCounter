package com.company.database;

public interface IDataBaseConstants {
    String url = "jdbc:pgsql://localhost:5432/traffic_limits";
    String user = "postgres";
    String password = "mypassword";
    String table = "limits_per_hour";
}
