/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sicaf;

import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author AreaInterpretación
 */
public class Sistema {
 
    public String Encriptar(String texto) {
 
        String secretKey = "qualityinfosolutions"; //llave para encriptar datos
        String base64EncryptedString = "";
 
        try {
 
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digestOfPassword = md.digest(secretKey.getBytes("utf-8"));
            byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
 
            SecretKey key = new SecretKeySpec(keyBytes, "DESede");
            Cipher cipher = Cipher.getInstance("DESede");
            cipher.init(Cipher.ENCRYPT_MODE, key);
 
            byte[] plainTextBytes = texto.getBytes("utf-8");
            byte[] buf = cipher.doFinal(plainTextBytes);
            byte[] base64Bytes = Base64.encodeBase64(buf);
            base64EncryptedString = new String(base64Bytes);
 
        } catch (Exception ex) {}
        return base64EncryptedString;
    }
    
    public String Desencriptar(String textoEncriptado) throws Exception {
 
        String secretKey = "qualityinfosolutions"; //llave para desenciptar datos
        String base64EncryptedString = "";
 
        try {
            byte[] message = Base64.decodeBase64(textoEncriptado.getBytes("utf-8"));
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digestOfPassword = md.digest(secretKey.getBytes("utf-8"));
            byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
            SecretKey key = new SecretKeySpec(keyBytes, "DESede");
 
            Cipher decipher = Cipher.getInstance("DESede");
            decipher.init(Cipher.DECRYPT_MODE, key);
 
            byte[] plainText = decipher.doFinal(message);
 
            base64EncryptedString = new String(plainText, "UTF-8");
 
        } catch (Exception ex) {}
        return base64EncryptedString;
    }
    
    public int esConsonante(char a){
        if((a == 'A')||(a == 'E')||(a == 'I')||(a == 'O')||(a == 'U'))
            return 0; //0 quiere decir que es vocal
        else return 1; //1 quiere decir que es consonante
    }
    
    public int Divisible2(char a){
        int b = Integer.parseInt(String.valueOf(a)) % 2;
        if((b == 0)) return 0;  //0 quiere decir que el numero es divisible entre 2 o es par
        else return 1;
    }
    
    public int Letra(char a){
        if(Character.isDigit(a) == true) return 0; //0 es numero
        else return 1; //1 es letra
    }
    
    public int checkDVerif(char Lic[]){
        int suma = Integer.parseInt(String.valueOf(Lic[1]))+Integer.parseInt(String.valueOf(Lic[6]))+
                   Integer.parseInt(String.valueOf(Lic[10]))+Integer.parseInt(String.valueOf(Lic[12]))+
                   Integer.parseInt(String.valueOf(Lic[18]))+Integer.parseInt(String.valueOf(Lic[20]))+
                   Integer.parseInt(String.valueOf(Lic[27]))+Integer.parseInt(String.valueOf(Lic[29]));
        
        return suma/2;
    }
    
    public String[] VerificaLicencia(String licencia){
        char Lic[] = new char[30];
        String Regresa[] = new String[2];
        Lic = licencia.toCharArray();
        int verificador = 0;
        Regresa[0] = String.valueOf(""+Lic[1]+Lic[12]+Lic[10]+Lic[6]+Lic[5]+Lic[29]+Lic[27]+Lic[11]+Lic[20]+Lic[18]);
        
        if((esConsonante(Lic[0]) == 1)&&(esConsonante(Lic[16]) == 1)&&(esConsonante(Lic[26]) == 1)&&(esConsonante(Lic[15]) == 0)&&(esConsonante(Lic[28]) == 0)){
            if(Divisible2(Lic[2]) == 0){
                if((Lic[3] >= 'D')&&(Lic[3] <= 'K')){
                    if(Divisible2(Lic[4]) == 1){
                        if((Letra(Lic[7]) == 1)&&(Letra(Lic[8]) < 'L')&&(Letra(Lic[9]) == 1)){
                           if(Integer.parseInt(String.valueOf(Lic[13])) < 10){
                               if(Divisible2(Lic[14]) == 0){
                                   if((Integer.parseInt(String.valueOf(Lic[19])) >= 5)&&(Integer.parseInt(String.valueOf(Lic[19])) <= 9)){
                                        if((Letra(Lic[21]) == 0)&&(Letra(Lic[22]) == 1)){
                                            if((Integer.parseInt(String.valueOf(Lic[24])) >= 1)&&(Integer.parseInt(String.valueOf(Lic[24])) <= 6)){
                                                if((Lic[25] >= 'A')&&(Lic[25] <= 'F')){
                                                    if(Integer.parseInt(String.valueOf(Lic[30])) == checkDVerif(Lic)) verificador = 0;
                                                    else verificador ++;
                                                } else verificador ++;
                                            } else verificador ++;
                                        } else verificador ++;
                                   } else verificador ++;
                               } else verificador ++;
                           } else verificador ++;
                        } else verificador ++;
                    } else verificador ++;
                } else verificador ++;
            } else verificador ++;
        } else verificador ++;
        
        Regresa[1] = Integer.toString(verificador);
        return Regresa;
    }
}
