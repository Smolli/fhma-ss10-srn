package de.fhma.ss10.srn.tischbein.core.db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.ListModel;

import de.fhma.ss10.srn.tischbein.core.Utils;

/**
 * Datanbankklasse. Kapselt die gesamte Datenbankstruktur.
 * 
 * @author Smolli
 */
public final class Database {

    /** Standard-Datei für die User-Tabelle. */
    private static final String DB_USERS_TB = "db/users.tb";
    /** CSV-Separator. */
    private static final char SEPARATOR = ';';
    /** Singleton-Instanz der Datenbank. */
    private static Database instance = null;

    /**
     * Gibt die Instanz der Datenbank zurück. Wenn die Datenbank nicht gestartet werden kann, wird eine
     * <code>RuntimeException</code> mit dem Grund geworfen.
     * 
     * @return Gibt die Instanz der Datenbank zurück.
     */
    public static synchronized Database getInstance() {
        if (Database.instance == null) {
            try {
                Database.instance = Database.open();
            } catch (Exception e) {
                e.printStackTrace();

                throw new RuntimeException("FATAL: Kann keine Instanz der Datenbank erzeugen!", e);
            }
        }

        return Database.instance;
    }

    /**
     * Öffnet ein bestehendes Datenbankschema.
     * 
     * @return Gibt die geladene Datenbank zurück.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Datenbankstruktur korrupt ist oder die Datenbank aus anderen Gründen nicht
     *             geladen werden konnte.
     */
    private static Database open() throws DatabaseException {
        try {
            Database db = new Database();

            db.loadUsers();
            //      db.  loadFiles();

            return db;
        } catch (Exception e) {
            throw new DatabaseException("Kann die Datenbankstruktur nicht laden!", e);
        }
    }

    /** Enthält alle bekannten Benutzer in einer Map. */
    private final TreeMap<String, User> users = new TreeMap<String, User>();

    /**
     * Privater ctor, um die Instanziierung außerhalb zu verhindern.
     */
    private Database() {
    }

    /**
     * Erzeugt einen neuen Benutzer mit dem übergebenen Namen und Passwort. Es werden alle Schlüssel und die
     * Benutzereigenen Tabellen angelegt.
     * 
     * @param name
     *            Der Benutzername.
     * @param pass
     *            Das benutzerpasswort.
     * @throws DatabaseException
     *             Wird geworfen, wenn der Benutzer nicht angelegt werden konnte.
     */
    public void createUser(final String name, final String pass) throws DatabaseException {
        try {
            if (this.users.containsKey(name.toLowerCase())) {
                throw new DatabaseException("Benutzer existiert schon!");
            }

            User user = User.create(name, pass);

            this.saveToUsers(user, pass);
        } catch (Exception e) {
            throw new DatabaseException("Fehler beim Anlegen des neuen Benutzers!", e);
        }
    }

    /**
     * Gibt einen <code>Vector<code> mit allen bekannten Benutzernamen zurück.
     * 
     * @param user
     * @deprecated Diese Methode wird demnächst ersetzt. Ziel ist es ein {@link ListModel} zu erzeugen, in dem Alle
     *             Benutzernamen und der Wert des Zuordnungshäkchens gespeichert ist.
     * @return Der Vector mit allen Benutzernamen.
     */
    @Deprecated
    public Vector<String> getUserList(final User user) {
        return new Vector<String>(this.users.keySet());
    }

    public User loginUser(final String name, final String pass) throws DatabaseException {
        try {
            User user = this.users.get(name.toLowerCase());

            if (user == null) {
                throw new DatabaseException("Benutzer existiert nicht!");
            }

            if (!user.unlock(pass)) {
                throw new DatabaseException("Falsches Passwort!");
            }

            return user;
        } catch (Exception e) {
            throw new DatabaseException("Fehler beim Einloggen!", e);
        }
    }

    private void loadUsers() throws DatabaseException {
        try {
            BufferedReader fr = new BufferedReader(new FileReader(Database.DB_USERS_TB));
            String line;

            while ((line = fr.readLine()) != null) {
                User user = User.parse(line);

                this.users.put(user.getName().toLowerCase(), user);
            }

            fr.close();
        } catch (Exception e) {
            throw new DatabaseException("Kann die Benutzertabelle nicht laden!", e);
        }
    }

    private void saveToUsers(final User user, final String pass) throws DatabaseException {
        try {
            FileWriter fw = new FileWriter(Database.DB_USERS_TB, true);
            StringBuilder userLine = new StringBuilder();

            userLine.append(user.getName());
            userLine.append(Database.SEPARATOR);
            userLine.append(user.getPassHash());
            userLine.append(Database.SEPARATOR);
            userLine.append(Utils.toHexString(user.getPublicKey().getEncoded()));
            userLine.append(Database.SEPARATOR);
            userLine.append(Utils.toHexString(Utils.encrypt(user.getPrivateKey().getEncoded(), pass)));

            fw.append(userLine);
            fw.append("\n");
            fw.flush();

            fw.close();
        } catch (Exception e) {
            throw new DatabaseException("Fehler beim Schreiben in die Users-Tabelle!", e);
        }
    }
}
