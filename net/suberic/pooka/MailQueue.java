package net.suberic.pooka;
import net.suberic.pooka.gui.*;
import javax.mail.*;
import java.util.*;

public class MailQueue {
    Hashtable transportTable;
    Session session;
    
    public MailQueue(Session newSession) {
	session = newSession;
	transportTable = new Hashtable();
    }

    /**
     * This adds the Message to the queue associated with the given 
     * transportURL.  If Message.sendImmediately is set to true, then this
     * will also go ahead and try to send all queued Messages using
     * sendQueued().
     */
    public void sendMessage(Message m, URLName transportURL) throws MessagingException {
	Vector transportQueue = (Vector)transportTable.get(transportURL);
	if (transportQueue == null) {
	    transportQueue = new Vector();
	    transportTable.put(transportURL, transportQueue);
	}
	
	transportQueue.add(m);
	if (Pooka.getProperty("Message.sendImmediately", "false").equals("true")) {
	    sendQueued();
	}

    }

    /**
     * This will try to send all the Messages currently in the queue.  If 
     * it encounters any major errors (i.e being unable to connect to the
     * mailserver), it will more or less exit out, throwing a 
     * MessagingException saying why it failed.  If it manages to send some
     * Messages but not others, it will send the ones that it can, and return
     * a MessagingException with all the individual sub-exceptions tacked
     * on to it using MessagingException.setNextException().
     */
    public void sendQueued() throws MessagingException {
	Enumeration keys = transportTable.keys();
	URLName transportURL;
	Transport sendTransport;
	Vector sendQueue;
	Message msg;
	boolean error = false;
	
	while (keys.hasMoreElements()) {
	    transportURL = (URLName)keys.nextElement();
	    sendQueue = (Vector)transportTable.get(transportURL);
	    
	    if (!(sendQueue.isEmpty())) {
		MessagingException returnException = null;
		sendTransport = session.getTransport(transportURL); 
		sendTransport.connect();
		
		for (int i=sendQueue.size()-1 ; i >= 0; i--) {
		    msg = (Message)sendQueue.elementAt(i);
		    try {
			sendTransport.sendMessage(msg, msg.getAllRecipients());
			sendQueue.removeElementAt(i);
		    } catch (SendFailedException sfe) {
			if (returnException == null) 
			    returnException = sfe;
			else
			    returnException.setNextException(sfe);
		    } catch (MessagingException me) {
			if (returnException == null)  
			    returnException = me;
			else
			    returnException.setNextException(me);
		    }
		}
		if (returnException != null)
		    throw returnException;
	    }
	}
    }
}
    

