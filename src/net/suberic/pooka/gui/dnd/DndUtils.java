package net.suberic.pooka.gui.dnd;

import java.awt.datatransfer.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;

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
    System.err.println("isLinux = " + isLinux);
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
	System.err.println("importing Linux flavors.");
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
	if (availableFlavors[i] != null) {
	  System.err.println("availableFlavors[" + i + "] = " + availableFlavors[i] + "; name is " + availableFlavors[i].getHumanPresentableName() + "; class is " + availableFlavors[i].getRepresentationClass());
	} else {
	  System.err.println("availableFlavors[" + i + "] = " + availableFlavors[i]);
	}
	for (int j = 0; j < acceptableFlavors.length; j++) {
	  if (availableFlavors[i] != null && availableFlavors[i].match(acceptableFlavors[j]))
	    return availableFlavors[i];
	}
      }
    }
    
    return null;
  }
}
