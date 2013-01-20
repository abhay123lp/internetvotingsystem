package evotingcommon;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EVotingCommon {

    public static int CLA_VALIDATION_REQ = 0,
            CTF_CANDIDATES_REQ = 0,
            CTF_VOTE_REQ = 1,
            CLA_OK_RESP = 0,
            CLA_WRONG_DATA_RESP = 1,
            CLA_SERVER_ERROR_RESP = 2,
            CTF_OK_RESP = 0,
            CTF_VALIDATION_INCORRECT_RESP = 1,
            CTF_VALIDATION_USED_RESP = 2,
            CTF_ID_USED_RESP = 3,
            CTF_SERVER_ERROR_RESP = 4,
            CTF_CANDIDATES_RESP = 5,
            CTF_WRONG_DATA_RESP = 6;
    public final static String MainDirectory;
    public final static String SSLKeyAndCertStorageDir;
    public final static String CLAKeyStoreAddr;
    public final static String CTFKeyStoreAddr;
    public final static String ClientTrustStoreAddr;
    public final static String CLADBAddr;
    public final static String CTFDBAddr;
    public final static String CLADBUsername;
    public final static String CLADBPassword;
    public final static String CTFDBUsername;
    public final static String CTFDBPassword;
    public final static String CLACreatingScriptFileAddress;
    public final static String CTFCreatingScriptFileAddress;
    public final static String CLAPopulatingScriptFileAddress;
    public final static String CTFPopulatingScriptFileAddress;
    public final static String CLAToCTFUrnFile;
    public final static String resultsHTMLAddress;
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

        CLADBAddr = MainDirectory + File.separator + "CLADB" + File.separator + "CLADB";
        CTFDBAddr = MainDirectory + File.separator + "CTFDB" + File.separator + "CTFDB";
        CLADBUsername = "admin";
        CLADBPassword = "admin1";
        CTFDBUsername = "admin";
        CTFDBPassword = "admin1";
        CLACreatingScriptFileAddress = MainDirectory + File.separator + "DerbyScripts" + File.separator + "CLADBStructure.sql";
        CTFCreatingScriptFileAddress = MainDirectory + File.separator + "DerbyScripts" + File.separator + "CTFDBStructure.sql";
        CLAPopulatingScriptFileAddress = MainDirectory + File.separator + "DerbyScripts" + File.separator + "populateCLADB.sql";
        CTFPopulatingScriptFileAddress = MainDirectory + File.separator + "DerbyScripts" + File.separator + "populateCTFDB.sql";
        CLAToCTFUrnFile = MainDirectory + File.separator + "UrnDump" + File.separator + "Urns.txt";
        resultsHTMLAddress = MainDirectory + File.separator + "Results" + File.separator + "results.txt";
    }

    public static void main(String[] args) {
    }
}
