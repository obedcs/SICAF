/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sicaf;

import java.net.URL;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JDialog;
import javax.swing.JFrame;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.swing.JRViewer;
import net.sf.jasperreports.view.JasperViewer;
import java.util.Date;
import net.sf.jasperreports.view.save.JRPdfSaveContributor.*;

/**
 *
 * @author Obed
 */
public class Report {
    static Connection conn = null;
    static Statement st = null;
    static ResultSet rs = null;
    private static JasperPrint report;
    static String carpeta="";
    static String carpetaPDF="";
    
    public static Date ConvertirFecha(String Fecha){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        
	try {date = formatter.parse(Fecha);} catch (ParseException e) {e.printStackTrace();}
        
        return date;
    }
    
    public static void mostrarReporteNota(Connection con,String folio) throws SQLException, JRException {
        Map parametro = new HashMap();
        parametro.put("folio", folio);
        java.lang.String url = carpeta+"RepVentaTicket.jrxml";
        conn = con;

        JasperReport reportes = JasperCompileManager.compileReport(url);
        JasperPrint print = JasperFillManager.fillReport(reportes, parametro, conn);
        
        //para imprimir directamente
        JasperPrintManager.printReport(print, false);
        /*JDialog viewer = new JDialog(new JFrame(),"Vista Previa del Reporte", true);
        viewer.setSize(950,700);
        viewer.setLocationRelativeTo(null);
        JRViewer jrv = new JRViewer(print);
        viewer.getContentPane().add(jrv);
        viewer.show();*/
    }
    
    public static void mostrarReporteCargo(Connection con,String folio) throws SQLException, JRException {
        Map parametro = new HashMap();
        parametro.put("folio", folio);
        java.lang.String url = carpeta+"RepVentaCargo.jrxml";
        conn = con;

        JasperReport reportes = JasperCompileManager.compileReport(url);
        JasperPrint print = JasperFillManager.fillReport(reportes, parametro, conn);
        
        //para imprimir directamente
        JasperPrintManager.printReport(print, false);
        /*JDialog viewer = new JDialog(new JFrame(),"Vista Previa del Reporte", true);
        viewer.setSize(950,700);
        viewer.setLocationRelativeTo(null);
        JRViewer jrv = new JRViewer(print);
        viewer.getContentPane().add(jrv);
        viewer.show();*/
    }
    
    public static void ReporteCorte(Connection con,String cajero,String Inicio, String Fin) throws SQLException, JRException {
        Map parametro = new HashMap();
        parametro.put("cajero", cajero);
        parametro.put("fechainicio", Inicio);
        parametro.put("fechafin", Fin);
        parametro.put("logo", "C:\\Users\\VM\\Desktop\\Compartido\\SICAF\\Logo\\Logo.png");
        
        java.lang.String url = carpeta+"RepVenta.jrxml";
        conn = con;

        JasperReport reportes = JasperCompileManager.compileReport(url);
        JasperPrint print = JasperFillManager.fillReport(reportes, parametro, conn);
        
        //para mostrar el reporte        
        JDialog viewer = new JDialog(new JFrame(),"Vista Previa del Reporte", true);
        viewer.setSize(950,700);
        viewer.setLocationRelativeTo(null);
        JRViewer jrv = new JRViewer(print);
        viewer.getContentPane().add(jrv);
        viewer.show();
    }
    
    public static void ReporteCorteCargo(Connection con,String Inicio, String Fin, String Report) throws SQLException, JRException {
        Map parametro = new HashMap();
        parametro.put("fechainicio", Inicio);
        parametro.put("fechafin", Fin);
        java.lang.String url = carpeta+Report+".jrxml";
        conn = con;

        JasperReport reportes = JasperCompileManager.compileReport(url);
        JasperPrint print = JasperFillManager.fillReport(reportes, parametro, conn);
        
        //para mostrar el reporte        
        JDialog viewer = new JDialog(new JFrame(),"Vista Previa del Reporte", true);
        viewer.setSize(950,700);
        viewer.setLocationRelativeTo(null);
        JRViewer jrv = new JRViewer(print);
        viewer.getContentPane().add(jrv);
        viewer.show();
    }
    
    public static void ReporteCorteCargoSinUsuario(Connection con,String Inicio, String Fin) throws SQLException, JRException {
        Map parametro = new HashMap();
        parametro.put("fechainicio", Inicio);
        parametro.put("fechafin", Fin);
        java.lang.String url = carpeta+"RepCargosSinUsuario.jrxml";
        conn = con;

        JasperReport reportes = JasperCompileManager.compileReport(url);
        JasperPrint print = JasperFillManager.fillReport(reportes, parametro, conn);
        
        //para mostrar el reporte        
        JDialog viewer = new JDialog(new JFrame(),"Vista Previa del Reporte", true);
        viewer.setSize(950,700);
        viewer.setLocationRelativeTo(null);
        JRViewer jrv = new JRViewer(print);
        viewer.getContentPane().add(jrv);
        viewer.show();
    }
    
    public static void ReporteExistencias(Connection con) throws SQLException, JRException {
        Map parametro = new HashMap();
        java.lang.String url = carpeta+"Existencias.jrxml";
        conn = con;

        JasperReport reportes = JasperCompileManager.compileReport(url);
        JasperPrint print = JasperFillManager.fillReport(reportes, parametro, conn);
        
        //para mostrar el reporte
        JDialog viewer = new JDialog(new JFrame(),"Vista Previa del Reporte", true);
        viewer.setSize(950,700);
        viewer.setLocationRelativeTo(null);
        JRViewer jrv = new JRViewer(print);
        viewer.getContentPane().add(jrv);
        viewer.show();
    }
}
