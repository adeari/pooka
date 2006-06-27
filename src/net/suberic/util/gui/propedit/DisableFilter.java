package net.suberic.util.gui.propedit;
import java.util.*;

/**
 * A PropertyEditorListener which enables or disables certain editors
 * depending on whether or not a particular property is enabled.
 */
public class DisableFilter extends PropertyEditorAdapter implements ConfigurablePropertyEditorListener {
  List enableValues;
  List affectedEditors;

  /**
   * Configures this filter from the given key.
   */
  public void configureListener(String key, String property, String propertyBase, String editorTemplate, PropertyEditorManager manager) {
    enableValues = manager.getPropertyAsList(key + "._enableValues", "");
    affectedEditors = manager.getPropertyAsList(key + "._affectedEditors", "");
  }

  /**
   * In this case, if the property value is in the enabled list, then
   * the affectedEditors are enabled.  if not, then they are disabled.
   */
  public void propertyChanged(PropertyEditorUI source, String property, String newValue) {

  }
}
