package net.suberic.pooka.gui;

import net.suberic.pooka.Pooka;
import net.suberic.util.*;
import net.suberic.util.thread.*;
import net.suberic.util.gui.propedit.*;
import javax.swing.*;
import java.util.*;

import javax.mail.MessagingException;


/**
 * This is effectively a wizard to run at startup if there's no UserProfile or
 * Store configured.
 */

public class NewAccountPooka {

  // this is such a mess.

  private MessagePanel messagePanel = null;
  private PropertyEditorManager manager = null;
  private PropertyEditorFactory factory = null;
  private String accountName = null;
  private Vector propertyList = new Vector();
  
  public NewAccountPooka() {
  };
  
  public NewAccountPooka(MessagePanel newMP) {
    setMessagePanel(newMP);
  }
  
  public void start() {
    
    /**
     * I really should make this easier to configure, shouldn't I?
     */
    
    // first set up the default connection.
    setupDefaultConnection();
    
    if (JOptionPane.showInternalConfirmDialog(getMessagePanel(), Pooka.getProperty("NewAccountPooka.introMessage", "Welcome to Pooka.\nIt seems that you don't yet have an Email account configured.\n\nWould you like to configure one now?"), Pooka.getProperty("NewAccountPooka.introMessage.title", "New Account Pooka"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
      
      PropertyEditorFactory factory = Pooka.getUIFactory().getEditorFactory();
      setFactory(factory);
      
      manager = new PropertyEditorManager(Pooka.getResources(), factory);
      manager.setWriteChanges(false);

      showFirstEntryWindow();
    }
  }
  
  public void showFirstEntryWindow() {
    /**
     * This takes the username, fullname, password, servername, and type 
     * (imap, etc.) and then passes it on to handleFirstEntry().
     */
    
    java.util.Vector propertyVector = new java.util.Vector();
    
    propertyVector.add("NewAccountPooka.firstPanel");
    
    JInternalFrame firstEntryWindow = new JInternalFrame(Pooka.getProperty("NewAccountPooka.entryWindowMessage.title", "Enter Email Account Information"), false, false, false, false);
    JComponent contentPane = (JComponent) firstEntryWindow.getContentPane();
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    
    // is there a way to make this wrap automatically, without adding
    // explicit newlines?
    JTextArea jta = new JTextArea(Pooka.getProperty("NewAccountPooka.entryWindowMessage", "Please enter the following \ninformation in order\nto configure your client."));
    jta.setEditable(false);

    JLabel jl = new JLabel("test");
    jta.setBackground(jl.getBackground());
    //jta.setForeground(jl.getForeground());
    jta.setFont(jl.getFont());
    contentPane.add(jta);
    
    contentPane.add(new PropertyEditorPane(manager,
					   propertyVector,
					   propertyVector,
					   firstEntryWindow));
    firstEntryWindow.pack();
    firstEntryWindow.show();
    firstEntryWindow.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
	
	public void internalFrameClosed(javax.swing.event.InternalFrameEvent e) {
	  SwingUtilities.invokeLater( new Runnable() {
	      public void run() {
		handleFirstEntry();
	      }
	    });
	}
      });
    

    getMessagePanel().add(firstEntryWindow);
    firstEntryWindow.setVisible(true);
    try {
      firstEntryWindow.setSelected(true);
    } catch (java.beans.PropertyVetoException pve) {
    }
    
  }
  
