package net.suberic.util;
import java.util.*;
import java.io.*;

/**
 * VariableBundle is a combination of a Properties object, a ResourceBundle
 * object, and (optionally) a second Properties object to act as the 'parent'
 * properties.  This allows both for a single point of reference for 
 * variables, as well as the ability to do hierarchical lookups with the
 * parent (see getProperty() for an example).
 *
 * The order of lookup is as follows:  Local properties are checked first,
 * then parent properties, and then finally (if the value is not found in 
 * any properties) the ResourceBundle is checked.
 */

public class VariableBundle extends Object {
  private Properties properties;
  private Properties writableProperties;
  private Properties temporaryProperties = new Properties();
  private ResourceBundle resources;
  private VariableBundle parentProperties;
  private File saveFile;
  private Vector removeList = new Vector();
  private Hashtable VCListeners = new Hashtable();
  private Hashtable VCGlobListeners = new Hashtable();
    
    public VariableBundle(InputStream propertiesFile, String resourceFile, VariableBundle newParentProperties) {
	
	writableProperties = new Properties();

	if (resourceFile != null) 
	    try {
		resources = ResourceBundle.getBundle(resourceFile, Locale.getDefault());
	    } catch (MissingResourceException mre) {
		System.err.println("Error loading resource " + mre.getClassName() + mre.getKey() + ":  trying default locale.");
		try {
		    resources = ResourceBundle.getBundle(resourceFile, Locale.US);
		} catch (MissingResourceException mreTwo){ 
		    System.err.println("Unable to load default (US) resource bundle; exiting.");
		    System.exit(1);
		}
	    }
	else
	    resources=null;
	
	properties = new Properties();

	if (propertiesFile != null) 
	    try {
		properties.load(propertiesFile);
	    } catch (java.io.IOException ioe) {
		System.err.println(ioe.getMessage() + ":  " + propertiesFile);
	    }
	
	parentProperties = newParentProperties;

    }

  public VariableBundle(File propertiesFile, VariableBundle newParentProperties) throws java.io.FileNotFoundException {
    this(new FileInputStream(propertiesFile), newParentProperties);
    saveFile = propertiesFile;
    
  }
    
    public VariableBundle(InputStream propertiesFile, String resourceFile) {
	this(propertiesFile, resourceFile, null);
    }

    public VariableBundle(InputStream propertiesFile, VariableBundle newParentProperties) {
	this(propertiesFile, null, newParentProperties);
    }

  public VariableBundle(Properties editableProperties, VariableBundle newParentProperties) {
    writableProperties = editableProperties;
    parentProperties = newParentProperties;
    properties = new Properties();
    resources = null;
  }
  public String getProperty(String key, String defaultValue) {
    String returnValue;
    
    returnValue = temporaryProperties.getProperty(key, "");
    if (returnValue == "") {
      returnValue = writableProperties.getProperty(key, "");
      if (returnValue == "") {
	returnValue = properties.getProperty(key, "");
	if (returnValue == "") {
	  returnValue=getParentProperty(key, "");
	  if (returnValue == "") {
	    if (resources != null)
	      try {
		returnValue = resources.getString(key);
	      } catch (MissingResourceException mre) {
		returnValue=defaultValue;
	      } 
	    else
	      returnValue = defaultValue;
	  }
	}
      }
    }
    return returnValue;
  }

  public String getProperty(String key) throws MissingResourceException {
    String returnValue;
    
    returnValue = getProperty(key, "");
    if (returnValue == "") {
      throw new MissingResourceException(key, "", key);
    }
    return returnValue;
  }
  
  
  
  private String getParentProperty(String key, String defaultValue) {
    if (parentProperties==null) {
      return defaultValue;
    } else {
      return parentProperties.getProperty(key, defaultValue);
    }
  }
  
  
  public ResourceBundle getResources() {
    return resources;
  }
  
  public void setResourceBundle (ResourceBundle newResources) {
    resources = newResources;
  }
  
  public Properties getProperties() {
    return properties;
  }
  
  public void setProperties(Properties newProperties) {
    properties = newProperties;
  }

  public VariableBundle getParentProperties() {
    return parentProperties;
  }
  
  public Properties getWritableProperties() {
    return writableProperties;
  }
  
  public void setProperty(String propertyName, String propertyValue) {
    temporaryProperties.remove(propertyName);
    writableProperties.setProperty(propertyName, propertyValue);
    if (propertyValue == null || propertyValue.equalsIgnoreCase("")) {
      removeProperty(propertyName);
    } else {
      unRemoveProperty(propertyName);
    }
    fireValueChanged(propertyName);
  }
  
