package net.suberic.pooka.gui.dnd;

import java.awt.datatransfer.*;

/**
 * A set of utility methods to use with drag and drop.
 */
public class DndUtils {

  static DataFlavor fileFlavor = DataFlavor.javaFileListFlavor;

  /**
   * Returns true if this set of DataFlavors includes a FileFlavor.
   */
  public static boolean hasFileFlavor(DataFlavor[] flavors) {
    if (flavors != null) {
      for (int i = 0; i < flavors.length; i++) {
	if (fileFlavor == flavors[i])
	  return true;
      }
    }

    return false;
  }


  /**
   * Finds the first acceptable DataFlavor match and returns it,
   * or null if no match is found.
   */
  public static DataFlavor matchDataFlavor(DataFlavor[] acceptableFlavors, DataFlavor[] availableFlavors) {
    if (acceptableFlavors != null && availableFlavors != null) {
      for (int i = 0; i < availableFlavors.length; i++) {
	if (availableFlavors[i] != null) {
	  System.err.println("availableFlavors[" + i + "] = " + availableFlavors[i] + "; name is " + availableFlavors[i].getHumanPresentableName() + "; class is " + availableFlavors[i].getDefaultRepresentationClass());
	} else {
	  System.err.println("availableFlavors[" + i + "] = " + availableFlavors[i]);
	}
	for (int j = 0; j < acceptableFlavors.length; j++) {
	  if (availableFlavors[i] == acceptableFlavors[j])
	    return availableFlavors[i];
	}
      }
    }
    
    return null;
  }
}
