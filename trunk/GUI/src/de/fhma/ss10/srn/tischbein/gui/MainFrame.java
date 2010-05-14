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

import de.fhma.ss10.srn.tischbein.core.User;
import de.fhma.ss10.srn.tischbein.core.db.Database;
import de.fhma.ss10.srn.tischbein.gui.actions.LoginAction;
import de.fhma.ss10.srn.tischbein.gui.actions.NewUserAction;

public class MainFrame extends JFrame {

    private JPasswordField userpass;
    private JTextField username;

    public MainFrame() {
        this.initFrame();

        this.initComponents();
    }

    private void initComponents() {
        BorderLayout layout = new BorderLayout();

        this.getContentPane().setLayout(layout);

        this.getContentPane().add(this.createLoginPanel(), BorderLayout.CENTER);
    }

    private Container createLoginPanel() {
        GridLayout layout = new GridLayout(4, 2);
        JPanel loginPanel = new JPanel();

        loginPanel.setLayout(layout);

        loginPanel.add(new JLabel("Tischbein"));
        loginPanel.add(new JLabel("Login"));

        loginPanel.add(new JLabel("Benutzername:"));
        loginPanel.add(this.username = new JTextField());

        loginPanel.add(new JLabel("Passwort:"));
        loginPanel.add(this.userpass = new JPasswordField());

        loginPanel.add(new JButton(new LoginAction()));
        loginPanel.add(new JButton(new NewUserAction()));

        return loginPanel;
    }

    private void initFrame() {
        this.setTitle("Tischbein v0.1");
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        this.setSize(800, 600);
    }

    public JPasswordField getUserpass() {
        return this.userpass;
    }

    public JTextField getUsername() {
        return this.username;
    }

    public void setUser(final User user) {
        this.getContentPane().removeAll();

        this.createWorkbench(user);

        this.getContentPane().repaint();
        this.getContentPane().validate();
    }

    private void createWorkbench(final User user) {
        JList list = new JList(Database.getInstance().getUserList(user));

        this.getContentPane().add(list, BorderLayout.EAST);

        this.getContentPane().add(new JButton("Logout"), BorderLayout.SOUTH);
        
        this.getContentPane().add(new JLabel("Tischbein v0.1"),BorderLayout.NORTH);
    }

}
