package net.suberic.util.gui;
import javax.swing.*;
import net.suberic.util.VariableBundle;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.MissingResourceException;
import javax.swing.Action;

/**
 * This is a JToolbar which implements the ConfigurableUI interface, and
 * therefore may be dynamically created using a VariableBundle and key,
 * and updated using an array of Actions.
 */

public class ConfigurableToolbar extends JToolBar implements ConfigurableUI {

    // the latest commands list.  i'm storing this for now because i 
    // can't do a JButton.removeActionListeners().

    private Hashtable commands = new Hashtable();

    /**
     * This creates a new ConfigurableToolbar using the toolbarID as the
     * configuration key, and vars as the source forthe values of all the
     * properties.
     *
     * If toolbarID doesn't exist in vars, then this returns an empty 
     * Toolbar.
     */

    public ConfigurableToolbar(String toolbarID, VariableBundle vars) {
	super();
	
	configureComponent(toolbarID, vars);
    }

    /**
     * This configures the Toolbar using the given toolbarID and 
     * VariableBundle.
     *
     * As defined in interface net.suberic.util.gui.ConfigurableUI.
     */

    public void configureComponent(String toolbarID, VariableBundle vars) {
	if ((toolbarID != null) && (vars.getProperty(toolbarID, "") != "")) {
	    StringTokenizer tokens = new StringTokenizer(vars.getProperty(toolbarID, ""), ":");
	    while (tokens.hasMoreTokens()) {
		JButton b = createToolButton(tokens.nextToken(), vars);
		if (b != null) {
		    this.add(b);
		}
	    }
	}
    }    

    protected JButton createToolButton(String key, VariableBundle vars) {
	JButton bi;
	try {
	    java.net.URL url =this.getClass().getResource(vars.getProperty("MainToolbar." + key + ".Image"));
	    bi = new JButton(new ImageIcon(url));
	    
	} catch (MissingResourceException mre) {
	    return null;
	}
	
	try {
	    bi.setToolTipText(vars.getProperty("MainToolbar." +key+ ".ToolTip"));
	} catch (MissingResourceException mre) {
	}
	
	String cmd = vars.getProperty("MainToolbar." + key + ".Action", key);
	
	bi.setActionCommand(cmd);
	
	return bi;
    }

    /**
     * This updates the Actions on the Toolbar.
     *
     * As defined in interface net.suberic.util.gui.ConfigurableUI.
     */

    public void setActive(Hashtable newCommands) {
	clearListeners();
	commands=newCommands;
	for (int i = 0; i < this.getComponentCount(); i++) {
	    JButton bi = (JButton)(this.getComponentAtIndex(i));
	    
	    Action a = getAction(bi.getActionCommand());
	    if (a != null) {
		bi.addActionListener(a);
		bi.setEnabled(true);
	    } else {
		bi.setEnabled(false);
	    }
	}
    }

    /**
     * This updates the Actions on the Toolbar.
     *
     * As defined in interface net.suberic.util.gui.ConfigurableUI.
     */
    public void setActive(Action[] newActions) {
	clearListeners();
	Hashtable tmpHash = new Hashtable();
	if (newActions != null && newActions.length > 0) {
	    for (int i = 0; i < newActions.length; i++) {
		String cmdName = (String)newActions[i].getValue(Action.NAME);
		tmpHash.put(cmdName, newActions[i]);
	    }
	}
	setActive(tmpHash);
    }

    /**
     * This clears the current listeners.  I think this shouldn't be
     * necessary--I think that you can only have one listener at a time,
     * so this shouldn't really be necessary.  Still...
     */
    private void clearListeners() {
	for (int i = 0; i < this.getComponentCount(); i++) {
	    if ((this.getComponentAtIndex(i)) instanceof JButton) {
		JButton button = (JButton)(this.getComponentAtIndex(i));
		Action a = getAction(button.getActionCommand());
		if (a != null) {
		    button.removeActionListener(a);
		}
	    }
	    
	}   
    }

    private Action getAction(String key) {
	try {
	    return (Action)commands.get(key);
	} catch (ClassCastException cce) {
	    return null;
	}
    }

    
}


