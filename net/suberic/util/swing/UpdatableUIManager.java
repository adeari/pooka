package net.suberic.util.swing;

import javax.swing.*;
import java.awt.Font;
import java.awt.Color;
import java.util.*;

import net.suberic.util.*;

/**
 * A class that allows one to apply arbitrary styles to individual 
 * Components.
 */
public class UpdatableUIManager implements ValueChangeListener, ItemCreator, ItemListChangeListener {

  // -------- static values -------- //
  static String[] resourceStrings = new String[] {
    "border", "font", "caretForeground", "selectionBackground", "selectionForeground", "background", "foreground", "disabledText", "select", "focus", "icon", "text", "disabledSelectedText", "disabledBackground", "disabledSelectedBackground", "focusInsets", "horizontalThumbIcon", "verticalThumbIcon", "foregroundHighligh", "backgroundHighlight", "listBackground", "listForeground", "titleColor", "disabledForeground", "shadow", "darkShadow", "thumb", "thumbShadow", "thumbHighlight", "tabAreaBackground", "lightHighlight", "selectedHighlight", "focusCellBackground", "gridColor", "acceleratorFont", "acceleratorForeground", "acceleratorSelectionForeground", "textForeground", "textBackground", "selectionBorderColor", "dockingBackground", "floatingBackground", "dockingForeground", "floatingForeground", "selected"
  };

  static String[] resourceNames = new String[] {
    "Button",
    "ToggleButton",
    "RadioButtonCheckBox",
    "ColorChooser",
    "ComboBox",
    "Label",
    "List",
    "MenuBar",
    "MenuItem",
    "RadioButtonMenuItem",
    "CheckBoxMeuItem",
    "Menu",
    "PopupMenu",
    "OptionPane",
    "Panel",
    "ProgressBar",
    "ScrollPane",
    "Viewport",
    "TabbedPane",
    "Table",
    "TableHeader",
    "TextField",
    "PasswordField",
    "TextArea",
    "TextPane",
    "EditorPane",
    "TitledBorder",
    "ToolBar",
    "ToolTip",
    "Tree"
  };

  // ---- instance values --- //

  private ItemManager manager;
  private LinkedList listenerList = new LinkedList();
  private VariableBundle sourceBundle;
  private String resourceString;
  
  boolean updateUI = false;

  /**
   * Creates a UpdatableUIManager.
   */
  public UpdatableUIManager(String newResourceString, VariableBundle bundle) {
    resourceString = newResourceString;
    sourceBundle = bundle;
    createUIConfigs();
  }

  /**
   * Creates the UIConfig entries.
   */
  private void createUIConfigs() {
    manager = new ItemManager(resourceString, sourceBundle, this);
    manager.addItemListChangeListener(this);
  }
  
  /**
   * Applies the given UIConfig map to the component.
   */
  public void applyUI(HashMap uiConfig, java.awt.Component component) {
    HashMap saveMap = applyConfig(uiConfig);
    SwingUtilities.updateComponentTreeUI(component);
    applyConfig(saveMap);
  }

  /**
   * applies the given values to the UIManager.
   */
  private HashMap applyConfig(HashMap uiConfig) {
    HashMap saveMap = new HashMap();
    if (uiConfig != null) {
      Iterator it = uiConfig.keySet().iterator();
      while (it.hasNext()) {
	Object key = it.next();
	Object oldValue = UIManager.get(key);
	saveMap.put(key, oldValue);
	UIManager.put(key, uiConfig.get(key));
      }
    }
    return saveMap;
  }

  /**
   * updates the given Component with the configuration from the given
   * UpdatableUI.
   */
  public void updateUI(UpdatableUI uui, java.awt.Component component) {
    UIConfig newConfig = uui.getUIConfig(this);
    if (newConfig != null && newConfig != getDefaultUIConfig()) {
      applyUI(newConfig.getUiMap(), component);
    }
    
  }

  /**
   * Gets the default configuration for the system.
   */
  public UIConfig getDefaultUIConfig() {
    String defaultString = sourceBundle.getProperty(resourceString + "._default", "");
    if (defaultString != null && ! defaultString.equals("")) {
      return getUIConfig(defaultString);
    }

    return null;
  }

  /**
   * Gets the named configuration, or null if no such configuration 
   * exists.
   */
  public UIConfig getUIConfig(String configID) {
    if (configID == null)
      return null;

    return (UIConfig) manager.getItem(configID);
  }

  /**
   * Called when a ui value changes.
   */
  public void valueChanged(String changedValue) {

  }

  /**
   * Overrides the values in defaultMap with the values in overrideMap.
   */
  public HashMap overrideStyle(HashMap defaultMap, HashMap overrideMap) {
    HashMap returnValue = new HashMap(defaultMap);
    Set overrideSet = overrideMap.keySet();
    Iterator iter = overrideSet.iterator();
    while (iter.hasNext()) {
      Object key = iter.next();
      returnValue.put(key, overrideMap.get(key));
    }

    return returnValue;
  }

  /**
   * As defined in net.suberic.util.ItemListChangeListener.
   * 
   * This listens for ItemListChangeEvents, which result from changes to the 
   * resourceString property.  The result is just that the event is passed 
   * to listeners to this object.
   */
  public void itemListChanged(ItemListChangeEvent e) {
    fireItemListChanged(e);
  }

  /**
   * This notifies all listeners that the UIConfig list has changed.
   */
  public void fireItemListChanged(ItemListChangeEvent e) {
    for (int i = 0; i < listenerList.size(); i++)
      ((ItemListChangeListener)listenerList.get(i)).itemListChanged(e);
  }
  
  /**
   * Creates an item from the given sourceBundle, resourceString, and itemId.
   *
   * Creates a new UpdatableUIManager.UIConfig object.
   */
  public Item createItem(VariableBundle sourceBundle, String resourceString, String itemId) {
    return new UIConfig(resourceString, itemId, sourceBundle);
  }
  
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
      for (int i = 0; i < resourceNames.length; i++) {
	loadUISubProperty(uiProperty, resourceNames[i], returnValue, bundle);
      }
      
      return returnValue;
    }
    
    /**
     * Loads a UIConfig from the given VariableBundle, using the given name.
     */
    
    private void loadUISubProperty(String uiProperty, String resourceName, HashMap uiConfig, VariableBundle bundle) {
      // sample uiProperty: UIManager.uiConfig.default
      // sample resourceName:  TabbedPane
      for (int i = 0; i < resourceStrings.length; i++) {
	//String currentProperty = resourceName + "." + resourceStrings[i];
	// now we have the full resource name.
	loadUIResource(uiProperty, resourceName, resourceStrings[i], uiConfig, bundle);
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


}
