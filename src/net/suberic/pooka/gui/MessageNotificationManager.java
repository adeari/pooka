package net.suberic.pooka.gui;
import net.suberic.pooka.Pooka;
import net.suberic.pooka.FolderInfo;
import net.suberic.pooka.MessageInfo;
import net.suberic.util.gui.ConfigurablePopupMenu;

import java.util.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.mail.event.MessageCountEvent;
import java.awt.event.ActionEvent;

import org.jdesktop.jdic.tray.TrayIcon;
import org.jdesktop.jdic.tray.SystemTray;

/**
 * This manages the display of new message notifications.
 */
public class MessageNotificationManager {

  private MainPanel mPanel;
  private boolean mNewMessageFlag = false;
  private TrayIcon mTrayIcon = null;
  private Map mNewMessageMap;
  private int mNewMessageCount = 0;

  private Action[] mDefaultActions;

  // icons and displays
  private String mStandardTitle = Pooka.getProperty("Title", "Pooka");
  private String mNewMessageTitle = Pooka.getProperty("Title.withNewMessages", "* Pooka *");
  
  private ImageIcon mStandardIcon = null;
  private ImageIcon mNewMessageIcon = null;
  private ImageIcon mStandardTrayIcon = null;
  private ImageIcon mNewMessageTrayIcon = null;

  /**
   * Creates a new MessageNotificationManager for the given MainPanel.
   */
  public MessageNotificationManager(MainPanel pPanel) {
    mPanel = pPanel;
    mNewMessageMap = new HashMap();

    mDefaultActions = new Action[] { new NewMessageAction(), new ClearStatusAction() };

    java.net.URL standardUrl = this.getClass().getResource(Pooka.getProperty("Pooka.standardIcon", "images/PookaIcon.gif")); 
    if (standardUrl != null) {
      mStandardIcon = new ImageIcon(standardUrl);
      setCurrentIcon(mStandardIcon);
    }

    java.net.URL standardTrayUrl = this.getClass().getResource(Pooka.getProperty("Pooka.standardTrayIcon", "images/PookaIcon.gif")); 
    if (standardTrayUrl != null) {
      mStandardTrayIcon = new ImageIcon(standardTrayUrl);
      mTrayIcon = new TrayIcon(mStandardTrayIcon);
      mTrayIcon.setIconAutoSize(true);
      mTrayIcon.setPopupMenu(createPopupMenu());
      mTrayIcon.addActionListener(new AbstractAction() {
	  public void actionPerformed(ActionEvent e) {
	    System.err.println("action:  " + e);
	    mTrayIcon.displayMessage("Pooka", createStatusMessage(), TrayIcon.INFO_MESSAGE_TYPE);
	    System.err.println("trying to bring frame to front.");
	    Pooka.getMainPanel().getParentFrame().toFront();
	  }
	});
      
      SystemTray.getDefaultSystemTray().addTrayIcon(mTrayIcon);

    }

    
    java.net.URL newMessageUrl = this.getClass().getResource(Pooka.getProperty("Pooka.newMessageIcon", "images/PookaNewMessageIcon.gif")); 
    if (newMessageUrl != null) {
      mNewMessageIcon = new ImageIcon(newMessageUrl);
    }
    
    java.net.URL newMessageTrayUrl = this.getClass().getResource(Pooka.getProperty("Pooka.newMessageTrayIcon", "images/PookaNewMessageIcon_20x20.png")); 
    if (newMessageTrayUrl != null) {
	mNewMessageTrayIcon = new ImageIcon(newMessageTrayUrl);
    }
    
    getMainPanel().getParentFrame().addWindowListener(new WindowAdapter() {
	public void windowActivated(WindowEvent e) {
	  clearNewMessageFlag();
	}
      });
  }

  /**
   * This resets the title of the main Frame to have the newMessageFlag
   * or not, depending on if there are any new messages or not.
   */
  protected void updateStatus() {
    synchronized(this) {
      if (getNewMessageFlag()) {
	getMainPanel().getParentFrame().setTitle(mNewMessageTitle);
	
	setCurrentIcon(getNewMessageIcon());
	if (getTrayIcon() != null)
	  getTrayIcon().setIcon(mNewMessageTrayIcon);
      } else {
	getMainPanel().getParentFrame().setTitle(mStandardTitle);
	
	setCurrentIcon(getStandardIcon());
	if (getTrayIcon() != null)
	  getTrayIcon().setIcon(mStandardTrayIcon);
      }
    } //synchronized
  }

  /**
   * Clears the status.
   */
  public synchronized void clearNewMessageFlag() {
    boolean doUpdate = mNewMessageFlag;
    mNewMessageFlag = false;
    mNewMessageCount = 0;
    mNewMessageMap = new HashMap();
    mTrayIcon.setToolTip("Pooka: No new messages.");
    mTrayIcon.setCaption("Pooka: caption.");
    if (doUpdate) {
      //Thread.currentThread().dumpStack();
      updateStatus();
    }
  }