  public void handleFirstEntry() {
    /*
     * this converts the initial entires into an appropriate UserProfile
     * and Store entry.
     */
    String userName = manager.getProperty("NewAccountPooka.firstPanel.userName", "");
    String fullName = manager.getProperty("NewAccountPooka.firstPanel.fullName", "");
    String password = manager.getProperty("NewAccountPooka.firstPanel.password", "");
    String serverName = manager.getProperty("NewAccountPooka.firstPanel.serverName", "");
    String protocol = manager.getProperty("NewAccountPooka.firstPanel.protocol", "");
    String smtpServerName = manager.getProperty("NewAccountPooka.firstPanel.smtpServer", "");
    
    if (userName.equals("") || serverName.equals("") || protocol.equals("") || smtpServerName.equals(""))
      invalidFirstEntry();
    else {
      String accountName = userName + "@" + serverName;

      // set up the smtp server

      manager.setProperty("OutgoingServer", smtpServerName);
      manager.setProperty("OutgoingServer." + smtpServerName + ".server", smtpServerName);
      propertyList.add("OutgoingServer." + smtpServerName + ".server");
      manager.setProperty("OutgoingServer." + smtpServerName + ".connection", Pooka.getProperty("Pooka.connection.defaultName", "default"));
      propertyList.add("OutgoingServer." + smtpServerName + ".connection");

      manager.setProperty("OutgoingServer._default", smtpServerName);
      propertyList.add("OutgoingServer._default");

      // set up the user.

      manager.setProperty("UserProfile", accountName);
      propertyList.add("UserProfile." + accountName + ".mailHeaders.From");
      manager.setProperty("UserProfile." + accountName + ".mailHeaders.From", accountName);
      manager.setProperty("UserProfile." + accountName + ".mailHeaders.FromPersonal", fullName);
      propertyList.add("UserProfile." + accountName + ".mailHeaders.FromPersonal");
      manager.setProperty("UserProfile." + accountName + ".mailServer", smtpServerName);
      propertyList.add("UserProfile." + accountName + ".mailServer");

      manager.setProperty("UserProfile.default", accountName);
      propertyList.add("UserProfile.default");


      // set up mail server information

      manager.setProperty("Store", accountName);
      manager.setProperty("Store." + accountName + ".server", serverName);
      propertyList.add("Store." + accountName + ".server");
      manager.setProperty("Store." + accountName + ".protocol", protocol);
      propertyList.add("Store." + accountName + ".protocol");
      manager.setProperty("Store." + accountName + ".user", userName);
      propertyList.add("Store." + accountName + ".user");
      manager.setProperty("Store." + accountName + ".password", password);
      propertyList.add("Store." + accountName + ".password");
      manager.setProperty("Store." + accountName + ".defaultProfile", accountName);
      propertyList.add("Store." + accountName + ".defaultProfile");
      manager.setProperty("Store." + accountName + ".connection", Pooka.getProperty("Pooka.connection.defaultName", "default"));
      propertyList.add("Store." + accountName + ".connection");
      
      if (protocol.equalsIgnoreCase("POP3")) {
	manager.setProperty("OutgoingServer." + smtpServerName + ".sendOnConnect", "true");
	propertyList.add("OutgoingServer." + smtpServerName + ".sendOnConnect");
      } else {
	manager.setProperty("Store." + accountName + ".useSubscribed", "true");
	propertyList.add("Store." + accountName + ".useSubscribed");
      }

      Pooka.getUIFactory().showStatusMessage("Creating mail store " + smtpServerName + "...");

      saveProperties();
	
      Pooka.getUIFactory().showStatusMessage("Connecting to mailserver " + smtpServerName + "...");

      java.util.Vector allStores = Pooka.getStoreManager().getStoreList();
      net.suberic.pooka.StoreInfo si = null;
      if (allStores.size() > 0) {
	si = (net.suberic.pooka.StoreInfo) allStores.get(0);
      }
      
      if (si != null) {
	ActionThread thread = si.getStoreThread();
	final net.suberic.pooka.StoreInfo storeInfo = si;

	javax.swing.Action connectionAction = new javax.swing.AbstractAction() {
	    public void actionPerformed(java.awt.event.ActionEvent ae) {
	      try {
		storeInfo.connectStore();
		javax.swing.SwingUtilities.invokeLater( new Runnable() {

		    public void run() {
		      MailTreeNode mtn = null;
		      net.suberic.pooka.FolderInfo fi = storeInfo.getChild("INBOX");
		      if (fi != null) {
			mtn = fi.getFolderNode();
		      } else {
			mtn = storeInfo.getStoreNode();
		      }
		      if (mtn != null) {
			javax.swing.JTree folderTree = ((FolderPanel)mtn.getParentContainer()).getFolderTree();
			folderTree.scrollPathToVisible(new javax.swing.tree.TreePath(mtn.getPath()));
		      }
		      
		      Pooka.getUIFactory().clearStatus();
		      
		      showConfirmation();
		    }
		  });
	      } catch (MessagingException me) {
		final MessagingException error = me;
		javax.swing.SwingUtilities.invokeLater( new Runnable() {
		    
		    public void run() {
		      Pooka.getUIFactory().clearStatus();
		      StringBuffer errorMessage = new StringBuffer(Pooka.getProperty("error.NewAccountPooka.connectingToStore", "Failed to connect to store.  \nReceived the following error:\n"));
		      errorMessage.append(error.getMessage());
		      errorMessage.append("\n\n");
		      errorMessage.append(Pooka.getProperty("error.NewAccountPooka.continueMessage", "Would you like to re-enter your information?"));

		      JTextArea jta = new JTextArea(errorMessage.toString());
		      JLabel jl = new JLabel("test");
		      jta.setBackground(jl.getBackground());
		      jta.setFont(jl.getFont());

		      int continueResponse = Pooka.getUIFactory().showConfirmDialog(new Object[] { jta }, "Failed to connect to Store.", javax.swing.JOptionPane.OK_CANCEL_OPTION);
		      if (continueResponse == javax.swing.JOptionPane.OK_OPTION)
			showFirstEntryWindow();
		    }
		  });
	      }
	    }
	  };

	thread.addToQueue(connectionAction, new java.awt.event.ActionEvent(this, 0, "connectStore"));
      }
    }
  }
  
