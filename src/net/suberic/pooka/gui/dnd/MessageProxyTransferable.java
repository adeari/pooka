package net.suberic.pooka.gui.dnd;

import net.suberic.pooka.*;
import net.suberic.pooka.gui.*;

import java.awt.datatransfer.*;
import java.util.*;

import java.io.*;

/**
 * A Transferable version of a MessageProxy.
 */
public class MessageProxyTransferable implements Transferable {

  // DataFlavor information
  public static DataFlavor sMessageProxyDataFlavor = null;
  static {
    try {
      sMessageProxyDataFlavor = new DataFlavor(Class.forName("net.suberic.pooka.gui.MessageProxy"), "MessageProxy");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  MessageProxy mMessageProxy = null;
  int mActionType = javax.swing.TransferHandler.COPY;
  
  public MessageProxyTransferable(MessageProxy pMessageProxy) {
    setMessageProxy(pMessageProxy);
  }
  
  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {
      sMessageProxyDataFlavor,
      DataFlavor.javaFileListFlavor
    };
  }

  public boolean isDataFlavorSupported(DataFlavor flavor) {
    if (flavor == sMessageProxyDataFlavor)
      return true;
    else if (flavor != null && flavor.isFlavorJavaFileListType())
      return true;
    else 
      return false;
  }

  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    if (flavor == sMessageProxyDataFlavor) {
      return mMessageProxy;
    } else if (flavor != null && flavor.isFlavorJavaFileListType()) {
      ArrayList returnValue = new ArrayList();
      MessageInfo info = mMessageProxy.getMessageInfo();
      if (info instanceof MultiMessageInfo) {
	MultiMessageInfo multi = (MultiMessageInfo) info;
	for (int i = 0; i < multi.getMessageCount(); i++) {
	  returnValue.add(extractMessageInfo(multi.getMessageInfo(i)));
	}
      } else {
	returnValue.add(extractMessageInfo(info));
      }

      return returnValue;
    } else {
      throw new UnsupportedFlavorException(flavor);
    }
  }

  /**
   * Extracts the given MessageInfo into a file.
   */
  private File extractMessageInfo(MessageInfo info) throws java.io.IOException {
    try {
      File f = File.createTempFile("pooka", "message");
      info.saveMessageAs(f);
      return f;
    } catch (javax.mail.MessagingException me) {
      IOException ioe = new IOException("Error saving file");
      ioe.initCause(me);
      throw ioe;
    }
  }

  /**
   * Returns the MessageProxy.
   */
  public MessageProxy getMessageProxy() {
    return mMessageProxy;
  }
 
  /**
   * Sets the MessageProxy.
   */
  public void setMessageProxy(MessageProxy pMessageProxy) {
    mMessageProxy = pMessageProxy;
  }

  /**
   * Sets move or copy value.
   */
  public void setActionType(int pActionType) {
    mActionType = pActionType;
  }

  /**
   * Gets the action type for this Transferable.
   */
  public int getActionType() {
    return mActionType;
  }
}
