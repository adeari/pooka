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

  static DataFlavor[] acceptableFlavors = new DataFlavor[] {
    MessageProxyTransferable.sMessageProxyDataFlavor
  };

  public boolean importData(JComponent c, Transferable t) {
    if (!canImport(c, t.getTransferDataFlavors())) {
      return false;
    } else {
      FolderInfo fi = DndUtils.getFolderInfo(c);
      if (fi != null) {
	MessageProxy mp = null;
	try {
	  mp = (MessageProxy) t.getTransferData(MessageProxyTransferable.sMessageProxyDataFlavor);
	  if (mp != null) {
	    mp.getMessageInfo().copyMessage(fi);
	    return true;
	  }
	} catch (Exception e) {
	  if (mp != null)
	    mp.showError( Pooka.getProperty("error.Message.CopyErrorMessage", "Error:  could not copy messages to folder:  ") + fi.toString() +"\n", e);
	  if (Pooka.isDebug())
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
    if (action == MOVE) {
      try {
	MessageProxy mp = (MessageProxy) data.getTransferData(MessageProxyTransferable.sMessageProxyDataFlavor);
	if (mp != null) {
	  mp.deleteMessage(true);
	}
      } catch (Exception e) {
	e.printStackTrace();
      }
    }
  }

  public boolean canImport(JComponent c, DataFlavor[] flavors) {
    
    boolean returnValue = (DndUtils.matchDataFlavor(acceptableFlavors, flavors) != null);
    return returnValue;
  }

}
