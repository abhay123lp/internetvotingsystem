package evotingclient;

//import com.sun.xml.internal.ws.resources.SoapMessages;
import evotingcommon.EVotingCommon;
import evotingcommon.RequestMessage;
import evotingcommon.ResponseMessage;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class EVotingClient {

    private final static String votingSiteAddress = "http://www.ii.uj.edu.pl/~ziarkows/voting";
    private static SSLSocket socket;
    private static int CLA_VALIDATION_REQ = 0, 
            CTF_CANDIDATES_REQ = 0, 
            CTF_VOTE_REQ = 1, 
            CLA_OK_RESP = 0, 
            CLA_WRONG_DATA_RESP = 1, 
            CLA_SERVER_ERROR_RESP = 2,
            CTF_OK_RESP = 0, 
            CTF_VALIDATION_INCORRECT_RESP = 1, 
            CTF_VALIDATION_USED_RESP = 2, 
            CTF_ID_USED_RESP = 3, 
            CTF_SERVER_ERROR_RESP = 4;

    public static void main(String[] args) {
        
        //ustawienie zmiennych srodowiskowych dla implementacji javowej protokolu ssl
        
        System.setProperty("javax.net.ssl.trustStore", EVotingCommon.SSLKeyAndCertStorageDir + "/VotComCertMag");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456");
        
        //
        
        Scanner keyboardScanner = new Scanner(System.in);
        SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        String validationNo = null;

        System.out.println("Witaj w aplikacji klienckiej systemu głosowania elektronicznego! \n"
                + "Jakie dane powinieneś posiadać, aby wziąć udział w głosowaniu?\n"
                + "- swój numer PESEL\n"
                + "- specjalne hasło do głosowania, które każdy wyborca otrzymuje drogą pocztową\n"
                + "\nW razie wątpliwości, zapraszamy na stronę projektu: " + votingSiteAddress + "\n");
        boolean end = false;
        while (!end) {
            System.out.println("Wybierz działanie: 1-odbierz numer walidacyjny 2-zagłosuj 3-wyjdź\n");
            int command = 0;
            try {
                if (keyboardScanner.hasNext()) {
                    if (keyboardScanner.hasNextInt()) {
                        command = keyboardScanner.nextInt();
                    } else {
                        System.out.println("Nieprawidłowa komenda. Wybierz jedną z dostępnych opcji.");
                        keyboardScanner.next();
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
                    out = new ObjectOutputStream(socket.getOutputStream());
                    in = new ObjectInputStream(socket.getInputStream());
                    RequestMessage request = null;
                    ResponseMessage response = null;

                    request = new RequestMessage();
                    request.setType(CLA_VALIDATION_REQ);
                    System.out.print("Podaj swój numer PESEL: ");
                    int pesel = keyboardScanner.nextInt();
                    System.out.print("Podaj swoje tajne hasło: ");
                    String password = keyboardScanner.next();
                    List<String> data = new ArrayList<String>();
                    data.add(Integer.toString(pesel));
                    data.add(password);
                    request.setData(data);
                    out.writeObject(request);
                    out.flush();
                    response = (ResponseMessage) in.readObject();
                    int responseStatus = response.getStatus();
                    if (responseStatus == CLA_OK_RESP) {
                        validationNo = response.getData().get(0);
                        System.out.printf("Twój numer walidacyjny to: %s", validationNo);

                    } else if (responseStatus == CLA_WRONG_DATA_RESP) {
                        String errorMessage = response.getData().get(0);
                        System.out.println("Podane dane zostały odrzucone przez serwer.");
                        System.out.println("Wiadomość zwrócona z serwera: \"" + errorMessage + "\""
                                + "\nSprawdź, czy dobrze podałeś dane. Jeżeli jesteś pewien, że tak - skontaktuj się z administratorem poprzez stronę " + votingSiteAddress);
                    } else if (responseStatus == CLA_SERVER_ERROR_RESP) {
                        System.out.println("Serwer do głosowania doświadczył błędu - być może jest to czasowy problem. Spróbuj zagłosować za chwilę."
                                + "\nJeżeli problem będzie się powtarzał, skontaktuj się z administratorem poprzez stronę " + votingSiteAddress);
                    } else {
                        System.out.println("Nieznany błąd. Aplikacja nie działa poprawnie. Skontaktuj się z administratorem poprzez stronę " + votingSiteAddress);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        out.close();
                        in.close();
                        socket.close();
                    } catch (Exception e) {
                        System.out.println("Błąd techniczny podczas zamykania strumieni służących do komunikacji z serwerem.");
                    }
                }
            } else if (command == 2) {
                System.out.println("Apikacja połączy Cię teraz z serwerem zajmującym się zbieraniem głosów.\n Łączenie...");
                try {
                    socket = (SSLSocket) socketFactory.createSocket(EVotingCommon.CTFIPAddress, EVotingCommon.CTFPortNumber);
                    out = new ObjectOutputStream(socket.getOutputStream());
                    in = new ObjectInputStream(socket.getInputStream());
                    List<String> data = null;
                    RequestMessage request = null;
                    ResponseMessage response = null;

                    request = new RequestMessage();
                    request.setType(CTF_CANDIDATES_REQ);
                    request.setData(null);
                    //pytanie: czy nie powinnismy przedstawic swojego nru ktory uprzednio uzyskalismy od cla
                    //oraz losowego nru ktory sobie wygenerowalismy tak jak w protokole?
                    out.writeObject(request);
                    out.flush();
                    response = (ResponseMessage) in.readObject();
                    if (response.getStatus() == CTF_OK_RESP) {
                        data = response.getData();
                        boolean voted = false;
                        while (!voted) {
                            System.out.println("Oto lista dostępnych kandydatów:");
                            for (int i = 0; i < data.size(); i++) {
                                System.out.print("Kandydat nr " + i + ": " + data.get(i));
                            }
                            System.out.print("Wybierz numer kandydata, na którego chcesz zagłosować: ");
                            int candidate = keyboardScanner.nextInt();
                            System.out.println("Chcesz zagłosować na kandydata nr " + candidate + ", czyli: " + data.get(candidate));
                            System.out.print("Kontynuować? (t/n):");
                            String decision = keyboardScanner.next();
                            if (decision.equals("t")) {
                                voted = true;
                                System.out.println("Teraz Twój głos zostanie wysłany do serwera.\nWysyłanie...");
                                String identificationNo = null;
                                request = new RequestMessage();
                                request.setType(CTF_VOTE_REQ);
                                boolean idAccepted = false;
                                while (!idAccepted) {
                                    //LOSOWANIE NUMERU IDENTYFIKACYJNEGO
                                    //powtarzane do skutku - aż będzie unikalny (za każdym razem wysyłane zapytanie do CTF)
                                    Random rnd = new Random();
                                    identificationNo = Long.toString(Math.abs(rnd.nextLong()));
                                    //
                                    data = new ArrayList<String>();
                                    data.add(validationNo);
                                    data.add(identificationNo);
                                    data.add(Integer.toString(candidate));
                                    request.setData(data);
                                    out.writeObject(request);
                                    out.flush();
                                    response = (ResponseMessage) in.readObject();
                                    if (response.getStatus() != CTF_ID_USED_RESP) {
                                        idAccepted = true;
                                    }
                                }
                                if (response.getStatus() == CTF_VALIDATION_INCORRECT_RESP) {
                                    System.out.println("Podany numer walidacyjny nie uprawnia do głosowania!");
                                } else if (response.getStatus() == CTF_VALIDATION_USED_RESP) {
                                    System.out.println("Podany numer walidacyjny został już użyty");
                                } else if(response.getStatus()==CTF_OK_RESP){
                                    System.out.println("Udało się zagłosować na wybranego kandydata!"
                                            + "\nNumer identyfikacyjny Twojego głosu to: "+identificationNo);
                                    System.out.println("UWAGA! Zachowaj powyższy numer, aby być w stanie zweryfikować później poprawne podliczenie Twojego głosu!");
                                    System.out.println("Dziękujemy za skorzystanie z naszego systemu");
                                } else {
                                    
                                }
                            }
                        }
                    } else {
                        System.out.println("Nieprzewidziana wartość zwrócona z serwera. Skontaktuj się z administratorem poprzez stronę " + votingSiteAddress);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        out.close();
                        in.close();
                        socket.close();
                    } catch (Exception e) {
                        System.out.println("Błąd techniczny podczas zamykania strumieni służących do komunikacji z serwerem.");
                    }
                }
            } else if (command == 3) {
                System.out.print("Czy na pewno chcesz opuścić aplikację? \n"
                        + "Pamiętaj o zachowaniu swoich numerów: identyfikacyjnego i walidacyjnego, w celu późniejszego sprawdzenia poprawności zliczania głosów!\n"
                        + "Wyjść z programu? (t/n): ");
                String decision = keyboardScanner.next();
                if (decision.equals("t")) {
                    end = true;
                }
            } else {
                System.out.println("Nieprawidłowa komenda. Wybierz jedną z dostępnych opcji.");
            }
        }
    }
}