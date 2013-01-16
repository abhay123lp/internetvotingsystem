package evotingcommon;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EVotingCommon {

    public final static String MainDirectory;
    public final static String SSLKeyAndCertStorageDir;
    public final static String CLAKeyStoreAddr;
    public final static String CTFKeyStoreAddr;
    public final static String ClientTrustStoreAddr;
    
    public final static String CLAIPAddress = "localhost", CTFIPAddress = "localhost";
    public final static int CLAPortNumber = 8102, CTFPortNumber = 8103;

    static {
        String formalHelper = "..";
        try {
            formalHelper = new File("..").getCanonicalPath();
        } catch (IOException ex) {
            System.out.println("Problemy z ustaleniem katalogu macierzystego - sprawdz czy to oby na pewno ten, gdyz inaczej java nie odnajdzie magazynu certyfikatow.");
            Logger.getLogger(EVotingCommon.class.getName()).log(Level.SEVERE, null, ex);
        }
        MainDirectory = formalHelper;
        SSLKeyAndCertStorageDir = MainDirectory + File.separator + "SSLKeys";//Trzeba sprawdzic czy to na pewno bedzie ten katalog w strukturze
        CLAKeyStoreAddr = SSLKeyAndCertStorageDir + File.separator + "CLAKeyStore";
        CTFKeyStoreAddr = SSLKeyAndCertStorageDir + File.separator + "CTFKeyStore";
        ClientTrustStoreAddr = SSLKeyAndCertStorageDir + File.separator + "VotComCertMag";
    }

    public static void main(String[] args) {
    }
}
