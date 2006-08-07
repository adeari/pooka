package net.suberic.util.gui.propedit;
import java.util.*;

/**
 * A PropertyEditorListener which enables or disables certain editors
 * depending on whether or not a particular property is enabled.
 */
public class DisableFilter extends PropertyEditorAdapter implements ConfigurablePropertyEditorListener {
  List<String> disableValues;
  List<String> affectedEditors;
  List<PropertyEditorUI> affectedEditorUIs;
  PropertyEditorManager manager;
  String propertyBase;

  /**
   * Configures this filter from the given key.
   */
  public void configureListener(String key, String property, String pPropertyBase, String editorTemplate, PropertyEditorManager pManager) {
    manager = pManager;
    propertyBase = pPropertyBase;
    disableValues = manager.getPropertyAsList(key + ".disableValues", "");
    affectedEditors = manager.getPropertyAsList(key + ".affectedEditors", "");
    //System.err.println("setting for " + property + ", disableValue= " + manager.getPropertyAsList(key + ".disableValues", "") + ", affectedEditors=" + manager.getPropertyAsList(key + ".affectedEditors", ""));
  }

  /**
   * In this case, if the property value is in the enabled list, then
   * the affectedEditors are enabled.  if not, then they are disabled.
   */
  public void propertyChanged(PropertyEditorUI source, String property, String newValue) {
    //System.err.println("property " + property + " Changed.");
    if (disableValues.contains(newValue)) {
      setAllEnabled(false);
    } else {
      setAllEnabled(true);
    }
  }

  /**
   * Sets all affected editors as enabled or disabled.
   */
  void setAllEnabled(boolean pEnabled) {
    //System.err.println("setting enabled.");
    for (String affectedProperty: affectedEditors) {
      String fullProperty = affectedProperty;
      if (affectedProperty != null && affectedProperty.startsWith(".")) {
        fullProperty = propertyBase + affectedProperty;
      }
      //System.err.println("setting enabled/disabled for " + fullProperty);
      PropertyEditorUI ui = manager.getPropertyEditor(fullProperty);
      if (ui != null) {
        //System.err.println("found.  setting.");
        ui.setEnabled(pEnabled);
      }
    }
  }
}
