package net.suberic.pooka.gui.dnd;

import net.suberic.pooka.*;
import net.suberic.pooka.gui.*;

import java.awt.datatransfer.*;

import java.io.IOException;

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
      sMessageProxyDataFlavor
    };
  }

  public boolean isDataFlavorSupported(DataFlavor flavor) {
    if (flavor == sMessageProxyDataFlavor)
      return true;
    else 
      return false;
  }

  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    if (isDataFlavorSupported(flavor)) {
      return mMessageProxy;
    } else {
      throw new UnsupportedFlavorException(flavor);
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
