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
 * A TransferHandler for Folder Nodes.
 */
public class FolderNodeTransferHandler extends TransferHandler {

  static DataFlavor[] acceptableFlavors = new DataFlavor[] {
    MessageProxyTransferable.sMessageProxyDataFlavor
  };

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
  
  public boolean canImport(JComponent c, DataFlavor[] flavors) {
    return (DndUtils.matchDataFlavor(acceptableFlavors, flavors) != null);
  }
  
}
