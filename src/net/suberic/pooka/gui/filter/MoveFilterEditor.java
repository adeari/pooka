package net.suberic.pooka.gui.filter;
import net.suberic.pooka.gui.propedit.FolderSelectorPane;
import java.util.Properties;

/**
 * This is a class that lets you choose your filter actions.
 */
public class MoveFilterEditor extends FilterEditor {
  String originalFolderName;
  
  FolderSelectorPane fsp;
  
  public static String FILTER_CLASS = "net.suberic.pooka.filter.MoveFilterAction";
  
  /**
   * Configures the given FilterEditor from the given VariableBundle and
   * property.
   */
  public void configureEditor(net.suberic.util.gui.propedit.PropertyEditorManager newManager, String propertyName) {
    property = propertyName;
    manager = newManager;
    
    fsp = new FolderSelectorPane();
    fsp.configureEditor(propertyName + ".targetFolder", manager);
    
    this.add(fsp);
  }
  
  /**
   * Gets the values that would be set by this FilterEditor.
   */
  public java.util.Properties getValue() {
    Properties props = fsp.getValue();
    
    String oldClassName = manager.getProperty(property + ".class", "");
    if (!oldClassName.equals(FILTER_CLASS))
      props.setProperty(property + ".class", FILTER_CLASS);
    
    return props;
  }
  
  /**
   * Sets the values represented by this FilterEditor in the manager.
   */
  public void setValue() {
    
    fsp.setValue();
    
    String oldClassName = manager.getProperty(property + ".class", "");
    if (!oldClassName.equals(FILTER_CLASS))
      manager.setProperty(property + ".class", FILTER_CLASS);
  }
  
  /**
   * Returns the class that will be set for this FilterEditor.
   */
  public String getFilterClassValue() {
    return FILTER_CLASS;
  }

}
