package net.suberic.util.gui;
import javax.swing.*;
import net.suberic.util.VariableBundle;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.MissingResourceException;
import javax.swing.Action;

/**
 * This is a JMenuBar which implements the ConfigurableUI interface, and
 * therefore may be dynamically created using a VariableBundle and key,
 * and updated using an array of Actions.
 */

public class ConfigurableMenuBar extends JMenuBar implements ConfigurableUI {

    // the latest commands list.  i'm storing this for now because i 
    // can't do a JButton.removeActionListeners().

    private Hashtable commands = new Hashtable();

    /**
     * This creates a new ConfigurableMenuBar using the menubarID as the
     * configuration key, and vars as the source forthe values of all the
     * properties.
     *
     * If menubarID doesn't exist in vars, then this returns an empty 
     * Menubar.
     */

    public ConfigurableMenuBar(String menuBarID, VariableBundle vars) {
	super();
	
	configureComponent(menuBarID, vars);
    }

    /**
     * This configures the Menubar using the given menubarID and 
     * VariableBundle.
     *
     * As defined in interface net.suberic.util.gui.ConfigurableUI.
     */

    public void configureComponent(String menubarID, VariableBundle vars) {
	if ((menubarID != null) && (vars.getProperty(menubarID, "") != "")) {
	    StringTokenizer tokens = new StringTokenizer(vars.getProperty(menubarID, ""), ":");
	    while (tokens.hasMoreTokens()) {
		JMenu m = createMenu(menubarID + "." + tokens.nextToken(), vars);
		if (m != null) {
		    this.add(m);
		}
	    }
	}
    }    
    
    
      /**
   * Create a menu for the app.  By default this pulls the
   * definition of the menu from the associated resource file.
   */

    protected JMenu createMenu(String key, VariableBundle vars) {
	StringTokenizer iKeys = null;
	try {
	    iKeys = new StringTokenizer(vars.getProperty(key), ":");
	} catch (MissingResourceException mre) {
	    try {
		System.err.println(vars.getProperty("error.NoSuchResource") + " " + mre.getKey());
	    } catch (MissingResourceException mretwo) {
		System.err.println("Unable to load resource " + mre.getKey());
	    } finally {
	      return null;
	    }
	}
	String currentToken;
	JMenu menu;
	
	try {
	    menu = new JMenu(vars.getProperty(key + ".Label"));
	} catch (MissingResourceException mre) {
	    menu = new JMenu(key);
    }
	
    while (iKeys.hasMoreTokens()) {
	currentToken=iKeys.nextToken();
	if (currentToken.equals("-")) {
	    menu.addSeparator();
	} else {
	    JMenuItem mi = createMenuItem(key, currentToken, vars);
	    menu.add(mi);
	}
    }
    return menu;
    }
        /**
     * And this actually creates the menu items themselves.
     */
    protected JMenuItem createMenuItem(String menuID, String menuItemID, VariableBundle vars) {
    // TODO:  should also make these undo-able.
	
	/*	if (vars.getProperty(menuID + "." + menuItemID, "") == "") { */
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
	    } /*catch (java.net.MalformedURLException mue) {
		System.out.println("malformedURL for " + menuID + "." + menuItemID + ".Image");
		}*/
	    if (url != null) {
		mi.setHorizontalTextPosition(JButton.RIGHT);
		mi.setIcon(new ImageIcon(url));
	    }
	    
	    String cmd = vars.getProperty(menuID + "." + menuItemID + ".Action", menuItemID);
	    
	    mi.setActionCommand(cmd);	
	    return mi;
	    /*	}
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
    public void setActive(Hashtable commands) {
	clearListeners();
    }

    private void setActiveMenuItems(JMenu men) {
	if (men instanceof net.suberic.util.DynamicMenu) {
	    ((net.suberic.util.DynamicMenu)men).setActiveMenus(this);
	} else {
	    for (int j = 0; j < men.getItemCount(); j++) {
		if ((men.getItem(j)) instanceof JMenu) {
		    setActiveMenuItems((JMenu)(men.getItem(j)));
		} else {
		    JMenuItem mi = men.getItem(j);
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
    }	    

    private void setActiveMenus(JMenuBar menuBar) {
	for (int i = 0; i < menuBar.getMenuCount(); i++) {
	    setActiveMenuItems(menuBar.getMenu(i));
	}
    }


    /**
     * This clears all of the current listeners on the Menu.
     */
    
    private void clearListeners() {
	for (int i = 0; i < this.getMenuCount(); i++) {
	    removeActiveMenuItems(getMenu(i));
	}
    }

    private void removeActiveMenuItems(JMenu men) {
	for (int j = 0; j < men.getItemCount(); j++) {
	    if ((men.getItem(j)) instanceof JMenu) {
		removeActiveMenuItems((JMenu)(men.getItem(j)));
	    } else {
		JMenuItem mi = men.getItem(j);
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
