package net.suberic.util;

import javax.swing.*;

public abstract class DynamicMenu extends JMenu {

    String actionCommand;

    public DynamicMenu(String title) {
	super(title);
    }

    public void setActiveMenus(JComponent mp) {
    }

    public void setActionCommand(String newActionCommand) {
	actionCommand = newActionCommand;
    }

    public String getActionCommand() {
	return actionCommand;
    }
}
