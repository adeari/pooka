package net.suberic.util.gui.propedit;
import javax.swing.*;
import java.util.List;
import java.util.LinkedList;
import javax.help.CSH;
import java.awt.*;

/**
 * This is a top-level editor for properties.  It includes buttons for
 * activating changes, accepting and closing, and cancelling the action.
 */
public class PropertyEditorPane extends JPanel {
  SwingPropertyEditor editor;
  PropertyEditorManager manager;
  Container container;

  /**
   * This contructor creates a PropertyEditor using the given
   * SwingPropertyEditor.
   */
  public PropertyEditorPane(PropertyEditorManager newManager,
                            SwingPropertyEditor newEditor,
                            Container newContainer) {
    manager = newManager;
    container = newContainer;
    editor = newEditor;

    Component editorComponent = editor;

    if (editor instanceof LabelValuePropertyEditor) {
      JPanel editorPanel = new JPanel();
      SpringLayout editorPanelLayout = new SpringLayout();
      editorPanel.setLayout(editorPanelLayout);

      LabelValuePropertyEditor lvEditor = (LabelValuePropertyEditor) editor;
      editorPanel.add(lvEditor.getLabelComponent());
      editorPanel.add(lvEditor.getValueComponent());

      editorPanelLayout.putConstraint(SpringLayout.WEST, lvEditor.getLabelComponent(), 5, SpringLayout.WEST, editorPanel);
      editorPanelLayout.putConstraint(SpringLayout.NORTH, lvEditor.getLabelComponent(), 5, SpringLayout.NORTH, editorPanel);
      editorPanelLayout.putConstraint(SpringLayout.SOUTH, lvEditor.getLabelComponent(), -5, SpringLayout.SOUTH, editorPanel);

      editorPanelLayout.putConstraint(SpringLayout.WEST, lvEditor.getValueComponent(), 5 ,SpringLayout.EAST, lvEditor.getLabelComponent());

      editorPanelLayout.putConstraint(SpringLayout.NORTH, lvEditor.getValueComponent(), 5 ,SpringLayout.NORTH, editorPanel);
      editorPanelLayout.putConstraint(SpringLayout.SOUTH, editorPanel, 5 ,SpringLayout.SOUTH, lvEditor.getValueComponent());
      editorPanelLayout.putConstraint(SpringLayout.EAST, editorPanel, 5 ,SpringLayout.EAST, lvEditor.getValueComponent());

      editorComponent = editorPanel;
    }

    JPanel buttonPanel = createButtonPanel();

    pepLayout(editorComponent, buttonPanel);

  }

  /**
   * Does the layout for the PropertyEditorPane.
   */
  private void pepLayout(Component editorPanel, Component buttonPanel) {

    SpringLayout layout = new SpringLayout();
    this.setLayout(layout);

    this.add(editorPanel);

    layout.putConstraint(SpringLayout.WEST, editorPanel, 5, SpringLayout.WEST, this);
    layout.putConstraint(SpringLayout.NORTH, editorPanel, 5, SpringLayout.NORTH, this);
    layout.putConstraint(SpringLayout.EAST, this, 5, SpringLayout.EAST, editorPanel);
    layout.putConstraint(SpringLayout.SOUTH, this, 5, SpringLayout.SOUTH, editorPanel);

    this.add(buttonPanel);
    layout.putConstraint(SpringLayout.NORTH, buttonPanel, 5, SpringLayout.SOUTH, editorPanel);

    layout.putConstraint(SpringLayout.WEST, buttonPanel, 5, SpringLayout.WEST, this);
    layout.putConstraint(SpringLayout.EAST, buttonPanel, -5, SpringLayout.EAST, this);
    layout.putConstraint(SpringLayout.SOUTH, this, 5, SpringLayout.SOUTH, buttonPanel);

  }

  /**
   * Accepts the changes for the edited properties, and writes them to
   * the PropertyEditorManager.
   */
  public void setValue() throws PropertyValueVetoException {
    editor.setValue();
  }

  /**
   * Gets the currently selected values for the edited properties.
   */
  public java.util.Properties getValue() {
    return editor.getValue();
  }

  /**
   * Resets the original values for the edited properties.
   */
  public void resetDefaultValue() throws PropertyValueVetoException {
    editor.resetDefaultValue();
  }

  /**
   * Creates the appropriate buttons (Ok, Accept, Cancel) to this component.
   */
  public JPanel createButtonPanel() {
    JPanel buttonPanel = new JPanel();

    JButton helpButton = createButton("Help", new AbstractAction() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          System.err.println("showing help for " + editor.getHelpID());
          //manager.getFactory().getHelpBroker().showID(editor.getHelpID(), null, null);
          new CSH.DisplayHelpFromSource(manager.getFactory().getHelpBroker()).actionPerformed(e);
        }
      }, true);

    CSH.setHelpIDString(helpButton, "UserProfile");
    buttonPanel.add(helpButton);

    buttonPanel.add(createButton("Ok", new AbstractAction() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          try {
            setValue();
            manager.commit();
            if (container instanceof JInternalFrame) {
              try {
                ((JInternalFrame)container).setClosed(true);
              } catch (java.beans.PropertyVetoException pve) {
              }
            } else if (container instanceof JFrame) {
              ((JFrame)container).dispose();
            } else if (container instanceof JDialog) {
              ((JDialog)container).dispose();
            }
          } catch (PropertyValueVetoException pvve) {
            manager.getFactory().showError(PropertyEditorPane.this, "Error changing value " + pvve.getProperty() + " to " + pvve.getRejectedValue() + ":  " + pvve.getReason());
          }
        }
      }, true));

    buttonPanel.add(createButton("Apply", new AbstractAction() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          try {
            setValue();
            manager.commit();
          } catch (PropertyValueVetoException pvve) {
            manager.getFactory().showError(PropertyEditorPane.this, "Error changing value " + pvve.getProperty() + " to " + pvve.getRejectedValue() + ":  " + pvve.getReason());
          }
        }
      }, false));

    buttonPanel.add(createButton("Cancel", new AbstractAction() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          if (container instanceof JInternalFrame) {
            try {
              ((JInternalFrame)container).setClosed(true);
            } catch (java.beans.PropertyVetoException pve) {
            }
          } else if (container instanceof JFrame) {
            ((JFrame)container).dispose();
          } else if (container instanceof JDialog) {
            ((JDialog)container).dispose();
          }
        }
      }, false));

    return buttonPanel;
  }

  /**
   * Creates the appropriate Button.
   */
  private JButton createButton(String label, Action e, boolean isDefault) {
    JButton thisButton;

    thisButton = new JButton(manager.getProperty("label." + label, label));
    String mnemonic = manager.getProperty("label." + label + ".mnemonic", "");
    if (mnemonic.length() > 0) {
      thisButton.setMnemonic(mnemonic.charAt(0));
    }

    thisButton.setSelected(isDefault);

    thisButton.addActionListener(e);

    return thisButton;
  }

  /**
   * Returns the Container for this PropertyEditorPane.
   */
  public Container getContainer() {
    return container;
  }

}
