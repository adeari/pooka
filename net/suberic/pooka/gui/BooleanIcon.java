package net.suberic.pooka.gui;
import javax.swing.*;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.awt.Component;

public class BooleanIcon {
    public boolean bool;
    public String iconFile;
    private static HashMap labelTable = new HashMap();

    public BooleanIcon(boolean boolValue, String newIconFile) {
	bool=boolValue;
	iconFile = newIconFile;
    }

    public Component getIcon() {
	if (labelTable.containsKey(iconFile))
	    return (Component)labelTable.get(iconFile);
	
	Component returnValue = null;

	try {
	    java.net.URL url = this.getClass().getResource(iconFile);
	    if (url != null) {
		returnValue = new JLabel(new ImageIcon(url));
		((JLabel)returnValue).setOpaque(true);
		
	    } else
		returnValue = null;
	} catch (MissingResourceException mre) {
	    returnValue = null;
	}
	
	labelTable.put(iconFile, returnValue);
	return returnValue;
    }

    public String toString() {
	return "";
    }
}
