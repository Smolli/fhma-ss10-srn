package de.fhma.ss10.srn.tischbein.gui.forms;

// CHECKSTYLE:OFF
public class LoginForm extends javax.swing.JFrame {

    private static final long serialVersionUID = -1640306132743702745L;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JButton addUserButton;
    protected javax.swing.JButton closeButton;
    protected javax.swing.JButton loginButton;
    protected javax.swing.JPasswordField passwordField;
    protected javax.swing.JComboBox usernameField;

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
        this.loginButton = new javax.swing.JButton();
        this.addUserButton = new javax.swing.JButton();
        this.closeButton = new javax.swing.JButton();
        javax.swing.JPanel northPanel = new javax.swing.JPanel();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel(
                de.fhma.ss10.srn.tischbein.gui.launcher.Launcher.PRODUCT_NAME);
        javax.swing.JPanel centerPanel = new javax.swing.JPanel();
        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        this.usernameField = new javax.swing.JComboBox();
        javax.swing.JLabel jLabel3 = new javax.swing.JLabel();
        this.passwordField = new javax.swing.JPasswordField();

        this.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        this.setTitle("Login...");
        this.setLocationByPlatform(true);

        this.loginButton.setText("Login");
        this.loginButton.setEnabled(false);
        southPanel.add(this.loginButton);

        this.addUserButton.setText("Neuer Benutzer");
        this.addUserButton.setEnabled(false);
        southPanel.add(this.addUserButton);

        this.closeButton.setText("Beenden");
        southPanel.add(this.closeButton);

        this.getContentPane().add(southPanel, java.awt.BorderLayout.SOUTH);

        jLabel1.setFont(new java.awt.Font("Candara", 1, 18)); // NOI18N
        northPanel.add(jLabel1);

        this.getContentPane().add(northPanel, java.awt.BorderLayout.NORTH);

        jPanel2.setLayout(new java.awt.GridLayout(2, 2));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Benutzername: ");
        jPanel2.add(jLabel2);

        this.usernameField.setEditable(true);
        this.usernameField.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3",
                "Item 4" }));
        jPanel2.add(this.usernameField);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Passwort: ");
        jLabel3.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        jPanel2.add(jLabel3);
        jPanel2.add(this.passwordField);

        centerPanel.add(jPanel2);

        this.getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

        this.pack();
    }// </editor-fold>//GEN-END:initComponents

}
//CHECKSTYLE:ON
