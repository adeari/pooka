package net.suberic.util.gui.propedit;
import net.suberic.util.VariableBundle;
import net.suberic.util.gui.IconManager;
import java.util.*;

/**
 * This manages a set of PropertyEditors.  Basically, this acts as a
 * Transaction for the PropertyEditors.
 */
public class PropertyEditorManager {

  protected HashMap editorMap = new HashMap();

  protected VariableBundle sourceBundle;

  protected PropertyEditorFactory propertyFactory;

  protected HashMap pendingListenerMap = new HashMap();

  protected boolean writeChanges = true;

  protected Properties localProps = new Properties();

  protected HashSet<String> removeProps = new HashSet<String>();

  protected IconManager iconManager;

  // whether or not we've created a PropertyEditorPane for this Manager.
  public boolean createdEditorPane = false;

  /**
   * Creates a new PropertyEditorManager.
   */
  protected PropertyEditorManager() {
  }

  /**
   * Creates a PropertyEditorManager using the given VariableBundle,
   * PropertyEditorFactory, and IconManager.
   */
  public PropertyEditorManager(VariableBundle vb, PropertyEditorFactory factory, IconManager manager) {
    sourceBundle = vb;
    propertyFactory = factory;
    iconManager = manager;
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
  public void registerPropertyEditor(String property, PropertyEditorUI editor) {
    List listenerList = (List) pendingListenerMap.get(property);
    if (listenerList != null) {
      Iterator it = listenerList.iterator();
      while (it.hasNext()) {
        editor.addPropertyEditorListener((PropertyEditorListener) it.next());
      }
    }

    editorMap.put(property, editor);
  }

  /**
   * Gets the PropertyEditorFactory for this manager.
   */
  public PropertyEditorFactory getFactory() {
    return propertyFactory;
  }

  /**
   * Gets the IconManager for this PropertyEditorManager.
   */
  public IconManager getIconManager() {
    return iconManager;
  }

  /**
   * Gets the value of the given property.
   */
  public String getProperty(String property, String defaultValue) {
    // check the localProps first
    String tmpValue = (String) localProps.get(property);
    if (tmpValue != null)
      return tmpValue;
    return sourceBundle.getProperty(property, defaultValue);
  }

  /**
   * Gets the value of the given property.
   */
  public List<String> getPropertyAsList(String property, String defaultValue) {
    // check the localProps first
    String tmpValue = (String) localProps.get(property);
    if (tmpValue != null) {
      return VariableBundle.convertToVector(tmpValue);
    }
    return sourceBundle.getPropertyAsList(property, defaultValue);
  }

  /**
   * Sets the given property to the given value.
   */
  public void setProperty(String property, String value) {
    localProps.setProperty(property, value);
    removeProps.remove(property);
  }

  /**
   * Removes the given property.
   */
  public void removeProperty(String property) {
    removeProps.add(property);
  }

  /**
   * Creates an appropriate PropertyEditorUI for the given property and
   * editorTemplate, using this PropertyEditorManager.
   */
  public PropertyEditorUI createEditor(String property, String editorTemplate, String propertyBase) {
    return getFactory().createEditor(property, editorTemplate, propertyBase, this, true);
  }

  /**
   * Commits the changes to the underlying VariableBundle.
   */
  public void commit() {
    if (writeChanges) {
      System.err.println("committing.");
      for (String removeProp: removeProps) {
        System.err.println("removing property " + removeProp);
        sourceBundle.removeProperty(removeProp);
      }

      for (String property: localProps.stringPropertyNames()) {
        System.err.println("setting property " + property);
        sourceBundle.setProperty(property, localProps.getProperty(property));
      }

      sourceBundle.saveProperties();
    }
  }

  /**
   * Adds the given PropertyEditorListener as a listener to the editor
   * for the given property.  If no editor exists yet, then the listener
   * is saved until a PropertyEditorUI is registered.
   */
  public void addPropertyEditorListener(String property, PropertyEditorListener listener) {
    PropertyEditorUI editor = (PropertyEditorUI) editorMap.get(property);
    if ( editor != null) {
      editor.addPropertyEditorListener(listener);
    } else {
      List listenerList = (List) pendingListenerMap.get(property);
      if (listenerList == null) {
        listenerList = new ArrayList();
      }
      listenerList.add(listener);
      pendingListenerMap.put(property, listenerList);
    }
  }

  /**
   * Creates an appropriate PropertyEditorListener from the given
   * String.
   */
  public PropertyEditorListener createListener(String key, String property, String propertyBase, String editorTemplate) {
    try {
      Class pelClass = Class.forName(getProperty(key + ".class", ""));
      ConfigurablePropertyEditorListener pel = (ConfigurablePropertyEditorListener) pelClass.newInstance();
      pel.configureListener(key, property, propertyBase, editorTemplate, this);
      return pel;
    } catch (Exception e) {
      System.err.println("error configuring listener from key " + key + " for property " + property);
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Sets whether or not this PEM should write its changes to the source
   * VariableBundle.
   */
  public void setWriteChanges(boolean newValue) {
    writeChanges = newValue;
  }

}

