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

  int mPageCount = 0;

  String mCurrentDoc = "";

  String mTitle;

  JTextPane mDisplayPane = null;
  JButton mOkButton = null;
  JButton mCancelButton = null;

  boolean mInternal = false;

  JDialog mDialog = null;
  JInternalFrame mDialogFrame = null;

  Object mSource;

  /**
   * Creates a new MessagePrinterDisplay using the given MessagePrinter
   * and the given DocPrintJob.
   */
  public MessagePrinterDisplay(MessagePrinter pPrinter, DocPrintJob pJob, Object pSource) {
    mPrinter = pPrinter;
    mJob = pJob;
    mSource = pSource;
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
    displayMessage.append(mPageCount);
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
   * Sets the page count.
   */
  public void setPageCount(int pPageCount) {
    mPageCount = pPageCount;
    updateDisplayPane();
  }
  
  /**
   * Checks to see if this is an internal dialog or not.
   */
  private boolean checkInternal() {
    if (mSource instanceof JComponent) {
      PookaUIFactory uiFactory = Pooka.getUIFactory();
      if (uiFactory instanceof PookaDesktopPaneUIFactory) {
	JComponent sourceComponent = (JComponent) mSource;
	if (((PookaDesktopPaneUIFactory) uiFactory).isInMainFrame(sourceComponent))
	  return true;
      }
    }

    return false;
    
  }

  /**
   * Shows the MessagePrinterDisplay.
   */
  public void show() {
    mDisplayPane = new JTextPane();
    mDisplayPane.setBorder(BorderFactory.createEtchedBorder());
    JLabel jl = new JLabel();
    mDisplayPane.setBackground(jl.getBackground());
    mDisplayPane.setFont(jl.getFont());

    if (mInternal) {
      mDialogFrame = new JInternalFrame("Printing", true, false, false, true);
      mDialogFrame.getContentPane().setLayout(new BoxLayout(mDialogFrame.getContentPane(), BoxLayout.Y_AXIS));
      
      mDialogFrame.getContentPane().add(mDisplayPane);
      if (mJob instanceof CancelablePrintJob) {
	Box buttonBox = createButtonBox();
	mDialogFrame.getContentPane().add(buttonBox);
      }
      updateDisplayPane();
      mDialogFrame.pack();

      MessagePanel mp = ((PookaDesktopPaneUIFactory)Pooka.getUIFactory()).getMessagePanel();
      mp.add(mDialogFrame);
      mDialogFrame.setLocation(mp.getNewWindowLocation(mDialogFrame, true));
      mDialogFrame.setVisible(true);
      
    } else {
      if (mSource instanceof JComponent && SwingUtilities.getWindowAncestor((JComponent) mSource) instanceof java.awt.Frame) {
	mDialog = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor((JComponent) mSource));
      } else {
	mDialog = new JDialog();
      }
      mDialog.getContentPane().setLayout(new BoxLayout(mDialog.getContentPane(), BoxLayout.Y_AXIS));
      
      mDialog.getContentPane().add(mDisplayPane);

      if (mJob instanceof CancelablePrintJob) {
	Box buttonBox = createButtonBox();
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
    returnValue.add(Box.createHorizontalGlue());
    returnValue.add(cancelButton);
    returnValue.add(Box.createHorizontalGlue());
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
	try {
	  mDialogFrame.setClosed(true);
	} catch (java.beans.PropertyVetoException e) {
	}
      } else {
	mDialog.dispose();
      }
    } else {
      SwingUtilities.invokeLater(new Runnable() {
	  public void run() {
	    if (mInternal) {
	      try {
		mDialogFrame.setClosed(true);
	      } catch (java.beans.PropertyVetoException e) {
	      }
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
