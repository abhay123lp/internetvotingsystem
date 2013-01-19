package evotingcla;

import evotingcommon.EVotingCommon;
import evotingcommon.RequestMessage;
import evotingcommon.ResponseMessage;
import evotingcommon.ServerKiller;
import java.io.*;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class EVotingCLA extends Thread {

    private static CLADataBase cladb = null;

    public static void main(String[] args) throws SQLException, FileNotFoundException, ClassNotFoundException, IOException {

        //ustawienie zmiennych srodowiskowych dla implementacji javowej protokolu ssl
        System.setProperty("javax.net.ssl.keyStore", EVotingCommon.SSLKeyAndCertStorageDir + "/CLAKeyStore");
        System.setProperty("javax.net.ssl.keyStorePassword", "123456");



        //Administrator podczas startu decyduje czy utworzyc nowa baze danych - w domyslnej strukturze katalogow
        //Decyzja ta powinna byc oparta na tym czy baza juz istnieje czy jeszcze nie i musimy to zrobic
        System.out.println("Jesli nie ma jeszcze bazy danych wpisz: utworz.\n"
                + " Jesli chcesz zrzucic numery dla komisji T zapisane w kolejnych liniach do pliku tekstowego wpisz: zrzuc.\n"
                + " W przeciwnym razie wcisnij cokolwiek.");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line = br.readLine();
        if (line.equals("utworz")) {
            System.out.println("Tworze baze danych ...");
            CLADataBase.createDBOnDisk(EVotingCommon.CLADBAddr, EVotingCommon.CLACreatingScriptFileAddress, EVotingCommon.CLADBUsername, EVotingCommon.CLADBPassword);
            CLADataBase.populate(EVotingCommon.CLADBAddr, EVotingCommon.CLAPopulatingScriptFileAddress, EVotingCommon.CLADBUsername, EVotingCommon.CLADBPassword);
        }

        try {
            cladb = new CLADataBase(EVotingCommon.CLADBAddr, EVotingCommon.CLADBUsername, EVotingCommon.CLADBPassword);
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(EVotingCLA.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (line.equals("zrzuc")) {
            writeURNsToFile(cladb);
            return;
        }

        System.out.println("Przechodze do nasluchu ...");

        ServerSocketFactory socketFactory = SSLServerSocketFactory.getDefault();
        ServerSocket serverSocket;
        try {
            serverSocket = (SSLServerSocket) socketFactory.createServerSocket(EVotingCommon.CLAPortNumber);
            serverSocket.setSoTimeout(10000);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Błąd tworzenia gniazda. Napraw usterkę i uruchom system ponownie.");
            return;
        }
        List<EVotingCLA> threads = new ArrayList<>();
        ServerKiller killer = new ServerKiller();
        killer.start();
        while (killer.isAlive()) {
            try {
                new EVotingCLA((SSLSocket) serverSocket.accept()).start();
            } catch (SocketTimeoutException e) {
            } catch (IOException e) {
                System.err.println("BŁĄD: Nieudane połączenie z klientem...\n");
            }
        }
        for (EVotingCLA t : threads) {
            t.interrupt();
        }
        System.out.println("Zanim sobie pójdziesz: czy chcesz zrzucić poprawne numery walidacyjne do pliku?\n"
                + "Wprowadź swój wybór (t/n):");
        String decision = br.readLine();
        if (decision.equalsIgnoreCase("t")) {
            writeURNsToFile(cladb);
        }

    }
    private SSLSocket socket;

    public EVotingCLA(SSLSocket socket) {
        this.socket = socket;
    }

    public static void writeURNsToFile(CLADataBase cladb) {
        try {
            List<String> urns = cladb.registeredUsersUrns();

            PrintWriter pw = null;
            try {
                pw = new PrintWriter(new BufferedWriter(new FileWriter(EVotingCommon.CLAToCTFUrnFile)));
                for (String urn : urns) {
                    pw.println(urn);
                }
            } finally {
                pw.flush();
                pw.close();
            }
        } catch (Exception e) {
            System.err.println("Uwaga! Problem z zapisem numerów walidacyjnych do pliku! Numery walidacyjne są zapisane w bazie danych. "
                    + "\nSpróbuj naprawić problem lub pobrać je ręcznie z bazy danych.");
        }
    }

    public void run() {
        ObjectInputStream inputStream = null;
        ObjectOutputStream outputStream = null;

        try {
            inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream = new ObjectOutputStream(socket.getOutputStream());

            boolean end = false;
            while (!end && !isInterrupted()) {
                RequestMessage request = (RequestMessage) inputStream.readObject();
                if (request.getType() != EVotingCommon.CLA_VALIDATION_REQ) {
                    System.out.println("Powinienem otrzymac cla_validation_req - sprawdz to");
                }
                String pesel = request.getData().get(0);
                String password = request.getData().get(1);
                ResponseMessage response = new ResponseMessage();
                List<String> data = new ArrayList<>();
                response.setData(data);
                //dowiedzmy sie dwoch rzeczy - czy te dane sa poprawne i czy wyborca odebral juz swoj nr
                boolean registered = cladb.userAlreadyRegistered(pesel);
                boolean matching = cladb.usernameMatchesPassword(pesel, password);
                System.out.println("registered: " + registered + " matching: " + matching);
                if (!registered && matching) {
                    response.setStatus(EVotingCommon.CLA_OK_RESP);
                    Random rnd = new Random();
                    String validationNo = Long.toString(Math.abs(rnd.nextLong()));
                    while (cladb.URNAlreadyExists(validationNo)) {
                        System.out.println("Unfortunately the number alredy exists.");
                        validationNo = Long.toString(Math.abs(rnd.nextLong()));
                    }
                    cladb.alterVoter(pesel, validationNo);
                    response.getData().add(validationNo);
                } else {
                    response.setStatus(EVotingCommon.CLA_WRONG_DATA_RESP);
                    response.getData().add("Hasło jest poprawne: " + matching + ". Użytkownik ma juz swoj nr walidacyjny:" + registered);
                }
                outputStream.writeObject(response);
                outputStream.flush();
            }
        } catch (EOFException e) {
            System.out.println("Użytkownik się rozłączył");
        } catch (Exception e) {
            e.printStackTrace();
            ResponseMessage response = new ResponseMessage();
            response.setStatus(EVotingCommon.CLA_SERVER_ERROR_RESP);
            response.setData(null);
            try {
                outputStream.writeObject(response);
                outputStream.flush();
            } catch (IOException f) {
                f.printStackTrace();
                System.err.println("BŁĄD: Niedziałające połączenie - nie udało się wysłać klientowi wiadomości o wyjątku po stronie serwera...");
                System.err.println("Błąd wystąpił przy połączeniu z klientem - IP: " + socket.getInetAddress() + " port: " + socket.getPort());
            }
            //UWAGA!!!!
            //ta klauzula przygotowuje sobie response ale go nie wysyla !!! Poza tym trzeba by 
            //sprawdzic czy nie wyslano odp przed rzuceniem wyjatku bo wtedy doszloby do rozsynchronizowania komunikacji
        } finally {
            try {
                outputStream.close();
            } catch (Exception e) {
            }
            try {
                inputStream.close();
            } catch (Exception e) {
            }
        }
    }
}
