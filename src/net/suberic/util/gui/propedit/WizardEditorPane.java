package net.suberic.util.gui.propedit;
import javax.swing.*;

/**
 * A SwingEditorPane that implements Wizard functionality.
 */
public class WizardEditorPane extends CompositeSwingPropertyEditor {

  String mState = null;

  /**
   * Configures the editor.
   */
  public void configureEditor(String propertyName, String template, String propertyBaseName, PropertyEditorManager newManager, boolean isEnabled) {
    configureBasic(propertyName, template, propertyBaseName, newManager, isEnabled);

  }

  /**
   * Returns the current Wizard state.
   */
  public String getState() {
    return mState;
  }

  /**
   * Returns if this is the beginning state.
   */
  public boolean inBeginningState() {
    return true;
  }

  /**
   * Returns if this is in a valid end state.
   */
  public boolean inEndState() {
    return true;
  }
}
