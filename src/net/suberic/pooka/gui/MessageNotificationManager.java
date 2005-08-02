package net.suberic.pooka.gui;
import net.suberic.pooka.Pooka;
import net.suberic.pooka.FolderInfo;
import net.suberic.pooka.MessageInfo;
import net.suberic.util.gui.ConfigurablePopupMenu;
import net.suberic.util.*;

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
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsConfiguration;

import org.jdesktop.jdic.tray.TrayIcon;
import org.jdesktop.jdic.tray.SystemTray;

/**
 * This manages the display of new message notifications.
 */
public class MessageNotificationManager implements ValueChangeListener {

  public static int WARNING_MESSAGE_TYPE = TrayIcon.WARNING_MESSAGE_TYPE;

  private MainPanel mPanel;
  private boolean mNewMessageFlag = false;
  private TrayIcon mTrayIcon = null;
  private Map mNewMessageMap;
  private int mNewMessageCount = 0;

  private Action[] mOfflineActions;
  private Action[] mOnlineActions;

  // icons and displays
  private String mStandardTitle = Pooka.getProperty("Title", "Pooka");
  private String mNewMessageTitle = Pooka.getProperty("Title.withNewMessages", "* Pooka *");
  
  private ImageIcon mStandardIcon = null;
  private ImageIcon mNewMessageIcon = null;
  private ImageIcon mStandardTrayIcon = null;
  private ImageIcon mNewMessageTrayIcon = null;

  private boolean mShowNewMailMessage = true;
  private boolean mBlinkNewMail = true;

  /**
   * Creates a new MessageNotificationManager.
   */
  public MessageNotificationManager() {
    mNewMessageMap = new HashMap();

    mOfflineActions = new Action[] { 
      new NewMessageAction(), 
      new PreferencesAction(), 
      new ExitPookaAction(), 
      new StartPookaAction()
    };

    mOnlineActions = new Action[] { 
      new NewMessageAction(), 
      new PreferencesAction(), 
      new ExitPookaAction(), 
      new ClearStatusAction()
    };

    // set up the images to use.
    setupImages();

    // create the tray icon.
    configureTrayIcon();

    // add a listener so we can add/remove the tray icon if the setting
    // changes.
    Pooka.getResources().addValueChangeListener(this, "Pooka.trayIcon.enabled");
  }

  /**
   * Creates the SystemTrayIcon, if configured to do so.
   */
  void configureTrayIcon() {
    if (Pooka.getProperty("Pooka.trayIcon.enabled", "true").equalsIgnoreCase("true")) {
      try {
	mTrayIcon = new TrayIcon(mStandardTrayIcon);
	mTrayIcon.setIconAutoSize(true);
	mTrayIcon.setTimeout(5000);
	
	mTrayIcon.setPopupMenu(createPopupMenu());
	mTrayIcon.addActionListener(new AbstractAction() {
	    public void actionPerformed(ActionEvent e) {
	      System.err.println("action:  " + e);
	      if (getMainPanel() != null) {
		mTrayIcon.displayMessage("Pooka", createStatusMessage(), TrayIcon.INFO_MESSAGE_TYPE);
		bringToFront();
	      } else {
		startMainWindow();
	      }
	    }
	  });
	
	SystemTray.getDefaultSystemTray().addTrayIcon(mTrayIcon);
      } catch (Error e) {
	System.err.println("Error starting up tray icon:  " + e.getMessage());
      }
    } else if (mTrayIcon != null) {
      // remove the tray icon.
      SystemTray.getDefaultSystemTray().removeTrayIcon(mTrayIcon);
      mTrayIcon = null;
    }
  }

  /**
   * Sets up the images to use for the tray icon and for the main window.
   */
  void setupImages() {
    java.net.URL standardUrl = this.getClass().getResource(Pooka.getProperty("PookastandardIcon", "images/PookaIcon.gif")); 
    if (standardUrl != null) {
      mStandardIcon = new ImageIcon(standardUrl);
      setCurrentIcon(mStandardIcon);
    }

    java.net.URL standardTrayUrl = this.getClass().getResource(Pooka.getProperty("Pooka.standardTrayIcon", "images/PookaIcon_20x20.png")); 
    if (standardTrayUrl != null) {
      mStandardTrayIcon = new ImageIcon(standardTrayUrl);
    }

    
    java.net.URL newMessageUrl = this.getClass().getResource(Pooka.getProperty("Pooka.newMessageIcon", "images/PookaNewMessageIcon.gif")); 
    if (newMessageUrl != null) {
      mNewMessageIcon = new ImageIcon(newMessageUrl);
    }
    
    java.net.URL newMessageTrayUrl = this.getClass().getResource(Pooka.getProperty("Pooka.newMessageTrayIcon", "images/PookaNewMessageIcon_20x20.png")); 
    if (newMessageTrayUrl != null) {
	mNewMessageTrayIcon = new ImageIcon(newMessageTrayUrl);
    }

  }

