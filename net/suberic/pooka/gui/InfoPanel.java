package net.suberic.pooka.gui;
import javax.swing.*;

public class InfoPanel extends JPanel {
    JLabel currentLabel = null;
    public InfoPanel() {
	super(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
	currentLabel = new JLabel();
	this.add(currentLabel);

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
