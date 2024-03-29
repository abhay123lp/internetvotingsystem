package evotingctf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

public class CTFDataBase {

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
    public CTFDataBase(String dbAddress, String username, String password) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
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
            if (sqlStatement != null && !"".equals(sqlStatement)) {
                statement = connection.createStatement();
                statement.execute(sqlStatement);
                System.out.println(sqlStatement);
            }
        }
    }

    public static void readValidationNumbersFile(String dbAddress, String validationFileAddress, String username, String password) throws ClassNotFoundException, SQLException {
        File file = null;
        Scanner sc = null;
        try {
            file = new File(validationFileAddress);
            sc = new Scanner(file);
        } catch (FileNotFoundException e) {
            System.out.println("Nie znaleziono podanego pliku. Nie wczytuję danych dotyczących numerów walidacyjnych!");
            return;
        }
        List<String> urns = new ArrayList<>();
        while (sc.hasNext()) {
            urns.add(sc.next());
        }
        sc.close();

        String connAddr = "jdbc:derby:" + dbAddress;
        Connection connection = null;
        Class.forName(driverClassName);
        Properties params = new Properties();
        params.setProperty("user", username);
        params.setProperty("password", password);
        params.setProperty("create", "true");
        connection = DriverManager.getConnection(connAddr, params);
        Statement stmt = connection.createStatement();
        StringBuilder insert = new StringBuilder("INSERT INTO validList (urn,used) VALUES ");
        int urnsSize = urns.size();
        for (int i = 0; i < urnsSize; i++) {
            insert.append("('").append(urns.get(i)).append("', 0)");
            if (i < urnsSize - 1) {
                insert.append(",\n");
            }
        }
        System.out.println(insert.toString());
        stmt.executeUpdate("DELETE FROM validList");
        stmt.executeUpdate(insert.toString());
    }

    public Map<String, String> getCandidates() throws SQLException {
        Statement stmt = connection.createStatement();
        String SQLQuery = "SELECT * FROM candidates";
        ResultSet rs = stmt.executeQuery(SQLQuery);
        Map<String, String> candidates = new HashMap<>();
        while (rs.next()) {
            String c_id = rs.getString("c_id"), name = rs.getString("first_name"), surname = rs.getString("last_name");
            candidates.put(c_id, name + " " + surname);
        }
        return candidates;
    }

    public boolean checkValidationCorrect(String validationNo) throws SQLException {
        Statement stmt = connection.createStatement();
        String SQLQuery = "SELECT * FROM validList WHERE urn='" + validationNo + "'";
        ResultSet rs = stmt.executeQuery(SQLQuery);
        if (rs.next()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkValidationUsed(String validationNo) throws SQLException, Exception {
        Statement stmt = connection.createStatement();
        String SQLQuery = "SELECT * FROM validList WHERE urn='" + validationNo + "'";
        ResultSet rs = stmt.executeQuery(SQLQuery);

        if (rs.next()) {
            int used = rs.getInt("used");
            if (used == 0) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
            //throw new Exception("Nie ma glosujacego o takim urn!");
        }
    }

    public void markValidationUsed(String validationNo) throws SQLException {
        Statement stmt = connection.createStatement();
        String SQLUpdateQuery = "UPDATE validList SET used=1 WHERE urn='" + validationNo + "'";
        stmt.executeUpdate(SQLUpdateQuery);
    }

    public boolean checkIdentificationUsed(String identificationNo) throws SQLException {
        Statement stmt = connection.createStatement();
        String SQLQuery = "SELECT * FROM votes WHERE id='" + identificationNo + "'";
        ResultSet rs = stmt.executeQuery(SQLQuery);

        if (rs.next()) {
            return true;
        } else {
            return false;
        }
    }

    public void addVote(String identificationNo, int candidate) throws SQLException {
        Statement stmt = connection.createStatement();
        String SQLInsertQuery = "INSERT INTO votes (id,vote) VALUES ('" + identificationNo + "', " + candidate + ")";
        stmt.executeUpdate(SQLInsertQuery);
    }

    public List<String> getVotesForCandidate(String candidateNumber) throws SQLException {
        Statement stmt = connection.createStatement();
        String SQLQuery = "SELECT id FROM votes WHERE vote=" + candidateNumber;
        ResultSet rs = stmt.executeQuery(SQLQuery);
        List<String> votes = new ArrayList<>();
        while (rs.next()) {
            String id = rs.getString("id");
            votes.add(id);
        }
        return votes;
    }

    public int getNumberOfVotes() throws SQLException {

        Statement stmt = connection.createStatement();
        String SQLQuery = "SELECT COUNT(*) FROM votes";
        ResultSet rs = stmt.executeQuery(SQLQuery);
        rs.next();
        return rs.getInt(1);
    }

    public int getNumberOfIllegalVotes() throws SQLException {

        Statement stmt = connection.createStatement();
        String SQLQuery = "SELECT COUNT(*) FROM votes WHERE vote NOT IN (SELECT c_id FROM candidates)";
        ResultSet rs = stmt.executeQuery(SQLQuery);
        rs.next();
        return rs.getInt(1);
    }

    public Map<String, Integer> getResults() throws SQLException {
        Statement stmt = connection.createStatement();
        String SQLQuery = "SELECT vote, COUNT(*) AS result FROM votes WHERE vote IN (SELECT c_id FROM candidates) GROUP BY vote ORDER BY result";
        ResultSet rs = stmt.executeQuery(SQLQuery);
        Map<String, Integer> results = new HashMap<>();
        while (rs.next()) {
            String c_id = new Integer(rs.getInt("vote")).toString();
            int result = rs.getInt("result");
            results.put(c_id, result);
        }
        return results;
    }
}
