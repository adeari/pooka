package net.suberic.util.gui;
import javax.swing.*;
import net.suberic.util.VariableBundle;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.MissingResourceException;
import javax.swing.Action;

/**
 * This is a JMenu which implements the ConfigurableUI interface, and
 * therefore may be dynamically created using a VariableBundle and key,
 * and updated using an array of Actions.
 */

public class ConfigurableMenu extends JMenu implements ConfigurableUI {

    // the latest commands list.  i'm storing this for now because i 
    // can't do a JButton.removeActionListeners().

    private Hashtable commands = new Hashtable();

    /**
     * This creates a new ConfigurableMenu using the menuID as the
     * configuration key, and vars as the source for the values of all the
     * properties.
     *
     * If menuID doesn't exist in vars, then this returns an empty 
     * Menu.
     */

    public ConfigurableMenu(String menuID, VariableBundle vars) {
	super();
	
	configureComponent(menuID, vars);
    }

    /**
     * This configures the Menu using the given menuID and 
     * VariableBundle.
     *
     * As defined in interface net.suberic.util.gui.ConfigurableUI.
     */

    public void configureComponent(String key, VariableBundle vars) {
	StringTokenizer iKeys = null;
	try {
	    iKeys = new StringTokenizer(vars.getProperty(key), ":");
	} catch (MissingResourceException mre) {
	    try {
		System.err.println(vars.getProperty("error.NoSuchResource") + " " + mre.getKey());
	    } catch (MissingResourceException mretwo) {
		System.err.println("Unable to load resource " + mre.getKey());
	    } finally {
		return;
	    }
	}
	String currentToken;
	
	try {
	    setText(vars.getProperty(key + ".Label"));
	} catch (MissingResourceException mre) {
	}
	
	while (iKeys.hasMoreTokens()) {
	    currentToken=iKeys.nextToken();
	    if (currentToken.equals("-")) {
		this.addSeparator();
	    } else {
		JMenuItem mi = createMenuItem(key, currentToken, vars);
		this.add(mi);
	    }
	}
    }
    /**
     * And this actually creates the menu items themselves.
     */
    protected JMenuItem createMenuItem(String menuID, String menuItemID, VariableBundle vars) {
	// TODO:  should also make these undo-able.
	
	/*	
		when we become able to do custom menus again.
		if (vars.getProperty(menuID + "." + menuItemID, "") == "") { 
	*/

	JMenuItem mi;
	try {
	    mi = new JMenuItem(vars.getProperty(menuID + "." + menuItemID + ".Label"));
	} catch (MissingResourceException mre) {
	    mi = new JMenuItem(menuItemID);
	}
	
	java.net.URL url = null;
	
	try {
	    url = this.getClass().getResource(vars.getProperty(menuID + "." + menuItemID + ".Image"));
	} catch (MissingResourceException mre) {
	}

	if (url != null) {
	    mi.setHorizontalTextPosition(JButton.RIGHT);
	    mi.setIcon(new ImageIcon(url));
	}
	
	String cmd = vars.getProperty(menuID + "." + menuItemID + ".Action", menuItemID);
	
	mi.setActionCommand(cmd);	
	
	return mi;
	/*	
		}
		if (vars.getProperty(menuID + "." + menuItemID, "").equals("folderList")) {
		return new FolderMenu(menuID + "." + menuItemID, getFolderPanel());
		}
		else
		return createMenu(menuID + "." + menuItemID );
	*/
    }

    /**
     * As defined in net.suberic.util.gui.ConfigurableUI
     */
    public void setActive(javax.swing.Action[] newActions) {
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
     * As defined in net.suberic.util.gui.ConfigurableUI
     */
    public void setActive(Hashtable newCommands) {
	clearListeners();
	commands = newCommands;
	setActiveMenuItems();
    }

    private void setActiveMenuItems() {
	for (int j = 0; j < getItemCount(); j++) {
	    if (getItem(j) instanceof ConfigurableMenu) {
		((ConfigurableMenu)getItem(j)).setActive(commands);
	    } else {
		JMenuItem mi = getItem(j);
		Action a = getAction(mi.getActionCommand());
		if (a != null) {
		    //mi.removeActionListener(a);
		    mi.addActionListener(a);
		    mi.setEnabled(true);
		} else {
		    mi.setEnabled(false);
		}
	    }
	}
    }	    
    
    /**
     * This clears all of the current listeners on the Menu.
     */
    
    private void clearListeners() {
	for (int j = 0; j < getItemCount(); j++) {
	    if (getItem(j) instanceof ConfigurableMenu) {
		// we don't have to clear the listeners here because
		// it will be done in setActive().
		;
	    } else {
		JMenuItem mi = getItem(j);
		Action a = getAction(mi.getActionCommand());
		if (a != null) {
		    mi.removeActionListener(a);
		}
	    }
	}
    }

    /**
     * This gets an action from the supported commands.  If there is no
     * supported action, it returns null
     */
    
    public Action getAction(String command) {
	return (Action)commands.get(command);
    }


}
