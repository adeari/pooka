package net.suberic.util.gui.propedit;
import net.suberic.util.*;
import javax.swing.*;
import java.awt.Dimension;
import java.util.List;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * A Swing implementation of the PropertyEditorUI.
 */
public abstract class SwingPropertyEditor extends JPanel implements PropertyEditorUI {
  // debug flag
  protected boolean debug = false;

  // shows whether or not this component is enabled.
  protected boolean enabled;

  // the property being edited.
  protected String property;

  // the template to use
  protected String editorTemplate;

  // the property base to use
  protected String propertyBase;

  // the original value of the property.
  protected String originalValue;

  // the PorpertyEditorManager for this instance.
  protected PropertyEditorManager manager;

  // the listener list.
  protected List listenerList = new LinkedList();

  // the logger
  protected static Logger sLogger =  Logger.getLogger("editors.debug");

  /**
   * Creates a new SwingPropertyEditor, in this case a JPanel with a
   * SpringLayout.  Note that configureEditor() will need to get called
   * on this component in order to make it useful.
   */
  public SwingPropertyEditor() {
    super();
    this.setLayout(new java.awt.GridBagLayout());
  }

  /**
   * Creates a SwingPropertyEditor using the given property and manager.
   *
   * @param propertyName The property to be edited.  This property will
   *        also be used to define the layout of this Editor.
   * @param template The template to be used for this property
   * @param baseProperty The base property to be used for other scoped
   *                     properties.
   * @param newManager The PropertyEditorManager that will manage the
   *                   changes.
   * @param isEnabled Whether or not this editor is enabled by default.
   */
  public SwingPropertyEditor(String propertyName, String template, String baseProperty, PropertyEditorManager newManager, boolean isEnabled) {
    configureEditor(propertyName, template, baseProperty,  newManager, isEnabled);
  }

  /**
   * Creates a SwingPropertyEditor using the given property and manager.
   *
   * @param propertyName The property to be edited.  This property will
   *        also be used to define the layout of this Editor.
   * @param newManager The PropertyEditorManager that will manage the
   *                   changes.
   * @param isEnabled Whether or not this editor is enabled by default.
   */
  public SwingPropertyEditor(String propertyName, PropertyEditorManager newManager, boolean isEnabled) {
    configureEditor(propertyName, propertyName, newManager, isEnabled);
  }

  /**
   * Creates a SwingPropertyEditor using the given property and manager.
   *
   * @param propertyName The property to be edited.
   * @param template The property that will define the layout of the
   *                 editor.
   * @param newManager The PropertyEditorManager that will manage the
   *                   changes.
   */
  public SwingPropertyEditor(String propertyName, String template, PropertyEditorManager newManager ) {
    configureEditor(propertyName, template, newManager, true);
  }


  /**
   * Creates a SwingPropertyEditor using the given property and manager.
   *
   * @param propertyName The property to be edited.  This property will
   *        also be used to define the layout of this Editor.
   * @param newManager The PropertyEditorManager that will manage the
   *                   changes.
   */
  public void configureEditor(String propertyName, PropertyEditorManager newManager) {
    configureEditor(propertyName, propertyName, newManager, true);
  }

  /**
   * Creates a SwingPropertyEditor using the given property and manager.
   *
   * @param propertyName The property to be edited.  This property will
   *        also be used to define the layout of this Editor.
   * @param newManager The PropertyEditorManager that will manage the
   *                   changes.
   * @param isEnabled Whether or not this editor is enabled by default.
   */
  public void configureEditor(String propertyName, PropertyEditorManager newManager, boolean isEnabled) {
    configureEditor(propertyName, propertyName, newManager, isEnabled);
  }

  public void configureEditor(String propertyName, String template, PropertyEditorManager manager, boolean isEnabled) {
    configureEditor(propertyName, template, propertyName, manager, isEnabled);
  }

  /**
   * Loads the basic properties for all SwingPropertyEditors.
   */
  public void configureBasic(String propertyName, String template, String propertyBaseName, PropertyEditorManager newManager, boolean isEnabled) {
    manager=newManager;
    propertyBase=propertyBaseName;
    editorTemplate = template;
    if (propertyBaseName == null || propertyBaseName.length() == 0 || propertyBaseName.equals(propertyName)) {
      property = propertyName;
    } else {
      property = propertyBaseName + "." + propertyName;
    }
    addDefaultListeners();
    enabled=isEnabled;
    originalValue = manager.getProperty(property, "");
    firePropertyInitializedEvent(originalValue);
  }