  /**
   * sets a property as temporary (so it won't be saved).
   */
  public void setProperty(String propertyName, String propertyValue, boolean temporary) {
    if (temporary) {
      temporaryProperties.setProperty(propertyName, propertyValue);
      fireValueChanged(propertyName);
    } else {
      setProperty(propertyName, propertyValue);
    }
  }
  
  /**
   * Returns a property which has multiple values separated by a ':' (colon)
   * as a java.util.Enumeration.
   */
  
  public Enumeration getPropertyAsEnumeration(String propertyName, String defaultValue) {
    StringTokenizer tokens = new StringTokenizer(getProperty(propertyName, defaultValue), ":");
    return tokens;
  }
  
  /**
   * Converts a value which has multiple values separated by a ':' (colon)
   * to a java.util.Vector.
   */
  public static Vector convertToVector(String value) {
    Vector returnValue = new Vector();
    StringTokenizer tokens = new StringTokenizer(value, ":");
    while (tokens.hasMoreElements())
      returnValue.add(tokens.nextElement());
    return returnValue;
  }
  
  /**
   * Converts the given property value to a Vector using the convertToVector
   * call.
   */
  public Vector getPropertyAsVector(String propertyName, String defaultValue) {
    return convertToVector(getProperty(propertyName, defaultValue));
  }
  
  
  /**
   * Saves the current properties in the VariableBundle to a file.  Note
   * that this only saves the writableProperties of this particular
   * VariableBundle--underlying defaults are not written.
   */
  public void saveProperties() {
    if (saveFile != null) {
      saveProperties(saveFile);
    }
  }
  
  /**
   * Saves the current properties in the VariableBundle to a file.  Note
   * that this only saves the writableProperties of this particular
   * VariableBundle--underlying defaults are not written.
   */
  public void saveProperties(File saveFile) {
    if (writableProperties.size() > 0) { 
      File outputFile;
      String currentLine, key;
      int equalsLoc;
      
      try {
	if (!saveFile.exists())
	  saveFile.createNewFile();
	
	outputFile  = saveFile.createTempFile(saveFile.getName(), ".tmp", saveFile.getParentFile());
	
	BufferedReader readSaveFile = new BufferedReader(new FileReader(saveFile));
	BufferedWriter writeSaveFile = new BufferedWriter(new FileWriter(outputFile));
	currentLine = readSaveFile.readLine();
	while (currentLine != null) {
	  equalsLoc = currentLine.indexOf('=');
	  if (equalsLoc != -1) {
	    String rawKey = currentLine.substring(0, equalsLoc);
	    key = unEscapeString(rawKey);
	    
	    if (!propertyIsRemoved(key)) {
	      if (writableProperties.getProperty(key, "").equals("")) {
		
		writeSaveFile.write(currentLine);
		writeSaveFile.newLine();
		
	      } else {
		writeSaveFile.write(rawKey + "=" + escapeWhiteSpace(writableProperties.getProperty(key, "")));
		writeSaveFile.newLine();
		properties.setProperty(key, writableProperties.getProperty(key, ""));
		writableProperties.remove(key);
	      }
	      removeProperty(key);
	    }
	    
	  } else {
	    writeSaveFile.write(currentLine);
	    writeSaveFile.newLine();
	  }
	  currentLine = readSaveFile.readLine();
	}
	
	// write out the rest of the writableProperties
	
	Enumeration propsLeft = writableProperties.keys();
	while (propsLeft.hasMoreElements()) {
	  String nextKey = (String)propsLeft.nextElement();
	  String nextKeyEscaped = escapeWhiteSpace(nextKey);
	  String nextValueEscaped = escapeWhiteSpace(writableProperties.getProperty(nextKey, ""));
	  writeSaveFile.write(nextKeyEscaped + "=" + nextValueEscaped);
	  writeSaveFile.newLine();
	  
	  properties.setProperty(nextKey, writableProperties.getProperty(nextKey, ""));
	  writableProperties.remove(nextKey);
	}
	
	clearRemoveList();
	
	readSaveFile.close();
	writeSaveFile.flush();
	writeSaveFile.close();
	
	// if you don't delete the .old file first, then the
	// rename fails under Windows.
	String oldSaveName = saveFile.getAbsolutePath() + ".old";
	File oldSave = new File (oldSaveName);
	if (oldSave.exists())
	  oldSave.delete();
	
	String fileName = new String(saveFile.getAbsolutePath());
	saveFile.renameTo(oldSave);
	outputFile.renameTo(new File(fileName));
	
      } catch (Exception e) {
	System.out.println(getProperty("VariableBundle.saveError", "Error saving properties file: " + saveFile.getName() + ": " + e.getMessage()));
	e.printStackTrace(System.err);
      }
      
    }
  }
  
