package net.suberic.pooka.gui;
import javax.swing.*;

public class InfoPanel extends JPanel {
  JLabel currentLabel = null;
  ConnectionMonitor monitor = null;
    public InfoPanel() {
      //super(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
      super(new java.awt.BorderLayout());
      currentLabel = new JLabel();
      this.add(currentLabel, java.awt.BorderLayout.WEST);
      
      monitor = new ConnectionMonitor();
      monitor.monitorConnectionManager(net.suberic.pooka.Pooka.getConnectionManager());
      this.add(monitor,java.awt.BorderLayout.EAST);

	javax.swing.border.Border border = BorderFactory.createLoweredBevelBorder();

	this.setBorder(border);
	this.setMinimumSize(getPreferredSize());
    }

    public void setMessage(String newMessage) {
	final String msg = newMessage;
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    currentLabel.setText(msg);
		    currentLabel.repaint();
		}
	    });
    }

    public void clear() {
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    currentLabel.setText("");
		    currentLabel.repaint();
		}
	    });
    }
}
