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
 * A TransferHandler for a Folder.
 */
public class FolderTransferHandler extends TransferHandler {
  private boolean shouldRemove;

  static DataFlavor[] acceptableFlavors = new DataFlavor[] {
    MessageProxyTransferable.sMessageProxyDataFlavor
  };

  public boolean importData(JComponent c, Transferable t) {
    System.err.println("importing.");
    if (!canImport(c, t.getTransferDataFlavors())) {
      System.err.println("can't import.");
      return false;
    } else {
      System.err.println("class is " + c);
      FolderInfo fi = DndUtils.getFolderInfo(c);
      if (fi != null) {
	System.err.println("got folder info.");
	try {
	  MessageProxy mp = (MessageProxy) t.getTransferData(MessageProxyTransferable.sMessageProxyDataFlavor);
	  if (mp != null) {
	    System.err.println("copying.");
	    mp.copyMessage(fi);
	    shouldRemove = true;
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
    System.err.println("creating transferable.");
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
    System.err.println("export Done.");
    if (action == MOVE && shouldRemove) {
      try {
	MessageProxy mp = (MessageProxy) data.getTransferData(MessageProxyTransferable.sMessageProxyDataFlavor);
	if (mp != null) {
	  System.err.println("deleting message.");
	  mp.deleteMessage(true);
	}
      } catch (Exception e) {
	e.printStackTrace();
      }
    }
  }

  public boolean canImport(JComponent c, DataFlavor[] flavors) {
    
    boolean returnValue = (DndUtils.matchDataFlavor(acceptableFlavors, flavors) != null);
    System.err.println("canImport = " + returnValue);
    return returnValue;
  }

}
