package net.suberic.util.swing;

import net.suberic.util.*;
import javax.swing.*;
import java.awt.Font;
import java.awt.Color;
import java.util.*;

  public class UIConfig implements Item {
    
    private String itemId;
    private String resourceString;
    private HashMap uiMap;
    
    public UIConfig(String newResourceString, String newItemId, VariableBundle sourceBundle) {
      itemId = newItemId;
      resourceString = newResourceString;
      
      uiMap = loadUIConfig(getItemProperty(), sourceBundle);
    }

    /**
     * The Item ID.  For example, if you were to have a list of users, a
     * given user's itemID may be "defaultUser".
     */
    public String getItemID() {
      return itemId;
    }

    /**
     * The Item property.  For example, if you were to have a list of users, a
     * given user's itemPropertymay be "Users.defaultUser".
     */
    public String getItemProperty() {
      return  resourceString + "." + itemId;
    }

    /**
     * The HashMap with the interesting information in it.  :)
     */
    public HashMap getUiMap() {
      return uiMap;
    }
    
    /**
     * Loads a UIConfig from the given VariableBundle, using the given name.
     */
    private HashMap loadUIConfig(String uiProperty, VariableBundle bundle) {
      // sample uiProperty:  UIManager.uiConfig.default
      
      HashMap returnValue = new HashMap();
      for (int i = 0; i < UpdatableUIManager.resourceNames.length; i++) {
	loadUISubProperty(uiProperty, UpdatableUIManager.resourceNames[i], returnValue, bundle);
      }
      
      return returnValue;
    }
    
    /**
     * Loads a UIConfig from the given VariableBundle, using the given name.
     */
    
    private void loadUISubProperty(String uiProperty, String resourceName, HashMap uiConfig, VariableBundle bundle) {
      // sample uiProperty: UIManager.uiConfig.default
      // sample resourceName:  TabbedPane
      for (int i = 0; i < UpdatableUIManager.resourceStrings.length; i++) {
	//String currentProperty = resourceName + "." + UpdatableUIManager.resourceStrings[i];
	// now we have the full resource name.
	loadUIResource(uiProperty, resourceName, UpdatableUIManager.resourceStrings[i], uiConfig, bundle);
      }
      
    }
    
    /**
     * Loads a UIConfig from the given VariableBundle, using the given name.
     */
    
    private void loadUIResource(String uiProperty, String resourceName, String resourceSubProperty, HashMap uiConfig, VariableBundle bundle) {
      // sample uiProperty: UIManager.uiConfig.default
      // sample resourceName:  TabbedPane
      // sample resourceSubProperty:  background
      
      String fullProperty = uiProperty + "." + resourceName + "." + resourceSubProperty;
      if (bundle.getProperty(fullProperty + "._enabled", "false").equalsIgnoreCase("true")) {
	String resourceType = bundle.getProperty("UIManager.types." + resourceSubProperty, "color");
	if (resourceType.equalsIgnoreCase("font")) {
	  addFont(uiProperty, resourceName + "." + resourceSubProperty, uiConfig, bundle);
	} else {
	  addColor(uiProperty, resourceName + "." + resourceSubProperty, uiConfig, bundle);
	}
      }
      
    }

    /**
     * Adds a font.
     */
    private void addFont(String uiProperty, String resource, HashMap uiConfig, VariableBundle bundle) {
      String fontString = bundle.getProperty(uiProperty + "." + resource, "");
      Font newFont = Font.decode(fontString);
      if (newFont != null) {
	uiConfig.put(resource, new javax.swing.plaf.FontUIResource(newFont));
      }
    }
    
    /**
     * Adds a color.
     */
    private void addColor(String uiProperty, String resource, HashMap uiConfig, VariableBundle bundle) {
      String rgbString = bundle.getProperty(uiProperty + "." + resource + ".rgb", "");
      if (! rgbString.equals("")) {
	try {
	  Color newColor = new Color(Integer.parseInt(rgbString));
	  if (newColor != null) {
	  uiConfig.put(resource, new javax.swing.plaf.ColorUIResource(newColor));
	  }
	} catch (Exception e) {
      }
      }
    }
    

  } // UIConfig
