package net.suberic.pooka.gui;

import javax.swing.*;
import java.awt.Font;
import java.awt.Color;
import java.util.*;

import net.suberic.util.*;
import net.suberic.pooka.Pooka;

/**
 * A class that controls the colors and fonts for Pooka.
 */
public class PookaUIManager implements ValueChangeListener {

  HashMap uiConfigMap = new HashMap();

  /*
  String[] resourceStrings = new String[] {
    "font",
    "background",
    "foreground",
    "disabledText",
    "select",
    "focus"
  };
  */
  String[] resourceStrings = new String[] {
    "border", "font", "caretForeground", "selectionBackground", "selectionForeground", "background", "foreground", "disabledText", "select", "focus", "icon", "text", "disabledSelectedText", "disabledBackground", "disabledSelectedBackground", "focusInsets", "horizontalThumbIcon", "verticalThumbIcon", "foregroundHighligh", "backgroundHighlight", "listBackground", "listForeground", "titleColor", "disabledForeground", "shadow", "darkShadow", "thumb", "thumbShadow", "thumbHighlight", "tabAreaBackground", "lightHighlight", "selectedHighlight", "focusCellBackground", "gridColor", "acceleratorFont", "acceleratorForeground", "acceleratorSelectionForeground", "textForeground", "textBackground", "selectionBorderColor", "dockingBackground", "floatingBackground", "dockingForeground", "floatingForeground", "selected"
  };

