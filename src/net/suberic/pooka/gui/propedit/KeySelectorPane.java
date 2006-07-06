package net.suberic.pooka.gui.propedit;
import net.suberic.util.gui.propedit.*;
import net.suberic.util.VariableBundle;
import net.suberic.pooka.gui.filechooser.*;
import net.suberic.pooka.*;

import javax.swing.*;
import java.awt.event.ActionEvent;

import java.util.Set;
import java.util.Vector;

/**
 * This displays the currently selected key (if any).
 */

public class KeySelectorPane extends LabelValuePropertyEditor {

  JLabel label;
  JComboBox valueDisplay;

  /**
   * @param propertyName The property to be edited.
   * @param template The property that will define the layout of the
   *                 editor.
   * @param manager The PropertyEditorManager that will manage the
   *                   changes.
   * @param isEnabled Whether or not this editor is enabled by default.
   */
  public void configureEditor(String propertyName, String template, String propertyBaseName, PropertyEditorManager newManager, boolean isEnabled) {
    configureBasic(propertyName, template, propertyBaseName, newManager, isEnabled);

    if (debug) {
      System.out.println("property is " + property + "; editorTemplate is " + editorTemplate);
    }

    label = createLabel();

    valueDisplay = createKeyList(originalValue);

    //valueDisplay.setPreferredSize(new java.awt.Dimension(150 - inputButton.getPreferredSize().width, valueDisplay.getMinimumSize().height));

    this.add(label);
    labelComponent = label;
    JPanel tmpPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0,0));
    tmpPanel.add(valueDisplay);
    tmpPanel.setPreferredSize(new java.awt.Dimension(Math.max(150, tmpPanel.getMinimumSize().width), valueDisplay.getMinimumSize().height));
    valueComponent = tmpPanel;
    this.add(tmpPanel);

    this.setEnabled(isEnabled);

  }

  /**
   * Creates a button that will bring up a way to select a folder.
   */
  public JComboBox createKeyList(String defaultValue) {
    if (Pooka.isDebug())
      System.out.println("creating keylist.");

    String encryptionType = manager.getProperty(editorTemplate + ".encryptionType", "All");

    if (encryptionType.equalsIgnoreCase("All"))
      encryptionType = null;

    String keyType = manager.getProperty(editorTemplate + ".keyType", "private");

    Set keySet = null;

    try {
      if (keyType.equalsIgnoreCase("private"))
  keySet = Pooka.getCryptoManager().privateKeyAliases(encryptionType);
      else
  keySet = Pooka.getCryptoManager().publicKeyAliases(encryptionType);
    } catch (java.security.KeyStoreException kse) {
      keySet = null;
    }

    Vector listModel = null;

    if (keySet != null) {
      listModel = new Vector(keySet);
    } else {
      listModel = new Vector();
    }

    if (originalValue != null && originalValue != "") {
      if (! listModel.contains(originalValue))
  listModel.add(originalValue);
      JComboBox returnValue = new JComboBox(listModel);
      returnValue.setSelectedItem(originalValue);

      return returnValue;
    } else {
      JComboBox returnValue = new JComboBox(listModel);

      return returnValue;
    }
  }

  //  as defined in net.suberic.util.gui.PropertyEditorUI

  public void setValue() {
    if (Pooka.isDebug())
      System.out.println("calling ksp.setValue.  isEnabled() = " + isEnabled() + "; isChanged() = " + isChanged());

    String newValue = (String) valueDisplay.getSelectedItem();
    if (newValue == null)
      newValue = "";

    if (isEnabled() && isChanged()) {
      manager.setProperty(property, newValue);
    }
  }

  public java.util.Properties getValue() {
    java.util.Properties retProps = new java.util.Properties();

    String newValue = (String) valueDisplay.getSelectedItem();
    if (newValue == null)
      newValue = "";

    retProps.setProperty(property, newValue);

    return retProps;
  }

  public void resetDefaultValue() {
    valueDisplay.setSelectedItem(originalValue);
  }

  public boolean isChanged() {
    return (!(originalValue.equals(valueDisplay.getSelectedItem())));
  }

  public void setEnabled(boolean newValue) {
    if (Pooka.isDebug())
      System.out.println("calling ksp.setEnabled(" + newValue + ")");

    if (valueDisplay != null) {
      valueDisplay.setEnabled(newValue);
    }
    if (Pooka.isDebug())
      System.out.println("set enabled to " + newValue);

    enabled=newValue;
  }

}
