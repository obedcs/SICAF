/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sicaf;

import java.sql.Connection;
import java.sql.DriverManager;
import javax.swing.JOptionPane;
import java.sql.*;

/**
 *
 * @author Sistemas1
 */
public class conecxionbd {
    Connection con=null;
    
    public Connection Conecta(){
        con=null;
        try{
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = DriverManager.getConnection("jdbc:sqlserver://svrhste:1433;databaseName=TCADBHSU;user=sa;password=Assist7!;");
        }
        catch(Exception e){JOptionPane.showMessageDialog(null, e);}
        return con;
    }
    
    public Connection ConectaMySQL(String server,String port,String database,String host,String password){
        con=null;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            //Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            con = DriverManager.getConnection("jdbc:mysql://"+server+":"+port+"/"+database,host,password);
        }
        catch(Exception e){JOptionPane.showMessageDialog(null, e);}
        return con;
    }
}
