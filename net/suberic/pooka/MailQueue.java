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

    public void sendMessage(Message m, URLName transportURL) {
	System.out.println("MailQueue sending message at url " + transportURL);
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

    public void sendQueued() {
	Enumeration keys = transportTable.keys();
	URLName transportURL;
	Transport sendTransport;
	Vector sendQueue;
	Message msg;
	boolean error = false;
	
	while (keys.hasMoreElements()) {
	    transportURL = (URLName)keys.nextElement();
	    try {
		sendQueue = (Vector)transportTable.get(transportURL);
		
		if (!(sendQueue.isEmpty())) {
		    sendTransport = session.getTransport(transportURL); 
		    sendTransport.connect();
		    
		    for (int i=sendQueue.size()-1 ; i >= 0; i--) {
			msg = (Message)sendQueue.elementAt(i);
			try {
			    sendTransport.sendMessage(msg, msg.getAllRecipients());
			    sendQueue.removeElementAt(i);
			} catch (SendFailedException sfe) {
			    
			    System.out.println(sfe.getMessage());
			    error = true;
			} catch (MessagingException me) {
			    error = true;
			}
		    }
		}
	    } catch (NoSuchProviderException nspe) {
	    } catch (MessagingException  me ) {
	    }
	}
	if (error) 
	    System.out.println("Error!\n");
    }
}
    

