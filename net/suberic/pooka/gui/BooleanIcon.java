package net.suberic.pooka.gui;
import javax.swing.*;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.awt.Component;

public class BooleanIcon implements TableCellIcon {
    public boolean bool;
    public String iconFile;
    public static HashMap labelTable = new HashMap();
    protected static Component blankImage = new JLabel();

    public BooleanIcon(boolean boolValue, String newIconFile) {
	bool=boolValue;
	iconFile = newIconFile;
	((JLabel)blankImage).setOpaque(true);
    }

    /**
     * This returns a JLabel.  If the value of this BooleanIcon is true,
     * then it returns the configued image.  If it's false, then it just
     * returns a blank JLabel.
     */
    public Component getIcon() {
	if (bool) {
	    return getIcon(iconFile);
	} else
	    return blankImage;
    }

    public Component getIcon(String imageFile) {
	
	if (labelTable.containsKey(imageFile))
	    return (Component)labelTable.get(imageFile);
	else
	    return loadImage(imageFile);
    }

    public Component loadImage(String imageFile) {
	    Component returnValue = null;
	    
	    try {
		java.net.URL url = this.getClass().getResource(imageFile);
		if (url != null) {
		    returnValue = new JLabel(new ImageIcon(url));
		    ((JLabel)returnValue).setOpaque(true);
		    
		} else
		    returnValue = null;
	    } catch (MissingResourceException mre) {
		returnValue = blankImage;
	    }
	
	    labelTable.put(imageFile, returnValue);
	    return returnValue;
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