  String[] resourceNames = new String[] {
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

  boolean updateUI = false;

  /**
   * Creates a PookaUIManager.
   */
  public PookaUIManager() {
    for (int i = 0; i < resourceNames.length; i++) 
      updateResource(resourceNames[i]);

    createUIConfigs();
    Pooka.getResources().addValueChangeListener(this, "Pooka.uiConfig.*");
  }

  /**
   * Creates the UIConfig entries.
   */
  private void createUIConfigs() {
    Vector configNames = Pooka.getResources().getPropertyAsVector("UIManager.uiConfig", "");
    for (int i = 0; i < configNames.size(); i++) {
      String currentName = (String) configNames.get(i);
      HashMap currentMap = loadUIConfig("UIManager.uiConfig." + currentName, Pooka.getResources());
      uiConfigMap.put(currentName, currentMap);
    }
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
   * Updates the given resource.
   */
  private void updateResource(String resource) {
    if (resource != null && resource.length() > 0) {
      if (resource.equalsIgnoreCase("messagePanel")) {
	ContentPanel cp = Pooka.getMainPanel().getContentPanel();
	if (cp instanceof MessagePanel) {
	  ((MessagePanel) cp).configureInterfaceStyle();
	}
      } else if (resource.equalsIgnoreCase("folderPanel")) {
	Pooka.getMainPanel().getFolderPanel().configureInterfaceStyle();
      } else if (resource.equalsIgnoreCase("messageWindow")) {
      } else if (resource.equalsIgnoreCase("newMessageWindow")) {
      } else if (resource.equalsIgnoreCase("folderTable")) {
      } else {
	// if it's a system resource, then we set the values.

	for (int i = 0; i < resourceStrings.length; i++) {
	  String isEnabled = Pooka.getProperty("Pooka.uiConfig." + resource + "." + resourceStrings[i] + "._enabled", "false");
	  if (isEnabled.equalsIgnoreCase("true")) {
	    if (resourceStrings[i].equalsIgnoreCase("font")) {
	      applyFont(resource + "." + resourceStrings[i], Pooka.getProperty("Pooka.uiConfig." + resource + "." + resourceStrings[i], ""));
	    } else {
	      applyColor(resource + "." + resourceStrings[i], Pooka.getProperty("Pooka.uiConfig." + resource + "." + resourceStrings[i] + ".rgb", ""));
	    }
	  }
	}

	//and, what the hell, we also update the UI.
	updateUI = true;
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
	      if (updateUI) {
		if (Pooka.getMainPanel() != null)
		  javax.swing.SwingUtilities.updateComponentTreeUI(Pooka.getMainPanel());
		updateUI = false;
	      }
	    }
	  });
      }
    
    }
  }

  /**
   * Applies the given font to the given resource.
   */
  private void applyFont(String resource, String fontString) {
    Font newFont = Font.decode(fontString);
    UIManager.put(resource,newFont);
  }

  /**
   * Applies the given color to the given resource.
   */
  private void applyColor(String resource, String rgbString) {
    try {
      int rgbValue = Integer.parseInt(rgbString);
      Color c = new Color(rgbValue);
      UIManager.put(resource, c);
    } catch (Exception e) {
      System.out.println("caught exception setting color:  " + e);
      e.printStackTrace();
    }
  }
      
  /**
   * Called when one of the Pooka ui values changes.
   */
  public void valueChanged(String changedValue) {
    // the changed value is going to be Pooka.uiConfig.something...  see
    // which top-level component has changed and just redo it.
    
    String resource = changedValue.substring(15, changedValue.indexOf('.', 15));
    updateResource(resource);
  }

  /**
   * Returns a UIStyleDefinition for the MessagePanel.
   */
  public HashMap getMessagePanelStyle() {
    return createDefinitionForProperty("Pooka.uiConfig.messagePanel");
  }

  /**
   * Returns a UIStyleDefinition for the folder panel.
   */
  public HashMap getFolderPanelStyle() {
    return createDefinitionForProperty("Pooka.uiConfig.folderPanel");
  }

  /**
   * Returns a UIStyleDefinition for the given MessageUI.
   */
  public HashMap getMessageWindowStyle(MessageUI ui) {

    // not the most efficient method, but it should work.
    net.suberic.pooka.FolderInfo fi = ui.getMessageProxy().getMessageInfo().getFolderInfo();
    net.suberic.pooka.StoreInfo si = fi.getParentStore();

    HashMap defaultValues= createDefinitionForProperty("Pooka.uiConfig.messageWindow");

    HashMap storeValues = createDefinitionForProperty(si.getStoreProperty() + ".uiConfig.messageWindow");
    HashMap folderValues = createDefinitionForProperty(fi.getFolderProperty() + ".uiConfig.messageWindow");
    
    HashMap returnValue = overrideStyle(defaultValues, storeValues);

    returnValue = overrideStyle(returnValue, folderValues);

    return returnValue;
  }

  /**
   * Returns a UIStyleDefinition for the editors of the given NewMessageUI.
   */
  public HashMap getNewMessageWindowEditorStyle(NewMessageUI ui) {
    return getNewMessageWindowLabelStyle(ui);
    /*
    // not the most efficient method, but it should work.
    net.suberic.pooka.UserProfile pr = ui.getSelectedProfile();

    HashMap defaultValues= createDefinitionForProperty("Pooka.uiConfig.newMessageWindow-editor");
    HashMap profileValues = createDefinitionForProperty(pr.getUserProperty() + ".uiConfig.newMessageWindow");
    
    HashMap returnValue = overrideStyle(defaultValues, profileValues);
    return returnValue;
    */
  }

  /**
   * Returns a UIStyleDefinition for the editors of the given NewMessageUI.
   */
  public HashMap getNewMessageWindowLabelStyle(NewMessageUI ui) {
    net.suberic.pooka.UserProfile pr = ui.getSelectedProfile();
    String uiConfigName = Pooka.getProperty(pr.getUserProperty() + ".uiConfig", "");
    return (HashMap) uiConfigMap.get(uiConfigName);

    /*
    // not the most efficient method, but it should work.
    net.suberic.pooka.UserProfile pr = ui.getSelectedProfile();

    HashMap defaultValues= createDefinitionForProperty("Pooka.uiConfig.newMessageWindow-labels");
    HashMap profileValues = createDefinitionForProperty(pr.getUserProperty() + ".labelUiConfig.newMessageWindow");
    
    HashMap returnValue = overrideStyle(defaultValues, profileValues);
    return returnValue;
    */
  }

  /**
   * Returns a UIStyleDefinition for the given FolderDisplayUI.
   */
  public HashMap getFolderDisplayStyle(FolderDisplayUI ui) {
    // not the most efficient method, but it should work.
    net.suberic.pooka.FolderInfo fi = ui.getFolderInfo();
    net.suberic.pooka.StoreInfo si = fi.getParentStore();

    HashMap defaultValues= createDefinitionForProperty("Pooka.uiConfig.folderTable");
    HashMap storeValues = createDefinitionForProperty(si.getStoreProperty() + ".uiConfig.folderTable");
    HashMap folderValues = createDefinitionForProperty(fi.getFolderProperty() + ".uiConfig.folderTable");
    
    HashMap returnValue = overrideStyle(defaultValues, storeValues);
    returnValue = overrideStyle(returnValue, folderValues);
    return returnValue;
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
   * Creates a HashMap for the given UI resources.
   */
  public HashMap createDefinitionForProperty(String property) {
    HashMap returnValue = new HashMap();

    for (int i = 0; i < resourceStrings.length; i++) {
      String isEnabled = Pooka.getProperty(property + "." + resourceStrings[i] + "._enabled", "false");
      if (isEnabled.equalsIgnoreCase("true")) {
	if (resourceStrings[i].equalsIgnoreCase("font")) {
	  returnValue.put(resourceStrings[i], Font.decode(Pooka.getProperty(property + "." + resourceStrings[i], "")));
	} else {
	  int rgbValue = Integer.parseInt(Pooka.getProperty(property + "." + resourceStrings[i] + ".rgb", ""));
	  Color c = new Color(rgbValue);

	  returnValue.put(resourceStrings[i], c);
	}
      }
    }
    
    return returnValue;
  }

}