  /*
   * Converts encoded &#92;uxxxx to unicode chars
   * and changes special saved chars to their original forms
   *
   * ripped directly from java.util.Properties; hope they don't mind.
   */
  private String loadConvert (String theString) {
    char aChar;
    int len = theString.length();
    StringBuffer outBuffer = new StringBuffer(len);
    
    for(int x=0; x<len; ) {
      aChar = theString.charAt(x++);
      if (aChar == '\\') {
	aChar = theString.charAt(x++);
	if(aChar == 'u') {
	  // Read the xxxx
	  int value=0;
	  for (int i=0; i<4; i++) {
	    aChar = theString.charAt(x++);
	    switch (aChar) {
	    case '0': case '1': case '2': case '3': case '4':
	    case '5': case '6': case '7': case '8': case '9':
	      value = (value << 4) + aChar - '0';
	      break;
	    case 'a': case 'b': case 'c':
	    case 'd': case 'e': case 'f':
	      value = (value << 4) + 10 + aChar - 'a';
	      break;
	    case 'A': case 'B': case 'C':
	    case 'D': case 'E': case 'F':
	      value = (value << 4) + 10 + aChar - 'A';
	      break;
	    default:
	      throw new IllegalArgumentException(
						 "Malformed \\uxxxx encoding.");
	    }
	  }
	  outBuffer.append((char)value);
	} else {
	  if (aChar == 't') aChar = '\t';
	  else if (aChar == 'r') aChar = '\r';
	  else if (aChar == 'n') aChar = '\n';
	  else if (aChar == 'f') aChar = '\f';
	  outBuffer.append(aChar);
	}
      } else
	outBuffer.append(aChar);
    }
    return outBuffer.toString();
  }

    /*
     * Converts unicodes to encoded &#92;uxxxx
     * and writes out any of the characters in specialSaveChars
     * with a preceding slash
     *
     * ripped directly from java.util.Properties; hope they don't mind.
     */
    private String saveConvert(String theString, boolean escapeSpace) {
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len*2);

