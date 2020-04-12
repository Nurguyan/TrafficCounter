package com.company.database;

import com.impossibl.postgres.api.jdbc.PGConnection;

import java.sql.*;

public class DataBase {
    // JDBC variables for opening and managing connection
    private static PGConnection con;
    private static PreparedStatement pstmt;
    private static ResultSet rs;

    public static int getLimit(String limit_name){

        try {
            con = DriverManager.getConnection(IDataBaseConstants.url, IDataBaseConstants.user, IDataBaseConstants.password).unwrap(PGConnection.class);
            String query = "select * from (select *, max(effective_date) over (partition by limit_name) as max_date from " + IDataBaseConstants.table + ") as s where effective_date = max_date and limit_name = ?;";

            pstmt = con.prepareStatement(query);
            pstmt.setString(1, limit_name);

            rs = pstmt.executeQuery();  // executing SELECT query
            if (rs.next())
                return rs.getInt(2);


        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        } finally {
            try { if (con != null) con.close(); } catch(SQLException se) { se.printStackTrace(); }
            try { if (pstmt != null) pstmt.close(); } catch(SQLException se) { se.printStackTrace(); }
            try { if (rs != null) rs.close(); } catch(SQLException se) { se.printStackTrace(); }
        }
        return -1;
    }


}
