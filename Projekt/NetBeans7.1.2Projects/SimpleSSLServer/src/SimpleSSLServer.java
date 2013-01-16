
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleSSLServer {

    public static void main(String[] arstring) {
        System.out.println("OK - Jestem w srodku serwera");
        try {
            System.out.println(new File(".").getCanonicalPath());
            //to samo mozna wprowadzic z linii komend uzywajac parametrow -Djavax.net.ssl.keyStore=..\\..\\..\\mySrvKeystore -D...
            //zwroc uwage ze inny jest katalog biezacy jesli uruchamiasz z linii komend a inny z netbeansa!!!
            /*
            System.setProperty("javax.net.ssl.keyStore", "..\\mySrvKeystore");
            System.setProperty("javax.net.ssl.keyStorePassword", "123456");
            */
            
            System.setProperty("javax.net.ssl.keyStore", "..\\..\\..\\System\\SSLKeys\\MojMagazyn");
            System.setProperty("javax.net.ssl.keyStorePassword", "123456");
            
            SSLServerSocketFactory sslserversocketfactory =
                    (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket sslserversocket =
                    (SSLServerSocket) sslserversocketfactory.createServerSocket(9999);
            SSLSocket sslsocket = (SSLSocket) sslserversocket.accept();
            
            InputStream inputstream = sslsocket.getInputStream();
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

            String string = null;
            while ((string = bufferedreader.readLine()) != null) {
                System.out.println(string);
                System.out.flush();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
