/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sicaf;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import net.sf.jasperreports.engine.JRException;

/**
 *
 * @author Obed
 */
public class App extends javax.swing.JFrame {
    Querys consultas = new Querys();
    conecxionbd conexion = new conecxionbd();
    Report reportes = new Report();
    Sistema sistema = new Sistema();
    
    String fecha="",server="",port="",dbname="",dbusr="",dbpwd="";
    String currentuser = "",reportfolder = "",reportfolderPDF="";
    String currpass = "";
    int fechaconsultas = 0;
    int cargoss = 0;
    int tipocliente = 0;
    
    private TableRowSorter trsfiltro;
    
    Connection con;
    Statement stat;

    /**
     * Creates new form App
     */
    public App() {
        initComponents();
        
        jDateChooser1.setCalendar(Calendar.getInstance());
        jDateChooser2.setCalendar(Calendar.getInstance());
        jDateChooser2.setEnabled(false);
        jPanel16.setVisible(false);
        jLabel62.setVisible(false);
        jLabel74.setVisible(false);
        jLabel75.setVisible(false);
        jPasswordField7.setVisible(false);
        jButton4.setEnabled(false);
        
        LeeConfig();
        fecha = consultas.FechaVentas(Calendar.getInstance());
        con = conexion.ConectaMySQL(server,port,dbname,dbusr,dbpwd);
        Licencia();
        
        cat_emp.setVisible(false);
        this.setJMenuBar(jMenuBar1);
        jMenuBar1.setVisible(false);
        
        timer.start();
        
        checatabla();
        CargaDeptos();
        ComboCatalogo();
        cent_this();
        cent_log();
    }
    
