package de.fhma.ss10.srn.tischbein.gui.forms;

// CHECKSTYLE:OFF
public class LoginForm extends javax.swing.JFrame {

    private static final long serialVersionUID = -1640306132743702745L;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JButton addUserButton;
    protected javax.swing.JButton closeButton;
    protected javax.swing.JButton loginButton;
    protected javax.swing.JPasswordField passwordField;
    protected javax.swing.JTextField usernameField;
    // End of variables declaration//GEN-END:variables
    /** Creates new form MFrame */
    public LoginForm() {
        this.initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        javax.swing.JPanel southPanel = new javax.swing.JPanel();
        loginButton = new javax.swing.JButton();
        addUserButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        javax.swing.JPanel northPanel = new javax.swing.JPanel();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        javax.swing.JPanel centerPanel = new javax.swing.JPanel();
        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        usernameField = new javax.swing.JTextField();
        javax.swing.JLabel jLabel3 = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Tischbein v0.1 - Login");
        setLocationByPlatform(true);

        loginButton.setText("Login");
        southPanel.add(loginButton);

        addUserButton.setText("Neuer Benutzer");
        southPanel.add(addUserButton);

        closeButton.setText("Beenden");
        southPanel.add(closeButton);

        getContentPane().add(southPanel, java.awt.BorderLayout.SOUTH);

        jLabel1.setFont(new java.awt.Font("Candara", 1, 18));
        jLabel1.setText("Tischbein v0.1");
        northPanel.add(jLabel1);

        getContentPane().add(northPanel, java.awt.BorderLayout.NORTH);

        jPanel2.setLayout(new java.awt.GridLayout(2, 2));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Benutzername: ");
        jPanel2.add(jLabel2);
        jPanel2.add(usernameField);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Passwort: ");
        jLabel3.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        jPanel2.add(jLabel3);
        jPanel2.add(passwordField);

        centerPanel.add(jPanel2);

        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

}
//CHECKSTYLE:ON
