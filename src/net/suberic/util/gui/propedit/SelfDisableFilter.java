package net.suberic.util.gui.propedit;
import java.util.*;

/**
 * A PropertyEditorListener which enables or disables this editor if
 * certain values are set.
 */
public class SelfDisableFilter extends PropertyEditorAdapter implements ConfigurablePropertyEditorListener {
  Map<String,Set<String>> disableValues = new HashMap<String, Set<String>>();
  PropertyEditorManager manager;
  String propertyBase;
  String property;

  /**
   * Configures this filter from the given key.
   */
  public void configureListener(String key, String pProperty, String pPropertyBase, String editorTemplate, PropertyEditorManager pManager) {
    //System.err.println("init for " + key);
    manager = pManager;
    propertyBase = pPropertyBase;
    property = pProperty;
    List<String> disableKeys = manager.getPropertyAsList(key + ".disableValues", "");
    for (String keyString: disableKeys) {
      String[] pair = keyString.split("=");
      //System.err.println("split '" + keyString + "'; pair.length = " + pair.length);
      if (pair != null && pair.length == 1) {
        String[] newPair = new String[2];
        newPair[0] = pair[0];
        newPair[1] = "";
        pair = newPair;
      }
      if (pair != null && pair.length == 2) {
        Set<String> valueSet = disableValues.get(pair[0]);
        if (valueSet == null) {
          valueSet = new HashSet<String>();
          disableValues.put(pair[0], valueSet);
        }
        valueSet.add(pair[1]);
      }
    }
  }

  /**
   * On initialization, if any of the source properties are set to
   * values to be disabled, disable the editor.
   */
  public void propertyInitialized(PropertyEditorUI source, String property, String newValue) {
    //System.err.println("property " + property + " initializing.");
    for (String key: disableValues.keySet()) {
      String fullProperty = key;
      if (key != null && key.startsWith(".")) {
        fullProperty = propertyBase + key;
      }
      String propValue = manager.getProperty(fullProperty, "");
      //System.err.println("fullProperty to check is " + fullProperty + ", value = '" + propValue + "'");
      Set<String> valueSet = disableValues.get(key);
      for (String value: valueSet) {
        //System.err.println("checking value " + value);
        if (propValue.equals(value)) {
          //System.err.println("match found; setting enabled to false.");
          source.setEnabled(false);
        }
      }
    }
  }


}
