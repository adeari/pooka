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
    private ResourceBundle resources;
    private VariableBundle parentProperties;
    private Vector removeList = new Vector();
    private Hashtable VCListeners = new Hashtable();
    
    public VariableBundle(InputStream propertiesFile, String resourceFile, VariableBundle newParentProperties) {
	
	writableProperties = new Properties();

	if (resourceFile != null) 
	    try {
		resources = ResourceBundle.getBundle(resourceFile, Locale.getDefault());
	    } catch (MissingResourceException mre) {
		System.err.println(mre.getClassName() + mre.getKey());
		System.exit(1);
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
    
    public VariableBundle(InputStream propertiesFile, String resourceFile) {
	this(propertiesFile, resourceFile, null);
    }

    public VariableBundle(InputStream propertiesFile, VariableBundle newParentProperties) {
	this(propertiesFile, null, newParentProperties);
    }

    public String getProperty(String key, String defaultValue) {
	String returnValue;

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

    public void setProperty(String propertyName, String propertyValue) {
	writableProperties.setProperty(propertyName, propertyValue);
	unRemoveProperty(propertyName);
	fireValueChanged(propertyName);
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
     * Returns a property which has multiple values separated by a ':' (colon)
     * as a java.util.Vector.
     */

    public Vector getPropertyAsVector(String propertyName, String defaultValue) {
	Vector returnValue = new Vector();
	StringTokenizer tokens = new StringTokenizer(getProperty(propertyName, defaultValue), ":");
	while (tokens.hasMoreElements())
	    returnValue.add(tokens.nextElement());
	return returnValue;
	    
    }


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
			key = currentLine.substring(0, equalsLoc);
			if (!propertyIsRemoved(key)) 
			    if (writableProperties.getProperty(key, "").equals("")) {
				writeSaveFile.write(currentLine);
			    } else {
				writeSaveFile.write(key + "=" + writableProperties.getProperty(key, ""));
				properties.setProperty(key, writableProperties.getProperty(key, ""));
				writableProperties.remove(key);
			    }
			
		    } else 
			writeSaveFile.write(currentLine);
		    writeSaveFile.newLine();
		    currentLine = readSaveFile.readLine();
		}

		// write out the rest of the writableProperties

		Enumeration propsLeft = writableProperties.keys();
		while (propsLeft.hasMoreElements()) {
		    String nextKey = (String)propsLeft.nextElement();
		    writeSaveFile.write(nextKey + "=" + writableProperties.getProperty(nextKey, ""));
		    writeSaveFile.newLine();
		    properties.setProperty(nextKey, writableProperties.getProperty(nextKey, ""));
		    writableProperties.remove(nextKey);
		}

		readSaveFile.close();
		writeSaveFile.flush();
		writeSaveFile.close();
		String fileName = new String(saveFile.getAbsolutePath());
		saveFile.renameTo(new File(saveFile.getAbsolutePath() + ".old"));
		outputFile.renameTo(new File(fileName));

		
	    } catch (Exception e) {
		System.out.println(getProperty("VariableBundle.saveError", "Error saving properties file: " + saveFile.getName() + ": " + e.getMessage()));
		e.printStackTrace(System.err);
	    }

	}
    }

    public void removeProperty(String remProp) {
	if (! propertyIsRemoved(remProp))
	    removeList.add(remProp);
    }

    public void unRemoveProperty(String unRemProp) {
	for (int i = removeList.size() -1 ; i >= 0; i--) {
	    if (((String)removeList.elementAt(i)).equals(unRemProp))
		removeList.removeElementAt(i);
	}
    }

    public boolean propertyIsRemoved(String prop) {
	if (removeList.size() < 1)
	    return false;
	
	for (int i = 0; i < removeList.size(); i++) {
	    if (((String)removeList.elementAt(i)).equals(prop))
		return true;
	}

	return false;
    }

    public void fireValueChanged(String changedValue) {
	Vector listeners = (Vector)VCListeners.get(changedValue);
	if (listeners != null && listeners.size() > 0) {
	    for (int i=0; i < listeners.size(); i++)
		((ValueChangeListener)listeners.elementAt(i)).valueChanged(changedValue);
	}	    
    }

    public void addValueChangeListener(ValueChangeListener vcl, String property) {
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
		
    public void removeValueChangeListener(ValueChangeListener vcl) {
	Enumeration keys = VCListeners.keys();
	Vector currentListenerList;
	while (keys.hasMoreElements()) {
	    currentListenerList = (Vector)keys.nextElement();
	    while (currentListenerList.contains(vcl))
		currentListenerList.remove(vcl);
	}
    }
	
}