  /**
   * This handles the changes if the source property is modified.
   *
   * As defined in net.suberic.util.ValueChangeListener.
   */
  
  public void valueChanged(String pChangedValue) {
    if (pChangedValue.equals("Pooka.trayIcon.enabled")) {
      configureTrayIcon();
    }
  }

  /**
   * This resets the title of the main Frame to have the newMessageFlag
   * or not, depending on if there are any new messages or not.
   */
  protected void updateStatus() {
    synchronized(this) {
      if (getNewMessageFlag()) {
	//System.err.println("updating status.");
	if (getMainPanel() != null) {
	  getMainPanel().getParentFrame().setTitle(mNewMessageTitle);
	}
	setCurrentIcon(getNewMessageIcon());
	if (getTrayIcon() != null)
	  getTrayIcon().setIcon(mNewMessageTrayIcon);
      } else {
	if (getMainPanel() != null) {
	  getMainPanel().getParentFrame().setTitle(mStandardTitle);
	}
	
	setCurrentIcon(getStandardIcon());
	if (getTrayIcon() != null)
	  getTrayIcon().setIcon(mStandardTrayIcon);
      }
    } //synchronized
  }

  /**
   * Brings the main window to the front.
   *
   */
  void bringToFront() {
    //System.err.println("should be trying to bring frame to front, but not implemented yet.");
    //Pooka.getMainPanel().getParentFrame().setExtendedState(java.awt.Frame.ICONIFIED);
    //Pooka.getMainPanel().getParentFrame().setExtendedState(java.awt.Frame.NORMAL);
    Pooka.getMainPanel().getParentFrame().toFront();
    //Pooka.getUIFactory().bringToFront();
  }

