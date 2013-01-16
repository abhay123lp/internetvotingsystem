import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;

public class SimpleSSLClient {

    public static void main(String[] arstring) {
        System.out.println("OK - jestem w srodku klienta");
        /*
        System.setProperty("javax.net.ssl.trustStore", "..\\mySrvKeystore");
        System.setProperty("javax.net.ssl.keyStorePassword", "123456");
        */
        System.setProperty("javax.net.ssl.trustStore", "..\\..\\..\\System\\SSLKeys\\mojmagcertyfikatow");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456");
        
        
        try {
            SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket("localhost", 9999);

            InputStream inputstream = System.in;
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

            OutputStream outputstream = sslsocket.getOutputStream();
            OutputStreamWriter outputstreamwriter = new OutputStreamWriter(outputstream);
            BufferedWriter bufferedwriter = new BufferedWriter(outputstreamwriter);

            String string = null;
            while ((string = bufferedreader.readLine()) != null) {
                bufferedwriter.write(string + '\n');
                bufferedwriter.flush();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}