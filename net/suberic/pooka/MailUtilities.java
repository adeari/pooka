package net.suberic.pooka;

import java.util.Vector;
import javax.mail.*;
import javax.mail.internet.*;

public class MailUtilities {
    public MailUtilities() {
    }

    /**
     * This parses the message given into an AttachmentBundle.
     */
    private static AttachmentBundle parseAttachments(Message m) {
	AttachmentBundle bundle = new AttachmentBundle();
	try {
	    Object content = m.getContent();

	    if (content instanceof Multipart) {
		bundle.addAll(parseAttachments((Multipart)content));
	    } else if (content instanceof String) {
		bundle.textPart = new StringBuffer((String)content);
		bundle.allText.append(content);
	    }
	} catch (Exception e) {
	}
	return bundle;
    }

    /**
     * This parses a Mulitpart object into an AttachmentBundle.
     */
    private static AttachmentBundle parseAttachments(Multipart mp) {
	AttachmentBundle bundle = new AttachmentBundle();
	try {
	    for (int i = 0; i < mp.getCount(); i++) {
		MimeBodyPart mbp = (MimeBodyPart)mp.getBodyPart(i);
		ContentType ct = new ContentType(mbp.getContentType());
		if (ct.getPrimaryType().equalsIgnoreCase("text") && ct.getSubType().equalsIgnoreCase("plain")) {
		    if (bundle.textPart == null) {
			bundle.textPart = new StringBuffer((String)mbp.getContent());
		    } else {
			bundle.textAttachments.add(mbp);
			bundle.allAttachments.add(mbp);
		    }
		    bundle.allText.append((String)mbp.getContent());

		} else if (ct.getPrimaryType().equalsIgnoreCase("multipart")) {
		    bundle.addAll(parseAttachments((Multipart)mbp.getContent()));
		} else if (ct.getPrimaryType().equalsIgnoreCase("Message")) {
		    bundle.nonTextAttachments.add(mbp);
		    bundle.allAttachments.add(mbp);
		    bundle.addAll(parseAttachments((Message)mbp.getContent()));
		} else {
		    bundle.nonTextAttachments.add(mbp);
		    bundle.allAttachments.add(mbp);
		}
	    }
	} catch (Exception e) {
	    System.out.println("caught exception.");
	    e.printStackTrace();
	}
	
	return bundle;
    }

    /**
     * This gets the Text part of a message.  This is useful if you want
     * to display just the 'body' of the message without the attachments.
     */
    public static String getTextPart(Message m) {
	AttachmentBundle bundle = parseAttachments(m);
	return bundle.textPart.toString();
    }

    /**
     * This method returns all of the attachments marked as 'inline' which
     * are also of text or message/rfc822 types.
     */
    public static Vector getInlineTextAttachments(Message m) {
	AttachmentBundle bundle = parseAttachments(m);
	return bundle.textAttachments;
    }


    /**
     * This returns the Attachments (basically, all the Parts in a Multipart
     * except for the main body of the message).
     */
    public static Vector getAttachments(Message m) {
	AttachmentBundle bundle = parseAttachments(m);
	return bundle.allAttachments;
    }

    /**
     * This method returns the Message Text plus the text inline attachments.
     * The attachments are separated by the separator flag.
     */

    public static String getTextAndTextInlines(Message m, String separator) {
	StringBuffer returnValue = null;
	String retString = MailUtilities.getTextPart(m);
	if (retString != null && retString.length() > 0)
	    returnValue = new StringBuffer(retString);
	else
	    returnValue = new StringBuffer();

	Vector attachments = MailUtilities.getInlineTextAttachments(m);
	if (attachments != null && attachments.size() > 0) {
	    for (int i = 0; i < attachments.size(); i++) {
		try {
		    Object content = ((MimeBodyPart)attachments.elementAt(i)).getContent();
		    returnValue.append(separator);
		    if (content instanceof MimeMessage)
			returnValue.append(getTextAndTextInlines((MimeMessage)content, separator));
		    else
			returnValue.append(content);
		} catch (Exception e) {
	    System.out.println("caught exception.");
	    e.printStackTrace();

		    // if we get an exception getting the content, just
		    // ignore the attachment.
		}
	    }
	}

	return returnValue.toString();
    }

    /**
     * This returns the Attachments (basically, all the Parts in a Multipart
     * except for the main body of the message).
     */
    public static Vector getAttachments(Multipart mp) {
	AttachmentBundle bundle = parseAttachments(mp);
	return bundle.allAttachments;
    }

    /**
     * This method takes a given character array and returns the offset
     * position at which a line break should occur.
     *
     * If no break is necessary, the <code>finish</code> value is returned.
     * 
     */

    public static int getBreakOffset(String buffer, int breakLength) {
	if ( buffer.length() <= breakLength ) {
	    return buffer.length();
	}

	int breakLocation = -1;
	for (int caret = breakLength; breakLocation == -1 && caret >= 0; caret--) {
	    if (Character.isWhitespace(buffer.charAt(caret))) {
		breakLocation=caret + 1;
	    } 
	}

	if (breakLocation == -1)
	    breakLocation = breakLength;

	return breakLocation;
    }

    /**
     * This takes a String and words wraps it at length wrapLength.
     */
    public static String wrapText(String originalText, int wrapLength, char lineBreak) {
	if (originalText == null)
	    return null;

	StringBuffer wrappedText = new StringBuffer(originalText);

	int nextReal = -1;
	int lastReal = -1;
	int newBreak = -1;
	while (nextReal < wrappedText.length()) {
	    nextReal= indexOf(wrappedText, lineBreak, lastReal +1);
	    if (nextReal == -1)
		nextReal = wrappedText.length();
	    while ( newBreak < nextReal ) {
		newBreak = getBreakOffset(wrappedText.substring(lastReal +1, nextReal), wrapLength) + lastReal + 1;
		if (newBreak < nextReal) {
		    wrappedText.insert(newBreak, lineBreak); 
		    nextReal++;
		    lastReal = newBreak + 1;
		} else {
		    lastReal = nextReal;
		    newBreak = nextReal;
		}
	    }
	}
	
	return wrappedText.toString();
    } 

    
    /**
     * This just acts as an indexOf on a StringBuffer.
     */
    public static int indexOf(StringBuffer buffer, char toFind, int start) {
	for (int i = start; i < buffer.length(); i++) {
	    if (toFind == buffer.charAt(i))
		return i;
	}

	return -1;
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
