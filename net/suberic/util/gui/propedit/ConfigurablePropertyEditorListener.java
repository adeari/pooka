package net.suberic.util.gui.propedit;

/**
 * A PropertyEditorListener than can be configured from a property.
 */
public abstract class ConfigurablePropertyEditorListener extends PropertyEditorAdapter {

  public abstract void configureListener(String key, PropertyEditorManager pem);

}
