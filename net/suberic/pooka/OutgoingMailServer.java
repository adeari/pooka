package net.suberic.pooka;

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
    propertyName = "OutgoingMailServer." + newId;
  }

  /**
   * <p>Configures this mail server.</p>
   */
  protected void configure() {
    VariableBundle bundle = Pooka.getResources();

    connectionID = bundle.getProperty(getItemProperty() + ".connectionID", "");
    outboxID = bundle.getProperty(getItemProperty() + ".outboxID", "");

    sendMailURL = new URLName(bundle.getProperty(getItemProperty() + ".sendMailURL", ""));
    
  }

  /**
   * Sends all available messages.
   */
  /*
  public void sendAll() throws javax.mail.MessagingException {

    NetworkConnection connection = getConnection();

    if (connection.getStatus() == NetworkConnection.DOWN) 
      FIXME

    Transport sendTransport = Pooka.getDefaultSession().getTransport(sendMailURL); 
    try {
      sendTransport.connect();
      
      Message[] msgs = getFolder().getMessages();    
      
      try {
	for (int i = 0; i < msgs.length; i++) {
	  Message m = msgs[i];
	  if (! m.isSet(Flags.Flag.DRAFT)) {
	    sendTransport.sendMessage(m, m.getAllRecipients());
	    m.setFlag(Flags.Flag.DELETED, true);
	  }
	}
      } finally {
	getFolder().expunge();
      }
    } finally {
      sendTransport.close();
    }
  }
  */

  /**
   * Virtually sends a message.  If the current status is connected, then
   * the message will actually be sent now.  If not, and the 
   * Message.sendImmediately setting is true, then we'll attempt to send
   * the message anyway.  
   */
  /*
  public void sendMessage(NewMessageInfo nmi, boolean connect) throws javax.mail.MessagingException {
    this.appendMessages(new MessageInfo[] { nmi });
    if (online)
      sendAll();
    else if (connect || Pooka.getProperty("Message.sendImmediately", "false").equalsIgnoreCase("true")) {
      try {
	connect();
	if (online)
	  sendAll();
      } catch (MessagingException me) {
	System.err.println("me is a " + me);
	online = false;
      }
    }
  }
  */
  
  /**
   * Virtually sends a message.  If the current status is connected, then
   * the message will actually be sent now.  
   */
  /*
  public void sendMessage(NewMessageInfo nmi) throws javax.mail.MessagingException {
    sendMessage(nmi, false);
  }
  */

  /**
   * <p>The NetworkConnection that this OutgoingMailServer depends on.
   */
  public NetworkConnection getConnection() {
    /*
    NetworkConnectionManager connectionManager = Pooka.getConnectionManager();
    NetworkConnection returnValue = connectionManager.getConnection(connectionID);
    if (returnValue != null)
      return returnValue;
    else
      return connectionManager.getDefaultConnection();
    */
    return null;
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
