package net.suberic.util.gui.propedit;

public class PropertyValueVetoException extends Exception {
  
  String propertyName;
  String rejectedValue;
  String reason;
  PropertyEditorListener listener;

  /**
   * Creates a new PropertyValueVetoException.
   */
  public PropertyValueVetoException(String pProperty, String pRejectedValue, String pReason, PropertyEditorListener pListener) {
    propertyName = pProperty;
    rejectedValue = pRejectedValue;
    reason = pReason;
    listener = pListener;
  }

  /**
   * Returns the property being changed.
   */
  public String getProperty() {
    return propertyName;
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
