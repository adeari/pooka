package net.suberic.util.gui.propedit;
import java.util.*;

/**
 * A PropertyEditorListener which enables or disables certain editors
 * depending on whether or not a particular property is enabled.
 */
public class DisableFilter implements ConfigurablePropertyEditorListener {
  List enableValues;
  List affectedEditors;

  /**
   * Configures this filter from the given key.
   */
  public void configureListener(String key, PropertyEditorManager manager) {
    enableValues = manager.getPropertyAsList(key + "._enableValues", "");
    affectedEditors = manager.getPropertyAsList(key + "._affectedEditors", "");
  }
  
  /**
   *
   * a no-op.
   */
  public void propertyChanging(PropertyEditorUI source, String property, String newValue) throws PropertyValueVetoException {

  }

  /**
   * In this case, if the property value is in the enabled list, then 
   * the affectedEditors are enabled.  if not, then they are disabled.
   */
  public void propertyChanged(PropertyEditorUI source, String property, String newValue) {

  }
}
