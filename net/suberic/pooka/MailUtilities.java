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

    /**
     * This takes a String and words wraps it at length wrapLength, 
     * using lineBreak to indicate line breaks.
     */
    public static String wrapText(String originalText, int wrapLength, char lineBreak) {
	if (originalText == null || originalText.length() < wrapLength)
	    return originalText;

	StringBuffer wrappedText = new StringBuffer(originalText);

	int lastSpace=-1;
	char currentChar;
	for (int caret = 0, lineLocation = 0; caret < wrappedText.length(); caret++, lineLocation++) {
	    currentChar = wrappedText.charAt(caret);
	    if (currentChar == lineBreak) {
		lastSpace=-1;
		lineLocation=0;
	    } else if (Character.isWhitespace(currentChar)) {
		lastSpace=caret;
	    } 

	    if (lineLocation >= wrapLength) {
		if (lastSpace < 0) {
		    // no spaces in the line.  truncate here.
		    wrappedText.insert(caret, lineBreak);
		    lineLocation = -1;
		} else {
		    // for now, let's just break after lastSpace.
		    wrappedText.insert(lastSpace + 1, lineBreak);
		    caret++;
		    lineLocation = caret - lastSpace;
		} 
	    }
	}
	
	return wrappedText.toString();
    } 

    /**
     * A convenience method which wraps the given string using the
     * length specified by Pooka.lineLength.
     */
    public static String wrapText(String originalText) {
	int wrapLength;
	try {
	    String wrapLengthString = Pooka.getProperty("Pooka.lineLength");
	    wrapLength = Integer.parseInt(wrapLengthString);
	} catch (Exception e) {
	    wrapLength = 72;
	}

	return wrapText(originalText, wrapLength, '\n');
    }
}
