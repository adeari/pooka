package net.suberic.pooka.gui;

import javax.swing.*;
import javax.print.*;
import javax.print.event.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import net.suberic.pooka.*;

/**
 * Displays the current status of a print job in Pooka.
 */
public class MessagePrinterDisplay implements PrintJobListener {

  MessagePrinter mPrinter = null;
  DocPrintJob mJob = null;

  int mCurrentPage = 0;

  int mTotalPage = 0;

  String mCurrentDoc = "";

  String mTitle;

  JTextPane mDisplayPane = null;
  JButton mOkButton = null;
  JButton mCancelButton = null;

  boolean mInternal = false;

  JDialog mDialog = null;
  JInternalFrame mInternalFrame = null;

  /**
   * Creates a new MessagePrinterDisplay using the given MessagePrinter
   * and the given DocPrintJob.
   */
  public MessagePrinterDisplay(MessagePrinter pPrinter, DocPrintJob pJob) {
    mPrinter = pPrinter;
    mJob = pJob;
    mInternal = checkInternal();
    mPrinter.setDisplay(this);
  }

  /**
   * Refreshes the display pane to show the current printing status.
   */
  public void updateDisplayPane() {
    StringBuffer displayMessage = new StringBuffer();
    displayMessage.append("Printing:  ");
    displayMessage.append(mCurrentDoc);
    displayMessage.append("\r\n\r\n");
    displayMessage.append("Page ");
    displayMessage.append(mCurrentPage);
    displayMessage.append(" of ");
    displayMessage.append(mTotalPage);
    displayMessage.append("\r\n");

    final String msg = displayMessage.toString();
    if (SwingUtilities.isEventDispatchThread()) {
      mDisplayPane.setText(msg);
      mDisplayPane.repaint();
    } else {
      SwingUtilities.invokeLater(new Runnable() {
	  public void run() {
	    mDisplayPane.setText(msg);
	    mDisplayPane.repaint();
	  }
	});
    }
  }

  /**
   * Sets the current page.
   */
  public void setCurrentPage(int pCurrentPage) {
    mCurrentPage = pCurrentPage;
    updateDisplayPane();
  }
  
  /**
   * Checks to see if this is an internal dialog or not.
   */
  private boolean checkInternal() {
    return false;
  }

  /**
   * Shows the MessagePrinterDisplay.
   */
  public void show() {
    mDisplayPane = new JTextPane();

    if (mInternal) {

    } else {
      mDialog = new JDialog();
      mDialog.getContentPane().setLayout(new BoxLayout(mDialog.getContentPane(), BoxLayout.Y_AXIS));
      
      mDialog.getContentPane().add(mDisplayPane);

      Box buttonBox = createButtonBox();
      if (mJob instanceof CancelablePrintJob) {
	mDialog.getContentPane().add(buttonBox);
      }
      updateDisplayPane();
      mDialog.pack();
      mDialog.show();
    }

  }

  /**
   * Cancels the printjob.
   */
  public void cancel() {
    if (mJob instanceof CancelablePrintJob) {
      try {
	((CancelablePrintJob) mJob).cancel();
      } catch (PrintException e) {
	showError("Error canceling job:  ", e);
      }
    }
  }

  /**
   * Creates the buttonBox.
   */
  Box createButtonBox() {
    Box returnValue = new Box(BoxLayout.X_AXIS);
    JButton cancelButton = new JButton(Pooka.getProperty("button.cancel", "Cancel"));
    returnValue.add(cancelButton);
    cancelButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  cancel();
	}
      });
    
    return returnValue;
    
  }

  /**
   * Closes the MessageDisplay.
   */
  public void dispose() {
    if (SwingUtilities.isEventDispatchThread()) {
      if (mInternal) {

      } else {
	mDialog.dispose();
      }
    } else {
      SwingUtilities.invokeLater(new Runnable() {
	  public void run() {
	    if (mInternal) {

	    } else {
	      mDialog.dispose();
	    }
	  }
	});
    }
  }

  /**
   * Shows an error.
   */
  public void showError(String text) {
    dispose();
    Pooka.getUIFactory().showError(text);
  }

  /**
   * Shows an error.
   */
  public void showError(String text, Exception e) {
    dispose();
    Pooka.getUIFactory().showError(text, e);
  }

  // PrintJobListener

  public void printDataTransferCompleted(PrintJobEvent pje) {
    // do nothing.
  }
  
  public void printJobCompleted(PrintJobEvent pje) {
    // do nothing.
  }
  
  public void printJobCanceled(PrintJobEvent pje) {
    showError("Canceled.");
  }

  public void printJobFailed(PrintJobEvent pje) {
    showError("Failed");
  }
  
  public void printJobNoMoreEvents(PrintJobEvent pje) {
    dispose();
  }
  
  public void printJobRequiresAttention(PrintJobEvent pje) {
    showError("Needs attention.");
  }


}