    public void Licencia(){
        ResultSet rs = null;
        String Obtiene[] = new String[2];
        String cadena="",validez="";
        
        try{
            stat = con.createStatement();
            rs = consultas.Estructura(con,"SELECT * FROM servlicencias WHERE status = 1");
            if(rs.next()){
                cadena=sistema.Desencriptar(rs.getString("clave"))+rs.getString("digito");
                Obtiene = sistema.VerificaLicencia(cadena.toUpperCase());
                if(Obtiene[1].equals("1")){
                    JOptionPane.showMessageDialog(null, "formato incorrecto de licencia","error",JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }
                else{
                    validez = Obtiene[0].toString();
                    try {
                        if(checkValidez(validez) == 0){
                            if(checkFormatoValidez(validez) == 0){
                                JOptionPane.showMessageDialog(null, "licencia caducada ...","error",JOptionPane.ERROR_MESSAGE);
                                System.exit(0);
                            }
                            else{
                                jTextField58.setText("Licencia Correcta, vigencia: "+validez);
                            }
                        }
                        else{
                            JOptionPane.showMessageDialog(null, "licencia caducada","error",JOptionPane.ERROR_MESSAGE);
                            System.exit(0);
                        }
                    } catch (ParseException ex) { Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex); }
                }
            }
            else{
                JOptionPane.showMessageDialog(null, "no se encontró licencia activa","error",JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        }catch (Exception e){ JOptionPane.showMessageDialog(null,e); }
    }
    
    public int checkValidez(String fecha) throws ParseException{
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
        Date factual = formato.parse(FechaServer());
        Date fobtiene = formato.parse(fecha);
        int resultado = 0;
        
        if(fobtiene.before(factual) == true) resultado = 1; // licencia caducada
        
        return resultado;
    }
    
    public int checkFormatoValidez(String validez){
        int pasa = 0; // 0=licencia caducada, 1=licencia correcta
        String mes="", dia="", anio="";
        char Array[] = validez.toCharArray();
        
        dia += String.valueOf(Array[8]);
        dia += String.valueOf(Array[9]);
        
        mes += String.valueOf(Array[5]);
        mes += String.valueOf(Array[6]);
        
        anio += String.valueOf(Array[0]);
        anio += String.valueOf(Array[1]);
        anio += String.valueOf(Array[2]);
        anio += String.valueOf(Array[3]);
        
        if((Integer.parseInt(mes) >= 1)&&(Integer.parseInt(mes) <= 12)){
            if((Integer.parseInt(dia) >= 1)&&(Integer.parseInt(dia) <= 31)){
                if(Integer.parseInt(anio) >= 2020){
                    pasa = 1;
                }
            }
        }
        
        return pasa;
    }
    
    public String FechaServer(){
        ResultSet rs = null;
        String cons = "SELECT curdate() AS fecha;";
        
        try{
            stat = con.createStatement();
            rs = consultas.Estructura(con,cons);
            
            if(rs.next()) cons = rs.getString("fecha");
        }catch (Exception e){ JOptionPane.showMessageDialog(null,e); }
        
        return cons;
    }
    
    public void LeeConfig(){
        try{
            Properties p = new Properties();
            p.load(new FileInputStream("Config.ini"));
            //DATOS DE MYSQL
            server = p.getProperty("Server");
            port = p.getProperty("Port");
            dbname = p.getProperty("DataBaseName");
            dbusr = p.getProperty("User");
            dbpwd = sistema.Desencriptar(p.getProperty("Password"));
            reportfolder = p.getProperty("ReportFolder");
            reportfolderPDF = p.getProperty("ReportFolderPDF");
            
            jTextField54.setText(server);
            jTextField55.setText(port);
            jTextField56.setText(dbname);
            jTextField57.setText(dbusr);
            jPasswordField8.setText(dbpwd);
            
        } catch (Exception e) {System.out.println("error al leer el archivo .ini:"+ e.getMessage());}
    }
    
    public void EscribeConfig(){        
        try{
            Properties p = new Properties();
            p.load(new FileInputStream("Config.ini"));
            
            //DATOS DE MYSQL
            p.put("Server", jTextField54.getText());
            p.put("Port", jTextField55.getText());
            p.put("Password", sistema.Encriptar(jPasswordField8.getText()));
            p.put("DataBaseName", jTextField56.getText());
            p.put("User", jTextField57.getText());
                        
            FileOutputStream out = new FileOutputStream("Config.ini");
            p.save(out, "/* properties updated ");
            JOptionPane.showMessageDialog(null, "Datos Guardados");
            LeeConfig();
            
        } catch (Exception e) {System.out.println("error al leer el archivo .ini:"+ e.getMessage());}
    }
    
    Timer timer = new Timer (1000, new ActionListener () 
    { 
        public void actionPerformed(ActionEvent e){
            int hora0=0,minutos0=0,segundos0=0;
            String hora="",minutos="",segundos="";
            Calendar calendario = Calendar.getInstance();
            
            hora0 = calendario.get(Calendar.HOUR_OF_DAY);
            minutos0 = calendario.get(Calendar.MINUTE);
            segundos0 = calendario.get(Calendar.SECOND);
            
            if(hora0 < 10)
                hora = "0"+Integer.toString(hora0);
            else
                hora = Integer.toString(hora0);
            if(minutos0 < 10)
                minutos = "0"+Integer.toString(minutos0);
            else
                minutos = Integer.toString(minutos0);
            if(segundos0 < 10)
                segundos = "0"+Integer.toString(segundos0);
            else
                segundos = Integer.toString(segundos0);
            
            jTextField52.setText(consultas.FechaVentas(calendario));
            jTextField53.setText(hora + ":" + minutos + ":" + segundos);
        }
    });
    
    public void LogIn(String usuario, String contrasenia){
        consultas.Logueo(con, usuario, contrasenia);
    }
    
    public void cent_this(){
        this.pack();
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension ventana = this.getSize();
        this.setLocation(((pantalla.width - ventana.width)/2), ((pantalla.height - ventana.height)/2));
        this.setVisible(true);
    }
    
    public void cent_log(){
        Log.pack();
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension ventana = Log.getSize();
        Log.setLocation(((pantalla.width - ventana.width)/2), ((pantalla.height - ventana.height)/2));
        Log.setVisible(true);
    }
    
    public void cent_venta(){
        Venta.pack();
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension ventana = Venta.getSize();
        Venta.setLocation(((pantalla.width - ventana.width)/2), ((pantalla.height - ventana.height)/2));
        Venta.setVisible(true);
    }
    
    public void cent_pago(){
        Pago.pack();
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension ventana = Pago.getSize();
        Pago.setLocation(((pantalla.width - ventana.width)/2), ((pantalla.height - ventana.height)/2));
        Pago.setVisible(true);
    }
    
    public void cent_inventario(){
        Inventario.pack();
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension ventana = Inventario.getSize();
        Inventario.setLocation(((pantalla.width - ventana.width)/2), ((pantalla.height - ventana.height)/2));
        Inventario.setVisible(true);
    }
    
    public void cent_todosprod(){
        TodosProd.pack();
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension ventana = TodosProd.getSize();
        TodosProd.setLocation(((pantalla.width - ventana.width)/2), ((pantalla.height - ventana.height)/2));
        TodosProd.setVisible(true);
        
        jTextField26.grabFocus();
    }
    
    public void cent_cargo(){
        Cargo.pack();
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension ventana = Cargo.getSize();
        Cargo.setLocation(((pantalla.width - ventana.width)/2), ((pantalla.height - ventana.height)/2));
        Cargo.setVisible(true);
    }
    
    public void cent_reimpresion(){
        Reimpresion.pack();
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension ventana = Reimpresion.getSize();
        Reimpresion.setLocation(((pantalla.width - ventana.width)/2), ((pantalla.height - ventana.height)/2));
        Reimpresion.setVisible(true);
    }
    
    public void cent_usuarios(){
        usuarios.pack();
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension ventana = usuarios.getSize();
        usuarios.setLocation(((pantalla.width - ventana.width)/2), ((pantalla.height - ventana.height)/2));
        usuarios.setVisible(true);
    }
    
    public void cent_catalogos(){
        Catalogoss.pack();
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension ventana = Catalogoss.getSize();
        Catalogoss.setLocation(((pantalla.width - ventana.width)/2), ((pantalla.height - ventana.height)/2));
        Catalogoss.setVisible(true);
    }
    
    public void cent_clientes(){
        Clientes.pack();
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension ventana = Clientes.getSize();
        Clientes.setLocation(((pantalla.width - ventana.width)/2), ((pantalla.height - ventana.height)/2));
        Clientes.setVisible(true);
    }
    
    public void cent_contnota(){
        ContNota.pack();
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension ventana = ContNota.getSize();
        ContNota.setLocation(((pantalla.width - ventana.width)/2), ((pantalla.height - ventana.height)/2));
        ContNota.setVisible(true);
    }
    
    public void cent_editclientes(){
        Edit_Clientes.pack();
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension ventana = Edit_Clientes.getSize();
        Edit_Clientes.setLocation(((pantalla.width - ventana.width)/2), ((pantalla.height - ventana.height)/2));
        Edit_Clientes.setVisible(true);
    }
    
    public void cent_consvendedor(){
        ConsVendedor.pack();
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension ventana = ConsVendedor.getSize();
        ConsVendedor.setLocation(((pantalla.width - ventana.width)/2), ((pantalla.height - ventana.height)/2));
        ConsVendedor.setVisible(true);
    }
    
    public void cent_opcionreporte(){
        OpcionReporte.pack();
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension ventana = OpcionReporte.getSize();
        OpcionReporte.setLocation(((pantalla.width - ventana.width)/2), ((pantalla.height - ventana.height)/2));
        OpcionReporte.setVisible(true);
    }
    
    public void cent_config(){
        Conecta.pack();
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension ventana = Conecta.getSize();
        Conecta.setLocation(((pantalla.width - ventana.width)/2), ((pantalla.height - ventana.height)/2));
        Conecta.setVisible(true);
    }
    
    public void AltaInventario(){
        String consulta = "";
        String clave=jTextField12.getText();
        String descripcion=jTextField13.getText();
        String costo=jTextField14.getText();
        String venta=jTextField15.getText();
        String cant=jTextField16.getText();
        
        consulta = "INSERT INTO inventario(clave,descripcion,costo,venta,cantidad) VALUES("+clave+",'"+descripcion+"','";
        consulta=consulta+costo+"','"+venta+"',"+cant+");";
        
        if(consultas.InsertaActualiza(con, consulta) == 1){
            JOptionPane.showMessageDialog(null,"datos insertados");
            LimpiaAlta();
        }
    }
    
    public void LimpiaAlta(){
        jTextField12.setText("");
        jTextField13.setText("");
        jTextField14.setText("");
        jTextField15.setText("");
        jTextField16.setText("");
        
        NuevaClave();
    }
    
    public void NuevaClave(){
        jTextField12.setText(consultas.getClave(con));
    }
    
    public void NuevoFolio(){
        jTextField3.setText(consultas.getFolio(con));
        jTextField4.setText(fecha);
    }
    
    public void BuscaInventario(String clave){
        ResultSet rs = null;
        DefaultTableModel modelo = (DefaultTableModel)jTable5.getModel();
        int estado = jComboBox1.getSelectedIndex();
        String cons="";
        
        for(int i=modelo.getRowCount()-1;i>=0;i--)
            modelo.removeRow(i);
        
        if(estado == 0)
            cons = "SELECT * FROM inventario WHERE descripcion LIKE '%"+clave+"%' AND activo = 1;";
        else{
            if(estado == 1)
                cons = "SELECT * FROM inventario WHERE descripcion LIKE '%"+clave+"%' AND activo = 0;";
            else
                cons = "SELECT * FROM inventario WHERE descripcion LIKE '%"+clave+"%';";
        }
        
        try{
            stat = con.createStatement();
            rs = consultas.Estructura(con,cons);
            
            while(rs.next()){
                String clv = rs.getString("clave");
                String nombre = rs.getString("descripcion");
                String precio = rs.getString("venta");

                Object []Fila = new Object [3];
                Fila[0]=clv;
                Fila[1]=nombre;
                Fila[2]=consultas.ConvierteMoneda(precio);
                modelo.addRow(Fila);
            }
        }
        catch (Exception e){
            JOptionPane.showMessageDialog(null,"Error obteniendo coincidencias");
        }
    }
    
    public void BuscaInventarioBaja(DefaultTableModel modelo,String clave){
        ResultSet rs = null;
        String cons="SELECT * FROM inventario WHERE descripcion LIKE '%"+clave+"%' AND activo = 1;";
        
        for(int i=modelo.getRowCount()-1;i>=0;i--)
            modelo.removeRow(i);
        
        try{
            stat = con.createStatement();
            rs = consultas.Estructura(con,cons);
            
            while(rs.next()){
                String clv = rs.getString("clave");
                String nombre = rs.getString("descripcion");
                String vta = rs.getString("venta");
                String disp = rs.getString("cantidad");
                
                Object []Fila = new Object [4];
                Fila[0]=clv;
                Fila[1]=nombre;
                Fila[2]=vta;
                Fila[3]=disp;
                modelo.addRow(Fila);
            }
        }
        catch (Exception e){
            JOptionPane.showMessageDialog(null,"Error obteniendo coincidencias");
        }
    }
    
    public void BuscaInventarioCorr(DefaultTableModel modelo,String clave){
        ResultSet rs = null;
        String cons="SELECT clave,descripcion FROM inventario WHERE descripcion LIKE '%"+clave+"%' AND ACTIVO="+jComboBox11.getSelectedIndex()+" ORDER BY descripcion ASC;";
        
        for(int i=modelo.getRowCount()-1;i>=0;i--)
            modelo.removeRow(i);
        
        try{
            stat = con.createStatement();
            rs = consultas.Estructura(con,cons);
            
            while(rs.next()){
                Object []Fila = new Object [2];
                Fila[0]=rs.getString("clave");
                Fila[1]=rs.getString("descripcion");
                modelo.addRow(Fila);
            }
        }
        catch (Exception e){
            JOptionPane.showMessageDialog(null,"Error obteniendo coincidencias");
        }
    }
    
    public void BajaDeInventario(){
        DefaultTableModel modelo = (DefaultTableModel)jTable2.getModel();
        int seleccion = jTable2.getSelectedRow(),statt=0;
        String upd="";
        
        if(seleccion < 0)
            JOptionPane.showMessageDialog(null, "seleccione un producto","error",JOptionPane.ERROR_MESSAGE);
        else{
            upd = "UPDATE inventario SET activo = 0 WHERE clave ='"+modelo.getValueAt(seleccion, 0).toString()+"';";
            statt = consultas.InsertaActualiza(con, upd);
            
            if(statt > 0){
                JOptionPane.showMessageDialog(null, "el producto ha sido dado de baja exitosamente");
                BuscaInventarioBaja(modelo,"");
            }
        }
    }
    
    public void ChecaInventario(String clave){
        ResultSet rs=null;
        DefaultTableModel modelo = (DefaultTableModel)jTable1.getModel();
        int i = 0, cont = 0;
        String nuevacant="";
        
        if(clave.equals(""))
            cent_todosprod();
        
        else{
            try{
               stat = con.createStatement();
               rs=consultas.Estructura(con,"SELECT * FROM inventario WHERE clave = "+clave+";");

               if(rs.next()){
                   if(modelo.getRowCount() > 0){
                       for(i=0;i<modelo.getRowCount();i++){
                           String clavetabla = modelo.getValueAt(i, 2).toString();

                           if(clavetabla.equals(clave)){
                               nuevacant = Integer.toString(Integer.parseInt(modelo.getValueAt(i, 0).toString())+1);
                               modelo.setValueAt(nuevacant, i, 0);
                               cont++;
                           }
                       }
                       if(cont == 0){
                            Object []Fila = new Object [8];
                            Fila[0]="1";
                            Fila[1]=rs.getString("cantidad");
                            Fila[2]=rs.getString("clave");
                            Fila[3]=rs.getString("descripcion");
                            Fila[4]=rs.getString("venta");
                            Fila[5]="0";
                            Fila[6]="0";
                            Fila[7]="0";
                            modelo.addRow(Fila);
                       }
                   }
                   else{
                       if(modelo.getRowCount() == 0){
                           Object []Fila = new Object [8];
                            Fila[0]="1";
                            Fila[1]=rs.getString("cantidad");
                            Fila[2]=rs.getString("clave");
                            Fila[3]=rs.getString("descripcion");
                            Fila[4]=rs.getString("venta");
                            Fila[5]="0";
                            Fila[6]="0";
                            Fila[7]="0";
                            modelo.addRow(Fila);
                       }   
                   }
               }
               else{
                   JOptionPane.showMessageDialog(null, "no existe la clave","error",JOptionPane.ERROR_MESSAGE);
               }
           }catch (Exception e){JOptionPane.showMessageDialog(null, e);}
        }
        checatabla();
    }
    
    public void checatabla(){
        DefaultTableModel modelo = (DefaultTableModel)jTable1.getModel();
        
        if(modelo.getRowCount() > 0){
            jButton2.setEnabled(true);
            jButton10.setEnabled(true);
            jTextField27.setEnabled(true);
        }
        else{
            jButton2.setEnabled(false);
            jButton10.setEnabled(false);
            jTextField27.setEnabled(false);
        }
    }
    
    public void calculaventa(){
        DefaultTableModel modelo = (DefaultTableModel)jTable1.getModel();
        float precio=0,descto=0,cant=0,subtot=0,tot=0,gsub=0,gdesc=0;
        
        for(int i=0;i<modelo.getRowCount();i++){
            int cantidad=Integer.parseInt(modelo.getValueAt(i, 0).toString());
            int disp=Integer.parseInt(modelo.getValueAt(i, 1).toString());
            
            if(cantidad > disp){
                JOptionPane.showMessageDialog(null, "disponibilidad insuficiente; verifique","error",JOptionPane.ERROR_MESSAGE);
                modelo.removeRow(i);
            }
            else{
                precio = Float.parseFloat(modelo.getValueAt(i, 4).toString());
                cant = Float.parseFloat(modelo.getValueAt(i, 0).toString());
                descto = Float.parseFloat(modelo.getValueAt(i, 5).toString());

                subtot = (cant*precio);
                tot = subtot - descto;

                modelo.setValueAt(subtot, i, 6);
                modelo.setValueAt(tot, i, 7);
            }   
        }
        
        for(int i=0;i<modelo.getRowCount();i++){
            gdesc += Float.parseFloat(modelo.getValueAt(i, 5).toString());
            gsub += Float.parseFloat(modelo.getValueAt(i, 6).toString());
        }
        
        jTextField6.setText(Float.toString(gsub));
        jTextField7.setText(Float.toString(gdesc));
        jTextField8.setText(Float.toString(gsub-gdesc));
        
        checatabla();
    }
    
    public void AplicaDescuento(String porcentaje, int tipo){
        DefaultTableModel modelo = (DefaultTableModel)jTable1.getModel();
        int fila = jTable1.getSelectedRow();
        
        if(tipo == 0){
            float porciento = Float.parseFloat(porcentaje);
            
            if((porciento >= 0)&&(porciento <= 100)){
                for(int i=0;i<modelo.getRowCount();i++){
                    float precio = Float.parseFloat(modelo.getValueAt(i, 4).toString());
                    float cantd = Float.parseFloat(modelo.getValueAt(i, 0).toString());
                    float subtot = precio * cantd;
                    subtot = (subtot * (porciento / 100));
                    modelo.setValueAt(subtot, i, 5);
                }
                calculaventa();
            }
            else{
                JOptionPane.showMessageDialog(null, "el porcentaje de descuento debe ser entre 0 y 100","error",JOptionPane.ERROR_MESSAGE);
            }    
        }
        else{
            if(fila < 0)
                JOptionPane.showMessageDialog(null, "seleccione un articulo","error",JOptionPane.ERROR_MESSAGE);
            else{
                float monto = Float.parseFloat(modelo.getValueAt(fila, 4).toString());
                float descto = Float.parseFloat(porcentaje);
                if(descto > monto)
                    JOptionPane.showMessageDialog(null, "el valor monetario es mayor que el monto del producto","error",JOptionPane.ERROR_MESSAGE);
                else{
                    modelo.setValueAt(descto, fila, 5);
                    calculaventa();
                }
            }
        }
    }
    
    public void PagoVenta(int tipo){
        DefaultTableModel modelo = (DefaultTableModel)jTable1.getModel();
        String cliente="",folio="",cantd="",artic="",precio="",descto="",cons="";
        int insercion = 0;
        
        if (tipo == 0){
            if(jTextField27.getText().equals(""))
                jTextField27.setText("0");
            
            if(!jTextField27.getText().equals(""))
                AplicaDescuento(jTextField27.getText(),0);
            if(!jTextField28.getText().equals(""))
                AplicaDescuento(jTextField28.getText(),1);
            
            jTextField9.setText(jTextField8.getText());
            cent_pago();
        }
        else{
            if(tipo == 1){
                cliente = jTextField2.getText().toUpperCase();
                folio = jTextField3.getText();
                if(cliente.equals("")) cliente = "PUBLICO GENERAL";
                cons="INSERT INTO ventascab(cliente,folio,fecha,vendedor,cargo,hora) VALUES('"+cliente+"','"+folio+"','"+fecha+"','"+currentuser+"',"+cargoss+",curtime());";
                
                if(consultas.InsertaActualiza(con, cons) > 0){
                    for(int i=0;i<modelo.getRowCount();i++){
                        cantd = modelo.getValueAt(i, 0).toString();
                        artic = modelo.getValueAt(i, 2).toString();
                        precio = modelo.getValueAt(i, 4).toString();
                        descto = modelo.getValueAt(i, 5).toString();
                        
                        cons = "INSERT INTO ventascont(folio,articulo,precio,descuento,cantidad) VALUES('"+folio+"','"+artic+"','"+precio+"','";
                        cons=cons+descto+"','"+cantd+"');";
                        insercion += consultas.InsertaActualiza(con, cons);
                    }
                    if(insercion > 0){
                        if(cargoss == 0){Impresion_TicketVenta(folio);}
                        else{if(cargoss == 1){/*Impresion_TicketCargo(folio);*/}}
                        
                        LimpiaVenta();
                        cargoss = 0;
                        checatabla();
                    }
                }
            }
        }
    }
    
    public void AgregaCargo(){
        int opc = JOptionPane.showConfirmDialog(null, "¿agregar cargo?","confirme",JOptionPane.YES_NO_OPTION);
        String autor = jTextField36.getText();
        String coment = jTextArea1.getText();
        String insert = "";
        String dep = jLabel75.getText();
        
        if((autor.equals(""))||(coment.equals("")))
            JOptionPane.showMessageDialog(null, "llene todos los campos","error",JOptionPane.ERROR_MESSAGE);
        else{
            if(opc == 0){
                jTextField2.setText(jTextField35.getText());
                insert="INSERT INTO cargos(folio,fecha,autorizacion,depto,comentarios,hora) VALUES("+jTextField3.getText()+",'"+fecha+"','"+autor+"',"+dep+",'"+coment+"',curtime());";
                opc = consultas.InsertaActualiza(con, insert);
                
                if(opc > 0){
                    cargoss = 1;
                    Cargo.dispose();
                    PagoVenta(1);
                }
            }
        }
    }
    
    public void LimpiaVenta(){
        DefaultTableModel modelo = (DefaultTableModel)jTable1.getModel();
        
        for(int i=modelo.getRowCount()-1;i>=0;i--)
            modelo.removeRow(i);
        
        NuevoFolio();
        jTextField2.setText("");
        jTextField27.setText("");
        jTextField6.setText("");
        jTextField7.setText("");
        jTextField8.setText("");
        jTextField10.setText("");
        jTextField11.setText("");
    }
    
    public void AdminCambio(float total,float pago){
        if(pago >= total){
            float cambio = pago-total;
            jTextField11.setText(Float.toString(cambio));
            JOptionPane.showMessageDialog(null, "Cambio: $"+cambio);
            cargoss = 0;
            PagoVenta(1);
            Pago.dispose();
        }
        else{
            JOptionPane.showMessageDialog(null, "pago insuficiente; restan $"+Float.toString(total-pago),"error",JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void LogUsers(){
        ResultSet rs;
        String localusr = jTextField1.getText();
        String localpwd = jPasswordField1.getText();
        String bdpwd="",cons = "SELECT * FROM usuarios WHERE usuario='"+localusr+"' AND activo='1';";
        
        try{
            stat = con.createStatement();
            rs = consultas.Estructura(con,cons);
            
            if(rs.next()){
                bdpwd = rs.getString("password");
                if(bdpwd.equals(localpwd)){
                    currentuser = localusr;
                    jTextField33.setText(currentuser);
                    
                    if(rs.getString("cajero").equals("1")){
                        jTabbedPane2.setEnabledAt(0,true);
                        jTabbedPane2.setEnabledAt(2,true);
                    }
                    else{
                        jTabbedPane2.setEnabledAt(0,false);
                        jTabbedPane2.setEnabledAt(2,false);
                        jTabbedPane2.setSelectedIndex(1);
                    }
                    if(rs.getString("extraecorte").equals("1")){
                        jButton9.setEnabled(true); jButton12.setEnabled(true);
                    }
                    else{
                        jButton9.setEnabled(false); jButton12.setEnabled(false);
                    }
                    
                    
                    jTextField51.setText(rs.getString("nombre"));
                    AdminPrivilegios(rs.getString("privilegios"));
                    jMenuBar1.setVisible(true);
                    getUsuarios();
                    Log.dispose();
                    cent_this();
                    LlenaInventario();
                }
                else
                    JOptionPane.showMessageDialog(null, "contraseña incorrecta","error",JOptionPane.ERROR_MESSAGE);
            }
            else{
                JOptionPane.showMessageDialog(null, "usuario no existe","error",JOptionPane.ERROR_MESSAGE);
            }
        }
        catch (Exception e){
            JOptionPane.showMessageDialog(null,"Error obteniendo coincidencias");
        }
    }
    
    public void AdminPrivilegios(String privilegio){
        if(privilegio.equals("1")){
            inventar.setEnabled(true);
            admin.setEnabled(true);
        }
        else{
            inventar.setEnabled(false);
            admin.setEnabled(false);
        }
    }
    
    public void LlenaInventario(){
        ResultSet rs=null;
        String cons = "SELECT clave,descripcion FROM inventario ORDER BY descripcion ASC";
        DefaultTableModel modelo = (DefaultTableModel)jTable4.getModel();
        
        for(int i=modelo.getRowCount()-1;i>=0;i--)
            modelo.removeRow(i);
        
        try{
            stat = con.createStatement();
            rs = consultas.Estructura(con,cons);
            
            while(rs.next()){
                Object []Fila = new Object [2];
                    Fila[0]=rs.getString("clave");
                    Fila[1]=rs.getString("descripcion");
                modelo.addRow(Fila);
            }
        }catch (Exception e){JOptionPane.showMessageDialog(null,e);}
    }
    
    public void ExtraeVentasUsuario(String usuario){
        ResultSet rs;
        DefaultTableModel modelo = (DefaultTableModel)jTable6.getModel();
        String fechadia = consultas.FechaVentas(jDateChooser1.getCalendar());
        String fechadia2 = consultas.FechaVentas(jDateChooser2.getCalendar());
        String cons = "";
        
        if(fechaconsultas == 0){
            cons = "SELECT ventascab.folio,fecha,hora,vendedor,cliente,SUM((cantidad*precio)-descuento) AS total "+
            "FROM ventascab INNER JOIN ventascont ON ventascab.folio=ventascont.folio WHERE fecha = '"+fechadia+"' "+
            "AND vendedor = '"+usuario+"' AND cargo != 1 GROUP BY ventascab.folio ASC;";
        }
        else{
            cons = "SELECT ventascab.folio,fecha,hora,vendedor,cliente,SUM((cantidad*precio)-descuento) AS total "+
            "FROM ventascab INNER JOIN ventascont ON ventascab.folio=ventascont.folio WHERE fecha BETWEEN '"
            +fechadia+"' AND '"+fechadia2+"' AND vendedor = '"+usuario+"' AND cargo != 1 GROUP BY ventascab.folio ASC;";
        }
        
        for(int i=modelo.getRowCount()-1;i>=0;i--)
            modelo.removeRow(i);
        
        try{
            stat = con.createStatement();
            rs = consultas.Estructura(con,cons);
            
            while(rs.next()){
                Object []Fila = new Object [5];
                Fila[0]=rs.getString("ventascab.folio");
                Fila[1]=rs.getString("fecha")+"  "+rs.getString("hora");
                Fila[2]=rs.getString("vendedor");
                Fila[3]=consultas.ConvierteMoneda(rs.getString("total"));
                Fila[4]=rs.getString("cliente");
                modelo.addRow(Fila);
            }
            CalculaCorte(fechadia,fechadia2,usuario);
            ExtraeCargos(fechadia,fechadia2,usuario);
        }catch (Exception e){JOptionPane.showMessageDialog(null,"Error obteniendo datos");}
    }
    
    /*public String FechaPeriodo(Calendar c1){
        c1.getInstance();
        c1.add(c1.DATE, 1);
        String dia = Integer.toString(c1.get(Calendar.DATE)-1);
        String mes = Integer.toString(c1.get(Calendar.MONTH)+1);
        String annio = Integer.toString(c1.get(Calendar.YEAR));
        
        if(dia.length() < 2)
            dia = "0" + dia;
        if(mes.length() < 2)
            mes = "0" + mes;
        
        return annio+"-"+mes+"-"+dia;
    }*/
    
    public void ExtraeCargos(String fechadia,String fechadia2,String usuario){
        ResultSet rs;
        DefaultTableModel modelo = (DefaultTableModel)jTable8.getModel();
        String cons = "";
        float totalacumulado = 0;
        
        if(fechaconsultas == 0){
            cons = "SELECT ventascab.folio,ventascab.fecha,cargos.hora,vendedor,autorizacion,SUM((cantidad*precio)-descuento) AS total "+
            "FROM ventascab INNER JOIN ventascont ON ventascab.folio=ventascont.folio "+
            "INNER JOIN cargos ON cargos.folio=ventascab.folio WHERE ventascab.fecha = '"+fechadia+"' "+
            "AND vendedor = '"+usuario+"' AND cargo != 0 GROUP BY ventascab.folio ASC;";
        }
        else{
            cons = "SELECT ventascab.folio,ventascab.fecha,cargos.hora,vendedor,autorizacion,SUM((cantidad*precio)-descuento) AS total "+
            "FROM ventascab INNER JOIN ventascont ON ventascab.folio=ventascont.folio "+
            "INNER JOIN cargos ON cargos.folio=ventascab.folio WHERE ventascab.fecha BETWEEN '"
            +fechadia+"' AND '"+fechadia2+"' AND vendedor = '"+usuario+"' AND cargo != 0 GROUP BY ventascab.folio ASC;";
        }
        
        System.out.println(cons);
        for(int i=modelo.getRowCount()-1;i>=0;i--)
            modelo.removeRow(i);
        
        try{
            stat = con.createStatement();
            rs = consultas.Estructura(con,cons);
            
            while(rs.next()){
                totalacumulado += Float.parseFloat(rs.getString("total"));
                Object []Fila = new Object [5];
                Fila[0]=rs.getString("ventascab.folio");
                Fila[1]=rs.getString("ventascab.fecha")+"  "+rs.getString("cargos.hora");
                Fila[2]=rs.getString("vendedor");
                Fila[3]=consultas.ConvierteMoneda(rs.getString("total"));
                Fila[4]=rs.getString("autorizacion");
                modelo.addRow(Fila);
            }
            jTextField42.setText(consultas.ConvierteMoneda(Float.toString(totalacumulado)));
        }catch (Exception e){JOptionPane.showMessageDialog(null,"Error obteniendo datos");}
    }
    
    public void CalculaCorte(String fechadia,String fechadia2,String usuario){
        ResultSet rs;
        String cons="";
        
        if(fechaconsultas == 0){
            cons="SELECT sum(cantidad*precio) as subtotal,sum(descuento) as descu,"+
            "sum((cantidad*precio)-descuento) as total FROM ventascab "+
            "INNER JOIN ventascont ON ventascab.folio=ventascont.folio WHERE fecha = '"+fechadia+"' AND cargo = 0 "+
            "AND vendedor = '"+usuario+"';";;
        }
        else{
            cons="SELECT sum(cantidad*precio) as subtotal,sum(descuento) as descu,"+
            "sum((cantidad*precio)-descuento) as total FROM ventascab "+
            "INNER JOIN ventascont ON ventascab.folio=ventascont.folio WHERE fecha BETWEEN '"+fechadia+"' "+
            "AND '"+fechadia2+"' AND cargo = 0 AND vendedor = '"+usuario+"';";
        }
        
        try{
            stat = con.createStatement();
            rs = consultas.Estructura(con,cons);
            
            if(rs.next()){
                jTextField30.setText(consultas.ConvierteMoneda(rs.getString("subtotal")));
                jTextField31.setText(consultas.ConvierteMoneda(rs.getString("descu")));
                jTextField32.setText(consultas.ConvierteMoneda(rs.getString("total")));
            }
        }catch (Exception e){JOptionPane.showMessageDialog(null,"Error obteniendo datos");}
    }
    
    public void ConsultaNota(DefaultTableModel modelo,String folio){
        ResultSet rs;
        DefaultTableModel modelo2 = (DefaultTableModel)jTable7.getModel();
        String cons="SELECT articulo,precio,descuento,ventascont.cantidad,descripcion FROM ventascont "+
        "INNER JOIN inventario ON ventascont.articulo=inventario.clave WHERE folio = '"+folio+"';";
        
        for(int i=modelo2.getRowCount()-1;i>=0;i--)
            modelo2.removeRow(i);
        
        try{
            stat = con.createStatement();
            rs = consultas.Estructura(con,cons);
            
            while(rs.next()){
                Object []Fila = new Object [5];
                Fila[0]=rs.getString("ventascont.cantidad");
                Fila[1]=rs.getString("articulo");
                Fila[2]=rs.getString("descripcion");
                Fila[3]=consultas.ConvierteMoneda(rs.getString("precio"));
                Fila[4]=consultas.ConvierteMoneda(rs.getString("descuento"));
                modelo2.addRow(Fila);
            }
        }catch (Exception e){JOptionPane.showMessageDialog(null,"Error obteniendo datos");}
    }
    
    public void AjusteCantidades(){
        DefaultTableModel modelo = (DefaultTableModel)jTable3.getModel();
        int seleccion = jTable3.getSelectedRow(),cant=0,cantt=0,resultado=0;
        
        
        if(seleccion < 0)
            JOptionPane.showMessageDialog(null, "seleccione un producto","error",JOptionPane.ERROR_MESSAGE);
        else{
            cant=Integer.parseInt(modelo.getValueAt(seleccion, 3).toString());
            cantt=Integer.parseInt(jTextField19.getText());
            
            if(jRadioButton1.isSelected() == true)
                resultado = cant + cantt;
            else
                resultado = cant - cantt;
            
            jTextField20.setText(Integer.toString(resultado));
        }
    }
    
    public void AjustaInventario(){
        int opc = JOptionPane.showConfirmDialog(null, "¿proceder con el ajuste de cantidades?","confirme",JOptionPane.YES_NO_OPTION);
        DefaultTableModel modelo = (DefaultTableModel)jTable3.getModel();
        String upd="",clave=modelo.getValueAt(jTable3.getSelectedRow(), 0).toString();
        
        
        
        if(opc == 0){
            upd = "UPDATE inventario SET cantidad="+jTextField20.getText()+" WHERE clave="+clave+";";
            opc = consultas.InsertaActualiza(con, upd);
            
            if(opc > 0){
                JOptionPane.showMessageDialog(null, "actualizacion correcta");
                AjusteCantidades();
            }
        }
    }
    
    public void ConsInventario(){
        ResultSet rs=null;
        DefaultTableModel modelo = (DefaultTableModel)jTable4.getModel();
        String cons = "SELECT * FROM inventario WHERE clave ='"+modelo.getValueAt(jTable4.getSelectedRow(), 0).toString()+"';";
        
        try{
            stat = con.createStatement();
            rs = consultas.Estructura(con,cons);
            
            if(rs.next()){
                jTextField21.setText(rs.getString("clave"));
                jTextField22.setText(rs.getString("descripcion"));
                jTextField23.setText(rs.getString("costo"));
                jTextField24.setText(rs.getString("venta"));
                jTextField25.setText(rs.getString("cantidad"));
                jComboBox2.setSelectedIndex(Integer.parseInt(rs.getString("activo")));
            }
        }catch (Exception e){JOptionPane.showMessageDialog(null,"Error obteniendo datos");}
    }
    
    public void CorrigeInventario(){
        String descr=jTextField22.getText();
        String vta=jTextField24.getText();
        String costo=jTextField23.getText();
        String activ=Integer.toString(jComboBox2.getSelectedIndex());
        String upd = "UPDATE inventario SET descripcion='"+descr+"',costo='"+costo+"',venta='"+vta+"',activo="+activ+" WHERE clave='"+jTextField21.getText()+"';";
        
        int stat = consultas.InsertaActualiza(con, upd);
        if(stat > 0){
            JOptionPane.showMessageDialog(null, "actualizacion correcta");
            LimpiaCorreccion();
            LlenaInventario();
        }
    }
    
    public void LimpiaCorreccion(){
        jTextField21.setText("");
        jTextField22.setText("");
        jTextField23.setText("");
        jTextField24.setText("");
        jTextField25.setText("");
        jTextField34.setText("");
    }
    
    public void ReimprimeNota(){
        String folio = jTextField40.getText();
        
        for(int i=0;i<Integer.parseInt(jSpinner1.getValue().toString());i++){
            if(jComboBox3.getSelectedIndex() == 0)
                Impresion_TicketVenta(folio);
            else
                Impresion_TicketCargo(folio);
        }
    }
    
    public void InsertaUsuario(){
        String nombre=jTextField37.getText().toUpperCase();
        String usuario=jTextField38.getText();
        String pwd1=jPasswordField2.getText();
        String pwd2=jPasswordField3.getText();
        String priv=Integer.toString(jComboBox4.getSelectedIndex());
        
        if((nombre.equals(""))||(usuario.equals(""))||(pwd1.equals(""))||(pwd2.equals("")))
            JOptionPane.showMessageDialog(null, "existen campos en blanco","error",JOptionPane.ERROR_MESSAGE);
        else{
            if(!pwd1.equals(pwd2))
                JOptionPane.showMessageDialog(null, "las contraseñas no coinciden","error",JOptionPane.ERROR_MESSAGE);
            else{
                String cons = "INSERT INTO usuarios (nombre,usuario,password,privilegios) VALUES('"+nombre+"','"+usuario+"','"+pwd1+"','"+priv+"');";
                if(consultas.InsertaActualiza(con, cons) > 0){
                    JOptionPane.showMessageDialog(null, "insercion correcta");
                    getUsuarios();
                    LimpiaUser();
                }
            }
        }
    }
    
    public void getUsuarios(){
        ResultSet rs=null;
        String cons = "SELECT usuario FROM usuarios;";
        
        try{
            jComboBox5.removeAllItems();
            
            stat = con.createStatement();
            rs = consultas.Estructura(con,cons);
            
            while(rs.next()){
                jComboBox5.addItem(rs.getString("usuario"));
            }
        }catch (Exception e){JOptionPane.showMessageDialog(null,"Error obteniendo datos");}
    }
    
    public void LimpiaUser(){
        jTextField37.setText("");
        jTextField38.setText("");
        jPasswordField2.setText("");
        jPasswordField3.setText("");
        jComboBox4.setSelectedIndex(0);
    }
    
    public void ActualizaUsuario(int accion){
        if(accion == 0){
            ResultSet rs=null;
            String cons = "SELECT password FROM usuarios WHERE usuario = '"+jComboBox5.getSelectedItem().toString()+"';";

            try{
                stat = con.createStatement();
                rs = consultas.Estructura(con,cons);

                if(rs.next()){
                    if(!rs.getString("password").equals(jPasswordField4.getText()))
                        JOptionPane.showMessageDialog(null, "contraseña actual incorrecta","error",JOptionPane.ERROR_MESSAGE);
                    else{
                        if(!jPasswordField5.getText().equals(jPasswordField6.getText()))
                            JOptionPane.showMessageDialog(null, "la contraseña nueva no coincide","error",JOptionPane.ERROR_MESSAGE);
                        else{
                            cons="UPDATE usuarios SET password = '"+jPasswordField5.getText()+"' WHERE usuario='"+jComboBox5.getSelectedItem().toString()+"';";
                            if(consultas.InsertaActualiza(con, cons) > 0){
                                JOptionPane.showMessageDialog(null, "contraseña actualizada; se cerrará el sistema");
                                System.exit(0);
                            }
                        }
                    }
                }
            }catch (Exception e){JOptionPane.showMessageDialog(null,"Error obteniendo datos");}
        }
        else{
            if(accion == 1){
                String cons = "UPDATE usuarios SET nombre='"+jTextField39.getText()+"',usuario='"+jTextField41.getText()+"',privilegios='"+
                jComboBox7.getSelectedIndex()+"' WHERE usuario='"+jComboBox5.getSelectedItem().toString()+"';";
                
                if(consultas.InsertaActualiza(con, cons) > 0){
                    JOptionPane.showMessageDialog(null, "datos actualizados");
                    getDatosUsuario();
                }
            }
        }
    }
    
    public void getDatosUsuario(){
        ResultSet rs=null;
        String cons = "SELECT * FROM usuarios WHERE usuario = '"+jComboBox5.getSelectedItem().toString()+"';";
        
        try{
            stat = con.createStatement();
            rs = consultas.Estructura(con,cons);
            
            if(rs.next()){
                jTextField39.setText(rs.getString("nombre"));
                jTextField41.setText(rs.getString("usuario"));
                jComboBox7.setSelectedIndex(Integer.parseInt(rs.getString("privilegios")));
            }
        }catch (Exception e){JOptionPane.showMessageDialog(null,"Error obteniendo datos");}
    }
    
    public void BajaUsuario(){
        ResultSet rs=null;
        String cons = "SELECT password FROM usuarios WHERE usuario = '"+jComboBox5.getSelectedItem().toString()+"';";

        try{
            stat = con.createStatement();
            rs = consultas.Estructura(con,cons);

            if(rs.next()){
                if(!rs.getString("password").equals(jPasswordField7.getText()))
                    JOptionPane.showMessageDialog(null, "contraseña de usuario incorrecta","error",JOptionPane.ERROR_MESSAGE);
                else{
                    if(JOptionPane.showConfirmDialog(null, "¿eliminar "+jComboBox5.getSelectedItem().toString()+"?","confirme",JOptionPane.YES_NO_OPTION)==0){
                        if(consultas.InsertaActualiza(con, "DELETE FROM usuarios WHERE usuario = '"+jComboBox5.getSelectedItem().toString()+"';")>0){
                            JOptionPane.showMessageDialog(null, "usuario eliminado; se saldrá del sistema");
                            System.exit(0);
                        }
                    }
                }
            }
        }catch (Exception e){JOptionPane.showMessageDialog(null,"Error obteniendo datos");}
    }
    
    public void ConsFolioDevol(String folio){
        DefaultTableModel modelo2 = (DefaultTableModel)jTable9.getModel();
        ResultSet rs=null;
        String cargo = "SELECT cargo FROM ventascab WHERE folio = "+folio+";";
        String consulta = "SELECT vendedor,fecha,nombre,cargo,articulo,descripcion,precio,descuento,ventascont.cantidad," +
        "(SELECT SUM((precio*ventascont.cantidad)-descuento)) AS total FROM ventascont "+
        "INNER JOIN ventascab ON ventascont.folio=ventascab.folio " +
        "INNER JOIN usuarios ON ventascab.vendedor=usuarios.usuario " +
        "INNER JOIN inventario ON ventascont.articulo=inventario.clave " +
        "WHERE ventascab.folio="+folio+";";
        
        for(int i=modelo2.getRowCount()-1;i>=0;i--)
            modelo2.removeRow(i);
        
        try{
            stat = con.createStatement();
            rs = consultas.Estructura(con,cargo);
            
            if(rs.next()){
                if(rs.getString("cargo").equals("1"))
                    JOptionPane.showMessageDialog(null, "no se puede hacer la devolución de un cargo","error",JOptionPane.ERROR_MESSAGE);
                else{
                    try{
                        stat = con.createStatement();
                        rs = consultas.Estructura(con,consulta);
                        
                        while(rs.next()){
                            Object []Fila = new Object [7];
                            Fila[0]=rs.getString("ventascont.cantidad");
                            Fila[1]=rs.getString("articulo");
                            Fila[2]=rs.getString("descripcion");
                            Fila[3]=rs.getString("precio");
                            Fila[4]=rs.getString("descuento");
                            Fila[5]=(rs.getString("total"));
                            modelo2.addRow(Fila);
                            
                            jTextField44.setText((rs.getString("fecha")));
                            jTextField50.setText((rs.getString("vendedor")));
                        }
                    }catch (Exception e){JOptionPane.showMessageDialog(null,"Error obteniendo datos");}
                }
            }
            else
                JOptionPane.showMessageDialog(null, "el folio no existe","error",JOptionPane.ERROR_MESSAGE);
        }catch (Exception e){JOptionPane.showMessageDialog(null,"Error obteniendo datos");}       
    }
    
    public void CalculaDevol(){
        DefaultTableModel modelo = (DefaultTableModel)jTable9.getModel();
        float cant=0,precio=0,descto=0,cantdev=0,total=0;
        
        for(int i=0;i<modelo.getRowCount();i++){
            cantdev = Float.parseFloat(modelo.getValueAt(i, 6).toString());
            cant = Float.parseFloat(modelo.getValueAt(i, 0).toString());
            
            if(cantdev > cant)
                JOptionPane.showMessageDialog(null,"devolucion no puede exceder cantidad de venta");
            else{
                precio = Float.parseFloat(modelo.getValueAt(i, 3).toString());
                descto = Float.parseFloat(modelo.getValueAt(i, 4).toString());
                total += (cantdev * precio) - descto;
                
            }
        }
        jTextField46.setText(Float.toString(total));
    }
    
    public void AgregaProdNota(){
        DefaultTableModel modelo = (DefaultTableModel)jTable5.getModel();
        ChecaInventario(modelo.getValueAt(jTable5.getSelectedRow(), 0).toString());
        calculaventa();
        jTextField26.setText("");
        TodosProd.dispose();
    }
    
    public void BuscaProd(){
        ChecaInventario(jTextField5.getText());
        calculaventa();
        jTextField5.setText("");
    }
    
    public void BuscaCliente(int tipo){
        DefaultTableModel modelo = (DefaultTableModel)jTable10.getModel();
        ResultSet rs=null;
        String consulta="";
        
        if(tipo == 0){
            consulta = "SELECT nombre,departamento.depto " +
                       "FROM clientes " +
                       "INNER JOIN departamento ON clientes.depto=departamento.id";
        }
        else{
            if(tipo == 1){
                consulta = "SELECT nombre,departamento.depto " +
                           "FROM clientes " +
                           "INNER JOIN departamento ON clientes.depto=departamento.id "+
                           "WHERE nombre LIKE '%"+ jTextField48.getText() +"%' " +
                           "ORDER BY depto ASC;";
            }
        }
        
        for(int i=modelo.getRowCount()-1;i>=0;i--)
            modelo.removeRow(i);
        
        try{
            stat = con.createStatement();
            rs = consultas.Estructura(con,consulta);
            
            while(rs.next()){
                Object []Fila = new Object [2];
                Fila[0]=rs.getString("nombre");
                Fila[1]=rs.getString("departamento.depto");
                modelo.addRow(Fila);
            }
        }catch (Exception e){JOptionPane.showMessageDialog(null,"Error obteniendo datos");}
    }
    
    public void CargaDeptos(){
        ResultSet rs=null;
        String consulta="SELECT depto FROM departamento ORDER BY depto ASC";
        
        try{
            stat = con.createStatement();
            rs = consultas.Estructura(con,consulta);
            
            while(rs.next()){
                jComboBox8.addItem(rs.getString("depto"));
            }
        }catch (Exception e){JOptionPane.showMessageDialog(null,"Error obteniendo departamentos");}
    }
    
    public void AgregaCliente(int tipo){
        String nombre = jTextField45.getText().toUpperCase();
        String departamento = jLabel74.getText();
        String nuevodepto = jTextField47.getText().toUpperCase();
        String cons = "";
        ResultSet rs;
        
        if(tipo == 0)
            cons = "INSERT INTO clientes(nombre,depto) VALUES('"+nombre+"',"+departamento+");";
        else{
            cons = "SELECT depto FROM departamento WHERE depto ='"+nuevodepto+"';";
            try{
                stat = con.createStatement();
                rs = consultas.Estructura(con,cons);

                if(rs.next())
                    JOptionPane.showMessageDialog(null, "el departamento ya existe","error",JOptionPane.ERROR_MESSAGE);
                else
                    cons = "INSERT INTO departamento(depto) VALUES('"+nuevodepto+"');";
            }catch (Exception e){JOptionPane.showMessageDialog(null,"Error obteniendo departamentos");}
        }
        
        if(consultas.InsertaActualiza(con, cons) > 0){
            JOptionPane.showMessageDialog(null, "datos insertados correctamente; se cerrará el sistema");
            System.exit(0);
        }
    }
    
    public String getIDdepto(String depto){
        ResultSet rs=null;
        String consulta="SELECT id FROM departamento WHERE depto = '"+depto+"';";
        String idd="";
        
        try{
            stat = con.createStatement();
            rs = consultas.Estructura(con,consulta);
            
            if(rs.next())
              idd = rs.getString("id");
        }catch (Exception e){JOptionPane.showMessageDialog(null,"Error obteniendo departamentos");}
        return idd;
    }
    
    public void ComboCatalogo(){
        if(jComboBox9.getSelectedIndex() == 0){
            jTextField47.setEnabled(false);
            jTextField45.setEnabled(true);
            jLabel65.setEnabled(true);
            jLabel68.setEnabled(true);
            jLabel69.setEnabled(false);
            jComboBox8.setEnabled(true);
        }
        else{
            jTextField47.setEnabled(true);
            jTextField45.setEnabled(false);
            jLabel65.setEnabled(false);
            jLabel68.setEnabled(false);
            jLabel69.setEnabled(true);
            jComboBox8.setEnabled(false);
        }
    }
    
    public void AgregaClienteNota(){
        ResultSet rs;
        DefaultTableModel modelo = (DefaultTableModel)jTable10.getModel();
        String cliente = modelo.getValueAt(jTable10.getSelectedRow(), 0).toString();
        String depto = modelo.getValueAt(jTable10.getSelectedRow(), 1).toString();
        jLabel75.setText(getIDdepto(depto));
        
        if(tipocliente == 0){
            jTextField2.setText(cliente);
            Clientes.dispose();
            jTextField5.grabFocus();
        }
        else{
            jTextField36.setText(cliente);
            jTextField49.setText(depto);
            Clientes.dispose();
            cent_cargo();
            jTextArea1.grabFocus();
        }
        
    }
    
    public void cargaempleadosporid(){
        ResultSet rs;
        DefaultTableModel modelo = (DefaultTableModel)jTable11.getModel();
        DefaultTableModel modelo2 = (DefaultTableModel)jTable12.getModel();
        
        TableRowSorter<TableModel> elQueOrdena = new TableRowSorter<TableModel>(modelo);
        jTable11.setRowSorter(elQueOrdena);
        
        for(int i=modelo.getRowCount()-1;i>=0;i--)
            modelo.removeRow(i);
        
        for(int i=modelo2.getRowCount()-1;i>=0;i--)
            modelo2.removeRow(i);
        
        try{
            stat = con.createStatement();
            rs = consultas.Estructura(con,"SELECT * FROM clientes ORDER BY nombre ASC");
            
            while(rs.next()){
                Object []Fila = new Object [3];
                Fila[0]=rs.getString("id");
                Fila[1]=rs.getString("nombre");
                Fila[2]=rs.getString("depto");
                modelo.addRow(Fila);
            }
        }catch (Exception e){JOptionPane.showMessageDialog(null,"Error obteniendo departamentos");}
        
        try{
            stat = con.createStatement();
            rs = consultas.Estructura(con,"SELECT * FROM departamento ORDER BY depto ASC");
            
            while(rs.next()){
                Object []Fila = new Object [2];
                Fila[0]=rs.getString("id");
                Fila[1]=rs.getString("depto");
                modelo2.addRow(Fila);
            }
        }catch (Exception e){JOptionPane.showMessageDialog(null,"Error obteniendo departamentos");}
    }
    
    public void ModificaDeptoClientes(){
        DefaultTableModel modelo = (DefaultTableModel)jTable11.getModel();
        String cons = "";
        int cont=0;
        
        for(int i=0;i<modelo.getRowCount();i++){
            cons = "UPDATE clientes SET nombre = '"+modelo.getValueAt(i, 1)+"', depto= "+modelo.getValueAt(i, 2)+" WHERE "+
                   "ID = "+modelo.getValueAt(i, 0)+";";
            cont += consultas.InsertaActualiza(con, cons);
        }
        
        if(cont == modelo.getRowCount())
            JOptionPane.showMessageDialog(null, "Correcto!");
    }
    
    public void filtro() {trsfiltro.setRowFilter(RowFilter.regexFilter(jTextField29.getText(), 1));}
    
    public void ConsultaVendedores(){
        ResultSet rs;
        DefaultTableModel modelo = (DefaultTableModel)jTable13.getModel();
        String finicio = consultas.FechaVentas(jDateChooser3.getCalendar());
        String ffin = consultas.FechaVentas(jDateChooser4.getCalendar());
        String cons = "select distinct vendedor from ventascab where fecha between '"+finicio+"' and '"+ffin+"'";
        
        for(int i=modelo.getRowCount()-1;i>=0;i--)
            modelo.removeRow(i);
        
        try{
            stat = con.createStatement();
            rs = consultas.Estructura(con,cons);
            
            while(rs.next()){
                Object []Fila = new Object [1];
                Fila[0]=rs.getString("vendedor");
                modelo.addRow(Fila);
            }
        }catch (Exception e){JOptionPane.showMessageDialog(null,"Error obteniendo datos");}
    }
    
    public Calendar ConvierteCal(String Fecha) throws ParseException{
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
        cal.setTime(formato.parse(consultas.FechaDateChooser(Fecha)));
        return cal;
    }
    
    public void ReporteCargos(String Report){
        String inicio = consultas.FechaVentas(jDateChooser1.getCalendar());
        String fin = consultas.FechaVentas(jDateChooser2.getCalendar());
        
        if(fechaconsultas == 0)
            Impresion_CorteCargo(inicio,inicio,Report);
        else
            Impresion_CorteCargo(inicio,fin,Report);
    }
    
    public void TabbedClicked(){
        if(jTabbedPane2.getSelectedIndex() == 1){
            try {
                jDateChooser3.setCalendar(ConvierteCal(consultas.FechaVentas(Calendar.getInstance())));
                jDateChooser4.setCalendar(ConvierteCal(consultas.FechaVentas(Calendar.getInstance())));
                ConsultaVendedores();
            } catch (ParseException ex) { Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex); }
            cent_consvendedor();
        }
    }
        
    //-------- IMPRESION REPORTES
    public void Impresion_TicketVenta(String dato){
        try { Report.carpeta = reportfolder;
              Report.carpetaPDF = reportfolderPDF;
              Report.mostrarReporteNota(con, dato); }
        catch (SQLException ex) { Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex); }
        catch (JRException ex) { Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex); }
    }
    
    public void Impresion_TicketCargo(String dato){
        try { Report.carpeta = reportfolder;
              Report.carpetaPDF = reportfolderPDF;
              Report.mostrarReporteCargo(con, dato); }
        catch (SQLException ex) { Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex); }
        catch (JRException ex) { Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex); }
    }
    
