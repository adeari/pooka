package net.suberic.pooka.gui;
import javax.swing.*;
import java.awt.event.*;


/**
 * A simple implementation of ProgressDialog.  This creates and shows a
 * dialog with a JProgressBar and a cancel button.
 */
public class ProgressDialogImpl implements ProgressDialog {

  JProgressBar progressBar;
  JDialog dialog;
  boolean cancelled = false;
  int mCurrentValue;
  String mTitle;
  String mMessage;

  /**
   * Creates a ProgressDialogImpl with the given minimum, maximum, and
   * current values.
   */
  public ProgressDialogImpl(int min, int max, int current, String title, String message) {
    progressBar = new JProgressBar(min, max);

    mTitle = title;
    mMessage = message;

    dialog = new JDialog();
    dialog.getContentPane().setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));
    JLabel nameLabel = new JLabel(mTitle);
    JPanel buttonPanel = new JPanel();
    JButton cancelButton = new JButton(net.suberic.pooka.Pooka.getProperty("button.cancel", "Cancel"));
    cancelButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  cancelAction();
	}
      });
    buttonPanel.add(cancelButton);
    
    dialog.getContentPane().add(nameLabel);
    dialog.getContentPane().add(progressBar);
    dialog.getContentPane().add(buttonPanel);
    
    dialog.pack();
    
    setValue(current);
  }

  /**
   * Sets the minimum value for the progress dialog.
   */
  public void setMinimumValue(int minimum) {
    progressBar.setMinimum(minimum);
  }

  /**
   * Gets the minimum value for the progress dialog.
   */
  public int getMinimumValue() {
    return progressBar.getMinimum();
  }

  /**
   * Sets the maximum value for the progress dialog.
   */
  public void setMaximumValue(int maximum) {
    progressBar.setMaximum(maximum);
  }

  /**
   * Gets the maximum value for the progress dialog.
   */
  public int getMaximumValue() {
    return progressBar.getMaximum();
  }

  /**
   * Sets the current value for the progress dialog.
   */
  public void setValue(int current) {
    mCurrentValue = current;
    if (SwingUtilities.isEventDispatchThread())
      progressBar.setValue(mCurrentValue);
    else
      SwingUtilities.invokeLater(new Runnable() {
	  public void run() {
	    progressBar.setValue(mCurrentValue);
	  }
	});   
  }

  /**
   * Gets the current value for the progress dialog.
   */
  public int getValue() {
    return progressBar.getValue();
  }

  /**
   * Gets the title for the progress dialog.
   */
  public String getTitle() {
    return mTitle;
  }

  /**
   * Gets the display message for the progress dialog.
   */
  public String getMessage() {
    return mMessage;
  }
  
  /**
   * Cancels the current action.
   */
  public void cancelAction() {
    cancelled = true;
  }

  /**
   * Returns whether or not this action has been cancelled.
   */
  public boolean isCancelled() {
    return cancelled;
  }

  /**
   * Shows the dialog.
   */
  public void show() {
    if (SwingUtilities.isEventDispatchThread())
      dialog.show();
    else
      SwingUtilities.invokeLater(new Runnable() {
	  public void run() {
	    dialog.show();
	  }
	});
  }

  /**
   * Disposes of the dialog.
   */
  public void dispose() {
    if (SwingUtilities.isEventDispatchThread())
      dialog.dispose();
    else
      SwingUtilities.invokeLater(new Runnable() {
	  public void run() {
	    dialog.dispose();
	  }
	});
  }
}
