package net.suberic.util.gui.propedit;
import net.suberic.util.*;
import javax.swing.*;
import java.awt.Dimension;
import java.util.List;
import java.util.LinkedList;

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

  // the original value of the property.
  protected String originalValue;

  // the PorpertyEditorManager for this instance.
  protected PropertyEditorManager manager;
  
  // the label component.  this is used for a default implementation
  // of the sizing code we have below.
  protected java.awt.Container labelComponent;
  
  // the value component.  this is used for a default implementation
  // of the sizing code we have below.
  protected java.awt.Container valueComponent;

  // the listener list.
  protected List listenerList = new LinkedList();

  /**
   * Creates a new SwingPropertyEditor, in this case a JPanel with a 
   * GridBagLayout.  Note that configureEditor() will need to get called
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

  /**
   * A default implementation of setEnabled.  This simply sets the
   * enabled flag to the newValue.  If the labelComponent and 
   * valueComponent attributes are set, it will also call setEnabled
   * on those.
   *
   * Subclasses which do not use the default labelComponent and 
   * valueComponent attributes, or which require additional functionality,
   * should override this method.
   */
  public void setEnabled(boolean newValue) {
    enabled=newValue;
  }
  
  /**
   * Returns the enabled flag.
   */
  public boolean isEnabled() {
    return enabled;
  }
  
  /**
   * Creates a JLabel for this component.
   */
  public JLabel createLabel() {
    String defaultLabel;
    int dotIndex = property.lastIndexOf(".");
    if (dotIndex == -1) 
      defaultLabel = new String(property);
    else
      defaultLabel = property.substring(dotIndex+1);
    
    JLabel returnValue = new JLabel(manager.getProperty(editorTemplate + ".label", defaultLabel));
    
    return returnValue;
  }

  /**
   * Gets the minimum size for the labelComponent.
   */
  public Dimension getMinimumLabelSize() {
    if (labelComponent != null) {
      return labelComponent.getMinimumSize();
    } else {
      return new Dimension(0,0);
    }
  }
  
  /**
   * Gets the minimum size for the valueComponent.
   */
  public Dimension getMinimumValueSize() {
    if (valueComponent != null) {
      return valueComponent.getMinimumSize();
    } else {
      return new Dimension(0,0);
    }
  }
  
  /**
   * Returns the calculated minimum size for this component.
   */
  public Dimension getMinimumTotalSize() {
    return this.getMinimumSize();
  }
  
  /**
   * Sets the size for the label component and the value component.
   */
  public void setSizes(Dimension labelSize, Dimension valueSize) {
    if (labelComponent != null)
      labelComponent.setSize(labelSize);
    if (valueComponent != null)
      valueComponent.setSize(valueSize);
  }
  
  /**
   * Sets the widths for the label component and the value component.
   */
  public void setWidths(int labelWidth, int valueWidth) {
    if (labelComponent != null)
      labelComponent.setSize(new Dimension(labelWidth, labelComponent.getSize().height));
    if (valueComponent != null)
      valueComponent.setSize(new Dimension(valueWidth, valueComponent.getSize().height));
  }
  
  /**
   * Sets the heights for the label component and the value component.
   */
  public void setHeights(int labelHeight, int valueHeight) {
    if (labelComponent != null)
      labelComponent.setSize(new Dimension(labelComponent.getSize().width, labelHeight));
    if (valueComponent != null)
      valueComponent.setSize(new Dimension(valueComponent.getSize().width, valueHeight));
  }
  
  /**
   * Gets the current valueComponent.
   */
  public java.awt.Container getValueComponent() {
    return valueComponent;
  }

  /**
   * Gets the current labelComponent.
   */
  public java.awt.Container getLabelComponent() {
    return valueComponent;
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
    for (int i = 0; i < listenerList.size(); i++) {
      PropertyEditorListener current = (PropertyEditorListener) listenerList.get(i);
      current.propertyChanged(this, property, newValue);
    }
  }

  /**
   * Gets the parent PropertyEditorPane for the current valueComponent.
   */
  public PropertyEditorPane getPropertyEditorPane() {
    try {
      Class pepClass = Class.forName("net.suberic.util.gui.propedit.PropertyEditorPane");
      if (pepClass != null) {
	PropertyEditorPane pep = (PropertyEditorPane) SwingUtilities.getAncestorOfClass(pepClass, valueComponent);
	return pep;
      }
    } catch (Exception e) {
    }

    return null;
  }

  /**
   * Does a resize on the parent PropertyEditorPane, if any.
   */
  public void doResize() {
    PropertyEditorPane pep = getPropertyEditorPane();
    if (pep != null) {
      pep.resizeEditor();
    }
  }

  /**
   * Adds the appropriate listeners.
   */
  public void addDefaultListeners() {
    List propertyListenerList = manager.getPropertyAsList(editorTemplate + "._listeners", "");
    java.util.Iterator it = propertyListenerList.iterator();
    while (it.hasNext()) {
      String current = (String)it.next();
      PropertyEditorListener pel = manager.createListener(current);
      if (pel != null) {
	addPropertyEditorListener(pel);
      }
    }
  }
}
