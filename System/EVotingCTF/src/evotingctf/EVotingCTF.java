package evotingctf;

import evotingcommon.EVotingCommon;
import evotingcommon.RequestMessage;
import evotingcommon.ResponseMessage;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class EVotingCTF extends Thread {

    private static CTFDataBase ctfdb;

    public static void main(String[] args) throws SQLException, FileNotFoundException, ClassNotFoundException, IOException{

        //ŁADOWANIE DANYCH Z CLA - do zrobienia
        System.setProperty("javax.net.ssl.keyStore", EVotingCommon.SSLKeyAndCertStorageDir + "/CTFKeyStore");
        System.setProperty("javax.net.ssl.keyStorePassword", "123456");
        
        ServerSocketFactory socketFactory = SSLServerSocketFactory.getDefault();
        ServerSocket serverSocket;

        //Administrator podczas startu decyduje czy utworzyc nowa baze danych - w domyslnej strukturze katalogow
        //Decyzja ta powinna byc oparta na tym czy baza juz istnieje czy jeszcze nie i musimy to zrobic
        System.out.println("Jesli nie ma jeszcze bazy danych wpisz: utworz. W przeciwnym razie wcisnij cokolwiek.");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line = br.readLine();
        if (line.equals("utworz")) {
            System.out.println("Tworze baze danych ...");
            CTFDataBase.createDBOnDisk(EVotingCommon.CTFDBAddr, EVotingCommon.CTFCreatingScriptFileAddress, EVotingCommon.CTFDBUsername, EVotingCommon.CTFDBPassword);
            CTFDataBase.populate(EVotingCommon.CTFDBAddr, EVotingCommon.CTFPopulatingScriptFileAddress, EVotingCommon.CTFDBUsername, EVotingCommon.CTFDBPassword);
        } else {
            System.out.println("Nie tworze niczego.");
        }
        System.out.println("Przechodze do nasluchu ...");

        try {
            ctfdb = new CTFDataBase(EVotingCommon.CTFDBAddr, EVotingCommon.CTFDBUsername, EVotingCommon.CTFDBPassword);
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(EVotingCTF.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            serverSocket = (SSLServerSocket) socketFactory.createServerSocket(EVotingCommon.CTFPortNumber);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Błąd tworzenia gniazda. Napraw usterkę i uruchom system ponownie.");
            return;
        }
        System.out.println("Debug");
        while (true) {
            try {
                new EVotingCTF((SSLSocket) serverSocket.accept()).start();
            } catch (IOException e) {
                System.err.println("BŁĄD: Nieudane połączenie z klientem...\n");
            }
        }
    }
    private SSLSocket socket;

    public EVotingCTF(SSLSocket socket) {
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
            if (request.getType() == EVotingCommon.CTF_CANDIDATES_REQ) {
                ResponseMessage response = new ResponseMessage();
                response.setStatus(EVotingCommon.CTF_CANDIDATES_RESP);
                List<String> candidates = ctfdb.getCandidates();
                response.setData(candidates);
                outputStream.writeObject(response);
                outputStream.flush();
            } else if (request.getType() == EVotingCommon.CTF_VOTE_REQ) {
                List<String> data = request.getData();
                /*//to jest chyba zle - popatrz na odpowiednia czesc klienta
                String identificationNo = data.get(0),
                        validationNo = data.get(1);
                */
                
                String validationNo = data.get(0), identificationNo = data.get(1), candidateNo = data.get(2);
                int candidate = 0;
                try {
                    candidate = Integer.parseInt(candidateNo);
                } catch (NumberFormatException e) {
                    candidate = -1;
                    /*
                    System.err.println("BŁĄD: numer kandydata nie jest poprawną liczbą!");
                    ResponseMessage response = new ResponseMessage();
                    response.setStatus(EVotingCommon.CTF_WRONG_DATA_RESP);//a bedzie czekal na kolejna wiadomosc wyslana pare linijek nizej? - rozsynchronizuje sie
                    response.setData(null);
                    outputStream.writeObject(response);
                    outputStream.flush();
                    * 
                    */
                }
                
                boolean validationCorrect = ctfdb.checkValidationCorrect(validationNo),
                        alreadyVoted = ctfdb.checkValidationUsed(validationNo),
                        idUsed = ctfdb.checkIdentificationUsed(identificationNo);

                System.out.println("Numer walidacyjny jest ok: " + validationCorrect + ". Uzytkownik juz glosowal: " + alreadyVoted + ". Numer identyfikacyjny jest juz zajety: " + idUsed);
                
                ResponseMessage response = new ResponseMessage();
                response.setData(null);

                if (validationCorrect) {
                    if (!alreadyVoted) {
                        if (!idUsed) {
                            ctfdb.markValidationUsed(validationNo);
                            ctfdb.addVote(identificationNo, candidate);
                            response.setStatus(EVotingCommon.CTF_OK_RESP);
                            System.out.println("Glos zostal oddany.");
                        } else {
                            response.setStatus(EVotingCommon.CTF_ID_USED_RESP);
                            System.out.println("Numer identyfikacyjny jest juz zajety.");
                        }
                    } else {
                        response.setStatus(EVotingCommon.CTF_VALIDATION_USED_RESP);
                            System.out.println("Uzytkownik o tym numerze walidacyjnym juz glosowal.");
                    }
                } else {
                    response.setStatus(EVotingCommon.CTF_VALIDATION_INCORRECT_RESP);
                }
                outputStream.writeObject(response);
                outputStream.flush();
            } else {
            }

        } catch (Exception e) {
            e.printStackTrace();
            ResponseMessage response = new ResponseMessage();
            response.setStatus(EVotingCommon.CTF_SERVER_ERROR_RESP);
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
        }
    }
}
