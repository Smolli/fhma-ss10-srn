package de.fhma.ss10.srn.tischbein.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import de.fhma.ss10.srn.tischbein.core.db.Database;
import de.fhma.ss10.srn.tischbein.core.db.User;
import de.fhma.ss10.srn.tischbein.gui.actions.LoginAction;
import de.fhma.ss10.srn.tischbein.gui.actions.NewUserAction;

/**
 * Hauptfenster.
 * 
 * @author Smolli
 */
public final class MainFrame extends JFrame {

    /** Serial UID. */
    private static final long serialVersionUID = 5425989856036812026L;

    /** Fensterhöhe. */
    private static final int DEFAULT_APP_HEIGHT = 600;
    /** Fensterbreite. */
    private static final int DEFAULT_APP_WIDTH = 800;

    /** Hält das Benutzerpasswort. */
    private JPasswordField userpass;
    /** Hält den Benutzernamen. */
    private JTextField username;
    /** Hält das Login-Panel. */
    private Container loginPanel;
    /** Östlicher Arbeitsplatzbereich. */
    private Container wbEast;
    /** Nördlicher Arbeitsplatzbereich. */
    private Container wbNorth;
    /** Südlicher Arbeitsplatzbereich. */
    private Container wbSouth;
    /** Hält das Benutzerobjekt. */
    private User user;

    /**
     * Erstellt ein neues Hauptfenster.
     */
    public MainFrame() {
        this.initFrame();

        this.initLoginView();
    }

    /**
     * Setzt den eingeloggten Benutzer und schaltet die Arbeitsansicht frei.
     * 
     * @param newUser
     *            Der Benutzer.
     */
    public void setUser(final User newUser) {
        this.user = newUser;

        this.initWorkbenchView();
    }

    /**
     * Erstellt das Login-Panel.
     */
    private void createLoginPanel() {
        GridLayout layout = new GridLayout(4, 2);
        this.loginPanel = new JPanel();

        this.userpass = new JPasswordField();
        this.username = new JTextField();

        this.loginPanel.setLayout(layout);

        this.loginPanel.add(new JLabel("Tischbein"));
        this.loginPanel.add(new JLabel("Login"));

        this.loginPanel.add(new JLabel("Benutzername:"));
        this.loginPanel.add(this.username);

        this.loginPanel.add(new JLabel("Passwort:"));
        this.loginPanel.add(this.userpass);

        this.loginPanel.add(new JButton(new LoginAction(this.username, this.userpass)));
        this.loginPanel.add(new JButton(new NewUserAction(this.username, this.userpass)));
    }

    /**
     * Erstellt das Arbeitsplatz-Panel.
     */
    private void createWorkbench() {
        this.createWorkbenchEast();
        this.createWorkbenchSouth();
        this.createWorkbenchNorth();
    }

    /**
     * Erstellt den östliche Arbeitsplatzbereich.
     */
    private void createWorkbenchEast() {
        this.wbEast = new JPanel();
        JList list = new JList(Database.getInstance().getUserList());

        this.wbEast.add(list);
    }

    /**
     * Erstellt den nördlichen Arbeitsplatzbereich.
     */
    private void createWorkbenchNorth() {
        this.wbNorth = new JPanel();

        this.wbNorth.add(new JLabel("Tischbein v0.1"));
    }

    /**
     * Erstellt den südlichen Arbeitsplatzbereich.
     */
    private void createWorkbenchSouth() {
        this.wbSouth = new JPanel();

        this.wbSouth.add(new JButton("Logout"));
    }

    /**
     * Initialisiert das Fenster.
     */
    private void initFrame() {
        this.setTitle("Tischbein v0.1");
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        this.setSize(MainFrame.DEFAULT_APP_WIDTH, MainFrame.DEFAULT_APP_HEIGHT);
    }

    /**
     * Initialisiert die Login-Ansicht und zeigt sie an.
     */
    private void initLoginView() {
        BorderLayout layout = new BorderLayout();

        this.getContentPane().setLayout(layout);

        this.createLoginPanel();

        this.getContentPane().add(this.loginPanel, BorderLayout.CENTER);
    }

    /**
     * Initialisiert die Arbeitsplatz-Ansicht und zeigt sie an.
     */
    private void initWorkbenchView() {
        this.getContentPane().removeAll();

        this.createWorkbench();

        this.getContentPane().add(this.wbNorth, BorderLayout.NORTH);
        this.getContentPane().add(this.wbEast, BorderLayout.EAST);
        this.getContentPane().add(this.wbSouth, BorderLayout.SOUTH);

        this.getContentPane().repaint();
        this.getContentPane().validate();
    }

}