  /**
   * Returns the enabled flag.
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Gets the PropertyEditorManager
   */
  public PropertyEditorManager getManager() {
    return manager;
  }

  /**
   * Adds a PropertyEditorListener to the ListenerList.
   */
  public void addPropertyEditorListener(PropertyEditorListener pel) {
    if (pel != null && ! listenerList.contains(pel))
      listenerList.add(pel);
  }

  /**
   * Removes a PropertyEditorListener from the ListenerList.
   */
  public void removePropertyEditorListener(PropertyEditorListener pel) {
    if (pel != null && listenerList.contains(pel))
      listenerList.remove(pel);
  }

  /**
   * Fires a propertyChanging event to all of the PropertyEditorListeners.
   * If any of the listeners veto the new value, then this returns false.
   * Otherwise, returns true.
   */
  public void firePropertyChangingEvent(String newValue) throws PropertyValueVetoException {
    for (int i = 0; i < listenerList.size(); i++) {
      PropertyEditorListener current = (PropertyEditorListener) listenerList.get(i);
      current.propertyChanging(this, property, newValue);
    }
  }

  /**
   * Fires a propertyChanged event to all of the PropertyEditorListeners.
   */
  public void firePropertyChangedEvent(String newValue) {
    System.err.println("propChangedEvent:  listenerList.size() = " + listenerList.size());
    for (int i = 0; i < listenerList.size(); i++) {
      System.err.println("notifying listener " + i);
      PropertyEditorListener current = (PropertyEditorListener) listenerList.get(i);
      current.propertyChanged(this, property, newValue);
    }
  }

  /**
   * Fires a propertyInitialized event to all of the PropertyEditorListeners.
   */
  public void firePropertyInitializedEvent(String newValue) {
    for (int i = 0; i < listenerList.size(); i++) {
      PropertyEditorListener current = (PropertyEditorListener) listenerList.get(i);
      current.propertyInitialized(this, property, newValue);
    }
  }

  /**
   * Gets the parent PropertyEditorPane for the given component.
   */
  public abstract PropertyEditorPane getPropertyEditorPane();

  /**
   * Gets the parent PropertyEditorPane for the given component.
   */
  protected PropertyEditorPane getPropertyEditorPane(java.awt.Component component) {
    try {
      Class pepClass = Class.forName("net.suberic.util.gui.propedit.PropertyEditorPane");
      if (pepClass != null) {
        PropertyEditorPane pep = (PropertyEditorPane) SwingUtilities.getAncestorOfClass(pepClass, component);
        return pep;
      }
    } catch (Exception e) {
    }

    return null;
  }

  /**
   * Adds the appropriate listeners.
   */
  public void addDefaultListeners() {
    List propertyListenerList = manager.getPropertyAsList(editorTemplate + "._listeners", "");
    java.util.Iterator it = propertyListenerList.iterator();
    while (it.hasNext()) {
      String current = (String)it.next();
      System.err.println("adding listener " + current + " to editor for " + getProperty());
      PropertyEditorListener pel = manager.createListener(current, property, propertyBase, editorTemplate);
      if (pel != null) {
        System.err.println("adding listener " + current + " to editor for " + getProperty() + ":  listener is " + pel);
        addPropertyEditorListener(pel);
      }
    }
  }

  /**
   * Returns the currently edited property.
   */
  public String getProperty() {
    return property;
  }

  /**
   * Sets the original value.
   */
  public void setOriginalValue(String pOriginalValue) {
    originalValue = pOriginalValue;
  }

  /**
   * Returns the template for the current property.
   */
  public String getEditorTemplate() {
    return editorTemplate;
  }

  /**
   * Returns the helpId for this editor.
   */
  public String getHelpID() {
    return getEditorTemplate();
  }

  /**
   * Gets the Logger for this Editor.
   *
   */
  public Logger getLogger() {
    return sLogger;
  }

}
