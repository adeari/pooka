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
  public static String[] resourceStrings = new String[] {
    "border", "font", "caretForeground", "selectionBackground", "selectionForeground", "background", "foreground", "disabledText", "select", "focus", "icon", "text", "disabledSelectedText", "disabledBackground", "disabledSelectedBackground", "focusInsets", "horizontalThumbIcon", "verticalThumbIcon", "foregroundHighligh", "backgroundHighlight", "listBackground", "listForeground", "titleColor", "disabledForeground", "shadow", "darkShadow", "thumb", "thumbShadow", "thumbHighlight", "tabAreaBackground", "lightHighlight", "selectedHighlight", "focusCellBackground", "gridColor", "acceleratorFont", "acceleratorForeground", "acceleratorSelectionForeground", "textForeground", "textBackground", "selectionBorderColor", "dockingBackground", "floatingBackground", "dockingForeground", "floatingForeground", "selected"
  };

  public static String[] resourceNames = new String[] {
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
  


}
