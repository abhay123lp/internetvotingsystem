package evotingclient;

import evotingcommon.EVotingCommon;
import evotingcommon.RequestMessage;
import evotingcommon.ResponseMessage;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class EVotingClient {
    //http://docs.oracle.com/javase/1.5.0/docs/api/javax/net/ssl/SSLSocket.html
    //http://docs.oracle.com/javase/1.5.0/docs/api/javax/net/ssl/SSLServerSocket.html
    //http://docs.oracle.com/javase/1.5.0/docs/api/javax/net/ssl/SSLSocketFactory.html
    //http://docs.oracle.com/javase/1.5.0/docs/api/javax/net/ssl/SSLContext.html

    private final static String votingSiteAddress = "http://www.ii.uj.edu.pl/~ziarkows/voting";
    
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
                + "\nW razie wątpliwości, zapraszamy na stronę projektu: " + votingSiteAddress + "\n");
        boolean end = false;
        while (!end) {
            System.out.println("Wybierz działanie: 1-odbierz numer walidacyjny 2-zagłosuj 3-wyjdź\n");
            int command = 0;
            try {
                if (sc.hasNext()) {
                    if (sc.hasNextInt()) {
                        command = sc.nextInt();
                    } else {
                        System.out.println("Nieprawidłowa komenda. Wybierz jedną z dostępnych opcji.");
                        sc.next();
                        continue;
                    }
                }
            } catch (InputMismatchException e) {
                e.printStackTrace();
            }
            if (command == 1) {
                System.out.println("Apikacja połączy Cię teraz z serwerem zajmującym się uwierzytelnianiem wyborców.\n Łączenie...");
                try {
                    socket = (SSLSocket) socketFactory.createSocket(EVotingCommon.CLAIPAddress, EVotingCommon.CLAPortNumber);
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                    RequestMessage request = new RequestMessage();
                    request.setType(1);
                    System.out.print("Podaj swój numer PESEL: ");
                    int pesel = sc.nextInt();
                    System.out.print("Podaj swoje tajne hasło: ");
                    String password = sc.next();
                    List<String> data = new ArrayList<String>();
                    data.add(Integer.toString(pesel));
                    data.add(password);
                    request.setData(data);
                    out.writeObject(request);
                    out.flush();
                    ResponseMessage response = (ResponseMessage) in.readObject();
                    int responseStatus=response.getStatus();
                    if (responseStatus == 0) {
                        data = response.getData();
                        String validationNo = data.get(0);
                        System.out.printf("Twój numer walidacyjny to: %s", validationNo);

                    } else if (responseStatus==1) {
                        System.out.println("Podane dane zostały odrzucone przez serwer.");
                        String errorMessage = response.getData().get(0);
                        System.out.println("Wiadomość zwrócona z serwera: \""+errorMessage+"\""+
                                "\nSprawdź, czy dobrze podałeś dane. Jeżeli jesteś pewien, że tak - skontaktuj się z administratorem poprzez stronę "+votingSiteAddress);
                    }
                    else if (responseStatus == 2){
                        System.out.println("Serwer do głosowania doświadczył błędu - być może jest to czasowy problem. Spróbuj zagłosować za chwilę."
                                + "\nJeżeli problem będzie się powtarzał, skontaktuj się z administratorem poprzez stronę "+votingSiteAddress);
                    }
                    else {
                        System.out.println("Nieznany błąd. Aplikacja nie działa poprawnie. Skontaktuj się z administratorem poprzez stronę "+votingSiteAddress);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (command == 2) {


                System.out.println("Apikacja połączy Cię teraz z serwerem zajmującym się zbieraniem głosów.\n Łączenie...");
                try {
                    socket = (SSLSocket) socketFactory.createSocket(EVotingCommon.CTFIPAddress, EVotingCommon.CTFPortNumber);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (command == 3) {

                System.out.print("Czy na pewno chcesz opuścić aplikację? \n"
                        + "Pamiętaj o zachowaniu swoich numerów: identyfikacyjnego i walidacyjnego, w celu późniejszego sprawdzenia poprawności zliczania głosów!\n"
                        + "Wyjść z programu? (t/n): ");
                String decision = sc.next();
                if (decision.equals("t")) {
                    end = true;
                }

            } else {
                System.out.println("Nieprawidłowa komenda. Wybierz jedną z dostępnych opcji.");

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