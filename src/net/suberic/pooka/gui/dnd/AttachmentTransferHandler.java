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
 * A TransferHandler for an attachment.
 */
public class AttachmentTransferHandler extends TransferHandler {
  private DataFlavor messageFlavor;
  private boolean shouldRemove;
  
  protected Transferable createTransferable(JComponent c) {
    System.err.println("creating transferable for attachment");
    
    System.err.println("c is " + c);

    Attachment attachment = null;
    MessageProxy proxy = null;
    if (c instanceof net.suberic.pooka.gui.AttachmentPane) {
      attachment = ((AttachmentPane) c).getSelectedAttachment();
      proxy = ((AttachmentPane) c).getMessageProxy();
      System.err.println("selected attachment is " + attachment);
    } else if (c instanceof JTable) {
      try {
	Object o = SwingUtilities.getAncestorOfClass(Class.forName("net.suberic.pooka.gui.AttachmentPane"), c);
	System.err.println("o is " + o);
	if (o != null ) {
	  attachment = ((AttachmentPane) o).getSelectedAttachment();
	  proxy = ((AttachmentPane) o).getMessageProxy();
	  System.err.println("selected attachment is " + attachment);
	} else {
	  return null;
	}
      } catch ( Exception e) {
	return null;
      }
    } 
    
    if (attachment != null && proxy != null) {
      return new AttachmentTransferable(attachment, proxy);
    } else {
      return null;
    }
    
  }

  public int getSourceActions(JComponent c) {
    return COPY_OR_MOVE;
  }

  protected void exportDone(JComponent c, Transferable data, int action) {
    System.err.println("exportDone; exported " + data + ", action " + action);
    /*
    if (shouldRemove && (action == MOVE)) {
      if ((p0 != null) && (p1 != null) &&
	  (p0.getOffset() != p1.getOffset())) {
	try {
	  JTextComponent tc = (JTextComponent)c;
	  tc.getDocument().remove(p0.getOffset(), p1.getOffset() - p0.getOffset());
	} catch (BadLocationException e) {
	  System.out.println("Can't remove text from source.");
	}
      }
    }
    source = null;
    */
  }

  public boolean canImport(JComponent c, DataFlavor[] flavors) {
    System.err.println("checking canImport, flavors " + flavors);
    if (containsMessageProxy(flavors)) {
      System.err.println("can import.");
      return true;
    } else {
      System.err.println("can't import.");
      return false;
    }
  }

  /**
   * Returns true if the set of dataflavors contains the M
   * essageProxyTransferable flavor.
   */
  boolean containsMessageProxy(DataFlavor[] flavors) {
    if (flavors == null)
      return false;
    
    for (int i = 0 ; i < flavors.length; i++) {
      if (flavors[i] == MessageProxyTransferable.sMessageProxyDataFlavor)
	return true;
    }

    return false;
  }

  /**
   * Gets the FolderInfo from the given Component.
   */
  public FolderInfo getFolderInfo(JComponent c) {
    try {
      Object o = SwingUtilities.getAncestorOfClass(Class.forName("net.suberic.pooka.gui.FolderDisplayPanel"), c);
      if (o != null) {
	return ((FolderDisplayPanel) o).getFolderInfo();
      } 
      
      o = SwingUtilities.getAncestorOfClass(Class.forName("net.suberic.pooka.gui.FolderNode"), c);
      if (o != null) {
	return ((FolderNode) o).getFolderInfo();
      } 
      
      return null;
    } catch (Exception e) {
      return null;
    }
  }
}
