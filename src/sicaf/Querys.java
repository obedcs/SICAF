/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sicaf;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import javax.swing.JOptionPane;

/**
 *
 * @author Obed
 */
public class Querys {
    Statement stat;
    
    public int Logueo(Connection con, String usuario, String contrasenia){
        int bandera = 0;
        return bandera;
    }
    
    public ResultSet Estructura(Connection con,String consulta){
       ResultSet rs=null;
       try{
           stat = con.createStatement();
           rs = stat.executeQuery(consulta);
        }
        catch (Exception e){JOptionPane.showMessageDialog(null, e);}
       
       return rs;
   }
    
    public int InsertaActualiza(Connection con, String Comando){
       int status=0;
       try{
           stat = con.createStatement();
           stat.executeUpdate(Comando);
           status++;
       }catch (Exception e){JOptionPane.showMessageDialog(null, e);}
       return status;
   }
    
   public String getClave(Connection con){
       ResultSet rs=null;
       String consulta = "SELECT clave FROM inventario ORDER BY clave ASC";
       String clavee="";
       
       try{
           stat = con.createStatement();
           rs = stat.executeQuery(consulta);
           
           if(rs.last()){
               String clave = rs.getString("clave");
               clavee = Integer.toString(Integer.parseInt(clave) + 1);
           }
        }
        catch (Exception e){JOptionPane.showMessageDialog(null, e);}
       return clavee;
   }
   
   public String getFolio(Connection con){
       ResultSet rs=null;
       String consulta = "SELECT folio FROM ventascab ORDER BY folio ASC";
       String clavee="";
       
       try{
           stat = con.createStatement();
           rs = stat.executeQuery(consulta);
           
           if(rs.last()){
               String clave = rs.getString("folio");
               clavee = Integer.toString(Integer.parseInt(clave) + 1);
           }
        }
        catch (Exception e){JOptionPane.showMessageDialog(null, e);}
       return clavee;
   }
   
   public String FechaVentas(Calendar c1){
        String dia = Integer.toString(c1.get(Calendar.DATE));
        String mes = Integer.toString(c1.get(Calendar.MONTH)+1);
        String annio = Integer.toString(c1.get(Calendar.YEAR));
        
            if(dia.length() < 2)
                dia = "0" + dia;
            if(mes.length() < 2)
                mes = "0" + mes;
        
        return (annio+"-"+mes+"-"+dia);
    }
   
   public String ConvierteMoneda(String Monto){
        Locale locale = new Locale("es","MX"); // elegimos Mexico
        NumberFormat nf = NumberFormat.getCurrencyInstance(locale);
        
        return nf.format(Float.parseFloat(Monto));
    }
   
   public String FechaDateChooser(String Fecha) throws ParseException{
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
        cal.setTime(formato.parse(Fecha));
        cal.add(cal.DATE, 3);
        String dia = Integer.toString(cal.get(Calendar.DATE)-1);
        String mes = Integer.toString(cal.get(Calendar.MONTH)+1);
        String annio = Integer.toString(cal.get(Calendar.YEAR));
        
        if(dia.length() < 2)
            dia = "0" + dia;
        if(mes.length() < 2)
            mes = "0" + mes;
        
        System.out.println(annio+"-"+mes+"-"+dia);    
        return annio+"-"+mes+"-"+dia;
    }
    
}
