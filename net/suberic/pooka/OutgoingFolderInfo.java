package net.suberic.pooka;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * This class represents a folder which stores outgoing messages until they
 * can connect to the SMTP server.
 */
public class OutgoingFolderInfo extends FolderInfo {
  URLName transportURL;
  Thread sendMailThread = new net.suberic.util.thread.ActionThread(Pooka.getProperty("SendMailThread.name", "Send Mail Thread"));

  /**
   * Creates a new OutgoingFolderInfo for the given URLName.
   */
  public OutgoingFolderInfo(StoreInfo parent, String fname, URLName outgoingURL) {
    super(parent, fname);
    transportURL = outgoingURL;
  }

  /**
   * Sends all available messages.
   */
  public void sendAll() {
    
    Transport sendTransport = Pooka.getDefaultSession().getTransport(transportURL); 
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
  
}
