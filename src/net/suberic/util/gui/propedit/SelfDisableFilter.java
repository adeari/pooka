package net.suberic.util.gui.propedit;
import java.util.*;

/**
 * A PropertyEditorListener which enables or disables this editor if
 * certain values are set.
 */
public class SelfDisableFilter extends PropertyEditorAdapter implements ConfigurablePropertyEditorListener {
  Map<String,Set<String>> disableValues;
  PropertyEditorManager manager;
  String propertyBase;
  String property;

  /**
   * Configures this filter from the given key.
   */
  public void configureListener(String key, String pProperty, String pPropertyBase, String editorTemplate, PropertyEditorManager pManager) {
    manager = pManager;
    propertyBase = pPropertyBase;
    property = pProperty;
    List<String> disableKeys = manager.getPropertyAsList(key + ".disableValues", "");
    for (String keyString: disableKeys) {
      String[] pair = keyString.split("=");
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
    for (String key: disableValues.keySet()) {
      Set<String> valueSet = disableValues.get(key);
      for (String value: valueSet) {
        if (manager.getProperty(key, "").equals(value)) {
          source.setEnabled(false);
        }
      }
    }
  }


}
