package net.suberic.util.gui.propedit;
import net.suberic.util.VariableBundle;
import net.suberic.util.gui.IconManager;
import java.util.*;

/**
 * This manages a set of PropertyEditors.  Basically, this acts as a
 * Transaction for the PropertyEditors.
 */
public class PropertyEditorManager {

  protected HashMap<String,PropertyEditorUI> editorMap = new HashMap<String,PropertyEditorUI>();

  protected VariableBundle sourceBundle;

  protected PropertyEditorFactory propertyFactory;

  protected HashMap<String,List<PropertyEditorListener>> listenerMap = new HashMap<String,List<PropertyEditorListener>>();

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
  public String getCurrentProperty(String property, String defaultValue) {
    String tmpValue = null;
    // check the PropertyEditor first.
    PropertyEditorUI editor = getPropertyEditor(property);
    if (editor != null) {
      Properties value = editor.getValue();
      tmpValue = value.getProperty(property);
      if (tmpValue != null)
        return tmpValue;
    }
    // then check the localProps
    tmpValue = (String) localProps.get(property);
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
   * Gets the value of the given property.
   */
  public Set<String> getPropertyNamesStartingWith(String startsWith) {
    Set<String> returnValue = new HashSet<String>();
    // check local properties first.
    Set<String> lProps = localProps.stringPropertyNames();
    for (String prop: lProps) {
      if (prop.startsWith(startsWith))
        returnValue.add(prop);
    }
    returnValue.addAll(sourceBundle.getPropertyNamesStartingWith(startsWith));
    return returnValue;
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
      for (String removeProp: removeProps) {
        sourceBundle.removeProperty(removeProp);
      }

      for (String property: localProps.stringPropertyNames()) {
        sourceBundle.setProperty(property, localProps.getProperty(property));
      }

      sourceBundle.saveProperties();
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

  /**
   * Adds a PropertyEditorListener to the ListenerList.
   */
  public void addPropertyEditorListener(String property, PropertyEditorListener pel) {
    if (property != null) {
      List<PropertyEditorListener> listenerList = listenerMap.get(property);
      if (listenerList == null) {
        listenerList = new ArrayList<PropertyEditorListener>();
        listenerMap.put(property, listenerList);
      }
      if (pel != null && ! listenerList.contains(pel))
        listenerList.add(pel);
    }
  }

  /**
   * Removes a PropertyEditorListener from the ListenerList.
   */
  public void removePropertyEditorListener(String property, PropertyEditorListener pel) {
    if (property != null) {
      List<PropertyEditorListener> listenerList = listenerMap.get(property);
      if (listenerList != null) {
        if (pel != null && listenerList.contains(pel))
          listenerList.remove(pel);
      }
    }
  }

  /**
   * Fires a propertyChanging event to all of the PropertyEditorListeners.
   * If any of the listeners veto the new value, then this returns false.
   * Otherwise, returns true.
   */
  public void firePropertyChangingEvent(PropertyEditorUI propertyEditor, String newValue) throws PropertyValueVetoException {
    String property = propertyEditor.getProperty();
    List<PropertyEditorListener> listenerList = listenerMap.get(property);
    if (listenerList != null) {
      for (PropertyEditorListener current: listenerList) {
        current.propertyChanging(propertyEditor, property, newValue);
      }
    }
  }

  /**
   * Fires a propertyChanged event to all of the PropertyEditorListeners.
   */
  public void firePropertyChangedEvent(PropertyEditorUI propertyEditor, String newValue) {
    String property = propertyEditor.getProperty();
    List<PropertyEditorListener> listenerList = listenerMap.get(property);
    if (listenerList != null) {
      for (PropertyEditorListener current: listenerList) {
        current.propertyChanged(propertyEditor, property, newValue);
      }
    }
  }

  /**
   * Fires a propertyCommitting event to all of the PropertyEditorListeners.
   */
  public void firePropertyCommittingEvent(PropertyEditorUI propertyEditor, String newValue) throws PropertyValueVetoException {
    String property = propertyEditor.getProperty();
    List<PropertyEditorListener> listenerList = listenerMap.get(property);
    if (listenerList != null) {
      for (PropertyEditorListener current: listenerList) {
        current.propertyCommitting(propertyEditor, property, newValue);
      }
    }
  }

  /**
   * Fires a propertyInitialized event to all of the PropertyEditorListeners.
   */
  public void firePropertyInitializedEvent(PropertyEditorUI propertyEditor, String newValue) {
    String property = propertyEditor.getProperty();
    List<PropertyEditorListener> listenerList = listenerMap.get(property);
    if (listenerList != null) {
      for (PropertyEditorListener current: listenerList) {
        current.propertyInitialized(propertyEditor, property, newValue);
      }
    }
  }



}

