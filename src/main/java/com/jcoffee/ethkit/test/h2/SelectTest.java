package com.jcoffee.ethkit.test.h2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SelectTest {
    public void query(String SQL) {
        try {
            String sourceURL = "jdbc:h2:h2/bin/mydb";
            String user = "sa";
            String key = "";

            try {
                Class.forName("org.h2.Driver");
            } catch (Exception var8) {
                var8.printStackTrace();
            }

            Connection conn = DriverManager.getConnection(sourceURL, user, key);
            Statement stmt = conn.createStatement();
            ResultSet rset = stmt.executeQuery(SQL);

            while(rset.next()) {
                System.out.println(rset.getString("name") + "  " + rset.getString("sex"));
            }

            rset.close();
            stmt.close();
            conn.close();
        } catch (SQLException var9) {
            System.err.println(var9);
        }

    }

    public static void main(String[] args) {
        SelectTest tt = new SelectTest();
        tt.query("select * from mytable");
    }
}
