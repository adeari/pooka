package net.suberic.util.gui.propedit;
import java.util.*;

/**
 * A PropertyEditorListener which translates one value to another.  Useful
 * for version migrations
 */
public class PropertyTranslatorFilter extends PropertyEditorAdapter implements ConfigurablePropertyEditorListener {

  Map<String,String> translator = new HashMap<String,String>();

  /**
   * Configures this filter from the given key.
   */
  public void configureListener(String key, String property, String propertyBase, String editorTemplate, PropertyEditorManager manager) {
    List<String> translatorKeys = manager.getPropertyAsList(key + ".map", "");
    for (String translatorKey: translatorKeys) {
      String[] pair = translatorKey.split("=");
      if (pair != null && pair.length == 2) {
        translator.put(pair[0], pair[1]);
      }
    }
  }

  /**
   * On initialization, if the property is set to one of the values to
   * be translated, we set the new value to the translated value.
   */
  public void propertyInitialized(PropertyEditorUI source, String property, String newValue) {
    String translatedValue = translator.get(newValue);
    if (translatedValue != null) {
      source.setOriginalValue(translatedValue);
    }
  }

}
