package net.suberic.pooka.gui.filter;
import java.awt.*;


public class ColorDisplayFilter implements DisplayFilter {
 
    Color newColor;

    /**
     * Creates a new ColorDisplayFilter.
     */
    public ColorDisplayFilter() {
    }

    /**
     * a no-op.
     */
    public java.util.Vector performFilter(java.util.Vector tmp) {
	return tmp;
    }
 
    /**
     * Configures the filter from the given property.
     */
    public void initializeFilter(String propertyName) {
	try {
	    newColor = new Color(Integer.parseInt(net.suberic.pooka.Pooka.getProperty(propertyName + ".rgb", "742")));
	} catch (Exception e) {
	    newColor = new Color(742);
	}
	    
    }

    /**
     * Applies the filter to the given component.
     */
    public void apply(java.awt.Component target) {
	target.setForeground(newColor);
    }
}
