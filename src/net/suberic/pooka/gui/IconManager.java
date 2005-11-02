package net.suberic.pooka.gui;
import java.awt.Component;
import javax.swing.*;
import javax.mail.Message;
import javax.mail.search.SearchTerm;
import java.util.MissingResourceException;
import java.util.Vector;
import net.suberic.pooka.Pooka;

/**
 * This manages the icons for Pooka.
 */
public class IconManager {

  // the source property.
  String mProperty = null;

  // the default location for icons 
  String mIconDirectory = null;

  // the default extension for icons
  String mIconExtension = null;;

  /**
   * Creates a new IconManager from the given property.
   */
  public IconManager(String pProperty) {
    mProperty = pProperty;
    mIconDirectory = Pooka.getProperty(mProperty + ".defaultDirectory", "images");
    mIconExtension = Pooka.getProperty(mProperty + ".defaultExtension", "images");
  }

  /**
   * Gets the ImageIcon specified by the resource given.
   */
  public ImageIcon getIcon(String pIconString) {
    java.net.URL imageURL = this.getClass().getResource(Pooka.getProperty(mProperty + ".icon." + pIconString, mIconDirectory + "/" + pIconString + "." + mIconExtension));
    if (imageURL != null) {
      ImageIcon returnValue = new ImageIcon(imageURL);
      return returnValue;
    } else {
      return null;
    }
  }
   
}

