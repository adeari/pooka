package net.suberic.util.gui.propedit;

/**
 * This listens to a PropertyEditorUI and reacts to changes.
 */
public class PropertyEditorAdapter implements PropertyEditorListener {

  /**
   * Called when a property is about to change.  If the value is not ok
   * with the listener, a PropertyValueVetoException should be thrown.
   */
  public void propertyChanging(String newValue) throws PropertyValueVetoException {

  }

  /**
   * Called after a property changes.
   */
  public void propertyChanged(String newValue) {

  }
}
