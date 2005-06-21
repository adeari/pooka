package net.suberic.pooka.gui;

import javax.swing.*;
import java.util.*;
import net.suberic.pooka.Pooka;
import net.suberic.pooka.FolderInfo;
import net.suberic.util.*;

/**
 * A Menu that shows the list of all new messages available.
 */
public class RecentMessageMenu extends net.suberic.util.gui.ConfigurableMenu {

  /**
   * This creates a new RecentMessageMenu.
   */
  public RecentMessageMenu() {
  }
  
  /**
   * Overrides ConfigurableMenu.configureComponent().
   */
  public void configureComponent(String key, VariableBundle vars) {
    try {
      setText(vars.getProperty(key + ".Label"));
    } catch (MissingResourceException mre) {
    }
    
    this.setActionCommand(vars.getProperty(key + ".Action", "message-open"));

    /*
    MessageNotificationManager mnm = Pooka.getMainPanel().getMessageNotificationManager(); 
    Map newMessageMap = mnm.getNewMessageMap();
    Iterator folders = newMessageMap.keySet().iterator();
    while (folders.hasNext()) {
      String current = (String) folders.next();
      buildFolderMenu(current, (List)newMessageMap.get(current));
    }
    */

  }
  
  /**
   * This builds the menu for each folder/message group.
   */
  protected void buildFolderMenu(String pFolderName, List pMessageList) {
    MessageNotificationManager mnm = Pooka.getMainPanel().getMessageNotificationManager();
    JMenu newMenu = new JMenu(pFolderName);
    for(int i = 0 ; i < pMessageList.size(); i++) {
      JMenuItem mi = new JMenuItem();
      net.suberic.pooka.MessageInfo messageInfo = (net.suberic.pooka.MessageInfo) pMessageList.get(i);
      mi.setAction(mnm.new OpenMessageAction(messageInfo));
      try {
	mi.setLabel(messageInfo.getMessageProperty("From") + ":  " + messageInfo.getMessageProperty("Subject"));
      } catch (Exception e) {
	mi.setLabel("new message");
      }
      newMenu.add(mi);
      System.err.println("adding menuitem for message.");

    }
    System.err.println("adding menu for " + pFolderName);
    this.add(newMenu);
  }

  void reset() {
    System.err.println("resetting...");
    removeAll();
    MessageNotificationManager mnm = Pooka.getMainPanel().getMessageNotificationManager();
    if (mnm != null) {
      Map newMessageMap = mnm.getNewMessageMap();
      Iterator folders = newMessageMap.keySet().iterator();
      while (folders.hasNext()) {
	String current = (String) folders.next();
	buildFolderMenu(current, (List)newMessageMap.get(current));
      }
    }

  }
  
  /*
  public void addNotify() {
    reset();

    super.addNotify();
  }
  */
  
}
