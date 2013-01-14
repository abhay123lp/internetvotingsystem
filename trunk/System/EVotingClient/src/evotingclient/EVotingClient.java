package evotingclient;

import java.util.InputMismatchException;
import java.util.Scanner;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class EVotingClient {
    //http://docs.oracle.com/javase/1.5.0/docs/api/javax/net/ssl/SSLSocket.html
    //http://docs.oracle.com/javase/1.5.0/docs/api/javax/net/ssl/SSLServerSocket.html
    //http://docs.oracle.com/javase/1.5.0/docs/api/javax/net/ssl/SSLSocketFactory.html
    //http://docs.oracle.com/javase/1.5.0/docs/api/javax/net/ssl/SSLContext.html

    private final static String votingSiteAddress = "http://www.ii.uj.edu.pl/~ziarkows/voting";
    private final static String CLAIPAddress = "localhost", CTFIPAddress = "localhost";
    private final static int CLAPortNumber = 8102, CTFPortNumber = 8103;
    private static SSLSocket socket;

//    boolean loggedToL;
//    boolean loggedToT;
    
//    public EVotingClient() {
//        ssoc = null;
//        loggedToL = false;
//        loggedToT = false;
//    }
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

        System.out.println("Witaj w aplikacji klienckiej systemu głosowania elektronicznego! \nJakie dane powinieneś posiadać, aby wziąć udział w głosowaniu?\n"
                + "- swój numer PESEL\n- specjalne hasło do głosowania, które każdy wyborca otrzymuje drogą pocztową\n"
                + "\nW razie wątpliwości, zapraszamy na stronę projektu: "+votingSiteAddress+"\n");
        boolean end = false;
        while (!end) {
            System.out.println("Wybierz działanie: 1-odbierz numer walidacyjny 2-zagłosuj 3-wyjdź\n");
            int command = 0;
            try{
                if(sc.hasNext()){
                    if(sc.hasNextInt()){
                        command=sc.nextInt();
                    }
                    else{
                        System.out.println("Nieprawidłowa komenda. Wybierz jedną z dostępnych opcji.");
                        sc.next();
                        continue;
                    }
                }
            } catch (InputMismatchException e) {
                e.printStackTrace();
            }
            switch (command) {
                case 1:
                    System.out.println("Apikacja połączy Cię teraz z serwerem zajmującym się uwierzytelnianiem wyborców.\n Łączenie...");
                    try{
                        socket = (SSLSocket) socketFactory.createSocket(CLAIPAddress, CLAPortNumber);
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                    break;
                    
                case 2:
                    System.out.println("Apikacja połączy Cię teraz z serwerem zajmującym się zbieraniem głosów.\n Łączenie...");
                    try{
                        socket = (SSLSocket) socketFactory.createSocket(CTFIPAddress, CTFPortNumber);
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                    break;
                    
                case 3:
                    System.out.print("Czy na pewno chcesz opuścić aplikację? \n"
                            + "Pamiętaj o zachowaniu swoich numerów: identyfikacyjnego i walidacyjnego, w celu późniejszego sprawdzenia poprawności zliczania głosów!\n"
                            + "Wyjść z programu? (t/n): ");
                    String decision = sc.next();
                    if (decision.equals("t")) {
                        end = true;
                    }
                    break;
                    
                default:
                    System.out.println("Nieprawidłowa komenda. Wybierz jedną z dostępnych opcji.");
                    break;
            }
        }

//        String argl = null;
//        System.out.print("Wprowadz polecenie: ");
//        while ((argl = sc.nextLine()) != null && argl != "") {
//            String[] argt = argl.split("\\s");
//            String command = argt[0];
//            if (command.equalsIgnoreCase("polacz")) {
//            } else if (command.equalsIgnoreCase("pobierzURN")) {
//            } else if (command.equalsIgnoreCase("glosuj")) {
//            } else if (command.equalsIgnoreCase("pomoc")) {
//                System.out.println("Oto ropoznawalne polecenia: ");
//                System.out.println("polacz {l|t} {adresIp|adresUrl} portNr");
//                System.out.println("pobierzURN userId userPassword");
//                System.out.println("glosuj urn glos");
//                System.out.println("pomoc - wyswietla ten komunikat");
//            } else {
//                System.out.println("Nie rozpoznaje komendy - wpisz pomoc aby podejrzec liste dopuszczalnych opcji.");
//            }
//        }
    }
}