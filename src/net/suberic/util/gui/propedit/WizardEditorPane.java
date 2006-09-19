package net.suberic.util.gui.propedit;
import java.awt.CardLayout;
import java.util.*;
import javax.swing.*;

/**
 * A SwingEditorPane that implements Wizard functionality.
 */
public class WizardEditorPane extends CompositeSwingPropertyEditor {

  String mState = null;
  List<String> mStateList = null;
  Map<String, SwingPropertyEditor> layoutMap = new HashMap<String, SwingPropertyEditor>();
  CardLayout layout;
  WizardPropertyEditor wizardContainer = null;

  /**
   * Configures the editor.
   */
  public void configureEditor(String propertyName, String template, String propertyBaseName, PropertyEditorManager newManager, boolean isEnabled) {
    configureBasic(propertyName, template, propertyBaseName, newManager, isEnabled);

    layout = new CardLayout();
    this.setLayout(layout);

    mStateList = newManager.getPropertyAsList(template + "._states", "");
    if (mStateList.size() > 0) {
      mState = mStateList.get(0);
    }

    for (String stateString: mStateList) {
      String subProperty = createSubProperty(manager.getProperty(template + "._states." + stateString + ".editor", ""));
      String subTemplate = createSubTemplate(manager.getProperty(template + "._states." + stateString + ".editor", ""));
      SwingPropertyEditor newEditor = (SwingPropertyEditor) manager.getFactory().createEditor(subProperty, subTemplate, subTemplate, manager, true);
      layoutMap.put(stateString, newEditor);
      this.add(stateString, newEditor);
    }

    loadState(mState);

  }

  /**
   * Loads the current state.
   */
  public void loadState(String state) {
    mState = state;
    layout.show(this, mState);
    if (getWizardContainer() != null) {
      getWizardContainer().setBeginningState(inBeginningState());
      getWizardContainer().setEndState(inEndState());
    }
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
    if (mState == mStateList.get(0))
      return true;
    else
      return false;
  }

  /**
   * Returns if this is in a valid end state.
   */
  public boolean inEndState() {
    if (mState == mStateList.get(mStateList.size() - 1))
      return true;
    else
      return false;
  }

  /**
   * Goes back a state.
   */
  public void back() {
    if (inBeginningState())
      return;
    else {
      int current = mStateList.indexOf(mState);
      if (current >= 1) {
        String newState = mStateList.get(current - 1);
        loadState(newState);
      }
    }

  }

  /**
   * Goes forward a state.
   */
  public void next() throws PropertyValueVetoException {
    if (inEndState())
      return;
    else {
      int current = mStateList.indexOf(mState);
      if (current > -1 && current < (mStateList.size() -1)) {
        SwingPropertyEditor editor = layoutMap.get(mState);
        editor.setValue();
        String newState = mStateList.get(current + 1);
        loadState(newState);
      }
    }
  }

  /**
   * Sets the WizardPropertyEditor container.
   */
  public void setWizardContainer(WizardPropertyEditor wpe) {
    wizardContainer = wpe;
  }

  /**
   * Gets the WizardPropertyEditor container.
   */
  public WizardPropertyEditor getWizardContainer() {
    return wizardContainer;
  }
}
