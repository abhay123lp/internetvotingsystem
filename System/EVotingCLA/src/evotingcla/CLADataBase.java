package evotingcla;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;


public class CLADataBase {

    static final String driverClassName = "org.apache.derby.jdbc.EmbeddedDriver";
    Connection connection;

    /**
     * @param dbAddress adres w postaci sciezki systemu plikow badz adresu URL
     * do katalogu z baza danych
     * @param username login uzytkownika bazy danych
     * @param password haslo uzytkownika bazy danych Konstruktor tworzy obiekt
     * BazaDanych, będącej trwałą reprezentacją stanu wszystkich obiektów
     * systemu.
     */
    public CLADataBase(String dbAddress, String username, String password) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        String connAddr = "jdbc:derby:" + dbAddress;
        Class.forName(driverClassName);
        Properties params = new Properties();
        params.setProperty("user", username);
        params.setProperty("password", password);
        params.setProperty("create", "true");
        connection = DriverManager.getConnection(connAddr, params);
    }

    /**
     * @param dbAddress jest to adres systemu plikow badz URL w ktorym chcemy
     * utworzyc pliki bazy danych
     * @param creatingScriptFileAddress jest to adres dyskowy do skryptu
     * budujacego
     * @param username nazwa właściciela bazy w SZBD
     * @param password haslo dla konta o nazwie nazwa
     */
    public static void createDBOnDisk(String dbAddress, String creatingScriptFileAddress, String username, String password) throws SQLException, FileNotFoundException, ClassNotFoundException {
        String connAddr = "jdbc:derby:" + dbAddress;
        Connection connection = null;
        Class.forName(driverClassName);
        Properties params = new Properties();
        params.setProperty("user", username);
        params.setProperty("password", password);
        params.setProperty("create", "true");
        connection = DriverManager.getConnection(connAddr, params);
        Statement statement = null;
        Scanner sc = new Scanner(new BufferedReader(new FileReader(creatingScriptFileAddress)));
        String DDLFile = "";
        while (sc.hasNextLine()) {
            DDLFile += sc.nextLine();
        }
        for (String DDLStatement : DDLFile.split(";")) {//tak sie oddziela poszczegolne instrukcje sql
            System.out.println(DDLStatement);
            if (DDLStatement != null && !"".equals(DDLStatement)) {
                statement = connection.createStatement();
                statement.execute(DDLStatement);
            }
        }
    }

    public static void populate(String dbAddress, String populatingScriptFileAddress, String username, String password) throws ClassNotFoundException, SQLException, FileNotFoundException {
        String connAddr = "jdbc:derby:" + dbAddress;
        Connection connection = null;
        Class.forName(driverClassName);
        Properties params = new Properties();
        params.setProperty("user", username);
        params.setProperty("password", password);
        params.setProperty("create", "true");
        connection = DriverManager.getConnection(connAddr, params);

        Statement statement = null;
        Scanner sc = new Scanner(new BufferedReader(new FileReader(populatingScriptFileAddress)));
        String SQLInsertFile = "";
        while (sc.hasNextLine()) {
            SQLInsertFile += sc.nextLine();
        }
        for (String sqlStatement : SQLInsertFile.split(";")) {//tak sie oddziela poszczegolne instrukcje sql
            System.out.println(sqlStatement);
            if (sqlStatement != null && !"".equals(sqlStatement)) {
                statement = connection.createStatement();
                statement.execute(sqlStatement);
            }
        }
    }
    
    public boolean usernameMatchesPassword(String pesel, String password) throws SQLException, Exception {
        Statement stm = connection.createStatement();
        String SQLQuery = "SELECT * FROM CLATable WHERE pesel='" + pesel + "'";
        ResultSet result = stm.executeQuery(SQLQuery);
        if (result.next()) {
            if(pesel.equals(result.getString("pesel")) && password.equals(result.getString("password")))
                return true;
            else
                return false;
        } else {
            throw new Exception("Nie ma takiego glosujacego o nrze pesel: " + pesel);
        }
    }
    
    public boolean insertVoter(String pesel, String password) throws SQLException {
        Statement stm = null;
        String SQLInsertQuery = null;
        try {
            stm = connection.createStatement();
            SQLInsertQuery = "INSERT INTO CLATable VALUES('" + pesel + "', '" + password + "', NULL)";
            stm.executeUpdate(SQLInsertQuery);
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false;
        }
    }

    public boolean alterVoter(String pesel, String uniqueRandomNumber) throws SQLException {
        Statement stm = null;
        String SQLUpdateQuery = null;
        try {
            stm = connection.createStatement();
            SQLUpdateQuery = "UPDATE CLATable "
                    + " SET urn='" + uniqueRandomNumber + "' "
                    + " WHERE pesel='" + pesel + "' ";
            stm.executeUpdate(SQLUpdateQuery);
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false;
        }
    }

    public boolean alterVoter(String pesel, String password, String uniqueRandomNumber) throws SQLException {
        Statement stm = null;
        String SQLUpdateQuery = null;
        try {
            stm = connection.createStatement();
            SQLUpdateQuery = "UPDATE CLATable "
                    + " SET password='" + password + "', urn='" + uniqueRandomNumber + "' "
                    + "WHERE pesel='" + pesel + "' ";
            stm.executeUpdate(SQLUpdateQuery);
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false;
        }
    }

    public boolean userAlreadyRegistered(String pesel) throws SQLException, Exception {
        Statement stm = connection.createStatement();
        String SQLQuery = "SELECT * FROM CLATable WHERE pesel='" + pesel + "'";
        ResultSet result = stm.executeQuery(SQLQuery);
        if (result.next()) {
            String uri = result.getString("uri");
            if(uri == null || uri == "")
                return false;
            else
                return true;
        } else {
            throw new Exception("Nie ma takiego glosujacego o nrze pesel: " + pesel);
        }
    }

    public boolean URIAlreadyExists(String uri) throws SQLException {
        Statement stm = connection.createStatement();
        String SQLQuery = "SELECT * FROM CLATable WHERE uri='" + uri + "'";
        ResultSet result = stm.executeQuery(SQLQuery);
        if (result.next()) {
            return true;
        } else {
            return false;
        }
    }
    
    public List<String> registeredUsersUris() throws SQLException{
        List<String> list = new ArrayList<String>(100);
        Statement stm = connection.createStatement();
        String SQLQuery = "SELECT uri FROM CLATable WHERE uri IS NOT NULL";
        ResultSet result = stm.executeQuery(SQLQuery);
        while (result.next()) {
            String res;
            if(!"".equals(res = result.getString("uri")))
                list.add(res);
        }
        return list;
    }

    public void setAutoCommit(boolean on) throws SQLException{
        connection.setAutoCommit(on);
    }
    
    public void commit() throws SQLException{
        connection.commit();
    }
    
    public void rollback() throws SQLException{
        connection.rollback();
    }
}