  public void showSecondEntryWindow() {
    // here we just do the SMTP server.
    
    PropertyEditorFactory factory = getFactory();
    
    // set the properties we're going to show
    java.util.Vector propertyVector = new java.util.Vector();
    //propertyVector.add("OutgoingServer." + getDefaultName() + ".server");
    
    // this is a hack, but now we add descriptions to each of these.
    
    JInternalFrame secondEntryWindow = new JInternalFrame(Pooka.getProperty("NewAccountPooka.secondWindowMessage.title", "Outgoing Email Server"), false, false, false, false);
    secondEntryWindow.getContentPane().setLayout(new BoxLayout(secondEntryWindow.getContentPane(), BoxLayout.Y_AXIS));
    secondEntryWindow.getContentPane().add(new JTextArea(Pooka.getProperty("NewAccountPooka.secondWindowMessage", "Please enter the URL for your outgoing email.")));
    secondEntryWindow.getContentPane().add(new PropertyEditorPane(manager, propertyVector, secondEntryWindow));
    secondEntryWindow.pack();
    secondEntryWindow.show();
    secondEntryWindow.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
	public void internalFrameClosed(javax.swing.event.InternalFrameEvent e) {
	  SwingUtilities.invokeLater(new Runnable() {
	      public void run() {
		handleSecondEntry();
	      }
	    } );
	}
      });		
    
    getMessagePanel().add(secondEntryWindow);
    secondEntryWindow.setVisible(true);
    try {
      secondEntryWindow.setSelected(true);
    } catch (java.beans.PropertyVetoException pve) {
    }
  }
  
  public void handleSecondEntry() {
    saveProperties();
    showConfirmation();
  }
  
    
  public void showConfirmation() {
    JOptionPane.showInternalMessageDialog(getMessagePanel(), Pooka.getProperty("NewAccountPooka.finishedMessage", "Email account configured!  If you need to make changes,\nor to add new accounts, go to the Configuration menu."), Pooka.getProperty("NewAccountPooka.finishedMessage.title", "Done!"), JOptionPane.INFORMATION_MESSAGE);
    
  }
  
  public void invalidFirstEntry() {
    StringBuffer errorMessage = new StringBuffer(Pooka.getProperty("error.NewAccountPooka.invalidEntry", "invalid first entry."));
    errorMessage.append("\n\n");
    errorMessage.append(Pooka.getProperty("error.NewAccountPooka.continueMessage", "Would you like to re-enter your information?"));
    
    JTextArea jta = new JTextArea(errorMessage.toString());
    JLabel jl = new JLabel("test");
    jta.setBackground(jl.getBackground());
    jta.setFont(jl.getFont());
    
    int continueResponse = Pooka.getUIFactory().showConfirmDialog(new Object[] { jta }, "Failed to connect to Store.", javax.swing.JOptionPane.OK_CANCEL_OPTION);
    if (continueResponse == javax.swing.JOptionPane.OK_OPTION)
      showFirstEntryWindow();
    
  }

  /**
   * Saves the tagged properties from our local VariableBundle to the 
   * main Pooka properties list.
   */
  public void saveProperties() {
    for (int i = 0; i < propertyList.size(); i++) {
      String propertyName = (String) propertyList.elementAt(i);
      transferProperty(propertyName);
    }
    // have to add these after the others, else we get an
    // exception.
    
    //if (!Pooka.getProperty("UserProfile", "").equals(""))
    //Pooka.setProperty("UserProfile", Pooka.getProperty("UserProfile", "") + ":" + getProperties().getProperty("UserProfile", ""));
    //else
    transferProperty("UserProfile");
    
    //if (!Pooka.getProperty("Store", "").equals(""))
    //Pooka.setProperty("Store", Pooka.getProperty("Store", "") + ":" + getProperties().getProperty("Store", ""));
    //else
    transferProperty("Store");

    //if (!Pooka.getProperty("OutgoingServer", "").equals(""))
    //Pooka.setProperty("OutgoingServer", Pooka.getProperty("OutgoingServer", "") + ":" + manager.getProperty("OutgoingServer", ""));
    //else
    transferProperty("OutgoingServer");
  }

  /**
   * Transfers the given property from our local PropertyEditorManager
   * main Pooka properties list.
   */
  public void transferProperty(String propertyName) {
    if (!(manager.getProperty(propertyName, "").equals(""))) {
      Pooka.setProperty(propertyName, manager.getProperty(propertyName, ""));
    }
  }

  /**
   * Sets up the default NetworkConnection.
   */
  private void setupDefaultConnection() {
    String defaultName = Pooka.getProperty("Pooka.connection.defaultName", "default");
    Pooka.setProperty("Connection." + defaultName + ".valueOnStartup", "Connected");
    Pooka.setProperty("Connection", defaultName);
    Pooka.setProperty("Connection._default", defaultName);
  }
  
  
  public MessagePanel getMessagePanel() {
    return messagePanel;
  }
  
  public void setMessagePanel(MessagePanel newValue) {
    messagePanel=newValue;
  }
  
  public PropertyEditorFactory getFactory() {
    return factory;
  }
  
  public void setFactory(PropertyEditorFactory newValue) {
    factory=newValue;
  }
  
}
