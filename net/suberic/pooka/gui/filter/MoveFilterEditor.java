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
  public void configureEditor(net.suberic.util.VariableBundle bundle, String propertyName) {
    property = propertyName;
    sourceBundle = bundle;
    
    fsp = new FolderSelectorPane(propertyName + ".targetFolder", sourceBundle);
    
    this.add(fsp);
  }
  
  /**
   * Gets the values that would be set by this FilterEditor.
   */
  public java.util.Properties getValue() {
    Properties props = fsp.getValue();
    
    String oldClassName = sourceBundle.getProperty(property + ".class", "");
    if (!oldClassName.equals(FILTER_CLASS))
      props.setProperty(property + ".class", FILTER_CLASS);
    
    return props;
  }
  
  /**
   * Sets the values represented by this FilterEditor in the sourceBundle.
   */
  public void setValue() {
    
    fsp.setValue();
    
    String oldClassName = sourceBundle.getProperty(property + ".class", "");
    if (!oldClassName.equals(FILTER_CLASS))
      sourceBundle.setProperty(property + ".class", FILTER_CLASS);
  }
  
}
