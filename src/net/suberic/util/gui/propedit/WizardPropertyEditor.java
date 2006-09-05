package net.suberic.util.gui.propedit;
import java.awt.Component;
import java.awt.Container;
import javax.swing.*;


/**
 * A top-level editor for wizard properties.  Instead of having a single
 * panel with properties and a set of 'help', 'apply', 'ok', and 'cancel'
 * buttons, this has a series of panels with 'help', 'canel', and 'next'
 * buttons.  You must go through the workflow for the Wizard before
 * you reach an 'ok' stage.
 */
public class WizardPropertyEditor extends PropertyEditorPane {
  WizardEditorPane wizard = null;

  /**
   * This contructor creates a PropertyEditor using the given
   * SwingPropertyEditor.
   */
  public WizardPropertyEditor(PropertyEditorManager newManager,
                            SwingPropertyEditor newEditor,
                            Container newContainer,
                            boolean newCommit) {
    manager = newManager;
    container = newContainer;
    editor = newEditor;
    wizard = (WizardEditorPane) newEditor;
    doCommit = newCommit;

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
      editorPanelLayout.putConstraint(SpringLayout.SOUTH, editorPanel, 5 ,SpringLayout.SOUTH, lvEditor.getLabelComponent());
      //editorPanelLayout.putConstraint(SpringLayout.SOUTH, lvEditor.getLabelComponent(), -5, SpringLayout.SOUTH, editorPanel);

      editorPanelLayout.putConstraint(SpringLayout.WEST, lvEditor.getValueComponent(), 5 ,SpringLayout.EAST, lvEditor.getLabelComponent());

      editorPanelLayout.putConstraint(SpringLayout.NORTH, lvEditor.getValueComponent(), 5 ,SpringLayout.NORTH, editorPanel);
      //editorPanelLayout.putConstraint(SpringLayout.SOUTH, editorPanel, 5 ,SpringLayout.SOUTH, lvEditor.getValueComponent());
      editorPanelLayout.putConstraint(SpringLayout.EAST, editorPanel, 5 ,SpringLayout.EAST, lvEditor.getValueComponent());

      editorComponent = editorPanel;
    }

    JPanel buttonPanel = createButtonPanel();

    //pepLayout(editorComponent, buttonPanel);

  }

  /**
   * Creates the appropriate buttons (Ok, Accept, Cancel) to this component.
   */
  public JPanel createButtonPanel() {
    JPanel buttonPanel = new JPanel();
    /*
    SpringLayout buttonLayout = new SpringLayout();
    buttonPanel.setLayout(buttonLayout);

    JButton helpButton = createButton("Help", new AbstractAction() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          System.err.println("showing help for " + editor.getHelpID());
          //manager.getFactory().getHelpBroker().showID(editor.getHelpID(), null, null);
          //new CSH.DisplayHelpFromSource(manager.getFactory().getHelpBroker()).actionPerformed(e);
          manager.getFactory().getHelpBroker().setCurrentID(editor.getHelpID());
          manager.getFactory().getHelpBroker().setDisplayed(true);

        }
      }, true);

    //CSH.setHelpIDString(helpButton, "UserProfile");
    buttonPanel.add(helpButton);

    JButton okButton = createButton("Ok", new AbstractAction() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          try {
            setValue();
            if (doCommit) {
              manager.commit();
            }
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
            manager.getFactory().showError(WizardPropertyEditor.this, pvve.getMessage());
          }
        }
      }, true);

    JButton applyButton = createButton("Apply", new AbstractAction() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          try {
            setValue();
            if (doCommit) {
              manager.commit();
            }
          } catch (PropertyValueVetoException pvve) {
            //manager.getFactory().showError(WizardPropertyEditor.this, "Error changing value " + pvve.getProperty() + " to " + pvve.getRejectedValue() + ":  " + pvve.getReason());
            manager.getFactory().showError(WizardPropertyEditor.this, pvve.getMessage());
          }
        }
      }, false);

    JButton cancelButton = createButton("Cancel", new AbstractAction() {
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
      }, false);

    buttonPanel.add(helpButton);
    buttonPanel.add(cancelButton);
    buttonPanel.add(applyButton);
    buttonPanel.add(okButton);

    Spring buttonWidth = Spring.constant(0);
    buttonWidth = Spring.max(buttonWidth, buttonLayout.getConstraints(helpButton).getWidth());
    buttonWidth = Spring.max(buttonWidth, buttonLayout.getConstraints(cancelButton).getWidth());
    buttonWidth = Spring.max(buttonWidth, buttonLayout.getConstraints(applyButton).getWidth());
    buttonWidth = Spring.max(buttonWidth, buttonLayout.getConstraints(okButton).getWidth());

    buttonLayout.getConstraints(helpButton).setWidth(buttonWidth);
    buttonLayout.getConstraints(cancelButton).setWidth(buttonWidth);
    buttonLayout.getConstraints(applyButton).setWidth(buttonWidth);
    buttonLayout.getConstraints(okButton).setWidth(buttonWidth);

    buttonLayout.putConstraint(SpringLayout.WEST, helpButton, 5, SpringLayout.WEST, buttonPanel);
    buttonLayout.putConstraint(SpringLayout.NORTH, helpButton, 5, SpringLayout.NORTH, buttonPanel);
    buttonLayout.putConstraint(SpringLayout.SOUTH, buttonPanel, 5, SpringLayout.SOUTH, helpButton);

    buttonLayout.putConstraint(SpringLayout.WEST, cancelButton, Spring.constant(5, 5, 32000), SpringLayout.EAST, helpButton);
    buttonLayout.putConstraint(SpringLayout.NORTH, cancelButton, 5, SpringLayout.NORTH, buttonPanel);

    buttonLayout.putConstraint(SpringLayout.WEST, applyButton, 5, SpringLayout.EAST, cancelButton);
    buttonLayout.putConstraint(SpringLayout.NORTH, applyButton, 5, SpringLayout.NORTH, buttonPanel);

    buttonLayout.putConstraint(SpringLayout.WEST, okButton, 5, SpringLayout.EAST, applyButton);
    buttonLayout.putConstraint(SpringLayout.NORTH, okButton, 5, SpringLayout.NORTH, buttonPanel);
    buttonLayout.putConstraint(SpringLayout.EAST, buttonPanel, 5, SpringLayout.EAST, okButton);

    */
    return buttonPanel;
  }

}
