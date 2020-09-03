package com.jcoffee.ethkit.test.h2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class UpdateTest {
    public void runInsertDelete() {
        try {
            String sourceURL = "jdbc:h2:h2/bin/mydb";
            String user = "sa";
            String key = "";

            try {
                Class.forName("org.h2.Driver");
            } catch (Exception var6) {
                var6.printStackTrace();
            }

            Connection conn = DriverManager.getConnection(sourceURL, user, key);
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE mytable(name VARCHAR(100),sex VARCHAR(10))");
            stmt.executeUpdate("INSERT INTO mytable VALUES('Steven Stander','male')");
            stmt.executeUpdate("INSERT INTO mytable VALUES('Elizabeth Eames','female')");
            stmt.executeUpdate("DELETE FROM mytable WHERE sex=/'male/'");
            stmt.close();
            conn.close();
        } catch (SQLException var7) {
            System.err.println(var7);
        }

    }

    public static void main(String[] args) {
        (new UpdateTest()).runInsertDelete();
    }
}
