package net.suberic.pooka;

import java.util.Vector;
import javax.mail.*;
import javax.mail.internet.*;

public class MailUtilities {
    public MailUtilities() {
    }

    /**
     * This gets the Text part of a message.  This is useful if you want
     * to display just the 'body' of the message without the attachments.
     */
    public static String getTextPart(Message m) {
	try {
	    Object content = m.getContent();

	    if (content instanceof String)
		return (String)content;
	    else if (content instanceof MimeMultipart) {
		MimeMultipart mmp = (MimeMultipart)content;
		for (int i = 0; i < mmp.getCount(); i++) {
		    MimeBodyPart mbp = (MimeBodyPart)mmp.getBodyPart(i);
		    if (mbp.getContentType().regionMatches(true, 0, "text", 0, 4)) 
			return (String)(mbp.getContent());
		}
	    } 
	} catch (Exception e) {
	    // with any excpetion, we just return null.  i think that's
	    // safe for now.
	}

	return null;

    }

    /**
     * This returns the Attachments (basically, all the Parts in a Multipart
     * except for the main body of the message).
     */
    public static Vector getAttachments(Message m) {
	try {
	    Object content = m.getContent();

	    if (content instanceof Multipart) {
		Multipart mp = (Multipart)content;
		boolean textFound = false;
		Vector attachments = new Vector();
		for (int i = 0; i < mp.getCount(); i++) {
		    MimeBodyPart mbp = (MimeBodyPart)mp.getBodyPart(i);
		    if (textFound == false) {
			if (mbp.getContentType().regionMatches(true, 0, "text", 0, 4)) {
			    textFound = true;
			} else {
			    attachments.add(mbp);
			}
		    } else
			attachments.add(mbp);
		}

		return attachments;
	    }
	} catch (Exception e) {
	    // with any excpetion, we just return null.  i think that's
	    // safe for now.
	}
	
	return null;
	
    }
}
