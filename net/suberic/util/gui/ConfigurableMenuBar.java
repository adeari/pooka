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

public class ConfigurableMenubar extends JMenubar implements ConfigurableUI {

    // the latest commands list.  i'm storing this for now because i 
    // can't do a JButton.removeActionListeners().

    private Hashtable commands = new Hashtable();

    /**
     * This creates a new ConfigurableMenubar using the menubarID as the
     * configuration key, and vars as the source forthe values of all the
     * properties.
     *
     * If menubarID doesn't exist in vars, then this returns an empty 
     * Menubar.
     */

    public ConfigurableMenubar(String menuBarID, VariableBundle vars) {
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

    protected JMenu createMenu(String key) {
	StringTokenizer iKeys = null;
	try {
	    iKeys = new StringTokenizer(Pooka.getProperty(key), ":");
	} catch (MissingResourceException mre) {
	    try {
		System.err.println(Pooka.getProperty("error.NoSuchResource") + " " + mre.getKey());
	    } catch (MissingResourceException mretwo) {
		System.err.println("Unable to load resource " + mre.getKey());
	    } finally {
	      return null;
	    }
	}
	String currentToken;
	JMenu menu;
	
	try {
	    menu = new JMenu(Pooka.getProperty(key + ".Label"));
	} catch (MissingResourceException mre) {
	    menu = new JMenu(key);
    }
	
    while (iKeys.hasMoreTokens()) {
	currentToken=iKeys.nextToken();
	if (currentToken.equals("-")) {
	    menu.addSeparator();
	} else {
	    JMenuItem mi = createMenuItem(key, currentToken);
	    menu.add(mi);
	}
    }
    return menu;
    }
        /**
     * And this actually creates the menu items themselves.
     */
    protected JMenuItem createMenuItem(String menuID, String menuItemID) {
    // TODO:  should also make these undo-able.
	
	if (Pooka.getProperty(menuID + "." + menuItemID, "") == "") {
	    JMenuItem mi;
	    try {
		mi = new JMenuItem(Pooka.getProperty(menuID + "." + menuItemID + ".Label"));
	    } catch (MissingResourceException mre) {
		mi = new JMenuItem(menuItemID);
	    }
	    
	    java.net.URL url = null;
	    
	    try {
		url = this.getClass().getResource(Pooka.getProperty(menuID + "." + menuItemID + ".Image"));
	    } catch (MissingResourceException mre) {
	    } /*catch (java.net.MalformedURLException mue) {
		System.out.println("malformedURL for " + menuID + "." + menuItemID + ".Image");
		}*/
	    if (url != null) {
		mi.setHorizontalTextPosition(JButton.RIGHT);
		mi.setIcon(new ImageIcon(url));
	    }
	    
	    String cmd = Pooka.getProperty(menuID + "." + menuItemID + ".Action", menuItemID);
	    
	    mi.setActionCommand(cmd);	
	    return mi;
	} else 
	    if (Pooka.getProperty(menuID + "." + menuItemID, "").equals("folderList")) {
		return new FolderMenu(menuID + "." + menuItemID, getFolderPanel());
	    }
	    else
		return createMenu(menuID + "." + menuItemID );
    }