  /**
   * Called when a Folder that's being watched gets a messagesAdded event.
   */
  public synchronized void notifyNewMessagesReceived(MessageCountEvent e, String pFolderId) {
    // note:  called on the FolderThread that produced this event.
    mNewMessageCount+= e.getMessages().length;
    List newMessageList = (List) mNewMessageMap.get(pFolderId);
    if (newMessageList == null) {
      newMessageList = new ArrayList();
    }

    // get the MessageInfo for each of the added messages and add it to
    // the newMessageList.  oh, and while we're at it, add the string info
    // for the first three, also.
    StringBuffer infoLines = new StringBuffer();
    try {
      FolderInfo folder = Pooka.getStoreManager().getFolderById(pFolderId);
      if (folder != null) {
	for (int i = 0; i < e.getMessages().length; i++) {
	  MessageInfo current = folder.getMessageInfo(e.getMessages()[i]);
	  newMessageList.add(current);
	  if (i < 3)
	    infoLines.append("From: " + current.getMessageProperty("From") + ", Subj: " + current.getMessageProperty("Subject") + "\n");
	  else if (i == 3)
	    infoLines.append("...");
	}
      }
    } catch (javax.mail.MessagingException me) {
      // FIXME handle this better.
      me.printStackTrace();
    }
    //newMessageList.addAll(Arrays.asList(e.getMessages()));
    mNewMessageMap.put(pFolderId, newMessageList);

    // build the message
    final String fDisplayMessage = new String(e.getMessages().length + " messages received in " + pFolderId + "\n\n" + infoLines.toString());
    final String fToolTip = "Pooka: " + mNewMessageCount + " new messages.";
    boolean doUpdateStatus = false;
    if (! mNewMessageFlag) {
      mNewMessageFlag = true;
      doUpdateStatus = true;
    }

    final boolean fUpdateStatus = doUpdateStatus;

    SwingUtilities.invokeLater(new Runnable() {
	public void run() {
	  if (fUpdateStatus)
	    updateStatus();

	  mTrayIcon.setToolTip(fToolTip);
	  mTrayIcon.displayMessage("New Messages", fDisplayMessage, TrayIcon.INFO_MESSAGE_TYPE);
	}
      });
    
  }

  /**
   * Creates the JPopupMenu for this component.
   */
  public JPopupMenu createPopupMenu() {
    ConfigurablePopupMenu popupMenu = new ConfigurablePopupMenu();
    popupMenu.configureComponent("MessageNotificationManager.popupMenu", Pooka.getResources());	
    popupMenu.setActive(getActions());
    return popupMenu;
  }

  /**
   * Constructs a status message for the current state of new messages.
   */
  public String createStatusMessage() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("Pooka\n");
    if (mNewMessageMap.isEmpty()) {
      buffer.append("No new messages.");
    } else {
      Iterator folders = mNewMessageMap.keySet().iterator();
      while (folders.hasNext()) {
	String current = (String) folders.next();
	buffer.append(current + ":  " + ((List)mNewMessageMap.get(current)).size() + " new messages.\n");
      }
    }

    return buffer.toString();
  }
  
  /**
   * Returns the actions for this component.
   */
  public Action[] getActions() {
    return mDefaultActions;
  }

  /**
   * Get the standard icon for Pooka.
   */
  public ImageIcon getStandardIcon() {
    return mStandardIcon;
  }

  /**
   * Get the new message icon for Pooka.
   */
  public ImageIcon getNewMessageIcon() {
    return mNewMessageIcon;
  }

  /**
   * Sets the current icon for the frame.
   */
  public void setCurrentIcon(ImageIcon newIcon) {
    getMainPanel().getParentFrame().setIconImage(newIcon.getImage());
  }

  /**
   * Gets the tray icon.
   */
  public TrayIcon getTrayIcon() {
    return mTrayIcon;
  }

  /**
   * Returns the MainPanel for this MNM.
   */
  public MainPanel getMainPanel() {
    return mPanel;
  }

  /**
   * Returns the newMessageFlag.
   */
  public boolean getNewMessageFlag() {
    return mNewMessageFlag;
  }

  /**
   * Returns the current new message map.
   */
  public Map getNewMessageMap() {
    return mNewMessageMap;
  }

    //-----------actions----------------

  class NewMessageAction extends AbstractAction {
    
    NewMessageAction() {
      super("message-new");
    }
    
    public void actionPerformed(ActionEvent e) {
      System.err.println("sending new message.");
      net.suberic.pooka.messaging.PookaMessageSender sender =  new net.suberic.pooka.messaging.PookaMessageSender();
      try {
	sender.openConnection();
	if (sender.checkVersion()) {
	  sender.openNewEmail(null, null);
	}
      } catch (Exception exc) {
	mTrayIcon.displayMessage("Error", "Error sending new message:  " + exc, TrayIcon.WARNING_MESSAGE_TYPE);
      } finally {
	if (sender != null && sender.isConnected())
	  sender.closeConnection();
      }
    }
  }

  class OpenMessageAction extends AbstractAction {
    
    MessageInfo mMessageInfo;

    OpenMessageAction(MessageInfo pMessageInfo) {
      super("message-new");
      
      mMessageInfo = pMessageInfo;
    }
    
    public void actionPerformed(ActionEvent e) {
      System.err.println("opening message.");

      try {
	MessageProxy proxy = mMessageInfo.getMessageProxy();
	MessageUI mui = Pooka.getUIFactory().createMessageUI(proxy, new NewMessageFrame(new NewMessageProxy(new net.suberic.pooka.NewMessageInfo(new javax.mail.internet.MimeMessage(Pooka.getDefaultSession())))));
	mui.openMessageUI();
      } catch (Exception ex) {
	ex.printStackTrace();
      }
      /*
      net.suberic.pooka.messaging.PookaMessageSender sender =  new net.suberic.pooka.messaging.PookaMessageSender();
      try {
	sender.openConnection();
	if (sender.checkVersion()) {
	  //sender.openExistingEmail(null, null);
	}
      } catch (Exception exc) {
	mTrayIcon.displayMessage("Error", "Error opening message:  " + exc, TrayIcon.ERROE_MESSAGE_TYPE);
      } finally {
	if (sender != null && sender.isConnected())
	  sender.closeConnection();
      }
      */

    }
  }
  
  class ClearStatusAction extends AbstractAction {
    
    ClearStatusAction() {
      super("status-clear");
    }
    
    public void actionPerformed(ActionEvent e) {
      clearNewMessageFlag();
    }
  }

}
