package net.suberic.pooka.conf.vb;

import net.suberic.util.*;

/**
 * A VariableBundle-based Configuration.
 */
public class VBConfiguration {

  VariableBundle variableBundle;
  public VariableBundle getVariableBundle() {
    return variableBundle;
  }
  public void setVariableBundle(VariableBundle variableBundle) {
    this.variableBundle = variableBundle;
  }

  /**
   * Gets a String value.
   */
  public String getString(String property, String defaultValue) {
    return getVariableBundle().getProperty(property, defaultValue);
  }
  /**
   * Sets a String.
   */
  public void setString(String property, String value) {
    getVariableBundle().setProperty(property, value);
  }


  /**
   * Gets a boolean value.
   */
  public boolean getBoolean(String property, boolean defaultValue) {
    return getVariableBundle().getProperty(property, defaultValue ? "true" : "false").equalsIgnoreCase("true");
  }
  /**
   * Sets a boolean.
   */
  public void setBoolean(String property, boolean value) {
    getVariableBundle().setProperty(property, value ? "true" : "false");
  }

  /**
   * Gets a int value.
   */
  public int getInt(String property, int defaultValue) {
    int returnValue = defaultValue;
    String value = getString(property, "");
    if (! "".equals(value)) {
      try {
        returnValue = Integer.parseInt(value);
      } catch (Exception e) {
      }
    }
    return returnValue;
  }
  /**
   * Sets a int.
   */
  public void setInt(String property, int value) {
    setString(property, Integer.toString(value));
  }

}
