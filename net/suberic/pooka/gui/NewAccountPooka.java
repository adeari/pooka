package net.suberic.pooka.gui;

import net.suberic.pooka.Pooka;
import net.suberic.util.*;
import net.suberic.util.gui.*;
import javax.swing.*;
import java.util.*;


/**
 * This is effectively a wizard to run at startup if there's no UserProfile or
 * Store configured.
 */

public class NewAccountPooka {

    private MessagePanel messagePanel = null;
    private VariableBundle properties = null;
    private PropertyEditorFactory factory = null;
    private String accountName = null;

    public NewAccountPooka() {
    };

    public NewAccountPooka(MessagePanel newMP) {
	setMessagePanel(newMP);
    }

    public void start() {

	/**
	 * I really should make this easier to configure, shouldn't I?
	 */

	if (JOptionPane.showInternalConfirmDialog(getMessagePanel(), Pooka.getProperty("NewAccountPooka.introMessage", "Welcome to Pooka.\nIt seems that you don't yet have an Email account configured.\n\nWould you like to configure one now?"), Pooka.getProperty("NewAccountPooka.introMessage.title", "New Account Pooka"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

	    showFirstEntryWindow();
	}
    }

    public void showFirstEntryWindow() {
	/**
	 * This takes the username, fullname, password, servername, and type 
	 * (imap, etc.) and then passes it on to handleFirstEntry().
	 */

	setProperties(initializeProperties());
	PropertyEditorFactory factory = new PropertyEditorFactory(getProperties());
	setFactory(factory);
	java.util.Vector propertyVector = new java.util.Vector();
	propertyVector.add("NewAccountPooka.userName");
	propertyVector.add("NewAccountPooka.fullName");
	propertyVector.add("NewAccountPooka.password");
	propertyVector.add("NewAccountPooka.serverName");
	propertyVector.add("NewAccountPooka.type");

	JInternalFrame firstEntryWindow = new JInternalFrame(Pooka.getProperty("NewAccountPooka.entryWindowMessage.title", "Enter Email Account Information"), false, false, false, false);
        JComponent contentPane = 
          (JComponent) firstEntryWindow.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        // is there a way to make this wrap automatically, without adding
        // explicit newlines?
	contentPane.add(new JTextArea(Pooka.getProperty("NewAccountPooka.entryWindowMessage", "Please enter the following \ninformation in order\nto configure your client.")));

	contentPane.add(new PropertyEditorPane(factory,
                                               propertyVector,
                                               firstEntryWindow));

	firstEntryWindow.pack();
	firstEntryWindow.show();
	firstEntryWindow.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {

		public void internalFrameClosed(javax.swing.event.InternalFrameEvent e) {
		    handleFirstEntry();
		}
	    });

	getMessagePanel().add(firstEntryWindow);
	firstEntryWindow.setVisible(true);
	try {
	    firstEntryWindow.setSelected(true);
	} catch (java.beans.PropertyVetoException pve) {
	}
    }

    public VariableBundle initializeProperties() {
	/*
	 * this sets up the tempoary VariableBundle which we'll use during
	 * this wizard.
	 */

	Enumeration en = Pooka.getResources().getPropertyAsEnumeration("NewAccountPooka", "introMessage:introMessage.title:entryWindowMessage:entryWindowMessage.title:userName:fullName:password:serverName:type");
	VariableBundle vb = new VariableBundle(null, null, null);
	while (en.hasMoreElements()) {
	    String property = "NewAccountPooka." + (String)en.nextElement();
	    String propertyValue = Pooka.getProperty(property, "");
	    if (!(propertyValue.equals("")))
		vb.setProperty(property, propertyValue);
	    String labelValue = Pooka.getProperty(property+".label", "");
	    if (!(labelValue.equals("")))
		vb.setProperty(property + ".label", labelValue);
	    String propertyTypeValue =
              Pooka.getProperty(property + ".propertyType", "");
	    if (!(propertyTypeValue.equals("")))
		vb.setProperty(property + ".propertyType", propertyTypeValue);
	    String allowedValuesValue =
              Pooka.getProperty(property + ".allowedValues", "");
	    if (!(allowedValuesValue.equals("")))
		vb.setProperty(property + ".allowedValues", allowedValuesValue);
	}
	return vb;
    }


    public void handleFirstEntry() {
	/*
	 * this converts the initial entires into an appropriate UserProfile
	 * and Store entry.
	 */
	VariableBundle vb = getProperties();
	String userName = vb.getProperty("NewAccountPooka.userName", "");
	String fullName = vb.getProperty("NewAccountPooka.fullName", "");
	String password = vb.getProperty("NewAccountPooka.password", "");
	String serverName = vb.getProperty("NewAccountPooka.serverName", "");
	String type = vb.getProperty("NewAccountPooka.type", "");
	if (vb.getProperty("NewAccountPooka.userName", "").equals(""))
	    invalidFirstEntry();
	else {
	    String accountName = userName + "@" + serverName;
	    setAccountName(accountName);

	    vb.setProperty("UserProfile", accountName);
	    vb.setProperty("UserProfile." + accountName + ".mailHeaders.From", accountName);
	    vb.setProperty("UserProfile." + accountName + ".mailHeaders.FromPersonal", fullName);
	    vb.setProperty("UserProfile." + accountName + ".sendMailURL", "smtp://" + serverName + "/");
	    vb.setProperty("Store", accountName);
	    vb.setProperty("Store." + accountName + ".server", serverName);
	    vb.setProperty("Store." + accountName + ".protocol", type);
	    vb.setProperty("Store." + accountName + ".user", userName);
	    vb.setProperty("Store." + accountName + ".password", password);
	    vb.setProperty("Store." + accountName + ".defaultProfile", accountName);
	    vb.setProperty("UserProfile.default", accountName);

	    showSecondEntryWindow();
	}
    }

    public void showSecondEntryWindow() {
	// here we just do the SMTP server.

	PropertyEditorFactory factory = getFactory();

	// set the properties we're going to show
	java.util.Vector propertyVector = new java.util.Vector();
	propertyVector.add("UserProfile." + getAccountName() + ".sendMailURL");

	// this is a hack, but now we add descriptions to each of these.

	getProperties().setProperty("UserProfile." + getAccountName() + ".sendMailURL.label", Pooka.getProperty("NewAccountPooka.sendMailURL.label", "URL for outgoing mail."));

	JInternalFrame secondEntryWindow = new JInternalFrame(Pooka.getProperty("NewAccountPooka.secondWindowMessage.title", "Outgoing Email Server"), false, false, false, false);
	secondEntryWindow.getContentPane().setLayout(new BoxLayout(secondEntryWindow.getContentPane(), BoxLayout.Y_AXIS));
	secondEntryWindow.getContentPane().add(new JTextArea(Pooka.getProperty("NewAccountPooka.secondWindowMessage", "Please enter the URL for your outgoing email.")));
	secondEntryWindow.getContentPane().add(new PropertyEditorPane(factory, propertyVector, secondEntryWindow));
	secondEntryWindow.pack();
	secondEntryWindow.show();
	secondEntryWindow.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
		public void internalFrameClosed(javax.swing.event.InternalFrameEvent e) {
		    handleSecondEntry();
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
	
	String accountName = getAccountName();

	transferProperty("UserProfile." + accountName + ".mailHeaders.From");
	transferProperty("UserProfile." + accountName + ".mailHeaders.FromPersonal");
	transferProperty("UserProfile." + accountName + ".sendMailURL");
	transferProperty("Store." + accountName + ".server");
	transferProperty("Store." + accountName + ".protocol");
	transferProperty("Store." + accountName + ".user");
	transferProperty("Store." + accountName + ".password");
	transferProperty("Store." + accountName + ".defaultProfile");

	// have to add these after the others, else we get an
	// exception.

	if (!Pooka.getProperty("UserProfile", "").equals(""))
	    Pooka.setProperty("UserProfile", Pooka.getProperty("UserProfile", "") + ":" + getProperties().getProperty("UserProfile", ""));
	else
	    transferProperty("UserProfile");

	if (!Pooka.getProperty("Store", "").equals(""))
	    Pooka.setProperty("Store", Pooka.getProperty("Store", "") + ":" + getProperties().getProperty("Store", ""));
	else
	    transferProperty("Store");

	showConfirmation();
    }

    public void showConfirmation() {
	JOptionPane.showInternalMessageDialog(getMessagePanel(), Pooka.getProperty("NewAccountPooka.finishedMessage", "Email account configured!  If you need to make changes,\ngo to Edit->User Configuration or Edit->Mailbox Configuration."), Pooka.getProperty("NewAccountPooka.finishedMessage.title", "Done!"), JOptionPane.INFORMATION_MESSAGE);

    }

    public void invalidFirstEntry() {
	System.out.println("invalid first entry.");
    }

    public void transferProperty(String propertyName) {
	if (!(getProperties().getProperty(propertyName, "").equals(""))) {
	    Pooka.setProperty(propertyName, getProperties().getProperty(propertyName, ""));
	}
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

    public VariableBundle getProperties() {
	return properties;
    }

    public void setProperties(VariableBundle newValue) {
	properties=newValue;
    }

    public String getAccountName() {
	return accountName;
    }

    public void setAccountName(String newValue) {
	accountName=newValue;
    }
}
