package net.suberic.util.gui;
import java.awt.event.*;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.Action;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import net.suberic.util.VariableBundle;

/**
 * This class is a KeyBinding controller for a JComponent.
 */

public class ConfigurableKeyBinding implements ConfigurableUI {
    private JComponent currentComponent;
    private Hashtable commands = new Hashtable();
    private Hashtable keyTable = new Hashtable();
    private int condition = JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;

    /**
     * This creates a new ConfigurableKeyBinding which attaches to component
     * newComponent, and defines itself using componentID and vars.
     */
    public ConfigurableKeyBinding(JComponent newComponent, String componentID, VariableBundle vars) {
	currentComponent = newComponent;
	
	configureComponent(componentID, vars);
    }

    /**
     * This configures the KeyBindings using the given componentID
     * and VariableBundle.
     *
     * As defined in interface net.suberic.util.gui.ConfigurableUI.
     */

    public void configureComponent(String componentID, VariableBundle vars) {
	if (componentID != null && vars.getProperty(componentID, "") != "") {
	    Vector keys = vars.getPropertyAsVector(componentID, "");
	    for (int i = 0; i < keys.size(); i++) {
		String keyID = componentID + "." + (String)keys.elementAt(i);
		String keyAction = vars.getProperty(keyID + ".Action" , "");
		KeyStroke keyStroke = getKeyStroke(keyID, vars);
		if (keyAction != "" && keyStroke != null) {
		    keyTable.put(keyAction, keyStroke);
		}
	    }
	}
    }

    /**
     * This method returns the key defined by the property keyID in the
     * VariableBundle vars.
     */
    public KeyStroke getKeyStroke(String keyID, VariableBundle vars) {
	return KeyStroke.getKeyStroke(vars.getProperty(keyID + ".Key", ""));
    }

    /**
     * This method actually binds the configured KeyStrokes to the current
     * Actions.
     *
     * As defined in interface net.suberic.util.gui.ConfigurableUI.
     */
    public void setActive(Hashtable newCommands) {
	commands = newCommands;
	Enumeration hashKeys = keyTable.keys();
	while (hashKeys.hasMoreElements()) {
	    String actionCmd = (String)hashKeys.nextElement();
	    KeyStroke keyStroke = (KeyStroke)keyTable.get(actionCmd);
	    Action a = getAction(actionCmd);
	    if (a != null) {
		currentComponent.registerKeyboardAction(a, actionCmd, keyStroke, getCondition() );
	    } else {
		currentComponent.unregisterKeyboardAction(keyStroke);
	    }
	}
    }

    /**
     * This method actually binds the configured KeyStrokes to the current
     * Actions.
     *
     * As defined in interface net.suberic.util.gui.ConfigurableUI.
     */
    public void setActive(Action[] newActions) {
	Hashtable tmpHash = new Hashtable();
	if (newActions != null && newActions.length > 0) {
	    for (int i = 0; i < newActions.length; i++) {
		String cmdName = (String)newActions[i].getValue(Action.NAME);
		tmpHash.put(cmdName, newActions[i]);
	    }
	}
	setActive(tmpHash);
    }

    private Action getAction(String key) {
	try {
	    return (Action)commands.get(key);
	} catch (ClassCastException cce) {
	    return null;
	}
    }
    
    public void setCondition(int newCondition) {
	condition = newCondition;
    }

    public int getCondition() {
	return condition;
    }

}
