package net.suberic.util.swing;

/**
 * A UI which can use a Updatable
 */

public interface UpdatableUI {

  /**
   * Gets the UIConfig object from the UpdatableUIManager which is appropriate
   * for this UI.
   */
  public UIConfig getUIConfig(UpdatableUIManager uuim);

}
