package net.suberic.util.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 * A Display panel which has a button which, on pressing, will display
 * the stack trace for the given exception.
 */
public class ExceptionDisplayPanel extends JPanel {
  
  // the Exception whose stack trace will be displayed.
  Exception mException;

  // the Button
  JButton mButton;

  /**
   * Creates the ExceptionDisplayPanel using the given text for the
   * button and the given exception.
   */
  public ExceptionDisplayPanel(String pButtonText, Exception pException) {
    super();

    this.setLayout(new CardLayout());

    mException = pException;
    
    mButton = new JButton(pButtonText);
    
    Box buttonBox = Box.createHorizontalBox();
    buttonBox.add(Box.createHorizontalGlue());
    buttonBox.add("BUTTON", mButton);
    buttonBox.add(Box.createHorizontalGlue());

    this.add("BUTTON", buttonBox);

    mButton.addActionListener(new AbstractAction() {

	public void actionPerformed(ActionEvent ae) {
	  showStackTrace();
	}
      });
  }

  /**
   * Expands the display to show the stack trace for the exception.
   */
  public void showStackTrace() {
    // first make the stack trace.
    StringWriter exceptionWriter = new StringWriter();
    mException.printStackTrace(new PrintWriter(exceptionWriter));
    String exceptionString = exceptionWriter.toString();
    
    // now make the display location.
    JTextArea jta = new JTextArea(exceptionString);
    jta.setEditable(false);
    JScrollPane jsp = new JScrollPane(jta);

    this.add("EXCEPTION", jsp);

    ((CardLayout) getLayout()).show(this, "EXCEPTION");
    Dimension currentMinimum = getMinimumSize();
    this.setMinimumSize(new Dimension(Math.max(currentMinimum.width, 150), Math.max(currentMinimum.height, 100)));

    JInternalFrame parentIntFrame = null;
    try {
      parentIntFrame = (JInternalFrame) SwingUtilities.getAncestorOfClass(Class.forName("javax.swing.JInternalFrame"), this);
    } catch (Exception e) {
    }
    if (parentIntFrame != null) {
      parentIntFrame.pack();
      //parentIntFrame.resize();
    } else {
      Window parentWindow = SwingUtilities.getWindowAncestor(this);
      if (parentWindow != null) {
	parentWindow.pack();
	//parentWindow.resize();
      }
    }
  }
}
