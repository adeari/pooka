package net.suberic.util.gui.propedit;

public class PropertyValueVetoException extends Exception {
  
  String rejectedValue;
  String reason;
  PropertyEditorListener listener;

  /**
   * Creates a new PropertyValueVetoException.
   */
  public PropertyValueVetoException(String pRejectedValue, String pReason, PropertyEditorListener pListener) {
    rejectedValue = pRejectedValue;
    reason = pReason;
    listener = pListener;
  }

  /**
   * Returns the rejected value.
   */
  public String getRejectedValue() {
    return rejectedValue;
  }

  /**
   * Returns the reason for rejection.
   */
  public String getReason() {
    return reason;
  }

  /**
   * Returns the listener that rejected the change.
   */
  public PropertyEditorListener getListener() {
    return listener;
  }
}
