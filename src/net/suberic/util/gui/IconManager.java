package net.suberic.util.gui;
import java.awt.Component;
import javax.swing.*;
import java.util.*;

import net.suberic.util.VariableBundle;

/**
 * This manages a set of icons.
 *
 */
public class IconManager {

  // the source VariableBundle
  VariableBundle mResources = null;
  
  // the source property.
  String mProperty = null;

  // the default location for icons 
  String mIconDirectory = null;

  // the default extension for icons
  String mIconExtension = null;;

  /**
   * Creates a new IconManager from the given property.
   *
   * @param pResources the VariableBundle used to access the icons
   * @param pResourceBase a property in the given VariableBundle that will resolve to provide the correct property base 
   */
  public IconManager(VariableBundle pResources, String pResourceBase) {
    mResources = pResources;
    mProperty = pResources.getProperty(pResourceBase, "");
    mIconDirectory = mResources.getProperty(mProperty + ".defaultDirectory", "images");
    mIconExtension = mResources.getProperty(mProperty + ".defaultExtension", "images");
  }

  /**
   * Gets the ImageIcon specified by the resource given.
   */
  public ImageIcon getIcon(String pIconString) {

    java.net.URL imageURL = this.getClass().getResource(mResources.getProperty(mProperty + ".icon." + pIconString, mIconDirectory + "/" + pIconString + "." + mIconExtension));
    if (imageURL != null) {
      ImageIcon returnValue = new ImageIcon(imageURL);
      return returnValue;
    } else {
      return null;
    }
  }

   
}

