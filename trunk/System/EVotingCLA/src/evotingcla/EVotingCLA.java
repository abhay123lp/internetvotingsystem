package evotingcla;

import evotingcommon.EVotingCommon;
import evotingcommon.RequestMessage;
import evotingcommon.ResponseMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class EVotingCLA extends Thread {

    public static void main(String[] args) {
        
        //ustawienie zmiennych srodowiskowych dla implementacji javowej protokolu ssl
        
        System.setProperty("javax.net.ssl.keyStore", EVotingCommon.SSLKeyAndCertStorageDir + "/CLAKeyStore");
        System.setProperty("javax.net.ssl.keyStorePassword", "123456");
        
        //
        
        ServerSocketFactory socketFactory = SSLServerSocketFactory.getDefault();
        ServerSocket serverSocket;

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
                new EVotingCLA((SSLSocket)serverSocket.accept()).start();
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
            ResponseMessage response = new ResponseMessage();
            response.setStatus(0);
            List<String> data = new ArrayList<String>();
            data.add("135631265472452474525758");
            response.setData(data);
            outputStream.writeObject(response);

        } catch (Exception e) {
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
        }
    }
}
