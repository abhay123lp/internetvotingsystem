package klient;

import java.util.Scanner;
import javax.net.ssl.SSLSocket;

public class Klient {
    //http://docs.oracle.com/javase/1.5.0/docs/api/javax/net/ssl/SSLSocket.html
    //http://docs.oracle.com/javase/1.5.0/docs/api/javax/net/ssl/SSLServerSocket.html
    //http://docs.oracle.com/javase/1.5.0/docs/api/javax/net/ssl/SSLSocketFactory.html
    //http://docs.oracle.com/javase/1.5.0/docs/api/javax/net/ssl/SSLContext.html
    SSLSocket ssoc;
    boolean loggedToL;
    boolean loggedToT;
    
    public Klient(){
        ssoc = null;
        loggedToL = false;
        loggedToT = false;
    }
    
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String argl = null;
        System.out.print("Wprowadz polecenie: ");
        while((argl = sc.nextLine()) != null && argl != ""){
            String[] argt = argl.split("\\s");
            String command = argt[0];
            if(command.equalsIgnoreCase("polacz")){
                
            }else if(command.equalsIgnoreCase("pobierzURN")){
                
            }else if(command.equalsIgnoreCase("glosuj")){
                
            }else if(command.equalsIgnoreCase("pomoc")){
                System.out.println("Oto ropoznawalne polecenia: ");
                System.out.println("polacz {l|t} {adresIp|adresUrl} portNr");
                System.out.println("pobierzURN userId userPassword");
                System.out.println("glosuj urn glos");
                System.out.println("pomoc - wyswietla ten komunikat");
            }else{
                System.out.println("Nie rozpoznaje komendy - wpisz pomoc aby podejrzec liste dopuszczalnych opcji.");
            }
        }
    }
    
    String polacz(){
        
        return null;
    }
    
    String pobierzURN(){
        
        return null;
    }
    
    String glosuj(){
        
        return null;
    }
}