  /**
   * Clears the status.
   */
  public synchronized void clearNewMessageFlag() {
    boolean doUpdate = mNewMessageFlag;
    mNewMessageFlag = false;
    mNewMessageCount = 0;
    mNewMessageMap = new HashMap();
    if (mTrayIcon != null) {
      mTrayIcon.setToolTip("Pooka: No new messages.");
      mTrayIcon.setCaption("Pooka: No new messages.");
    }
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
	  
	  if (mTrayIcon != null) {
	    mTrayIcon.setToolTip(fToolTip);
	    mTrayIcon.setCaption(fToolTip);
	    if (mShowNewMailMessage) {
	      mTrayIcon.displayMessage("New Messages", fDisplayMessage, TrayIcon.INFO_MESSAGE_TYPE);
	    } 
	    if (mBlinkNewMail) {
	      Runnable runMe = new Runnable() {
		  public void run() {
		    Runnable removeMe = new Runnable() {
			public void run() {
			  synchronized(MessageNotificationManager.this) {
			    if (mNewMessageFlag) {
			      if (getTrayIcon() != null)
				getTrayIcon().setIcon(null);
			    }
			  }
			}
		      };

		    Runnable showMe = new Runnable() {
			public void run() {
			  synchronized(MessageNotificationManager.this) {
			    if (getTrayIcon() != null)
			      getTrayIcon().setIcon(getNewMessageIcon());
			  }
			}
		      };
		    
		    try {
		      for (int i = 0; i < 3; i++) {
			SwingUtilities.invokeLater(removeMe);
			Thread.currentThread().sleep(1000);
			SwingUtilities.invokeLater(showMe);
			Thread.currentThread().sleep(1000);
		      }
		    } catch (Exception e) {
		      
		    }
		  }
		};

	      Thread blinkThread = new Thread(runMe);
	      blinkThread.setPriority(Thread.NORM_PRIORITY);
	      blinkThread.start();
	    }
	  }
	}
      });
    
  }
  
  /**
   * Removes a message from the new messages list.
   */
  public synchronized void removeFromNewMessages(MessageInfo pMessageInfo) {
    String folderId = pMessageInfo.getFolderInfo().getFolderID();
    Object newMessageList = mNewMessageMap.get(folderId);
    if (newMessageList != null && newMessageList instanceof List) {
      ((List) newMessageList).remove(pMessageInfo);
      mNewMessageCount --;
      if (mNewMessageCount == 0) 
	clearNewMessageFlag();
    }

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
   * Starts up the pooka main window.
   */
  void startMainWindow() {
    net.suberic.pooka.messaging.PookaMessageSender sender =  new net.suberic.pooka.messaging.PookaMessageSender();
    try {
      sender.openConnection();
      if (sender.checkVersion()) {
	sender.sendStartPookaMessage();
      }
    } catch (Exception exc) {
      if (mTrayIcon != null)
	mTrayIcon.displayMessage("Error", "Error sending new message:  " + exc, TrayIcon.WARNING_MESSAGE_TYPE);
    } finally {
      if (sender != null && sender.isConnected())
	sender.closeConnection();
    }
  }
  
  /**
   * Returns the actions for this component.
   */
  public Action[] getActions() {
    if (getMainPanel() == null)
      return mOfflineActions;
    else
      return mOnlineActions;
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
    //System.err.println("setting icon to " + newIcon.getImage() + " on " + getMainPanel());
    //if (getMainPanel() != null)
      //System.err.println("setting icon to " + newIcon.getImage() + " on " + getMainPanel().getParentFrame());
    if (getMainPanel() != null) 
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

  WindowAdapter mAdapter = null;
  /**
   * Sets the MainPanel for this MNM.
   */
  public void setMainPanel(MainPanel pPanel) {
    if (mPanel != pPanel) {
      if (pPanel != null) {
	pPanel.getParentFrame().removeWindowListener(mAdapter);
	mAdapter = null;
      }
      mPanel = pPanel;
      
      if (mPanel != null) {
	mAdapter = new WindowAdapter() {
	    public void windowActivated(WindowEvent e) {
	      clearNewMessageFlag();
	    }
	  };
	mPanel.getParentFrame().addWindowListener(mAdapter);
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		updateStatus();
	    }
	  });
      }
    }
    
    if (mTrayIcon != null)
      mTrayIcon.setPopupMenu(createPopupMenu());

    //System.err.println("mainPanel now = " + mPanel);
  }

  /**
   * Displays a message.  
   *
   * @param pMessage the message to display
   * @param pTitle the title of the display window
   * @param pType the type of message to display
   * @return true if the message is displayed, false otherwise.
   */
  public boolean displayMessage(String pTitle, String pMessage, int pType) {
    if (mTrayIcon != null) {
      mTrayIcon.displayMessage(pTitle, pMessage, pType);
      return true;
    } else {
      return false;
    }
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
      //System.err.println("sending new message.");
      net.suberic.pooka.messaging.PookaMessageSender sender =  new net.suberic.pooka.messaging.PookaMessageSender();
      try {
	sender.openConnection();
	if (sender.checkVersion()) {
	  sender.openNewEmail(null, null);
	}
      } catch (Exception exc) {
	if (mTrayIcon != null) 
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
      //System.err.println("opening message.");

      try {
	MessageProxy proxy = mMessageInfo.getMessageProxy();
	MessageUI mui = Pooka.getUIFactory().createMessageUI(proxy, new NewMessageFrame(new NewMessageProxy(new net.suberic.pooka.NewMessageInfo(new javax.mail.internet.MimeMessage(Pooka.getDefaultSession())))));
	mui.openMessageUI();
	// and if that works, remove it from the new message map.
	removeFromNewMessages(mMessageInfo);
      } catch (Exception ex) {
	ex.printStackTrace();
      }
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

  class PreferencesAction extends AbstractAction {
    
    PreferencesAction() {
      super("file-preferences");
    }
    
    public void actionPerformed(ActionEvent e) {
      //System.err.println("show preferences here.  :)");
    }
  }

  class StartPookaAction extends AbstractAction {
    
    StartPookaAction() {
      super("file-start");
    }
    
    public void actionPerformed(ActionEvent e) {
      if (getMainPanel() != null)
	bringToFront();
      else {
	startMainWindow();
      }
    }
  }

  class ExitPookaAction extends AbstractAction {
    
    ExitPookaAction() {
      super("file-exit");
    }
    
    public void actionPerformed(ActionEvent e) {
      if (getMainPanel() != null)
	getMainPanel().exitPooka(0);
      else
	Pooka.exitPooka(0, this);
    }
  }

}
