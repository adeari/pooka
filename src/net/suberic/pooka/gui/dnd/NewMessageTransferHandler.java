package net.suberic.pooka.gui.dnd;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;

import net.suberic.pooka.*;
import net.suberic.pooka.gui.*;

/**
 * A TransferHandler for a MessageProxy object.
 */
public class NewMessageTransferHandler extends TransferHandler {
  private DataFlavor messageFlavor;
  private boolean shouldRemove;
  
  public boolean importData(JComponent c, Transferable t) {
    System.err.println("new message importing");
    if (!canImport(c, t.getTransferDataFlavors())) {
      return false;
    } else {
      try {
	NewMessageDisplayPanel nmdp = (NewMessageDisplayPanel) SwingUtilities.getAncestorOfClass(Class.forName("net.suberic.pooka.gui.NewMessageDisplayPanel"), c);
	if (nmdp != null && isMessageProxy(t)) {
	  MessageProxy proxy = (MessageProxy) t.getTransferData(MessageProxyTransferable.sMessageProxyDataFlavor);
	  
	  javax.mail.internet.MimeBodyPart mbp = new javax.mail.internet.MimeBodyPart();
	  mbp.setDataHandler(proxy.getMessageInfo().getRealMessage().getDataHandler());
	  nmdp.getNewMessageProxy().getNewMessageInfo().addAttachment(new MBPAttachment(mbp));

	  nmdp.attachmentAdded(nmdp.getNewMessageProxy().getNewMessageInfo().getAttachments().size() -1);
	  System.err.println("returning true.");
	  return true;
	}
      } catch (Exception e) {
	e.printStackTrace();
      }
    }

    return false;
  }
  
  public boolean canImport(JComponent c, DataFlavor[] flavors) {
    if (flavors != null) {
      if (flavors.length == 0) {
	System.err.println("flavors.length == null.");
      }

      for (int i = 0; i < flavors.length; i++) {
	System.err.println("flavor[" + i + "] = " + flavors[i]);
	if (flavors[i] == MessageProxyTransferable.sMessageProxyDataFlavor)
	  return true;
      }

    } else {
      System.err.println("flavors == null.");
    }

    return false;
  }

  /**
   * Returns if this is a MessageProxy.
   */
  public boolean isMessageProxy(Transferable t) {
    DataFlavor[] flavors = t.getTransferDataFlavors();
    if (flavors == null)
      return false;

    for (int i = 0; i < flavors.length; i++) {
      if (flavors[i] == MessageProxyTransferable.sMessageProxyDataFlavor)
	return true;
    }

    return false;
  }
}
