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
 * A TransferHandler for a FolderTable.
 */
public class FolderTableTransferHandler extends TransferHandler {
  private DataFlavor messageFlavor;
  private boolean shouldRemove;
  
  public boolean importData(JComponent c, Transferable t) {
    if (!canImport(c, t.getTransferDataFlavors())) {
      return false;
    } else {
      FolderInfo fi = DndUtils.getFolderInfo(c);
      if (fi != null) {
	try {
	  MessageProxy mp = (MessageProxy) t.getTransferData(MessageProxyTransferable.sMessageProxyDataFlavor);
	  if (mp != null) {
	    mp.moveMessage(fi);
	    return true;
	  }
	} catch (Exception e) {
	  e.printStackTrace();
	  return false;
	}
      } else {
	return false;
      }
    }

    return false;
  }
  
  protected Transferable createTransferable(JComponent c) {
    if (c instanceof net.suberic.pooka.gui.FolderDisplayPanel) {
      return new MessageProxyTransferable(((FolderDisplayPanel) c).getSelectedMessage());
    } else if (c instanceof JTable) {
      try {
	Object o = SwingUtilities.getAncestorOfClass(Class.forName("net.suberic.pooka.gui.FolderDisplayPanel"), c);
	if (o != null ) {
	  
	  Transferable returnValue = new MessageProxyTransferable(((FolderDisplayPanel) o).getSelectedMessage());
	  return returnValue;
	} else {
	  return null;
	}
      } catch (Exception e) {
	return null;
      }
    } else {
      return null;
    }
  }

  public int getSourceActions(JComponent c) {
    return COPY_OR_MOVE;
  }

  protected void exportDone(JComponent c, Transferable data, int action) {
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
    if (containsMessageProxy(flavors)) {
      return true;
    } else {
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

}
