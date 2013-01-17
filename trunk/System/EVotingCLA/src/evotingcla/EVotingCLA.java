package evotingcla;

import evotingcommon.EVotingCommon;
import evotingcommon.RequestMessage;
import evotingcommon.ResponseMessage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
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
    private static CLADataBase cladb;

    public static void main(String[] args) throws SQLException, FileNotFoundException, ClassNotFoundException, IOException {

        //ustawienie zmiennych srodowiskowych dla implementacji javowej protokolu ssl

        System.setProperty("javax.net.ssl.keyStore", EVotingCommon.SSLKeyAndCertStorageDir + "/CLAKeyStore");
        System.setProperty("javax.net.ssl.keyStorePassword", "123456");

        //

        ServerSocketFactory socketFactory = SSLServerSocketFactory.getDefault();
        ServerSocket serverSocket;

        //Administrator podczas startu decyduje czy utworzyc nowa baze danych - w domyslnej strukturze katalogow
        //Decyzja ta powinna byc oparta na tym czy baza juz istnieje czy jeszcze nie i musimy to zrobic
        System.out.println("Jesli nie ma jeszcze bazy danych wpisz: utworz. W przeciwnym razie wcisnij cokolwiek.");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line = br.readLine();
        if (line.equals("utworz")) {
            System.out.println("Tworze baze danych ...");
            CLADataBase.createDBOnDisk(EVotingCommon.CLADBAddr, EVotingCommon.CLACreatingScriptFileAddress, EVotingCommon.CLADBUsername, EVotingCommon.CLADBPassword);
            System.out.println("TO JEST TO: "+EVotingCommon.CLAPopulatingScriptFileAddress);
            CLADataBase.populate(EVotingCommon.CLADBAddr, EVotingCommon.CLAPopulatingScriptFileAddress, EVotingCommon.CLADBUsername, EVotingCommon.CLADBPassword);
        } else {
            System.out.println("Nie tworze niczego.");
        }
        System.out.println("Przechodze do nasluchu ...");

        try {
            cladb = new CLADataBase(EVotingCommon.CLADBAddr, EVotingCommon.CLADBUsername, EVotingCommon.CLADBPassword);
        } catch (InstantiationException ex) {
            Logger.getLogger(EVotingCLA.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(EVotingCLA.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            serverSocket = (SSLServerSocket) socketFactory.createServerSocket(EVotingCommon.CLAPortNumber);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Błąd tworzenia gniazda. Napraw usterkę i uruchom system ponownie.");
            return;
        }
        System.out.println("Debug");
        while (true) {
            try {
                new EVotingCLA((SSLSocket) serverSocket.accept()).start();
            } catch (IOException e) {
                System.err.println("BŁĄD: Nieudane połączenie z klientem...\n");
            }
        }
    }
    private SSLSocket socket;

    public EVotingCLA(SSLSocket socket) {
        this.socket = socket;
    }

    public void run() {
        ObjectInputStream inputStream = null;
        ObjectOutputStream outputStream = null;

        try {
            inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream = new ObjectOutputStream(socket.getOutputStream());

            //do zaimplementowania!!!
            RequestMessage request = (RequestMessage) inputStream.readObject();
            if (request.getType() != CLA_VALIDATION_REQ) {
                System.out.println("Powinienem otrzymac cla_validation_req - sprawdz to");
            }
            String pesel = request.getData().get(0);
            String password = request.getData().get(1);
            ResponseMessage response = new ResponseMessage();
            List<String> data = new ArrayList<String>();
            response.setData(data);
            //dowiedzmy sie dwoch rzeczy - czy te dane sa poprawne i czy wyborca odebral juz swoj nr
            boolean registered = cladb.userAlreadyRegistered(pesel);
            boolean matching = cladb.usernameMatchesPassword(pesel, password);
            if (registered && matching) {
                response.setStatus(CLA_OK_RESP);
                Random rnd = new Random();
                String validationNo = Long.toString(rnd.nextLong());
                while (cladb.URNAlreadyExists(validationNo)) {
                    validationNo = Long.toString(rnd.nextLong());
                }
                response.getData().add(validationNo);
            } else {
                response.setStatus(CLA_WRONG_DATA_RESP);
                response.getData().add("Uztkownik ma juz swoj nr walidacyjny:" + registered + ". Hasło jest niepoprawne: " + matching);
            }
            /*
             * List<String> data = new ArrayList<String>();
             * data.add("135631265472452474525758"); response.setData(data);
             * outputStream.writeObject(response);
             *
             */

        } catch (Exception e) {
            e.printStackTrace();
            ResponseMessage response = new ResponseMessage();
            response.setStatus(2);
            response.setData(null);
            try {
                outputStream.writeObject(response);
            } catch (IOException f) {
                f.printStackTrace();
                System.err.println("BŁĄD: Niedziałające połączenie - nie udało się wysłać klientowi wiadomości o wyjątku po stronie serwera...");
                System.err.println("Błąd wystąpił przy połączeniu z klientem - IP: " + socket.getInetAddress() + " port: " + socket.getPort());
            }
            //UWAGA!!!!
            //ta klauzula przygotowuje sobie response ale go nie wysyla !!! Poza tym trzeba by 
            //sprawdzic czy nie wyslano odp przed rzuceniem wyjatku bo wtedy doszloby do rozsynchronizowania komunikacji
        }
    }
}