        for(int x=0; x<len; x++) {
            char aChar = theString.charAt(x);
            switch(aChar) {
		case ' ':
		    if (x == 0 || escapeSpace) 
			outBuffer.append('\\');

		    outBuffer.append(' ');
		    break;
                case '\\':outBuffer.append('\\'); outBuffer.append('\\');
                          break;
                case '\t':outBuffer.append('\\'); outBuffer.append('t');
                          break;
                case '\n':outBuffer.append('\\'); outBuffer.append('n');
                          break;
                case '\r':outBuffer.append('\\'); outBuffer.append('r');
                          break;
                case '\f':outBuffer.append('\\'); outBuffer.append('f');
                          break;
                default:
                    if ((aChar < 0x0020) || (aChar > 0x007e)) {
                        outBuffer.append('\\');
                        outBuffer.append('u');
                        outBuffer.append(toHex((aChar >> 12) & 0xF));
                        outBuffer.append(toHex((aChar >>  8) & 0xF));
                        outBuffer.append(toHex((aChar >>  4) & 0xF));
                        outBuffer.append(toHex( aChar        & 0xF));
                    } else {
                        if (specialSaveChars.indexOf(aChar) != -1)
                            outBuffer.append('\\');
                        outBuffer.append(aChar);
                    }
            }
        }
        return outBuffer.toString();
    }

    /**
     * Escapes whitespace in a string by putting a '\' in front of each
     * whitespace character.
     */
    public String escapeWhiteSpace(String sourceString) {
      /*
	char[] origString = sourceString.toCharArray();
	StringBuffer returnString = new StringBuffer();
	for (int i = 0; i < origString.length; i++) {
	    char currentChar = origString[i];
	    if (Character.isWhitespace(currentChar) || '\\' == currentChar)
	      returnString.append('\\');
	      
	    returnString.append(currentChar);
	}

	return returnString.toString();
      */
      return saveConvert(sourceString, true);
    }

  /**
   * resolves a whitespace-escaped string.
   */
  public String unEscapeString(String sourceString) {
    return loadConvert(sourceString);
  }

    /**
     * Clears the removeList.  This should generally be called after
     * you do a writeProperties();
     */
    public void clearRemoveList() {
	removeList.clear();
    }

    /**
     * This removes the property from the currently VariableBundle.  This
     * is different than setting the value to "" (or null) in that, if the
     * property is removed, it is removed from the source property file.
     */
    public void removeProperty(String remProp) {
	if (! propertyIsRemoved(remProp))
	    removeList.add(remProp);
    }

    /**
     * Removes a property from the removeList.  Only necessary if a property
     * had been removed since the last save, and now has been set to a new
     * value.  It's probably a good idea, though, to call this method any
     * time a property has its value set.
     */
    public void unRemoveProperty(String unRemProp) {
	for (int i = removeList.size() -1 ; i >= 0; i--) {
	    if (((String)removeList.elementAt(i)).equals(unRemProp))
		removeList.removeElementAt(i);
	}
    }

    /**
     * Returns true if the property is in the removeList for this
     * VariableBundle.
     */
    public boolean propertyIsRemoved(String prop) {
	if (removeList.size() < 1)
	    return false;
	
	for (int i = 0; i < removeList.size(); i++) {
	    if (((String)removeList.elementAt(i)).equals(prop))
		return true;
	}

	return false;
    }

    /**
     * This notifies all registered listeners for changedValue that its
     * value has changed.
     */
    public void fireValueChanged(String changedValue) {
      // only notify each listener once.
      Set notified = new HashSet();

      Vector listeners = (Vector)VCListeners.get(changedValue);
      if (listeners != null && listeners.size() > 0) {
	for (int i=0; i < listeners.size(); i++) {
	  ((ValueChangeListener)listeners.elementAt(i)).valueChanged(changedValue);
	  notified.add(listeners.elementAt(i));
	}
      }	    

      // now add the glob listeners.

      Enumeration keys = VCGlobListeners.keys();
      while (keys.hasMoreElements()) {
	String currentPattern = (String) keys.nextElement();
	if (changedValue.startsWith(currentPattern)) {
	  Vector globListeners = (Vector) VCGlobListeners.get(currentPattern);
	  if (globListeners != null && globListeners.size() > 0) {
	    for (int i = 0; i < globListeners.size(); i++) {
	      ValueChangeListener currentListener = ((ValueChangeListener)globListeners.elementAt(i));
	      if (!notified.contains(currentListener)) {
		currentListener.valueChanged(changedValue);
		notified.add(currentListener);
	      }
	    }
	  }
	}
      }

    }

    /**
     * This adds the ValueChangeListener to listen for changes in the 
     * given property.
     */
    public void addValueChangeListener(ValueChangeListener vcl, String property) {
      if (property.endsWith("*")) {
	String startProperty = property.substring(0, property.length() - 1);
	Vector listeners = (Vector)VCGlobListeners.get(startProperty);
	if (listeners == null) {
	  listeners = new Vector();
	  listeners.add(vcl);
	  VCGlobListeners.put(startProperty, listeners);
	} else {
	  if (!listeners.contains(vcl)) 
	    listeners.add(vcl);
	}

      } else {
	Vector listeners = (Vector)VCListeners.get(property);
	if (listeners == null) {
	  listeners = new Vector();
	  listeners.add(vcl);
	  VCListeners.put(property, listeners);
	} else {
	  if (!listeners.contains(vcl)) 
	    listeners.add(vcl);
	}
      }
    }
		
    /**
     * This removes the given ValueChangeListener for all the values that
     * it's listening to.
     */
    public void removeValueChangeListener(ValueChangeListener vcl) {
      Enumeration keys = VCListeners.keys();
      Vector currentListenerList;
      while (keys.hasMoreElements()) {
	currentListenerList = (Vector)VCListeners.get(keys.nextElement());
	while (currentListenerList.contains(vcl))
	  currentListenerList.remove(vcl);
      }

      keys = VCGlobListeners.keys();
      while (keys.hasMoreElements()) {
	currentListenerList = (Vector)VCGlobListeners.get(keys.nextElement());
	while (currentListenerList.contains(vcl))
	  currentListenerList.remove(vcl);
      }
    }

  /**
   * Convert a nibble to a hex character
   * @param	nibble	the nibble to convert.
   */
  private static char toHex(int nibble) {
    return hexDigit[(nibble & 0xF)];
  }
  
  /** A table of hex digits */
  private static final char[] hexDigit = {
    '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
  };

  private static final String keyValueSeparators = "=: \t\r\n\f";
  
  private static final String strictKeyValueSeparators = "=:";

  private static final String specialSaveChars = "=: \t\r\n\f#!";
  
  private static final String whiteSpaceChars = " \t\r\n\f";
}


