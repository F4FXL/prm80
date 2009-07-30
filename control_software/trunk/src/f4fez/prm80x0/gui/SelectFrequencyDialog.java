/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SelectFrequencyDialog.java
 *
 * Created on 26 juin 2009, 21:59:18
 */

package f4fez.prm80x0.gui;

import javax.swing.JOptionPane;

/**
 *
 * @author florian
 */
public class SelectFrequencyDialog extends javax.swing.JDialog {
     private int rxFrequency = -1;
    /** Creates new form SelectFrequencyDialog */
    public SelectFrequencyDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        frequencyPanel = new javax.swing.JPanel();
        rxFrequencyLabel = new javax.swing.JLabel();
        rxFrequencyTextField = new javax.swing.JTextField();
        buttonPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(f4fez.prm80x0.PRM80X0App.class).getContext().getResourceMap(SelectFrequencyDialog.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.PAGE_AXIS));

        frequencyPanel.setName("frequencyPanel"); // NOI18N
        frequencyPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        rxFrequencyLabel.setText(resourceMap.getString("rxFrequencyLabel.text")); // NOI18N
        rxFrequencyLabel.setName("rxFrequencyLabel"); // NOI18N
        frequencyPanel.add(rxFrequencyLabel);

        rxFrequencyTextField.setText(resourceMap.getString("rxFrequencyTextField.text")); // NOI18N
        rxFrequencyTextField.setName("rxFrequencyTextField"); // NOI18N
        rxFrequencyTextField.setPreferredSize(new java.awt.Dimension(100, 25));
        frequencyPanel.add(rxFrequencyTextField);

        getContentPane().add(frequencyPanel);

        buttonPanel.setName("buttonPanel"); // NOI18N

        okButton.setText(resourceMap.getString("okButton.text")); // NOI18N
        okButton.setName("okButton"); // NOI18N
        okButton.setPreferredSize(new java.awt.Dimension(70, 27));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(okButton);

        cancelButton.setText(resourceMap.getString("cancelButton.text")); // NOI18N
        cancelButton.setName("cancelButton"); // NOI18N
        cancelButton.setPreferredSize(new java.awt.Dimension(70, 27));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(cancelButton);

        getContentPane().add(buttonPanel);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.rxFrequency = -1;
        this.setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (this.rxFrequencyTextField.getText().matches("^[0-9]{3}\\.[0-9]{4}$")) {
            Float ff = Float.parseFloat(this.rxFrequencyTextField.getText().substring(4));
            if (ff % 12.5f == 0 ) {
                this.rxFrequency = Integer.parseInt(this.rxFrequencyTextField.getText().replace(".", "")+"00");
                this.setVisible(false);
            }
            else {
                JOptionPane.showMessageDialog(this, "La fréquence doit être un multiple du pas de la PLL.", "Erreur de format", JOptionPane.ERROR_MESSAGE);
            }
        }
        else {
            JOptionPane.showMessageDialog(this, "Le format de la fréquence n'est pas corecte.", "Erreur de format", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_okButtonActionPerformed


    public void setRxFrequency(int f) {
        String sf = Integer.toString(f);
        this.rxFrequencyTextField.setText(sf.substring(0, 3)+"."+sf.substring(3, 7));
    }
    public int getRxFrequency() {
        return this.rxFrequency;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel frequencyPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel rxFrequencyLabel;
    private javax.swing.JTextField rxFrequencyTextField;
    // End of variables declaration//GEN-END:variables

}