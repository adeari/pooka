package net.suberic.pooka.gui.dnd;

import net.suberic.pooka.*;
import net.suberic.pooka.gui.*;

import java.awt.datatransfer.*;
import java.io.IOException;
import java.io.File;

public class AttachmentTransferable implements Transferable {
  
  Attachment mAttachment = null;
  MessageProxy mMessageProxy = null;
  
  public AttachmentTransferable(Attachment pAttachment, MessageProxy pMessageProxy) {
    setAttachment(pAttachment);
    setMessageProxy(pMessageProxy);
  }
  
  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {
      DataFlavor.javaFileListFlavor
    };
  }

  public boolean isDataFlavorSupported(DataFlavor flavor) {
    if (flavor == DataFlavor.javaFileListFlavor)
      return true;
    else 
      return false;
  }

  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    if (isDataFlavorSupported(flavor)) {
      java.util.LinkedList list = new java.util.LinkedList();

      File f = File.createTempFile("pooka", "attachment");
      AttachmentHandler handler = new AttachmentHandler(mMessageProxy);
      handler.saveFileAs(mAttachment, f);

      list.add(f);
      return list;
    } else {
      throw new UnsupportedFlavorException(flavor);
    }
  }

  /**
   * Returns the Attachment.
   */
  public Attachment getAttachment() {
    return mAttachment;
  }
 
  /**
   * Sets the Attachment.
   */
  public void setAttachment(Attachment pAttachment) {
    mAttachment = pAttachment;
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
  
  
}
