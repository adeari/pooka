package net.suberic.pooka.gui.filter;
import javax.swing.*;
import java.awt.*;

/**
 * This class allows you to choose colors for a ColorFilter.
 */
public class ColorFilterEditor extends JButton implements java.awt.event.ActionListener {
    Color currentColor;

    public ColorFilterEditor(int rgb) {
	setCurrentColor(new Color(rgb));
	this.addActionListener(this);
    }

    public void setCurrentColor(Color newColor) {
	currentColor = newColor;
	this.setBackground(currentColor);
    }

    public Color getCurrentColor() {
	return currentColor;
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
	Color newColor = JColorChooser.showDialog(this, "title", currentColor);
	if (newColor != null)
	    setCurrentColor(newColor);
    }
    
    /**
     * Sets the property for this action.  the propertyRoot should include
     * the ".action" part.
     */
    public void writeProperty(String propertyRoot, net.suberic.util.VariableBundle vars) {
	vars.setProperty(propertyRoot + ".rgb", Integer.toString(currentColor.getRGB()));
    }
}
