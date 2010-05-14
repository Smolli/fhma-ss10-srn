package de.fhma.ss10.srn.tischbein.core.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.TreeMap;
import java.util.Vector;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import de.fhma.ss10.srn.tischbein.core.User;
import de.fhma.ss10.srn.tischbein.core.Utils;

/**
 * Datanbankklasse. Kapselt die gesamte Datenbankstruktur.
 * 
 * @author Smolli
 */
public final class Database {

    private static final String DB_USERS_TB = "db/users.tb";
    private static final char SEPARATOR = ';';
    private static Database instance = null;

    private final TreeMap<String, User> users;
    private String rootPath;

    private Database() {
        this.users = new TreeMap<String, User>();
    }

    public static synchronized Database getInstance() {
        if (Database.instance == null) {
            try {
                Database.instance = Database.open("/");
            } catch (IOException e) {
                e.printStackTrace();

                Database.instance = null;
            }
        }

        return Database.instance;
    }

    private static Database open(final String root) throws IOException {
        Database db = new Database();
        File path = new File(root);

        if (!path.isDirectory()) {
            throw new IllegalArgumentException("Root path argument must be a directory!");
        }

        db.setRootDir(root);

        db.loadUsers();
        //      db.  loadFiles();

        return db;
    }

    private void loadUsers() throws IOException {
        BufferedReader fr = new BufferedReader(new FileReader(Database.DB_USERS_TB));
        String line;

        while ((line = fr.readLine()) != null) {
            User user = User.read(line);

            this.users.put(user.getName().toLowerCase(), user);
        }
    }

    private void setRootDir(final String root) {
        this.rootPath = root;
    }

    public void createUser(final String name, final String pass) throws InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, IOException {
        if (this.users.containsKey(name.toLowerCase())) {
            throw new IllegalArgumentException("Benutzer existiert schon!");
        }

        User user = User.create(name, pass);

        this.saveToUsers(user, pass);
    }

    private void saveToUsers(final User user, final String pass) throws IOException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        FileWriter fw = new FileWriter(Database.DB_USERS_TB, true);
        StringBuilder userLine = new StringBuilder();

        userLine.append(user.getName());
        userLine.append(Database.SEPARATOR);
        userLine.append(user.getPassHash());
        userLine.append(Database.SEPARATOR);
        userLine.append(Utils.toHex(user.getPublicKey().getEncoded()));
        userLine.append(Database.SEPARATOR);
        userLine.append(Utils.toHex(Utils.encrypt(user.getPrivateKey().getEncoded(), pass)));

        fw.append(userLine);
        fw.append("\n");
        fw.flush();

        fw.close();
    }

    public User loginUser(final String name, final String pass) throws InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {
        User user = this.users.get(name.toLowerCase());

        if (user == null) {
            throw new IllegalArgumentException("Benutzer existiert nicht!");
        }

        if (!user.unlock(pass)) {
            throw new IllegalArgumentException("Falsches Passwort!");
        }

        return user;
    }

    public Vector<String> getUserList(final User user) {
        return new Vector<String>(this.users.keySet());
    }
}
