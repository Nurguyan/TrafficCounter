package com.company.database

import com.company.database.DataBase
import com.company.database.IDataBaseConstants
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.sql.*

class DataBaseTest extends Assert {

    Connection con
    PreparedStatement pstmt

    @Before
    void connectToDB(){
        con = DriverManager.getConnection(IDataBaseConstants.url, IDataBaseConstants.user, IDataBaseConstants.password)
    }

    @After
    void closeConnection(){
        //close connection ,stmt and resultset here
        try { if (con != null) con.close(); } catch(SQLException se) { se.printStackTrace(); }
        try { if (pstmt != null) pstmt.close(); } catch(SQLException se) { se.printStackTrace(); }
    }

    @Test
    void testGetMinLimit() {
        int expected_min = 5412, actual_min
        String query
        Timestamp timestamp = new Timestamp(System.currentTimeMillis())

        //insert test record
        query = "INSERT INTO "+ IDataBaseConstants.table +"(limit_name, limit_value, effective_date) VALUES ('min', ?, ?);"
        pstmt = con.prepareStatement(query)
        pstmt.setInt(1, expected_min)
        pstmt.setTimestamp(2, timestamp)
        pstmt.executeUpdate()

        //check equality
        actual_min = DataBase.getLimit("min")
        Assert.assertEquals(expected_min, actual_min)

        //delete test record
        query = "delete from "+ IDataBaseConstants.table +" where limit_value = ? AND effective_date = ?;"
        pstmt = con.prepareStatement(query)
        pstmt.setInt(1, expected_min)
        pstmt.setTimestamp(2, timestamp)
        pstmt.executeUpdate()
    }

    @Test
    void testGetMaxLimit() {
        int expected_max = 123456, actual_max
        String query
        Timestamp timestamp = new Timestamp(System.currentTimeMillis())

        //insert test record
        query = "INSERT INTO "+ IDataBaseConstants.table +"(limit_name, limit_value, effective_date) VALUES ('max', ?, ?);"
        pstmt = con.prepareStatement(query)
        pstmt.setInt(1, expected_max)
        pstmt.setTimestamp(2, timestamp)
        pstmt.executeUpdate()

        //check equality
        actual_max = DataBase.getLimit("max")
        Assert.assertEquals(expected_max, actual_max)

        //delete test record
        query = "delete from "+ IDataBaseConstants.table +" where limit_value = ? AND effective_date = ?;"
        pstmt = con.prepareStatement(query)
        pstmt.setInt(1, expected_max)
        pstmt.setTimestamp(2, timestamp)
        pstmt.executeUpdate()
    }

}
