package net.suberic.util.gui.propedit;
import net.suberic.util.VariableBundle;
import java.util.HashMap;

/**
 * This manages a set of PropertyEditors.
 */
public class PropertyEditorManager {

  protected HashMap editorMap = new HashMap();

  protected VariableBundle sourceBundle;

  /**
   * Creates a new PropertyEditorManager.
   */
  protected PropertyEditorManager() {
  }

  /**
   * Creates a PropertyEditorManager using the given VariableBundle.
   */
  public PropertyEditorManager(VariableBundle vb) {
    sourceBundle = vb;
  }

  /**
   * Gets the PropertyEditor for the given Property.
   */
  public PropertyEditorUI getPropertyEditor(String propertyName) {
    return (PropertyEditorUI) editorMap.get(propertyName);
  }

  /**
   * Registers the given PropertyEditorUI as the editor for the given
   * Property.
   */
  public void registerPropertyEditor(String property, PropertyEditorUI peui) {
    editorMap.put(property, peui);
  }
}