    public void Impresion_CorteVenta(String cajero,String inicio,String fin){
        try { Report.carpeta = reportfolder;
              Report.carpetaPDF = reportfolderPDF;
              Report.ReporteCorte(con, cajero, inicio, fin); }
        catch (SQLException ex) { Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex); }
        catch (JRException ex) { Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex); }
    }
    
    public void Impresion_CorteCargo(String inicio,String fin,String Reporte){
        try { Report.carpeta = reportfolder;
              Report.carpetaPDF = reportfolderPDF;
              Report.ReporteCorteCargo(con, inicio, fin, Reporte); }
        catch (SQLException ex) { Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex); }
        catch (JRException ex) { Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex); }
    }
    
    public void Impresion_CorteCargoSinUsuario(String inicio,String fin){
        try { Report.carpeta = reportfolder;
              Report.carpetaPDF = reportfolderPDF;
              Report.ReporteCorteCargoSinUsuario(con,inicio, fin); }
        catch (SQLException ex) { Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex); }
        catch (JRException ex) { Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex); }
    }
    
    public void Impresion_Existencias(){
        try { Report.carpeta = reportfolder;
              Report.carpetaPDF = reportfolderPDF;
              Report.ReporteExistencias(con); }
        catch (SQLException ex) { Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex); }
        catch (JRException ex) { Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex); }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Log = new javax.swing.JDialog();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jPasswordField1 = new javax.swing.JPasswordField();
        Venta = new javax.swing.JDialog();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel7 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jTextField7 = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jTextField8 = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        jTextField27 = new javax.swing.JTextField();
        jLabel31 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jTextField28 = new javax.swing.JTextField();
        jPanel18 = new javax.swing.JPanel();
        jButton20 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTable6 = new javax.swing.JTable();
        jLabel35 = new javax.swing.JLabel();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jTextField33 = new javax.swing.JTextField();
        jScrollPane9 = new javax.swing.JScrollPane();
        jTable8 = new javax.swing.JTable();
        jLabel45 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jLabel38 = new javax.swing.JLabel();
        jTextField30 = new javax.swing.JTextField();
        jLabel37 = new javax.swing.JLabel();
        jTextField31 = new javax.swing.JTextField();
        jLabel39 = new javax.swing.JLabel();
        jTextField32 = new javax.swing.JTextField();
        jPanel11 = new javax.swing.JPanel();
        jLabel51 = new javax.swing.JLabel();
        jTextField42 = new javax.swing.JTextField();
        jButton12 = new javax.swing.JButton();
        jButton19 = new javax.swing.JButton();
        jButton23 = new javax.swing.JButton();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jDateChooser2 = new com.toedter.calendar.JDateChooser();
        jPanel17 = new javax.swing.JPanel();
        jButton17 = new javax.swing.JButton();
        jLabel63 = new javax.swing.JLabel();
        jTextField43 = new javax.swing.JTextField();
        jLabel64 = new javax.swing.JLabel();
        jTextField44 = new javax.swing.JTextField();
        jScrollPane10 = new javax.swing.JScrollPane();
        jTable9 = new javax.swing.JTable();
        jLabel66 = new javax.swing.JLabel();
        jTextField46 = new javax.swing.JTextField();
        jLabel67 = new javax.swing.JLabel();
        jTextField50 = new javax.swing.JTextField();
        Pago = new javax.swing.JDialog();
        jPanel2 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jTextField9 = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jTextField10 = new javax.swing.JTextField();
        jTextField11 = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        Inventario = new javax.swing.JDialog();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jTextField13 = new javax.swing.JTextField();
        jTextField14 = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jTextField15 = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jTextField16 = new javax.swing.JTextField();
        jTextField12 = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jTextField17 = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jButton5 = new javax.swing.JButton();
        jLabel19 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jLabel20 = new javax.swing.JLabel();
        jTextField18 = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        jTextField19 = new javax.swing.JTextField();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jButton6 = new javax.swing.JButton();
        jLabel22 = new javax.swing.JLabel();
        jTextField20 = new javax.swing.JTextField();
        jButton21 = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable4 = new javax.swing.JTable();
        jLabel23 = new javax.swing.JLabel();
        jTextField21 = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        jTextField22 = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        jTextField23 = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        jTextField24 = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        jTextField25 = new javax.swing.JTextField();
        jButton7 = new javax.swing.JButton();
        jLabel40 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox();
        jLabel41 = new javax.swing.JLabel();
        jTextField34 = new javax.swing.JTextField();
        jLabel77 = new javax.swing.JLabel();
        jComboBox11 = new javax.swing.JComboBox();
        buttonGroup1 = new javax.swing.ButtonGroup();
        TodosProd = new javax.swing.JDialog();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTable5 = new javax.swing.JTable();
        jLabel29 = new javax.swing.JLabel();
        jTextField26 = new javax.swing.JTextField();
        jComboBox1 = new javax.swing.JComboBox();
        Cargo = new javax.swing.JDialog();
        jLabel42 = new javax.swing.JLabel();
        jTextField35 = new javax.swing.JTextField();
        jLabel43 = new javax.swing.JLabel();
        jTextField36 = new javax.swing.JTextField();
        jLabel44 = new javax.swing.JLabel();
        jScrollPane8 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jButton11 = new javax.swing.JButton();
        jLabel72 = new javax.swing.JLabel();
        jTextField49 = new javax.swing.JTextField();
        jLabel75 = new javax.swing.JLabel();
        Reimpresion = new javax.swing.JDialog();
        jLabel49 = new javax.swing.JLabel();
        jTextField40 = new javax.swing.JTextField();
        jLabel50 = new javax.swing.JLabel();
        jComboBox3 = new javax.swing.JComboBox();
        jButton13 = new javax.swing.JButton();
        jLabel76 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();
        usuarios = new javax.swing.JDialog();
        jTabbedPane3 = new javax.swing.JTabbedPane();
        jPanel13 = new javax.swing.JPanel();
        jLabel46 = new javax.swing.JLabel();
        jTextField37 = new javax.swing.JTextField();
        jLabel47 = new javax.swing.JLabel();
        jTextField38 = new javax.swing.JTextField();
        jLabel48 = new javax.swing.JLabel();
        jLabel52 = new javax.swing.JLabel();
        jPasswordField2 = new javax.swing.JPasswordField();
        jPasswordField3 = new javax.swing.JPasswordField();
        jLabel53 = new javax.swing.JLabel();
        jComboBox4 = new javax.swing.JComboBox();
        jButton16 = new javax.swing.JButton();
        jPanel14 = new javax.swing.JPanel();
        jLabel54 = new javax.swing.JLabel();
        jComboBox5 = new javax.swing.JComboBox();
        jLabel55 = new javax.swing.JLabel();
        jComboBox6 = new javax.swing.JComboBox();
        jPanel15 = new javax.swing.JPanel();
        jLabel56 = new javax.swing.JLabel();
        jPasswordField4 = new javax.swing.JPasswordField();
        jLabel57 = new javax.swing.JLabel();
        jLabel58 = new javax.swing.JLabel();
        jPasswordField5 = new javax.swing.JPasswordField();
        jPasswordField6 = new javax.swing.JPasswordField();
        jButton14 = new javax.swing.JButton();
        jPanel16 = new javax.swing.JPanel();
        jTextField39 = new javax.swing.JTextField();
        jLabel59 = new javax.swing.JLabel();
        jLabel60 = new javax.swing.JLabel();
        jTextField41 = new javax.swing.JTextField();
        jLabel61 = new javax.swing.JLabel();
        jComboBox7 = new javax.swing.JComboBox();
        jButton15 = new javax.swing.JButton();
        jLabel62 = new javax.swing.JLabel();
        jPasswordField7 = new javax.swing.JPasswordField();
        jPanel10 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        Catalogoss = new javax.swing.JDialog();
        jLabel65 = new javax.swing.JLabel();
        jTextField45 = new javax.swing.JTextField();
        jLabel68 = new javax.swing.JLabel();
        jComboBox8 = new javax.swing.JComboBox();
        jLabel69 = new javax.swing.JLabel();
        jTextField47 = new javax.swing.JTextField();
        jLabel70 = new javax.swing.JLabel();
        jComboBox9 = new javax.swing.JComboBox();
        jButton18 = new javax.swing.JButton();
        jLabel74 = new javax.swing.JLabel();
        Clientes = new javax.swing.JDialog();
        jScrollPane11 = new javax.swing.JScrollPane();
        jTable10 = new javax.swing.JTable();
        jLabel71 = new javax.swing.JLabel();
        jTextField48 = new javax.swing.JTextField();
        jButton4 = new javax.swing.JButton();
        ContNota = new javax.swing.JDialog();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTable7 = new javax.swing.JTable();
        Edit_Clientes = new javax.swing.JDialog();
        jScrollPane12 = new javax.swing.JScrollPane();
        jTable11 = new javax.swing.JTable();
        jScrollPane13 = new javax.swing.JScrollPane();
        jTable12 = new javax.swing.JTable();
        jLabel36 = new javax.swing.JLabel();
        jTextField29 = new javax.swing.JTextField();
        jButton22 = new javax.swing.JButton();
        ConsVendedor = new javax.swing.JDialog();
        jLabel73 = new javax.swing.JLabel();
        jButton24 = new javax.swing.JButton();
        jScrollPane14 = new javax.swing.JScrollPane();
        jTable13 = new javax.swing.JTable();
        jDateChooser3 = new com.toedter.calendar.JDateChooser();
        jDateChooser4 = new com.toedter.calendar.JDateChooser();
        OpcionReporte = new javax.swing.JDialog();
        jLabel78 = new javax.swing.JLabel();
        jComboBox10 = new javax.swing.JComboBox();
        jButton26 = new javax.swing.JButton();
        Conecta = new javax.swing.JDialog();
        jLabel79 = new javax.swing.JLabel();
        jTextField54 = new javax.swing.JTextField();
        jLabel80 = new javax.swing.JLabel();
        jTextField55 = new javax.swing.JTextField();
        jLabel81 = new javax.swing.JLabel();
        jTextField56 = new javax.swing.JTextField();
        jLabel82 = new javax.swing.JLabel();
        jTextField57 = new javax.swing.JTextField();
        jLabel83 = new javax.swing.JLabel();
        jPasswordField8 = new javax.swing.JPasswordField();
        jButton25 = new javax.swing.JButton();
        jLabel28 = new javax.swing.JLabel();
        jTextField51 = new javax.swing.JTextField();
        jTextField52 = new javax.swing.JTextField();
        jTextField53 = new javax.swing.JTextField();
        jTextField58 = new javax.swing.JTextField();
        jMenuBar1 = new javax.swing.JMenuBar();
        inventar = new javax.swing.JMenu();
        movim = new javax.swing.JMenuItem();
        vent = new javax.swing.JMenu();
        nota = new javax.swing.JMenuItem();
        cat_emp = new javax.swing.JMenuItem();
        admin = new javax.swing.JMenu();
        users = new javax.swing.JMenuItem();
        catalogo = new javax.swing.JMenuItem();
        config = new javax.swing.JMenuItem();

        Log.setModal(true);
        Log.setUndecorated(true);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel1.setFont(new java.awt.Font("Comic Sans MS", 0, 36)); // NOI18N
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/usuario.png"))); // NOI18N
        jLabel1.setText("Usuario:");

        jLabel2.setFont(new java.awt.Font("Comic Sans MS", 0, 36)); // NOI18N
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/passwd.png"))); // NOI18N
        jLabel2.setText("Passwd:");

        jTextField1.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        jTextField1.setForeground(new java.awt.Color(153, 153, 255));
        jTextField1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jPasswordField1.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        jPasswordField1.setForeground(new java.awt.Color(153, 153, 255));
        jPasswordField1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPasswordField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPasswordField1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                    .addComponent(jPasswordField1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout LogLayout = new javax.swing.GroupLayout(Log.getContentPane());
        Log.getContentPane().setLayout(LogLayout);
        LogLayout.setHorizontalGroup(
            LogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        LogLayout.setVerticalGroup(
            LogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        Venta.setTitle(".:. Ventas .:.");
        Venta.setModal(true);

        jTabbedPane2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTabbedPane2MouseClicked(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("SubTotal:");

        jTextField6.setEditable(false);
        jTextField6.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("Descuento:");

        jTextField7.setEditable(false);
        jTextField7.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel11.setText("Total:");

        jTextField8.setEditable(false);
        jTextField8.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N

        jTable1.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Cant", "Disp", "Clave", "Descripcion", "Precio", "Descuento", "SubTotal", "Total"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                true, false, false, false, false, true, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTable1KeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTable1KeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(0).setMinWidth(40);
            jTable1.getColumnModel().getColumn(0).setMaxWidth(40);
            jTable1.getColumnModel().getColumn(1).setMinWidth(60);
            jTable1.getColumnModel().getColumn(1).setMaxWidth(60);
            jTable1.getColumnModel().getColumn(2).setMinWidth(50);
            jTable1.getColumnModel().getColumn(2).setMaxWidth(50);
            jTable1.getColumnModel().getColumn(3).setMinWidth(350);
            jTable1.getColumnModel().getColumn(3).setMaxWidth(350);
        }

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel3.setText("Cliente:");

        jTextField2.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel4.setText("Folio:");

        jTextField3.setEditable(false);
        jTextField3.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jTextField3.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel5.setText("Fecha:");

        jTextField4.setEditable(false);
        jTextField4.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jTextField4.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel6.setText("Producto:");

        jTextField5.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jTextField5.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField5ActionPerformed(evt);
            }
        });

        jLabel30.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel30.setText("Descuento General:");

        jTextField27.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jTextField27.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField27.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField27ActionPerformed(evt);
            }
        });

        jLabel31.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel31.setText("%");

        jLabel34.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel34.setText("Descuento Monetario:");

        jTextField28.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jTextField28.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField28.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField28ActionPerformed(evt);
            }
        });

        jPanel18.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jButton20.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jButton20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/nota.png"))); // NOI18N
        jButton20.setText("Nueva");
        jButton20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton20ActionPerformed(evt);
            }
        });

        jButton1.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/buscar.png"))); // NOI18N
        jButton1.setText("Buscar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/pagar.png"))); // NOI18N
        jButton2.setText("Pagar");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton10.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jButton10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/Cargo.png"))); // NOI18N
        jButton10.setText("Cargo");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton10)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton2)
                        .addComponent(jButton10))
                    .addComponent(jButton20))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 171, Short.MAX_VALUE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel30)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField27, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel31)
                                .addGap(21, 21, 21)
                                .addComponent(jLabel34)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField28, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                                        .addGap(22, 22, 22)
                                        .addComponent(jLabel7)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                                    .addGroup(jPanel7Layout.createSequentialGroup()
                                        .addComponent(jLabel10)
                                        .addGap(6, 6, 6)))
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jTextField6)
                                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel3)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel4)
                        .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel5)
                        .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 427, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextField6, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextField7)
                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField8, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel34)
                                .addComponent(jTextField28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel30)
                                .addComponent(jTextField27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel31)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(46, 46, 46))
        );

        jTabbedPane2.addTab("Nota de Venta", jPanel7);

        jLabel32.setText("Cajero:");

        jLabel33.setText("Fecha:");

        jCheckBox1.setText("Consultar por periodo");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        jTable6.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jTable6.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nota", "Fecha", "Usuario", "Monto", "Cliente"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable6MouseClicked(evt);
            }
        });
        jScrollPane6.setViewportView(jTable6);
        if (jTable6.getColumnModel().getColumnCount() > 0) {
            jTable6.getColumnModel().getColumn(0).setMinWidth(80);
            jTable6.getColumnModel().getColumn(0).setMaxWidth(80);
            jTable6.getColumnModel().getColumn(1).setMinWidth(150);
            jTable6.getColumnModel().getColumn(1).setMaxWidth(150);
            jTable6.getColumnModel().getColumn(2).setMinWidth(60);
            jTable6.getColumnModel().getColumn(2).setMaxWidth(60);
            jTable6.getColumnModel().getColumn(3).setMinWidth(70);
            jTable6.getColumnModel().getColumn(3).setMaxWidth(70);
        }

        jLabel35.setText("Notas:");

        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/imprimir.png"))); // NOI18N
        jButton8.setText("Nota");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jButton9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/reporte.png"))); // NOI18N
        jButton9.setText("Ventas");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jTextField33.setEditable(false);
        jTextField33.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jTable8.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jTable8.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nota", "Fecha", "Usuario", "Monto", "Autoriza"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable8MouseClicked(evt);
            }
        });
        jScrollPane9.setViewportView(jTable8);
        if (jTable8.getColumnModel().getColumnCount() > 0) {
            jTable8.getColumnModel().getColumn(0).setMinWidth(80);
            jTable8.getColumnModel().getColumn(0).setMaxWidth(80);
            jTable8.getColumnModel().getColumn(1).setMinWidth(150);
            jTable8.getColumnModel().getColumn(1).setMaxWidth(150);
            jTable8.getColumnModel().getColumn(2).setMinWidth(60);
            jTable8.getColumnModel().getColumn(2).setMaxWidth(60);
            jTable8.getColumnModel().getColumn(3).setMinWidth(70);
            jTable8.getColumnModel().getColumn(3).setMaxWidth(70);
        }

        jLabel45.setText("Historial de Cargos a Hospital:");

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("Ventas"));

        jLabel38.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel38.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel38.setText("SubTotal");

        jTextField30.setEditable(false);
        jTextField30.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jTextField30.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel37.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel37.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel37.setText("Descuento");

        jTextField31.setEditable(false);
        jTextField31.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jTextField31.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel39.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel39.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel39.setText("Total");

        jTextField32.setEditable(false);
        jTextField32.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jTextField32.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel38, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField30, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel37, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField31, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel39, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField32, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel38)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel37)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel39)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField32, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder("Cargos"));

        jLabel51.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel51.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel51.setText("Total");

        jTextField42.setEditable(false);
        jTextField42.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jTextField42.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel51, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextField42, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel51)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField42, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButton12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/reporte cargos.png"))); // NOI18N
        jButton12.setText("Cargos");
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });

        jButton19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/buscar.png"))); // NOI18N
        jButton19.setText("Cons");
        jButton19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton19ActionPerformed(evt);
            }
        });

        jButton23.setText("Consultar Usuarios");
        jButton23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton23ActionPerformed(evt);
            }
        });

        jDateChooser1.setDateFormatString("yyyy-MM-dd");

        jDateChooser2.setDateFormatString("yyyy-MM-dd");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jCheckBox1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton23))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel32)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField33))
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel33)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jButton19, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton12, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jDateChooser2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel35)
                            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jScrollPane9, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jScrollPane6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 519, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel45))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(11, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox1)
                    .addComponent(jButton23))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel32)
                            .addComponent(jTextField33, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel33, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(5, 5, 5)
                        .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton19)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton12)
                        .addGap(345, 345, 345))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabel35)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel45)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );

        jTabbedPane2.addTab("Corte de Caja", jPanel8);

        jButton17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/ok.png"))); // NOI18N
        jButton17.setText("Ejecutar");
        jButton17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton17ActionPerformed(evt);
            }
        });

        jLabel63.setText("Folio:");

        jTextField43.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField43.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField43ActionPerformed(evt);
            }
        });

        jLabel64.setText("Fecha:");

        jTextField44.setEditable(false);
        jTextField44.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jTable9.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "CANT", "CLAVE", "ARTICULO", "PRECIO", "DESC", "TOTAL", "DEVOLUCION"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable9.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTable9KeyReleased(evt);
            }
        });
        jScrollPane10.setViewportView(jTable9);
        if (jTable9.getColumnModel().getColumnCount() > 0) {
            jTable9.getColumnModel().getColumn(0).setMinWidth(50);
            jTable9.getColumnModel().getColumn(0).setMaxWidth(50);
            jTable9.getColumnModel().getColumn(1).setMinWidth(50);
            jTable9.getColumnModel().getColumn(1).setMaxWidth(50);
            jTable9.getColumnModel().getColumn(2).setMinWidth(250);
            jTable9.getColumnModel().getColumn(2).setMaxWidth(250);
            jTable9.getColumnModel().getColumn(6).setMinWidth(100);
            jTable9.getColumnModel().getColumn(6).setMaxWidth(100);
        }

        jLabel66.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel66.setText("TOTAL:");

        jTextField46.setEditable(false);
        jTextField46.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N

        jLabel67.setText("Vendedor:");

        jTextField50.setEditable(false);

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel17Layout.createSequentialGroup()
                        .addComponent(jLabel63)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField43, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel64)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField44, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel67)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField50, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel17Layout.createSequentialGroup()
                        .addComponent(jLabel66)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField46, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton17))
                    .addComponent(jScrollPane10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 770, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(76, Short.MAX_VALUE))
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel63)
                    .addComponent(jTextField43, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel64)
                    .addComponent(jTextField44, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel67)
                    .addComponent(jTextField50, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel66)
                    .addComponent(jTextField46, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton17))
                .addContainerGap(108, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Devolución", jPanel17);

        javax.swing.GroupLayout VentaLayout = new javax.swing.GroupLayout(Venta.getContentPane());
        Venta.getContentPane().setLayout(VentaLayout);
        VentaLayout.setHorizontalGroup(
            VentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(VentaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 861, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        VentaLayout.setVerticalGroup(
            VentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(VentaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 640, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        Pago.setTitle(".:. Pago .:.");
        Pago.setModal(true);
        Pago.setUndecorated(true);

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel8.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        jLabel8.setText("Total:");

        jTextField9.setEditable(false);
        jTextField9.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N

        jLabel9.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        jLabel9.setText("Pagado:");

        jTextField10.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        jTextField10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField10ActionPerformed(evt);
            }
        });

        jTextField11.setEditable(false);
        jTextField11.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N

        jLabel12.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        jLabel12.setText("Cambio:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField9)
                            .addComponent(jTextField10)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField11, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout PagoLayout = new javax.swing.GroupLayout(Pago.getContentPane());
        Pago.getContentPane().setLayout(PagoLayout);
        PagoLayout.setHorizontalGroup(
            PagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PagoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        PagoLayout.setVerticalGroup(
            PagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PagoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        Inventario.setTitle(".:. Control de Inventario .:.");
        Inventario.setModal(true);

        jLabel14.setText("Descripción:");

        jTextField14.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel16.setText("Precio de Venta:");

        jTextField15.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel17.setText("Cantidad:");

        jTextField16.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jTextField12.setEditable(false);
        jTextField12.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel13.setText("Clave:");

        jLabel15.setText("Costo:");

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/ok.png"))); // NOI18N
        jButton3.setText("Agregar");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel15)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField14, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel16)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField15, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel17)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField16, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 236, Short.MAX_VALUE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel14)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField13))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton3)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14)
                    .addComponent(jTextField13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(jTextField14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16)
                    .addComponent(jLabel17)
                    .addComponent(jTextField16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 269, Short.MAX_VALUE)
                .addComponent(jButton3)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Alta", jPanel3);

        jLabel18.setText("Artículo:");

        jTextField17.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField17KeyReleased(evt);
            }
        });

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Clave", "Descripcion", "Precio", "Activo"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(jTable2);
        if (jTable2.getColumnModel().getColumnCount() > 0) {
            jTable2.getColumnModel().getColumn(0).setMinWidth(60);
            jTable2.getColumnModel().getColumn(0).setMaxWidth(60);
            jTable2.getColumnModel().getColumn(1).setMinWidth(250);
            jTable2.getColumnModel().getColumn(1).setMaxWidth(250);
            jTable2.getColumnModel().getColumn(2).setHeaderValue("Precio");
            jTable2.getColumnModel().getColumn(3).setMinWidth(60);
            jTable2.getColumnModel().getColumn(3).setMaxWidth(60);
            jTable2.getColumnModel().getColumn(3).setHeaderValue("Cant");
        }

        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/ok.png"))); // NOI18N
        jButton5.setText("Ejecutar Baja");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jLabel19.setText("(seleccione un artículo para dar de baja)");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addComponent(jLabel18)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jTextField17, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jButton5))
                    .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 452, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 348, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel18)
                            .addComponent(jTextField17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel19)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton5)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Baja", jPanel4);

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Clave", "Descripcion", "Precio", "Cant"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(jTable3);
        if (jTable3.getColumnModel().getColumnCount() > 0) {
            jTable3.getColumnModel().getColumn(0).setMinWidth(60);
            jTable3.getColumnModel().getColumn(0).setMaxWidth(60);
            jTable3.getColumnModel().getColumn(1).setMinWidth(250);
            jTable3.getColumnModel().getColumn(1).setMaxWidth(250);
            jTable3.getColumnModel().getColumn(2).setHeaderValue("Precio");
            jTable3.getColumnModel().getColumn(3).setMinWidth(60);
            jTable3.getColumnModel().getColumn(3).setMaxWidth(60);
            jTable3.getColumnModel().getColumn(3).setHeaderValue("Cant");
        }

        jLabel20.setText("Artículo:");

        jTextField18.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField18KeyReleased(evt);
            }
        });

        jLabel21.setText("Ajuste:");

        jTextField19.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField19.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField19KeyReleased(evt);
            }
        });

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setText("Ajustar Entrada");
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText("Ajustar Salida");
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });

        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/ok.png"))); // NOI18N
        jButton6.setText("Ejecutar Ajuste");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jLabel22.setText("Total:");

        jTextField20.setEditable(false);
        jTextField20.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jButton21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/reporte.png"))); // NOI18N
        jButton21.setText("Existencias");
        jButton21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton21ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel20)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField18))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jRadioButton1)
                            .addComponent(jRadioButton2))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField19, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField20, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 452, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel20)
                            .addComponent(jTextField18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel21)
                            .addComponent(jTextField19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel22)
                            .addComponent(jTextField20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton6))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 348, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Ajuste", jPanel5);

        jTable4.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Clave", "Descripcion"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable4MouseClicked(evt);
            }
        });
        jTable4.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTable4KeyReleased(evt);
            }
        });
        jScrollPane4.setViewportView(jTable4);
        if (jTable4.getColumnModel().getColumnCount() > 0) {
            jTable4.getColumnModel().getColumn(0).setMinWidth(50);
            jTable4.getColumnModel().getColumn(0).setMaxWidth(50);
        }

        jLabel23.setText("Clave:");

        jTextField21.setEditable(false);

        jLabel24.setText("Descripción:");

        jLabel25.setText("Costo:");

        jLabel26.setText("Precio de Venta:");

        jLabel27.setText("Cantidad:");

        jTextField25.setEditable(false);

        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/ok.png"))); // NOI18N
        jButton7.setText("Guardar Cambios");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jLabel40.setText("Estado:");

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Baja", "Activo" }));

        jLabel41.setText("Búsqueda:");

        jTextField34.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField34KeyReleased(evt);
            }
        });

        jLabel77.setText("Mostrar:");

        jComboBox11.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Baja", "Activo" }));
        jComboBox11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox11ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 311, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel24)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField22))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel41)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField34))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel26)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField24, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel25)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField23, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jButton7)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel23)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField21, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel27)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField25, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel40)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel77)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 53, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel23)
                            .addComponent(jTextField21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel27)
                            .addComponent(jTextField25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel24)
                            .addComponent(jTextField22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel26)
                            .addComponent(jTextField24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel25)
                            .addComponent(jTextField23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel77)
                                .addComponent(jComboBox11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel40)
                                .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel41)
                            .addComponent(jTextField34, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton7))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 348, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Correcciones", jPanel6);

        javax.swing.GroupLayout InventarioLayout = new javax.swing.GroupLayout(Inventario.getContentPane());
        Inventario.getContentPane().setLayout(InventarioLayout);
        InventarioLayout.setHorizontalGroup(
            InventarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(InventarioLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );
        InventarioLayout.setVerticalGroup(
            InventarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(InventarioLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );

        TodosProd.setTitle(".:. Catálogo de Productos .:.");
        TodosProd.setModal(true);

        jTable5.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Clave", "Descripcion", "Precio"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable5.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTable5KeyPressed(evt);
            }
        });
        jScrollPane5.setViewportView(jTable5);
        if (jTable5.getColumnModel().getColumnCount() > 0) {
            jTable5.getColumnModel().getColumn(0).setMinWidth(70);
            jTable5.getColumnModel().getColumn(0).setMaxWidth(70);
            jTable5.getColumnModel().getColumn(2).setMinWidth(100);
            jTable5.getColumnModel().getColumn(2).setMaxWidth(100);
        }

        jLabel29.setText("Producto:");

        jTextField26.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField26KeyReleased(evt);
            }
        });

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Activos", "Baja", "Todos" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout TodosProdLayout = new javax.swing.GroupLayout(TodosProd.getContentPane());
        TodosProd.getContentPane().setLayout(TodosProdLayout);
        TodosProdLayout.setHorizontalGroup(
            TodosProdLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TodosProdLayout.createSequentialGroup()
                .addGroup(TodosProdLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(TodosProdLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel29)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField26)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(105, 105, 105))
                    .addGroup(TodosProdLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)))
                .addContainerGap())
        );
        TodosProdLayout.setVerticalGroup(
            TodosProdLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TodosProdLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 402, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(TodosProdLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel29)
                    .addComponent(jTextField26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        Cargo.setTitle(".:. Cargos a Hospital .:.");
        Cargo.setModal(true);

        jLabel42.setText("Nombre:");

        jTextField35.setEditable(false);
        jTextField35.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField35.setText("HOSPITAL DEL SURESTE A.C.");

        jLabel43.setText("Autorización:");

        jTextField36.setEditable(false);
        jTextField36.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel44.setText("Comentarios:");

        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jScrollPane8.setViewportView(jTextArea1);

        jButton11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/ok.png"))); // NOI18N
        jButton11.setText("aceptar");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        jLabel72.setText("Depto:");

        jTextField49.setEditable(false);
        jTextField49.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel75.setText("jLabel75");

        javax.swing.GroupLayout CargoLayout = new javax.swing.GroupLayout(Cargo.getContentPane());
        Cargo.getContentPane().setLayout(CargoLayout);
        CargoLayout.setHorizontalGroup(
            CargoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CargoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(CargoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(CargoLayout.createSequentialGroup()
                        .addComponent(jLabel42)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField35))
                    .addGroup(CargoLayout.createSequentialGroup()
                        .addComponent(jLabel43)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField36))
                    .addGroup(CargoLayout.createSequentialGroup()
                        .addComponent(jLabel72)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField49))
                    .addGroup(CargoLayout.createSequentialGroup()
                        .addComponent(jLabel44)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(CargoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(CargoLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jButton11))
                            .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 312, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, CargoLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel75)))
                .addContainerGap())
        );
        CargoLayout.setVerticalGroup(
            CargoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CargoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(CargoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel42)
                    .addComponent(jTextField35, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(CargoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel43)
                    .addComponent(jTextField36, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(CargoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel72)
                    .addComponent(jTextField49, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(CargoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel44)
                    .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel75)
                .addContainerGap())
        );

        Reimpresion.setTitle(".:. Reimpresión de Notas .:.");
        Reimpresion.setModal(true);

        jLabel49.setText("Folio:");

        jLabel50.setText("Tipo:");

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Venta", "Cargo" }));

        jButton13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/imprimir.png"))); // NOI18N
        jButton13.setText("Imprimir");
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });

        jLabel76.setText("Copias:");

        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(1, 1, 5, 1));

        javax.swing.GroupLayout ReimpresionLayout = new javax.swing.GroupLayout(Reimpresion.getContentPane());
        Reimpresion.getContentPane().setLayout(ReimpresionLayout);
        ReimpresionLayout.setHorizontalGroup(
            ReimpresionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ReimpresionLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel49)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField40, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel50)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel76)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton13)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        ReimpresionLayout.setVerticalGroup(
            ReimpresionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ReimpresionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ReimpresionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel49)
                    .addComponent(jTextField40, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel50)
                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton13)
                    .addComponent(jLabel76)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        usuarios.setTitle(".:. Administración de Usuarios .:.");
        usuarios.setModal(true);

        jLabel46.setText("Nombre:");

        jLabel47.setText("Nombre de Usuario:");

        jLabel48.setText("Contraseña:");

        jLabel52.setText("Confirmar Contraseña:");

        jLabel53.setText("Tipo de Usuario:");

        jComboBox4.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Cajero", "Administrador" }));

        jButton16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/ok.png"))); // NOI18N
        jButton16.setText("Aceptar");
        jButton16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton16ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(jLabel46)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField37))
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel48)
                            .addComponent(jLabel47)
                            .addComponent(jLabel52)
                            .addComponent(jLabel53))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jTextField38)
                                .addComponent(jPasswordField2, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
                                .addComponent(jPasswordField3))
                            .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 311, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel13Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton16)))
                .addContainerGap())
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel46)
                    .addComponent(jTextField37, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel47)
                    .addComponent(jTextField38, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel48, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPasswordField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel52, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPasswordField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel53)
                    .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 145, Short.MAX_VALUE)
                .addComponent(jButton16)
                .addContainerGap())
        );

        jTabbedPane3.addTab("Alta", jPanel13);

        jLabel54.setText("Seleccione Usuario:");

        jComboBox5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox5ActionPerformed(evt);
            }
        });

        jLabel55.setText("Tarea:");

        jComboBox6.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Cambiar Contraseña", "Dar de Baja", "Actualizar Datos" }));
        jComboBox6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox6ActionPerformed(evt);
            }
        });

        jPanel15.setBorder(javax.swing.BorderFactory.createTitledBorder("Cambio de Contraseña"));

        jLabel56.setText("Contraseña Anterior:");

        jLabel57.setText("Contraseña Nueva:");

        jLabel58.setText("Confirme Contraseña:");

        jButton14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/ok.png"))); // NOI18N
        jButton14.setText("Aceptar");
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton14)
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel57)
                            .addComponent(jLabel56)
                            .addComponent(jLabel58))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPasswordField4, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPasswordField5, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPasswordField6, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel56)
                    .addComponent(jPasswordField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel57)
                    .addComponent(jPasswordField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel58)
                    .addComponent(jPasswordField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton14)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel16.setBorder(javax.swing.BorderFactory.createTitledBorder("Actualización de Datos"));

        jLabel59.setText("Nombre:");

        jLabel60.setText("Nombre de Usuario:");

        jLabel61.setText("Tipo de Usuario:");

        jComboBox7.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Cajero", "Administrador" }));

        jButton15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/ok.png"))); // NOI18N
        jButton15.setText("Aceptar");
        jButton15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton15ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel16Layout.createSequentialGroup()
                        .addComponent(jLabel59)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField39))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel16Layout.createSequentialGroup()
                        .addComponent(jLabel60)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField41, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel61)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 70, Short.MAX_VALUE)
                        .addComponent(jButton15)))
                .addContainerGap())
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel59)
                    .addComponent(jTextField39, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton15)
                    .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel61)
                        .addComponent(jComboBox7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel60)
                        .addComponent(jTextField41, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        jLabel62.setText("Contraseña de Usuario:");

        jPasswordField7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPasswordField7ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel14Layout.createSequentialGroup()
                                .addComponent(jLabel54)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel55)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel14Layout.createSequentialGroup()
                                .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel62)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPasswordField7, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel54)
                    .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel55)
                    .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel62)
                        .addComponent(jPasswordField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane3.addTab("Actualización", jPanel14);

        javax.swing.GroupLayout usuariosLayout = new javax.swing.GroupLayout(usuarios.getContentPane());
        usuarios.getContentPane().setLayout(usuariosLayout);
        usuariosLayout.setHorizontalGroup(
            usuariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(usuariosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane3)
                .addContainerGap())
        );
        usuariosLayout.setVerticalGroup(
            usuariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(usuariosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 352, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        Catalogoss.setTitle(".:. Panel de Catálogos .:.");
        Catalogoss.setModal(true);

        jLabel65.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel65.setText("Nombre:");

        jLabel68.setText("Departamento:");

        jComboBox8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox8ActionPerformed(evt);
            }
        });

        jLabel69.setText("Añadir Depto:");

        jLabel70.setText("Tipo:");

        jComboBox9.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Cliente", "Departamento" }));
        jComboBox9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox9ActionPerformed(evt);
            }
        });

        jButton18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/ok.png"))); // NOI18N
        jButton18.setText("OK");
        jButton18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton18ActionPerformed(evt);
            }
        });

        jLabel74.setText("jLabel74");

        javax.swing.GroupLayout CatalogossLayout = new javax.swing.GroupLayout(Catalogoss.getContentPane());
        Catalogoss.getContentPane().setLayout(CatalogossLayout);
        CatalogossLayout.setHorizontalGroup(
            CatalogossLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CatalogossLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(CatalogossLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel65, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel68)
                    .addComponent(jLabel69))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(CatalogossLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextField45)
                    .addComponent(jTextField47, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                    .addGroup(CatalogossLayout.createSequentialGroup()
                        .addComponent(jComboBox8, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel74)))
                .addGap(58, 58, 58)
                .addComponent(jLabel70)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(CatalogossLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBox9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton18))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        CatalogossLayout.setVerticalGroup(
            CatalogossLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CatalogossLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(CatalogossLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel65)
                    .addComponent(jTextField45, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel70)
                    .addComponent(jComboBox9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(CatalogossLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel68)
                    .addComponent(jComboBox8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel74))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(CatalogossLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel69)
                    .addComponent(jTextField47, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton18))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        Clientes.setTitle(".:. Catálogo de Clientes .:.");
        Clientes.setModal(true);

        jTable10.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Jefe", "Departamento"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable10.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTable10KeyPressed(evt);
            }
        });
        jScrollPane11.setViewportView(jTable10);
        if (jTable10.getColumnModel().getColumnCount() > 0) {
            jTable10.getColumnModel().getColumn(1).setMinWidth(200);
            jTable10.getColumnModel().getColumn(1).setMaxWidth(200);
        }

        jLabel71.setText("Buscar:");

        jTextField48.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField48KeyReleased(evt);
            }
        });

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/nuevo.png"))); // NOI18N
        jButton4.setText("Nuevo");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout ClientesLayout = new javax.swing.GroupLayout(Clientes.getContentPane());
        Clientes.getContentPane().setLayout(ClientesLayout);
        ClientesLayout.setHorizontalGroup(
            ClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ClientesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane11, javax.swing.GroupLayout.DEFAULT_SIZE, 559, Short.MAX_VALUE)
                    .addGroup(ClientesLayout.createSequentialGroup()
                        .addComponent(jLabel71)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField48)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4)))
                .addContainerGap())
        );
        ClientesLayout.setVerticalGroup(
            ClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ClientesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel71)
                    .addComponent(jTextField48, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        ContNota.setTitle(".:. Contenido de Nota .:.");
        ContNota.setModal(true);

        jTable7.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jTable7.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Cant", "Articulo", "Descripcion", "Precio", "Descuento"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane7.setViewportView(jTable7);
        if (jTable7.getColumnModel().getColumnCount() > 0) {
            jTable7.getColumnModel().getColumn(0).setMinWidth(40);
            jTable7.getColumnModel().getColumn(0).setMaxWidth(40);
            jTable7.getColumnModel().getColumn(1).setMinWidth(50);
            jTable7.getColumnModel().getColumn(1).setMaxWidth(50);
            jTable7.getColumnModel().getColumn(3).setMinWidth(100);
            jTable7.getColumnModel().getColumn(3).setMaxWidth(100);
            jTable7.getColumnModel().getColumn(4).setMinWidth(100);
            jTable7.getColumnModel().getColumn(4).setMaxWidth(100);
        }

        javax.swing.GroupLayout ContNotaLayout = new javax.swing.GroupLayout(ContNota.getContentPane());
        ContNota.getContentPane().setLayout(ContNotaLayout);
        ContNotaLayout.setHorizontalGroup(
            ContNotaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ContNotaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 801, Short.MAX_VALUE)
                .addContainerGap())
        );
        ContNotaLayout.setVerticalGroup(
            ContNotaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ContNotaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        Edit_Clientes.setTitle(".:. Catálogo de Empleados .:.");

        jTable11.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "EMPLEADO", "DEPTO"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, true, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane12.setViewportView(jTable11);
        if (jTable11.getColumnModel().getColumnCount() > 0) {
            jTable11.getColumnModel().getColumn(0).setMinWidth(30);
            jTable11.getColumnModel().getColumn(0).setMaxWidth(30);
            jTable11.getColumnModel().getColumn(2).setMinWidth(50);
            jTable11.getColumnModel().getColumn(2).setMaxWidth(50);
        }

        jTable12.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "DESCRIPCION"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane13.setViewportView(jTable12);
        if (jTable12.getColumnModel().getColumnCount() > 0) {
            jTable12.getColumnModel().getColumn(0).setMinWidth(50);
            jTable12.getColumnModel().getColumn(0).setMaxWidth(50);
        }

        jLabel36.setText("Buscar:");

        jTextField29.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField29KeyReleased(evt);
            }
        });

        jButton22.setText("Actualizar");
        jButton22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton22ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout Edit_ClientesLayout = new javax.swing.GroupLayout(Edit_Clientes.getContentPane());
        Edit_Clientes.getContentPane().setLayout(Edit_ClientesLayout);
        Edit_ClientesLayout.setHorizontalGroup(
            Edit_ClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Edit_ClientesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(Edit_ClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(Edit_ClientesLayout.createSequentialGroup()
                        .addComponent(jLabel36)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField29))
                    .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 402, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(Edit_ClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton22))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        Edit_ClientesLayout.setVerticalGroup(
            Edit_ClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Edit_ClientesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(Edit_ClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(Edit_ClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel36)
                    .addComponent(jTextField29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton22))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        ConsVendedor.setTitle(".:. Consultar Usuarios Vendedores .:.");
        ConsVendedor.setModal(true);

        jLabel73.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel73.setText("Periodo:");

        jButton24.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jButton24.setText("OK");
        jButton24.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton24ActionPerformed(evt);
            }
        });

        jTable13.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jTable13.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "USUARIO"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable13.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable13MouseClicked(evt);
            }
        });
        jScrollPane14.setViewportView(jTable13);

        jDateChooser3.setDateFormatString("yyyy-MM-dd");

        jDateChooser4.setDateFormatString("yyyy-MM-dd");

        javax.swing.GroupLayout ConsVendedorLayout = new javax.swing.GroupLayout(ConsVendedor.getContentPane());
        ConsVendedor.getContentPane().setLayout(ConsVendedorLayout);
        ConsVendedorLayout.setHorizontalGroup(
            ConsVendedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ConsVendedorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ConsVendedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel73)
                    .addGroup(ConsVendedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(ConsVendedorLayout.createSequentialGroup()
                            .addGroup(ConsVendedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jDateChooser4, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jDateChooser3, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(25, 25, 25)
                            .addComponent(jButton24))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        ConsVendedorLayout.setVerticalGroup(
            ConsVendedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ConsVendedorLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel73)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ConsVendedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ConsVendedorLayout.createSequentialGroup()
                        .addComponent(jDateChooser3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jDateChooser4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(ConsVendedorLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton24)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        OpcionReporte.setTitle(".:. Tipo de Reporte .:.");
        OpcionReporte.setModal(true);

        jLabel78.setText("Tipo de Reporte:");

        jComboBox10.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Sencillo", "Detallado", "Administración" }));

        jButton26.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/reporte cargos.png"))); // NOI18N
        jButton26.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton26ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout OpcionReporteLayout = new javax.swing.GroupLayout(OpcionReporte.getContentPane());
        OpcionReporte.getContentPane().setLayout(OpcionReporteLayout);
        OpcionReporteLayout.setHorizontalGroup(
            OpcionReporteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(OpcionReporteLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(OpcionReporteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton26)
                    .addGroup(OpcionReporteLayout.createSequentialGroup()
                        .addComponent(jLabel78)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        OpcionReporteLayout.setVerticalGroup(
            OpcionReporteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(OpcionReporteLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(OpcionReporteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel78)
                    .addComponent(jComboBox10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton26)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        Conecta.setTitle(".:. Parámetros de Conexión .:.");
        Conecta.setModal(true);

        jLabel79.setText("Servidor:");

        jTextField54.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel80.setText("Puerto:");

        jTextField55.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel81.setText("Base de Datos:");

        jTextField56.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel82.setText("Usuario:");

        jTextField57.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel83.setText("Contraseña:");

        jPasswordField8.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jButton25.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/ok.png"))); // NOI18N
        jButton25.setText("Guardar");
        jButton25.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton25ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout ConectaLayout = new javax.swing.GroupLayout(Conecta.getContentPane());
        Conecta.getContentPane().setLayout(ConectaLayout);
        ConectaLayout.setHorizontalGroup(
            ConectaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ConectaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ConectaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel83)
                    .addComponent(jLabel82)
                    .addComponent(jLabel81)
                    .addComponent(jLabel80)
                    .addComponent(jLabel79))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ConectaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextField54)
                    .addComponent(jTextField55, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField56)
                    .addComponent(jTextField57)
                    .addComponent(jPasswordField8, javax.swing.GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ConectaLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton25)
                .addContainerGap())
        );
        ConectaLayout.setVerticalGroup(
            ConectaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ConectaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ConectaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel79)
                    .addComponent(jTextField54, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ConectaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel80)
                    .addComponent(jTextField55, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ConectaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel81)
                    .addComponent(jTextField56, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ConectaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel82)
                    .addComponent(jTextField57, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ConectaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel83)
                    .addComponent(jPasswordField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton25)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(".:. SICAF: Sistema de Cafetería Hospital del Sureste A.C. .:.");

        jLabel28.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/SICAF Diseño.png"))); // NOI18N

        jTextField51.setEditable(false);
        jTextField51.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField51.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField51MouseClicked(evt);
            }
        });

        jTextField52.setEditable(false);
        jTextField52.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jTextField53.setEditable(false);
        jTextField53.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jTextField58.setEditable(false);
        jTextField58.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField58.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField58MouseClicked(evt);
            }
        });

        inventar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/inventario.png"))); // NOI18N
        inventar.setText("Inventario");
        inventar.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N

        movim.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        movim.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/movimientos.png"))); // NOI18N
        movim.setText("Movimientos de Inventario");
        movim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                movimActionPerformed(evt);
            }
        });
        inventar.add(movim);

        jMenuBar1.add(inventar);

        vent.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/ventas.png"))); // NOI18N
        vent.setText("Ventas");
        vent.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N

        nota.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        nota.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/nota.png"))); // NOI18N
        nota.setText("Administrador de Ventas");
        nota.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                notaActionPerformed(evt);
            }
        });
        vent.add(nota);

        cat_emp.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cat_emp.setText("Catálogo de Empleados");
        cat_emp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cat_empActionPerformed(evt);
            }
        });
        vent.add(cat_emp);

        jMenuBar1.add(vent);

        admin.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/administrador.png"))); // NOI18N
        admin.setText("Administrador");
        admin.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N

        users.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        users.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/usuarios.png"))); // NOI18N
        users.setText("Usuarios");
        users.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usersActionPerformed(evt);
            }
        });
        admin.add(users);

        catalogo.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        catalogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/catalogos.png"))); // NOI18N
        catalogo.setText("Catalogos");
        catalogo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                catalogoActionPerformed(evt);
            }
        });
        admin.add(catalogo);

        config.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        config.setText("Configuración");
        config.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configActionPerformed(evt);
            }
        });
        admin.add(config);

        jMenuBar1.add(admin);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel28)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTextField51, javax.swing.GroupLayout.PREFERRED_SIZE, 321, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField52, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField53, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField58, javax.swing.GroupLayout.PREFERRED_SIZE, 321, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel28)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField51, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField52, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField53, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField58, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void movimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_movimActionPerformed
        // TODO add your handling code here:
        NuevaClave();
        cent_inventario();
    }//GEN-LAST:event_movimActionPerformed

    private void notaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_notaActionPerformed
        // TODO add your handling code here:
        if(jTabbedPane2.isEnabledAt(0) == true) NuevoFolio();
        else TabbedClicked();
        
        cent_venta();
    }//GEN-LAST:event_notaActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        if((jTextField13.getText().equals(""))||(jTextField14.getText().equals(""))||
           (jTextField15.getText().equals(""))||(jTextField16.getText().equals("")))
            JOptionPane.showMessageDialog(null, "llene todos los campos", "error", JOptionPane.ERROR_MESSAGE);
        else{
            AltaInventario();
            LlenaInventario();
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jTextField5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField5ActionPerformed
        // TODO add your handling code here:
        BuscaProd();
    }//GEN-LAST:event_jTextField5ActionPerformed

    private void jTextField26KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField26KeyReleased
        // TODO add your handling code here:
        BuscaInventario(jTextField26.getText());
    }//GEN-LAST:event_jTextField26KeyReleased

    private void jTable1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTable1KeyReleased
        // TODO add your handling code here:
        if (evt.getKeyCode() == KeyEvent.VK_ESCAPE){
            DefaultTableModel modelo = (DefaultTableModel)jTable1.getModel();
            modelo.removeRow(jTable1.getSelectedRow());
        }
        calculaventa();
        jTextField5.grabFocus();
    }//GEN-LAST:event_jTable1KeyReleased

    private void jTextField27ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField27ActionPerformed
        // TODO add your handling code here:
        AplicaDescuento(jTextField27.getText(),0);
    }//GEN-LAST:event_jTextField27ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        PagoVenta(0);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jTextField10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField10ActionPerformed
        // TODO add your handling code here:
        AdminCambio(Float.parseFloat(jTextField9.getText()),Float.parseFloat(jTextField10.getText()));
    }//GEN-LAST:event_jTextField10ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
        LogUsers();
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jPasswordField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPasswordField1ActionPerformed
        // TODO add your handling code here:
        LogUsers();
    }//GEN-LAST:event_jPasswordField1ActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        // TODO add your handling code here:
        if(jCheckBox1.isSelected() == true){
            jDateChooser2.setEnabled(true);
            fechaconsultas = 1;
        }
        else{
            jDateChooser2.setEnabled(false);
            fechaconsultas = 0;
        }
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
        cent_reimpresion();
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jTable6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable6MouseClicked
        // TODO add your handling code here:
        if (evt.getClickCount() == 2) {
            DefaultTableModel modelo = (DefaultTableModel)jTable6.getModel();
            String folio = modelo.getValueAt(jTable6.getSelectedRow(), 0).toString();
            ConsultaNota(modelo,folio);
            cent_contnota();
        }
    }//GEN-LAST:event_jTable6MouseClicked

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        // TODO add your handling code here:
        String cajero = jTextField33.getText();
        String inicio = consultas.FechaVentas(jDateChooser1.getCalendar());
        String fin = consultas.FechaVentas(jDateChooser2.getCalendar());
        
        if(fechaconsultas == 0)
            Impresion_CorteVenta(cajero,inicio,inicio);
        else
            Impresion_CorteVenta(cajero,inicio,fin);
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
        BuscaInventario(jTextField26.getText());
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jTextField17KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField17KeyReleased
        // TODO add your handling code here:
        DefaultTableModel modelo = (DefaultTableModel)jTable2.getModel();
        BuscaInventarioBaja(modelo,jTextField17.getText());
    }//GEN-LAST:event_jTextField17KeyReleased

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        BajaDeInventario();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        // TODO add your handling code here:
        AjusteCantidades();
    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void jTextField18KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField18KeyReleased
        // TODO add your handling code here:
        DefaultTableModel modelo = (DefaultTableModel)jTable3.getModel();
        BuscaInventarioBaja(modelo,jTextField18.getText());
    }//GEN-LAST:event_jTextField18KeyReleased

    private void jTextField19KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField19KeyReleased
        // TODO add your handling code here:
        AjusteCantidades();
    }//GEN-LAST:event_jTextField19KeyReleased

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        // TODO add your handling code here:
        AjusteCantidades();
    }//GEN-LAST:event_jRadioButton2ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
        AjusteCantidades();
        AjustaInventario();
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        // TODO add your handling code here:
        AgregaCargo();
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        // TODO add your handling code here:
        tipocliente = 1;
        BuscaCliente(0);
        cent_clientes();
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jTable8MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable8MouseClicked
        // TODO add your handling code here:
        if (evt.getClickCount() == 2) {
            DefaultTableModel modelo = (DefaultTableModel)jTable8.getModel();
            String folio = modelo.getValueAt(jTable8.getSelectedRow(), 0).toString();
            ConsultaNota(modelo,folio);
            cent_contnota();
        }
    }//GEN-LAST:event_jTable8MouseClicked

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        // TODO add your handling code here:
        cent_opcionreporte();
    }//GEN-LAST:event_jButton12ActionPerformed

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        // TODO add your handling code here:
        ReimprimeNota();
    }//GEN-LAST:event_jButton13ActionPerformed

    private void jTable4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable4MouseClicked
        // TODO add your handling code here:
        ConsInventario();
    }//GEN-LAST:event_jTable4MouseClicked

    private void jTable4KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTable4KeyReleased
        // TODO add your handling code here:
        ConsInventario();
    }//GEN-LAST:event_jTable4KeyReleased

    private void jTextField34KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField34KeyReleased
        // TODO add your handling code here:
        DefaultTableModel modelo = (DefaultTableModel)jTable4.getModel();
        BuscaInventarioCorr(modelo,jTextField34.getText());
    }//GEN-LAST:event_jTextField34KeyReleased

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // TODO add your handling code here:
        CorrigeInventario();
    }//GEN-LAST:event_jButton7ActionPerformed

    private void usersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usersActionPerformed
        // TODO add your handling code here:
        cent_usuarios();
    }//GEN-LAST:event_usersActionPerformed

    private void jComboBox6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox6ActionPerformed
        // TODO add your handling code here:
        if(jComboBox6.getSelectedIndex() == 0){
            jPanel15.setVisible(true);
            jPanel16.setVisible(false);
            jLabel62.setVisible(false);
            jPasswordField7.setVisible(false);
        }
        else{
            if(jComboBox6.getSelectedIndex() == 1){
                jPanel15.setVisible(false);
                jPanel16.setVisible(false);
                jLabel62.setVisible(true);
                jPasswordField7.setVisible(true);
            }
            else{
                if(jComboBox6.getSelectedIndex() == 2){
                    jPanel15.setVisible(false);
                    jPanel16.setVisible(true);
                    jLabel62.setVisible(false);
                    jPasswordField7.setVisible(false);
                }
            }
        }
        getDatosUsuario();
    }//GEN-LAST:event_jComboBox6ActionPerformed

    private void jButton16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton16ActionPerformed
        // TODO add your handling code here:
        InsertaUsuario();
    }//GEN-LAST:event_jButton16ActionPerformed

    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
        // TODO add your handling code here:
        ActualizaUsuario(0);
    }//GEN-LAST:event_jButton14ActionPerformed

    private void jComboBox5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox5ActionPerformed
        // TODO add your handling code here:
        getDatosUsuario();
    }//GEN-LAST:event_jComboBox5ActionPerformed

    private void jButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton15ActionPerformed
        // TODO add your handling code here:
        ActualizaUsuario(1);
    }//GEN-LAST:event_jButton15ActionPerformed

    private void jPasswordField7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPasswordField7ActionPerformed
        // TODO add your handling code here:
        BajaUsuario();
    }//GEN-LAST:event_jPasswordField7ActionPerformed

    private void jButton17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton17ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton17ActionPerformed

    private void jTextField43ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField43ActionPerformed
        // TODO add your handling code here:
        ConsFolioDevol(jTextField43.getText());
    }//GEN-LAST:event_jTextField43ActionPerformed

    private void catalogoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_catalogoActionPerformed
        // TODO add your handling code here:
        cent_catalogos();
    }//GEN-LAST:event_catalogoActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        BuscaProd();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTable5KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTable5KeyPressed
        // TODO add your handling code here:
        if (evt.getKeyCode() == KeyEvent.VK_ENTER)
            AgregaProdNota();
    }//GEN-LAST:event_jTable5KeyPressed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
        tipocliente = 0;
        BuscaCliente(0);
        cent_clientes();
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jTextField48KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField48KeyReleased
        // TODO add your handling code here:
        jTextField48.setText(jTextField48.getText().toUpperCase());
        BuscaCliente(1);
    }//GEN-LAST:event_jTextField48KeyReleased

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        cent_catalogos();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jComboBox9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox9ActionPerformed
        // TODO add your handling code here:
        ComboCatalogo();
    }//GEN-LAST:event_jComboBox9ActionPerformed

    private void jComboBox8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox8ActionPerformed
        // TODO add your handling code here:
        jLabel74.setText(getIDdepto(jComboBox8.getSelectedItem().toString()));
    }//GEN-LAST:event_jComboBox8ActionPerformed

    private void jButton18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton18ActionPerformed
        // TODO add your handling code here:
        AgregaCliente(jComboBox9.getSelectedIndex());
    }//GEN-LAST:event_jButton18ActionPerformed

    private void jTable10KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTable10KeyPressed
        // TODO add your handling code here:
        if (evt.getKeyCode() == KeyEvent.VK_ENTER)
            AgregaClienteNota();
    }//GEN-LAST:event_jTable10KeyPressed

    private void jTable1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTable1KeyPressed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_jTable1KeyPressed

    private void jComboBox11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox11ActionPerformed
        // TODO add your handling code here:
        DefaultTableModel modelo = (DefaultTableModel)jTable4.getModel();
        BuscaInventarioCorr(modelo,jTextField34.getText());
    }//GEN-LAST:event_jComboBox11ActionPerformed

    private void jButton19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton19ActionPerformed
        // TODO add your handling code here:
        ExtraeVentasUsuario(jTextField33.getText());
    }//GEN-LAST:event_jButton19ActionPerformed

    private void jButton20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton20ActionPerformed
        // TODO add your handling code here:
        DefaultTableModel modelo = (DefaultTableModel)jTable1.getModel();
        
        for(int i=modelo.getRowCount()-1;i>=0;i--)
            modelo.removeRow(i);
    }//GEN-LAST:event_jButton20ActionPerformed

    private void jTextField28ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField28ActionPerformed
        // TODO add your handling code here:
        AplicaDescuento(jTextField28.getText(),1);
    }//GEN-LAST:event_jTextField28ActionPerformed

    private void cat_empActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cat_empActionPerformed
        // TODO add your handling code here:
        cent_editclientes();
        cargaempleadosporid();
    }//GEN-LAST:event_cat_empActionPerformed

    private void jButton22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton22ActionPerformed
        // TODO add your handling code here:
        ModificaDeptoClientes();
    }//GEN-LAST:event_jButton22ActionPerformed

    private void jTextField29KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField29KeyReleased
        // TODO add your handling code here:
        DefaultTableModel modelo = (DefaultTableModel)jTable11.getModel();
        jTextField29.addKeyListener(new KeyAdapter() {
            public void keyReleased(final KeyEvent e) {
                String cadena = (jTextField29.getText()).toUpperCase();
                jTextField29.setText(cadena);
                repaint();
                filtro();
            }
        });
        trsfiltro = new TableRowSorter(jTable11.getModel());
        jTable11.setRowSorter(trsfiltro);
    }//GEN-LAST:event_jTextField29KeyReleased

    private void jButton24ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton24ActionPerformed
        // TODO add your handling code here:
        ConsultaVendedores();
    }//GEN-LAST:event_jButton24ActionPerformed

    private void jButton23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton23ActionPerformed
        // TODO add your handling code here:
        cent_consvendedor();
    }//GEN-LAST:event_jButton23ActionPerformed

    private void jTable13MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable13MouseClicked
        // TODO add your handling code here:
        if (evt.getClickCount() == 2) {
            DefaultTableModel modelo = (DefaultTableModel)jTable13.getModel();
            String folio = modelo.getValueAt(jTable13.getSelectedRow(), 0).toString();
            jTextField33.setText(folio);
            
            for(int i=modelo.getRowCount()-1;i>=0;i--)
                modelo.removeRow(i);
            
            ConsVendedor.dispose();
        }
    }//GEN-LAST:event_jTable13MouseClicked

    private void jTable9KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTable9KeyReleased
        // TODO add your handling code here:
        if (evt.getKeyCode() == KeyEvent.VK_ENTER)
            CalculaDevol();
    }//GEN-LAST:event_jTable9KeyReleased

    private void jButton26ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton26ActionPerformed
        // TODO add your handling code here:
        if(jComboBox10.getSelectedIndex()==0)
            ReporteCargos("RepCargosAdmin");
        else{
            if(jComboBox10.getSelectedIndex()==1)
                ReporteCargos("RepCargos");
            else{
                if(jComboBox10.getSelectedIndex()==2)
                    Impresion_CorteCargoSinUsuario(consultas.FechaVentas(jDateChooser1.getCalendar()),consultas.FechaVentas(jDateChooser2.getCalendar()));
            }
        }
    }//GEN-LAST:event_jButton26ActionPerformed

    private void jTabbedPane2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTabbedPane2MouseClicked
        // TODO add your handling code here:
        TabbedClicked();
    }//GEN-LAST:event_jTabbedPane2MouseClicked

    private void jButton21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton21ActionPerformed
        // TODO add your handling code here:
        Impresion_Existencias();
    }//GEN-LAST:event_jButton21ActionPerformed

    private void jButton25ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton25ActionPerformed
        // TODO add your handling code here:
        EscribeConfig();
    }//GEN-LAST:event_jButton25ActionPerformed

    private void configActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configActionPerformed
        // TODO add your handling code here:
        cent_config();
    }//GEN-LAST:event_configActionPerformed

    private void jTextField51MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextField51MouseClicked
        // TODO add your handling code here:
        if(evt.getClickCount() == 2) cent_config();
    }//GEN-LAST:event_jTextField51MouseClicked

    private void jTextField58MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextField58MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField58MouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(App.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(App.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(App.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(App.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new App().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog Cargo;
    private javax.swing.JDialog Catalogoss;
    private javax.swing.JDialog Clientes;
    private javax.swing.JDialog Conecta;
    private javax.swing.JDialog ConsVendedor;
    private javax.swing.JDialog ContNota;
    private javax.swing.JDialog Edit_Clientes;
    private javax.swing.JDialog Inventario;
    private javax.swing.JDialog Log;
    private javax.swing.JDialog OpcionReporte;
    private javax.swing.JDialog Pago;
    private javax.swing.JDialog Reimpresion;
    private javax.swing.JDialog TodosProd;
    private javax.swing.JDialog Venta;
    private javax.swing.JMenu admin;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JMenuItem cat_emp;
    private javax.swing.JMenuItem catalogo;
    private javax.swing.JMenuItem config;
    private javax.swing.JMenu inventar;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton19;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton20;
    private javax.swing.JButton jButton21;
    private javax.swing.JButton jButton22;
    private javax.swing.JButton jButton23;
    private javax.swing.JButton jButton24;
    private javax.swing.JButton jButton25;
    private javax.swing.JButton jButton26;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox10;
    private javax.swing.JComboBox jComboBox11;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JComboBox jComboBox3;
    private javax.swing.JComboBox jComboBox4;
    private javax.swing.JComboBox jComboBox5;
    private javax.swing.JComboBox jComboBox6;
    private javax.swing.JComboBox jComboBox7;
    private javax.swing.JComboBox jComboBox8;
    private javax.swing.JComboBox jComboBox9;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private com.toedter.calendar.JDateChooser jDateChooser2;
    private com.toedter.calendar.JDateChooser jDateChooser3;
    private com.toedter.calendar.JDateChooser jDateChooser4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JLabel jLabel69;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel70;
    private javax.swing.JLabel jLabel71;
    private javax.swing.JLabel jLabel72;
    private javax.swing.JLabel jLabel73;
    private javax.swing.JLabel jLabel74;
    private javax.swing.JLabel jLabel75;
    private javax.swing.JLabel jLabel76;
    private javax.swing.JLabel jLabel77;
    private javax.swing.JLabel jLabel78;
    private javax.swing.JLabel jLabel79;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel80;
    private javax.swing.JLabel jLabel81;
    private javax.swing.JLabel jLabel82;
    private javax.swing.JLabel jLabel83;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JPasswordField jPasswordField2;
    private javax.swing.JPasswordField jPasswordField3;
    private javax.swing.JPasswordField jPasswordField4;
    private javax.swing.JPasswordField jPasswordField5;
    private javax.swing.JPasswordField jPasswordField6;
    private javax.swing.JPasswordField jPasswordField7;
    private javax.swing.JPasswordField jPasswordField8;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable10;
    private javax.swing.JTable jTable11;
    private javax.swing.JTable jTable12;
    private javax.swing.JTable jTable13;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JTable jTable4;
    private javax.swing.JTable jTable5;
    private javax.swing.JTable jTable6;
    private javax.swing.JTable jTable7;
    private javax.swing.JTable jTable8;
    private javax.swing.JTable jTable9;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField11;
    private javax.swing.JTextField jTextField12;
    private javax.swing.JTextField jTextField13;
    private javax.swing.JTextField jTextField14;
    private javax.swing.JTextField jTextField15;
    private javax.swing.JTextField jTextField16;
    private javax.swing.JTextField jTextField17;
    private javax.swing.JTextField jTextField18;
    private javax.swing.JTextField jTextField19;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField20;
    private javax.swing.JTextField jTextField21;
    private javax.swing.JTextField jTextField22;
    private javax.swing.JTextField jTextField23;
    private javax.swing.JTextField jTextField24;
    private javax.swing.JTextField jTextField25;
    private javax.swing.JTextField jTextField26;
    private javax.swing.JTextField jTextField27;
    private javax.swing.JTextField jTextField28;
    private javax.swing.JTextField jTextField29;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField30;
    private javax.swing.JTextField jTextField31;
    private javax.swing.JTextField jTextField32;
    private javax.swing.JTextField jTextField33;
    private javax.swing.JTextField jTextField34;
    private javax.swing.JTextField jTextField35;
    private javax.swing.JTextField jTextField36;
    private javax.swing.JTextField jTextField37;
    private javax.swing.JTextField jTextField38;
    private javax.swing.JTextField jTextField39;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField40;
    private javax.swing.JTextField jTextField41;
    private javax.swing.JTextField jTextField42;
    private javax.swing.JTextField jTextField43;
    private javax.swing.JTextField jTextField44;
    private javax.swing.JTextField jTextField45;
    private javax.swing.JTextField jTextField46;
    private javax.swing.JTextField jTextField47;
    private javax.swing.JTextField jTextField48;
    private javax.swing.JTextField jTextField49;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField50;
    private javax.swing.JTextField jTextField51;
    private javax.swing.JTextField jTextField52;
    private javax.swing.JTextField jTextField53;
    private javax.swing.JTextField jTextField54;
    private javax.swing.JTextField jTextField55;
    private javax.swing.JTextField jTextField56;
    private javax.swing.JTextField jTextField57;
    private javax.swing.JTextField jTextField58;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    private javax.swing.JMenuItem movim;
    private javax.swing.JMenuItem nota;
    private javax.swing.JMenuItem users;
    private javax.swing.JDialog usuarios;
    private javax.swing.JMenu vent;
    // End of variables declaration//GEN-END:variables
}
