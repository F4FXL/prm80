/*
 * ConfigurationDialog.java
 *   Copyright (c) 2007, 2008 Florian MAZEN
 *   
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * Created on 4 mars 2008, 12:36
 */

package f4fez.prm80x0.gui;

import f4fez.prm80x0.*;
import gnu.io.CommPortIdentifier;
import java.util.Enumeration;

/**
 *
 * @author  fmazen
 */
public class ConfigurationDialog extends javax.swing.JDialog {
    
    private Option config;
    
    /** Creates new form ConfigurationDialog */
    public ConfigurationDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        fillComboBoxWithSerialPorts();
        this.portList.setSelectedIndex(0);
        
        config = new Option();
        this.config.setSerialPort(this.portList.getItemAt(0).toString());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        portListLabel = new javax.swing.JLabel();
        portList = new javax.swing.JComboBox();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(f4fez.prm80x0.PRM80X0App.class).getContext().getResourceMap(ConfigurationDialog.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setMinimumSize(new java.awt.Dimension(260, 60));
        setName("Form"); // NOI18N
        setResizable(false);
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.LINE_AXIS));

        mainPanel.setMinimumSize(new java.awt.Dimension(270, 100));
        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setOpaque(false);
        mainPanel.setPreferredSize(new java.awt.Dimension(270, 100));
        mainPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        portListLabel.setText(resourceMap.getString("portListLabel.text")); // NOI18N
        portListLabel.setName("portListLabel"); // NOI18N
        portListLabel.setPreferredSize(new java.awt.Dimension(30, 20));
        mainPanel.add(portListLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        portList.setName("portList"); // NOI18N
        mainPanel.add(portList, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 20, 190, -1));

        okButton.setText(resourceMap.getString("okButton.text")); // NOI18N
        okButton.setName("okButton"); // NOI18N
        okButton.setPreferredSize(new java.awt.Dimension(65, 23));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        mainPanel.add(okButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 60, -1, -1));

        cancelButton.setText(resourceMap.getString("cancelButton.text")); // NOI18N
        cancelButton.setName("cancelButton"); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        mainPanel.add(cancelButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 60, -1, -1));

        getContentPane().add(mainPanel);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.setVisible(false);
}//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        this.config.setSerialPort(this.portList.getSelectedItem().toString());
        this.setVisible(false);
}//GEN-LAST:event_okButtonActionPerformed
    
   
    private void fillComboBoxWithSerialPorts() {
        Enumeration thePorts = CommPortIdentifier.getPortIdentifiers();
        while (thePorts.hasMoreElements()) {
            CommPortIdentifier com = (CommPortIdentifier) thePorts.nextElement();
            switch (com.getPortType()) {
            case CommPortIdentifier.PORT_SERIAL:
                this.portList.addItem(com.getName());
            }
        }
    }
    
    /**
     * Initialise controls from a Option object
     * @param config Value to loads
     */
    public void setConfiguration(Option config) {
        this.config = config;
        this.okButton.setEnabled(true);   
        this.portList.setSelectedItem(config.getSerialPort());
    }
    
    /**
     * Obtain the current configuration parameters.
     * @return configuration
     */
    public Option getConfiguration() {
        return this.config;
    }
    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox portList;
    private javax.swing.JLabel portListLabel;
    // End of variables declaration//GEN-END:variables
    
}
