package net.suberic.util.gui.propedit;
import javax.swing.*;
import java.util.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.CardLayout;

/**
 * This will made a panel which can change depending on
 * exact properties which are then edited will depend on the value of
 * another propery.
 *
 * Special settings:  keyProperty is the property that will be used
 * to define this editor.
 */
public class VariableEditorPane extends CompositeSwingPropertyEditor {

  HashMap idToEditorMap = new HashMap();
  String keyProperty;

  /**
   * This configures this editor with the following values.
   *
   * @param propertyName The property to be edited.
   * @param template The property that will define the layout of the
   *                 editor.
   * @param manager The PropertyEditorManager that will manage the
   *                   changes.
   * @param isEnabled Whether or not this editor is enabled by default.
   */
  public void configureEditor(String propertyName, String template, String propertyBaseName, PropertyEditorManager newManager, boolean isEnabled) {
    configureBasic(propertyName, template, propertyBaseName, newManager, isEnabled);
    debug = manager.getProperty("editors.debug", "false").equalsIgnoreCase("true");

    keyProperty = createSubProperty(manager.getProperty(editorTemplate + ".keyProperty", ""));

    editors = new Vector();

    manager.addPropertyEditorListener(keyProperty, new PropertyEditorAdapter() {
        public void propertyChanged(PropertyEditorUI ui, String prop, String newValue) {
          showPanel(newValue);
        }
      });

    this.setLayout(new java.awt.CardLayout());

    String currentValue = manager.getProperty(keyProperty, "");
    if (currentValue == "") {
      // check the editor for this, if any.
      PropertyEditorUI keyEditor = manager.getPropertyEditor(keyProperty);
      if (keyEditor != null) {
        currentValue = keyEditor.getValue().getProperty(keyProperty, "");
      }
    }

    showPanel(currentValue);

    manager.registerPropertyEditor(property, this);
  }

  /**
   * This shows the editor window for the configured value.
   */
  public void showPanel(String selectedId) {
    boolean enableMe = true;
    if (selectedId == null || selectedId.equals("")) {
      enableMe = false;
    }

    CardLayout layout = (CardLayout) getLayout();

    Object newSelected = idToEditorMap.get(selectedId);
    if (newSelected == null) {
      // we'll have to make a new window.
      if (selectedId == null || selectedId.equals("")) {
        JPanel jp = new JPanel();
        this.add(selectedId, jp);
      } else {
        SwingPropertyEditor spe = createEditorPane(selectedId);

        // save reference to new pane in hash table
        idToEditorMap.put(selectedId, spe);
        editors.add(spe);

        spe.setEnabled(enableMe && enabled);
        this.add(selectedId, spe);
      }
    }
    layout.show(this, selectedId);
  }

  /**
   * Creates a SwingPropertyEditor for the given subproperty.
   */
  public SwingPropertyEditor createEditorPane(String selectedId) {

    String editValue = createSubTemplate("." + selectedId);

    /*
    if (scoped) {
      editValue = editorTemplate + "." + selectedId;
      if (debug) {
        System.out.println("scoped; editValue = " + editValue);
      }
    } else {
      if (debug)
        System.out.println("not scoped; editValue = " + editValue);
    }

    SwingPropertyEditor returnValue = (SwingPropertyEditor)manager.createEditor(property, editValue);
    */

    SwingPropertyEditor returnValue = (SwingPropertyEditor)manager.getFactory().createEditor(property, editValue, propertyBase, manager, enabled);
    return returnValue;
  }
}



