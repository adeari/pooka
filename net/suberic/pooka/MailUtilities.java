package net.suberic.pooka;

import java.util.Vector;
import java.util.StringTokenizer;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class MailUtilities {
    public MailUtilities() {
    }

    /**
     * This parses the message given into an AttachmentBundle.
     */
    private static AttachmentBundle parseAttachments(Message m, boolean showFullHeaders, boolean withHeaders) throws MessagingException {
	AttachmentBundle bundle = new AttachmentBundle();

	Object content = null;
	try {
	    content = m.getContent();
	} catch (UnsupportedEncodingException uee) {
	    try {
		/**
		 * Just read the InputStream directly into a byte array and
		 * hope for the best.  :)
		 */
		InputStream is = ((MimeMessage)m).getInputStream();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int b;
		while ((b = is.read()) != -1)
		    bos.write(b);
		byte[] barray = bos.toByteArray();
		content = new String(barray, Pooka.getProperty("Pooka.defaultCharset", "iso-8859-1"));
	    } catch (IOException ioe) {
		throw new MessagingException (Pooka.getProperty("error.Message.loadingAttachment", "Error loading attachment"), ioe);
	    }
	} catch (IOException ioe) {
	    throw new MessagingException (Pooka.getProperty("error.Message.loadingAttachment", "Error loading attachment"), ioe);
	}
	
	if (content instanceof Multipart) {
	    ContentType ct = new ContentType(((Multipart)content).getContentType());
	    
	    if (ct.getSubType().equalsIgnoreCase("alternative")) {
		Multipart mp = (Multipart)content;
		for (int i = 0; i < mp.getCount(); i++) {
		    MimeBodyPart mbp = (MimeBodyPart)mp.getBodyPart(i);
		    ContentType ct2 = new ContentType(mbp.getContentType());
		    if (ct2.getPrimaryType().equalsIgnoreCase("text") && ct2.getSubType().equalsIgnoreCase("plain")) {
			try {
			    bundle.textPart = new StringBuffer((String)mbp.getContent());
			} catch (IOException ioe) {
			    throw new MessagingException (Pooka.getProperty("error.Message.loadingAttachment", "Error loading attachment"), ioe);
			}

			try {
			    bundle.allText.append((String)mbp.getContent());
			} catch (IOException ioe) {
			    throw new MessagingException (Pooka.getProperty("error.Message.loadingAttachment", "Error loading attachment"), ioe);
			}
			break;
		    }
		}
	    } else {
		bundle.addAll(parseAttachments((Multipart)content, showFullHeaders));
	    }
	} else if (content instanceof String) {
	    bundle.textPart = new StringBuffer((String)content);
	    bundle.allText.append(content);
	}
	
	// add the headers for the message to the main textPart.
	
	if (withHeaders) {
	    if (bundle.textPart != null) {
		bundle.textPart = getHeaderInformation((MimeMessage)m, showFullHeaders).append(bundle.textPart); 
	    } else {
		bundle.textPart = getHeaderInformation((MimeMessage)m, showFullHeaders); 
	    }
	}
	return bundle;
    }
    
    private static AttachmentBundle parseAttachments(Message m, boolean showFullHeaders) throws MessagingException {
	return parseAttachments(m, showFullHeaders, false);
    }

    /**
     * This parses a Mulitpart object into an AttachmentBundle.
     */
    private static AttachmentBundle parseAttachments(Multipart mp, boolean showFullHeaders) throws MessagingException {
	AttachmentBundle bundle = new AttachmentBundle();
	for (int i = 0; i < mp.getCount(); i++) {
	    MimeBodyPart mbp = (MimeBodyPart)mp.getBodyPart(i);
	    ContentType ct = new ContentType(mbp.getContentType());
	    if (ct.getPrimaryType().equalsIgnoreCase("text") && ct.getSubType().equalsIgnoreCase("plain")) {
		if (bundle.textPart == null) {
		    try {
			bundle.textPart = new StringBuffer((String)mbp.getContent());
		    } catch (IOException ioe) {
			throw new MessagingException (Pooka.getProperty("error.Message.loadingAttachment", "Error loading attachment"), ioe);
		    }

		} else {
		    bundle.textAttachments.add(mbp);
		    bundle.allAttachments.add(mbp);
		}
		try {
		    bundle.allText.append((String)mbp.getContent());
		} catch (IOException ioe) {
		    throw new MessagingException (Pooka.getProperty("error.Message.loadingAttachment", "Error loading attachment"), ioe);
		}
		
	    } else if (ct.getPrimaryType().equalsIgnoreCase("multipart")) {
		try {
		    bundle.addAll(parseAttachments((Multipart)mbp.getContent(), showFullHeaders));
		} catch (IOException ioe) {
		    throw new MessagingException (Pooka.getProperty("error.Message.loadingAttachment", "Error loading attachment"), ioe);
		}

	    } else if (ct.getPrimaryType().equalsIgnoreCase("Message")) {
		bundle.nonTextAttachments.add(mbp);
		bundle.allAttachments.add(mbp);
		Object msgContent;
		try {
		    msgContent = mbp.getContent();
		} catch (IOException ioe) {
		    throw new MessagingException (Pooka.getProperty("error.Message.loadingAttachment", "Error loading attachment"), ioe);
		}
		if (msgContent instanceof Message)
		    bundle.addAll(parseAttachments((Message)msgContent, showFullHeaders, true));
		else if (msgContent instanceof java.io.InputStream)
		    bundle.addAll(parseAttachments(new MimeMessage(Pooka.getDefaultSession(), (java.io.InputStream)msgContent), showFullHeaders, true));
		else
		    System.out.println("Error:  unsupported Message Type:  " + msgContent.getClass().getName());
		
	    } else {
		bundle.nonTextAttachments.add(mbp);
		bundle.allAttachments.add(mbp);
	    }
	}
	return bundle;
    }

    /**
     * This gets the Text part of a message.  This is useful if you want
     * to display just the 'body' of the message without the attachments.
     */
    public static String getTextPart(Message m, boolean showFullHeaders, boolean withHeaders) throws MessagingException {
	AttachmentBundle bundle = parseAttachments(m, showFullHeaders, withHeaders);
	if (bundle.textPart != null)
	    return bundle.textPart.toString();
	else
	    return null;
    }

    public static String getTextPart(Message m, boolean showFullHeaders) throws MessagingException {
	return getTextPart(m, showFullHeaders, false);
    }

    /**
     * This method returns all of the attachments marked as 'inline' which
     * are also of text or message/rfc822 types.
     */
    public static Vector getInlineTextAttachments(Message m, boolean showFullHeaders, boolean withHeaders) throws MessagingException {
	AttachmentBundle bundle = parseAttachments(m, showFullHeaders, withHeaders);
	return bundle.textAttachments;
    }

    public static Vector getInlineTextAttachments(Message m, boolean showFullHeaders) throws MessagingException {
	return getInlineTextAttachments(m, showFullHeaders, false);
    }

    /**
     * This returns the Attachments (basically, all the Parts in a Multipart
     * except for the main body of the message).
     */
    public static Vector getAttachments(Message m, boolean showFullHeaders) throws MessagingException {
	AttachmentBundle bundle = parseAttachments(m, showFullHeaders);
	return bundle.allAttachments;
    }

    /**
     * This method returns the Message Text plus the text inline attachments.
     * The attachments are separated by the separator flag.
     */

    public static String getTextAndTextInlines(Message m, String separator, boolean showFullHeaders, boolean withHeaders, int maxLength) throws MessagingException {
	StringBuffer returnValue = null;
	String retString = MailUtilities.getTextPart(m, showFullHeaders, withHeaders);
	if (retString != null && retString.length() > 0)
	    returnValue = new StringBuffer(retString);
	else
	    returnValue = new StringBuffer();

	Vector attachments = MailUtilities.getInlineTextAttachments(m, showFullHeaders);
	if (attachments != null && attachments.size() > 0) {
	    for (int i = 0; i < attachments.size(); i++) {
		Object content = null;
		try {
		    int size = ((MimeBodyPart)attachments.elementAt(i)).getSize();
		    System.out.println("size of attachment is " + size);
		    if (size <= maxLength) {
			content = ((MimeBodyPart)attachments.elementAt(i)).getContent();
			returnValue.append(separator);
			if (content instanceof MimeMessage)
			    returnValue.append(getTextAndTextInlines((MimeMessage)content, separator, showFullHeaders, maxLength));
			else
			    returnValue.append(content);
		    }
		} catch (IOException ioe) {
		    throw new MessagingException (Pooka.getProperty("error.Message.loadingAttachment", "Error loading attachment"), ioe);
		}
	    }
	}
	
	return returnValue.toString();
    }
    
    public static String getTextAndTextInlines(Message m, String separator, boolean showFullHeaders, int maxLength) throws MessagingException {
	return getTextAndTextInlines(m, separator, showFullHeaders, false, maxLength);
    }

    /**
     * This returns the Attachments (basically, all the Parts in a Multipart
     * except for the main body of the message).
     */
    public static Vector getAttachments(Multipart mp, boolean showFullHeaders) throws MessagingException {
	AttachmentBundle bundle = parseAttachments(mp, showFullHeaders);
	return bundle.allAttachments;
    }

    /**
     * This returns the formatted header information for a message.
     */
    public static StringBuffer getHeaderInformation (MimeMessage mMsg, boolean showFullHeaders) throws MessagingException {
	StringBuffer headerText = new StringBuffer();
	
	if (showFullHeaders) {
	}
	else {
	    StringTokenizer tokens = new StringTokenizer(Pooka.getProperty("MessageWindow.Header.DefaultHeaders", "From:To:CC:Date:Subject"), ":");
	    String hdrLabel,currentHeader = null;
	    String[] hdrValue = null;
	    
	    while (tokens.hasMoreTokens()) {
		currentHeader=tokens.nextToken();
		hdrLabel = Pooka.getProperty("MessageWindow.Header." + currentHeader + ".label", currentHeader);
		hdrValue = mMsg.getHeader(Pooka.getProperty("MessageWindow.Header." + currentHeader + ".MIMEHeader", currentHeader));
		
		if (hdrValue != null && hdrValue.length > 0) {
		    headerText.append(hdrLabel + ":  ");
		    for (int i = 0; i < hdrValue.length; i++) {
			headerText.append(hdrValue[i]);
			if (i != hdrValue.length -1) 
			    headerText.append(", ");
		    }
		    headerText.append("\n");
		}
	    }
	    String separator = Pooka.getProperty("MessageWindow.separator", "");
	    if (separator.equals(""))
		headerText.append("\n\n");
	    else
		headerText.append(separator);
	}
	
	return headerText;
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
