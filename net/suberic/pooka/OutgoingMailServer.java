package net.suberic.pooka;

import java.util.LinkedList;

import javax.mail.*;
import javax.mail.internet.*;

import net.suberic.util.*;

/**
 * <p>Represents a mail server than can be used to send outgoing messages.
 *
 * @author Allen Petersen
 * @version $Revision$
 */
public class OutgoingMailServer implements net.suberic.util.Item {

  String id = null;

  String propertyName = null;

  URLName sendMailURL = null;

  String connectionID = null;

  String outboxID = null;

  /**
   * <p>Creates a new OutgoingMailServer from the given property.</p>
   */
  public OutgoingMailServer (String newId) {
    id = newId;
    propertyName = "OutgoingServer." + newId;
    
    configure();
  }

  /**
   * <p>Configures this mail server.</p>
   */
  protected void configure() {
    VariableBundle bundle = Pooka.getResources();

    connectionID = bundle.getProperty(getItemProperty() + ".connection", "");
    sendMailURL = new URLName("smtp://" + bundle.getProperty(getItemProperty() + ".server", "") + "/");

    outboxID = bundle.getProperty(getItemProperty() + ".outbox", "");

  }

  /**
   * Loads the outbox folders.
   */
  public void loadOutboxFolder() {
    FolderInfo outbox = getOutbox();
    if (outbox != null)
      outbox.setOutboxFolder(true);

  }

  /**
   * Sends all available messages in the outbox.
   */
  public void sendAll() throws javax.mail.MessagingException {

    Transport sendTransport = prepareTransport(true);

    try {
      sendTransport.connect();
      
      sendAll(sendTransport);
    } finally {
      sendTransport.close();
    }
  }
  
  /**
   * Sends all available messages in the outbox using the given, already 
   * open Transport object.  Leaves the Transport object open.
   */
  private void sendAll(Transport sendTransport) throws javax.mail.MessagingException {
    
    LinkedList exceptionList = new LinkedList();

    FolderInfo outbox = getOutbox();
    
    Message[] msgs = outbox.getFolder().getMessages();    
    
    try {
      for (int i = 0; i < msgs.length; i++) {
	Message m = msgs[i];
	if (! m.isSet(Flags.Flag.DRAFT)) {
	  try {
	    sendTransport.sendMessage(m, m.getAllRecipients());
	    m.setFlag(Flags.Flag.DELETED, true);
	  } catch (MessagingException me) {
	    exceptionList.add(me);
	  }
	}
      }
    } finally {
      if (exceptionList.size() > 0) {
	final int exceptionCount = exceptionList.size();
	javax.swing.SwingUtilities.invokeLater( new Runnable() {
	    public void run() {
	      Pooka.getUIFactory().showError(Pooka.getProperty("error.OutgoingServer.queuedSendFailed", "Failed to send message(s) in the Outbox.  Number of errors:  ") +  exceptionCount );
	    }
	  } );
      }
      outbox.getFolder().expunge();
    }
  }

  /**
   * Virtually sends a message.  If the current status is connected, then
   * the message will actually be sent now.  If not, and the 
   * Message.sendImmediately setting is true, then we'll attempt to send
   * the message anyway.  
   */
  public void sendMessage(NewMessageInfo nmi, boolean connect) throws javax.mail.MessagingException {
    
    boolean connected = false;
    Transport sendTransport = null;
    try {
      sendTransport = prepareTransport(connect);
      sendTransport.connect();
      connected = true;
    } catch (MessagingException me) {
      // if the connection/mail transport isn't available.
      FolderInfo outbox = getOutbox();
      
      if (outbox != null) {
	try {
	  outbox.appendMessages(new MessageInfo[] { nmi });

	javax.swing.SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
	      Pooka.getUIFactory().showError(Pooka.getProperty("error.MessageWindow.sendDelayed", "Connection unavailable.  Message saved to Outbox."));
	    }
	  });
	((net.suberic.pooka.gui.NewMessageProxy)nmi.getMessageProxy()).sendSucceeded();
	} catch(MessagingException nme) {
	  ((net.suberic.pooka.gui.NewMessageProxy)nmi.getMessageProxy()).sendFailed(nme);	  
	}
      } else {
	MessagingException nme = new MessagingException("Connection unavailable, and no Outbox specified.");
	((net.suberic.pooka.gui.NewMessageProxy)nmi.getMessageProxy()).sendFailed(nme);	  
      }
      
    }

    // if the connection worked.
    if (connected) {
      try {
	try {
	  Message m = nmi.getMessage();
	  sendTransport.sendMessage(m, m.getAllRecipients());
	  ((net.suberic.pooka.gui.NewMessageProxy)nmi.getMessageProxy()).sendSucceeded();
	} catch (MessagingException me) {
	  ((net.suberic.pooka.gui.NewMessageProxy)nmi.getMessageProxy()).sendFailed(me);	  
	}
	// whether or not the send failed. try sending all the other messages,
	// in the queue, too.
	sendAll(sendTransport);
      } finally {
	sendTransport.close();
      }
    }
  }
  
  /**
   * Virtually sends a message.  If the current status is connected, then
   * the message will actually be sent now.  
   */
  public void sendMessage(NewMessageInfo nmi) throws javax.mail.MessagingException {
    sendMessage(nmi, false);
  }

  /**
   * Gets a Transport object for this OutgoingMailServer.
   */
  protected Transport prepareTransport(boolean connect) throws javax.mail.MessagingException {
    
    NetworkConnection connection = getConnection();
    
    if (connect) {
      if (connection.getStatus() == NetworkConnection.DISCONNECTED) {
	connection.connect();
      }
    }
    
    if (connection.getStatus() != NetworkConnection.CONNECTED) {
      throw new MessagingException(Pooka.getProperty("error.connectionDown", "Connection down for Mail Server:  ") + getItemID());
    }
    
    Transport sendTransport = Pooka.getDefaultSession().getTransport(sendMailURL); 
    return sendTransport;
  }

  /**
   * <p>The NetworkConnection that this OutgoingMailServer depends on.
   */
  public NetworkConnection getConnection() {
    NetworkConnectionManager connectionManager = Pooka.getConnectionManager();
    NetworkConnection returnValue = connectionManager.getConnection(connectionID);
    if (returnValue != null)
      return returnValue;
    else
      return connectionManager.getDefaultConnection();
  }

  /**
   * <p>The FolderInfo where messages for this MailServer are
   * stored until they're ready to be sent.
   */
  public FolderInfo getOutbox() {
    
    return Pooka.getStoreManager().getFolder(outboxID);
  }

  /**
   * <p>The Item ID for this OutgoingMailServer.</p>
   */
  public String getItemID() {
    return id;
  }

  /**
   * <p>The Item property for this OutgoingMailServer.  This is usually
   * OutgoingMailServer.<i>itemID</i>.</p>
   */
  public String getItemProperty() {
    return propertyName;
  }

}
