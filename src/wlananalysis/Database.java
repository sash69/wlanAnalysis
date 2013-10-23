/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wlananalysis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author Alja
 */
public class Database {
    
    private static final String jdbcDriver = "com.mysql.jdbc.Driver";
    private static final String url = "jdbc:mysql://localhost:3306";
    private static final String username = "root";
    private static final String password = "";
    private static final String dbName = "wlananalysis";
    private static final String tblName = "requests";
    
    public static Connection prepareConnection()
    {
        try
        {
            Connection conn;
            ResultSet rs;
            Statement st;
            
            conn = DriverManager.getConnection(url, username, password);
            rs = conn.getMetaData().getCatalogs();
            
            boolean databaseFound = false;
            while (rs.next())
                if (rs.getString(1).equals(dbName))
                {
                    databaseFound = true;
                    break;
                }
            
            conn = prepareDatabase(dbName, databaseFound, conn);
            
            return conn;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
    
    private static Connection prepareDatabase(String databaseName, boolean found, Connection conn) {
        try
        {
            conn.setAutoCommit(false);
            Statement st = conn.createStatement();
            
            String sqlDB;
            String sqlTbl;            
                    
            if (!found)
            {
                sqlDB = "CREATE DATABASE " + databaseName + " CHARACTER SET = utf8 COLLATE = utf8_slovenian_ci;";
                st.execute(sqlDB);
                sqlDB = "USE " + databaseName;
                st.execute(sqlDB);
                sqlTbl = "CREATE TABLE IF NOT EXISTS `requests` (\n" +
                                "  `ix` bigint(20) NOT NULL AUTO_INCREMENT,\n" +
                                "  `date` date DEFAULT NULL,\n" +
                                "  `timestamp` timestamp NULL DEFAULT NULL,\n" +
                                "  `source` varchar(17) COLLATE utf8_slovenian_ci DEFAULT NULL,\n" +
                                "  `ssi` smallint(6) DEFAULT NULL,\n" +
                                "  `ssid` varchar(100) COLLATE utf8_slovenian_ci DEFAULT NULL,\n" +
                                "  PRIMARY KEY (`ix`)\n" +
                                ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_slovenian_ci AUTO_INCREMENT=1;";
                st.execute(sqlTbl);
            } else {
                sqlDB = "USE " + databaseName;
                st.execute(sqlDB);                
            }
            
            conn.commit();
            return conn;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }    

    static boolean insertListToDB(Connection conn, ArrayList<ProbeRequest> probeRequestsList) {
        try
        {
            ArrayList<String> columnNamesList = getTableColumnNames(conn);
            
            String insertIntoSQL = "INSERT INTO "+tblName+"(";
            for (String columnName : columnNamesList)
                insertIntoSQL += columnName + ",";
            insertIntoSQL = insertIntoSQL.substring(0, insertIntoSQL.length()-1) + ") VALUES ("; //remove last ,
            for (String columnName : columnNamesList)
                insertIntoSQL += "?,";
            insertIntoSQL = insertIntoSQL.substring(0, insertIntoSQL.length()-1) + ");"; //remove last ,
            
            PreparedStatement st = conn.prepareStatement(insertIntoSQL);
            ResultSet rs;

            for (ProbeRequest probeRequest : probeRequestsList)
            {
                if (columnNamesList.size() == 5)
                {
                    st.setDate(1,probeRequest.getDate());
                    st.setTimestamp(2, probeRequest.getTimestamp());
                    st.setString(3, probeRequest.getSourceMAC());                    
                    st.setInt(4, probeRequest.getSsi());
                    st.setString(5, probeRequest.getSSid());
                }
                else 
                    throw new Exception("Table does not have all required fields!");
                st.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (Exception e)
        {
            System.out.println("insertListIntoDB failed! " + e.getMessage());
            return false;
        }
    }

    private static ArrayList<String> getTableColumnNames(Connection conn) {
        try
        {
            ResultSet rs;
            ResultSetMetaData rsmd;
            
            String selectFirstRow = "SELECT * FROM "+tblName+" LIMIT 1;";
            PreparedStatement st = conn.prepareStatement(selectFirstRow);
            rs = st.executeQuery();
            rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            
            ArrayList<String> columnNamesList = new ArrayList<>();
            for (int i = 0; i < columnCount; i++)
                if(!rsmd.isAutoIncrement(i+1))
                    columnNamesList.add(rsmd.getColumnName(i+1));
            return columnNamesList;
        } catch(Exception e) {
            System.out.println("getTableColumnNames failed! " + e.getMessage());
            return null;
        }
    }

    static ProbeRequest getLastProbeRequest(Connection conn) {
        try
        {
            ProbeRequest lastProbeRequest;
            ResultSet rs;
            ResultSetMetaData rsmd;
            
            String select = "SELECT * FROM "+tblName+" ORDER BY ix DESC LIMIT 1";
            PreparedStatement st = conn.prepareStatement(select);
            rs = st.executeQuery();
            rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            
            if (columnCount != 6)
                throw new Exception("Table does not have all required fields!");
            
            if (rs.first())
                lastProbeRequest = new ProbeRequest(rs.getLong(1), rs.getDate(2), 
                                                    rs.getTimestamp(3), rs.getString(4),
                                                    rs.getInt(5), rs.getString(6));
            else //if no entry
                lastProbeRequest = null;
            
            return lastProbeRequest;
        } catch (Exception e)
        {
            System.out.println("getLastProbeRequest failed! " + e.getMessage());
            return null;
        }
    }
}
