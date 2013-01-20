package evotingctf;

import evotingcommon.EVotingCommon;
import evotingcommon.RequestMessage;
import evotingcommon.ResponseMessage;
import evotingcommon.ServerKiller;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class EVotingCTF extends Thread {

    private static CTFDataBase ctfdb;

    public static void main(String[] args) throws SQLException, FileNotFoundException, ClassNotFoundException, IOException {

        System.setProperty("javax.net.ssl.keyStore", EVotingCommon.SSLKeyAndCertStorageDir + "/CTFKeyStore");
        System.setProperty("javax.net.ssl.keyStorePassword", "123456");

        //Administrator podczas startu decyduje czy utworzyc nowa baze danych - w domyslnej strukturze katalogow
        //Decyzja ta powinna byc oparta na tym czy baza juz istnieje czy jeszcze nie i musimy to zrobic
        System.out.println("Jesli nie ma jeszcze bazy danych wpisz: utworz. W przeciwnym razie wcisnij cokolwiek.");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line = br.readLine();
        if (line.equals("utworz")) {
            System.out.println("Tworze baze danych ...");
            CTFDataBase.createDBOnDisk(EVotingCommon.CTFDBAddr, EVotingCommon.CTFCreatingScriptFileAddress, EVotingCommon.CTFDBUsername, EVotingCommon.CTFDBPassword);
            CTFDataBase.populate(EVotingCommon.CTFDBAddr, EVotingCommon.CTFPopulatingScriptFileAddress, EVotingCommon.CTFDBUsername, EVotingCommon.CTFDBPassword);
            CTFDataBase.readValidationNumbersFile(EVotingCommon.CTFDBAddr, EVotingCommon.CLAToCTFUrnFile, EVotingCommon.CTFDBUsername, EVotingCommon.CTFDBPassword);
        } else {
            System.out.println("Nie tworze niczego.");
        }


        try {
            ctfdb = new CTFDataBase(EVotingCommon.CTFDBAddr, EVotingCommon.CTFDBUsername, EVotingCommon.CTFDBPassword);
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(EVotingCTF.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Przechodze do nasluchu ...");

        ServerSocketFactory socketFactory = SSLServerSocketFactory.getDefault();
        ServerSocket serverSocket;
        try {
            serverSocket = (SSLServerSocket) socketFactory.createServerSocket(EVotingCommon.CTFPortNumber);
            serverSocket.setSoTimeout(10000);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Błąd tworzenia gniazda. Napraw usterkę i uruchom system ponownie.");
            return;
        }
        System.out.println("Debug");
        List<EVotingCTF> threads = new ArrayList<>();
        ServerKiller killer = new ServerKiller();
        killer.start();
        while (killer.isAlive()) {
            try {
                EVotingCTF newThread = new EVotingCTF((SSLSocket) serverSocket.accept());
                threads.add(newThread);
                newThread.start();
            } catch (SocketTimeoutException e) {
            } catch (IOException e) {
                System.err.println("BŁĄD: Nieudane połączenie z klientem...\n");
            }

        }
        for (EVotingCTF t : threads) {
            t.interrupt();
        }

        System.out.println("Zostanie sporządzona lista głosów oddanych na poszczególnych kandydatów i zapisana do pliku w formacie html.");
        Map<String, String> candidates = ctfdb.getCandidates();
        int n = candidates.size();
        StringBuilder html = new StringBuilder("<h1>Wyniki wyborów</h1>\n");
        int allVotes = ctfdb.getNumberOfVotes();
        int illegalVotes = ctfdb.getNumberOfIllegalVotes();
        Map<String, Integer> results = ctfdb.getResults();
        html.append("<p>Oddano głosów: ").append(allVotes).append(", w tym nieważnych: ").append(illegalVotes).append("</p>\n");
        html.append("<h2>Lista kandydatów wraz z wynikami:</h2>\n<ol>\n");
        for (String key : candidates.keySet()) {

            html.append("<li>").append(candidates.get(key)).append(": ").append(100.0 * (results.get(key) == null ? 0 : results.get(key)) / (allVotes==0?1:allVotes)).append("%</li>\n");
        }
        html.append("</ol>\n");
        html.append("<h2>Lista kandydatów wraz z listami głosów:</h2>\n<ul>\n");
        for (String cNumber : candidates.keySet()) {
            String cName = candidates.get(cNumber);

            List<String> ids = ctfdb.getVotesForCandidate(cNumber);
            html.append("<li>").append(cName).append(": ").append("\n<ol>\n");
            for (String id : ids) {
                html.append("<li>").append(id).append("</li>\n");
            }
            html.append("</ol>\n</li>\n<br/>");
        }
        html.append("</ul>\n").append("");
        String resultHTML = html.toString();
        File file = new File(EVotingCommon.resultsHTMLAddress);
        try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
            out.write(resultHTML);
            out.flush();
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

            boolean end = false;
            while (!end && !isInterrupted()) {
                RequestMessage request = (RequestMessage) inputStream.readObject();
                if (request.getType() == EVotingCommon.CTF_CANDIDATES_REQ) {
                    ResponseMessage response = new ResponseMessage();
                    response.setStatus(EVotingCommon.CTF_CANDIDATES_RESP);
                    Map<String, String> candidates = ctfdb.getCandidates();
                    List<String> responseData = new ArrayList<>();
                    for (String key : candidates.keySet()) {
                        responseData.add(key + ": " + candidates.get(key));
                    }
                    response.setData(responseData);
                    outputStream.writeObject(response);
                    outputStream.flush();
                } else if (request.getType() == EVotingCommon.CTF_VOTE_REQ) {
                    List<String> data = request.getData();

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
                                end = true;
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

            }
        } catch (EOFException e) {
            System.out.println("Użytkownik się rozłączył.");
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
