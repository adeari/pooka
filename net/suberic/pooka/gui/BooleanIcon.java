package net.suberic.pooka.gui;
import javax.swing.*;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.awt.Component;

public class BooleanIcon implements TableCellIcon {
    public boolean bool;
    public String iconFile;
    private static HashMap labelTable = new HashMap();
    private static Component blankImage = new JLabel();

    public BooleanIcon(boolean boolValue, String newIconFile) {
	bool=boolValue;
	iconFile = newIconFile;
    }

    /**
     * This returns a JLabel.  If the value of this BooleanIcon is true,
     * then it returns the configued image.  If it's false, then it just
     * returns a blank JLabel.
     */
    public Component getIcon() {
	if (bool) {
	    
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
	} else
	    return blankImage;
    }

    public int compareTo(Object o) {
	if (o instanceof BooleanIcon) {
	    boolean oValue = ((BooleanIcon)o).bool;
	    if (bool == oValue)
		return 0;
	    else if (bool == true)
		return 1;
	    else
		return -1;
	}
	throw new ClassCastException("object is not a BooleanIcon.");
    }

    public String toString() {
	return "";
    }
}
