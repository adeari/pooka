package net.suberic.pooka.gui.dnd;

import java.awt.datatransfer.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;

import net.suberic.pooka.FolderInfo;
import net.suberic.pooka.gui.*;

/**
 * A set of utility methods to use with drag and drop.
 */
public class DndUtils {

  static boolean isLinux = System.getProperty("os.name").startsWith("Linux");

  /**
   * Returns true if this set of DataFlavors might include a FileFlavor.
   */
  public static boolean hasFileFlavor(DataFlavor[] flavors) {
    // first see if we're on linux.
    if (flavors != null) {
      for (int i = 0; i < flavors.length; i++) {
	if (flavors[i]!= null && flavors[i].isFlavorJavaFileListType())
	  return true;
	else if (isLinux && flavors[i] != null && flavors[i].isFlavorTextType())
	  return true;
      }
    }

    return false;
  }

  /**
   * Extracts a List of File objects from a Transferable.
   */
  public static List extractFileList(Transferable t) throws UnsupportedFlavorException, java.io.IOException {
    DataFlavor[] availableFlavors = t.getTransferDataFlavors();
    DataFlavor match = matchDataFlavor(new DataFlavor[] { DataFlavor.javaFileListFlavor }, availableFlavors);
    if (match != null) {
      return (java.util.List) t.getTransferData(DataFlavor.javaFileListFlavor);
    } else if (isLinux) {
      match = matchDataFlavor(new DataFlavor[] { DataFlavor.stringFlavor }, availableFlavors);
      if (match != null) {
	ArrayList returnValue = new ArrayList();
	Reader urlReader = match.getReaderForText(t);
	BufferedReader br = new BufferedReader(urlReader);
	for (String line = br.readLine(); line != null && line.length() > 0; line = br.readLine()) {
	  try {
	    java.net.URI fileUri = new java.net.URI(line);
	    File currentFile = new File(fileUri);
	    returnValue.add(currentFile);
	  } catch (java.net.URISyntaxException e) {
	    e.printStackTrace();
	  }
	}

	return returnValue;
      }
    }

    return null;
  }

  /**
   * Finds the first acceptable DataFlavor match and returns it,
   * or null if no match is found.
   */
  public static DataFlavor matchDataFlavor(DataFlavor[] acceptableFlavors, DataFlavor[] availableFlavors) {
    if (acceptableFlavors != null && availableFlavors != null) {
      for (int i = 0; i < availableFlavors.length; i++) {
	for (int j = 0; j < acceptableFlavors.length; j++) {
	  if (availableFlavors[i] != null && availableFlavors[i].match(acceptableFlavors[j]))
	    return availableFlavors[i];
	}
      }
    }
    
    return null;
  }

  /**
   * Gets the FolderInfo from the given Component.
   */
  public static FolderInfo getFolderInfo(JComponent c) {
    try {
      if (c instanceof FolderDisplayPanel) {
	System.err.println("it's a FolderDisplayPanel.");
	return ((FolderDisplayPanel) c).getFolderInfo();
      }

      Object o = SwingUtilities.getAncestorOfClass(Class.forName("net.suberic.pooka.gui.FolderDisplayPanel"), c);
      if (o != null) {
	System.err.println("got a FolderDisplayPanel.");
	return ((net.suberic.pooka.gui.FolderDisplayPanel) o).getFolderInfo();
      } 
      
      // check for the folder tree.
      o = SwingUtilities.getAncestorOfClass(Class.forName("net.suberic.pooka.gui.FolderPanel"), c);
      if (o != null) {
	System.err.println("got a FolderTree.");
	Object selected = ((net.suberic.pooka.gui.FolderPanel) o).getSelectedNode();
	if (selected instanceof FolderNode) {
	  return ((FolderNode) selected).getFolderInfo();
	}
      }
      
      return null;
    } catch (Exception e) {
      return null;
    }
  }
}
