package net.suberic.pooka;
import java.util.*;
import java.io.IOException;
import javax.mail.internet.*;
import javax.mail.*;


/**
 * This class is here for my convenience so that I can have a single
 * set of attachment parsers that will return any set of information
 * that I want.
 */
class AttachmentBundle {
    Attachment textPart = null;
    Vector allAttachments = new Vector();
    Vector attachmentsAndTextPart = null;
    InternetHeaders headers = null;
    Vector headerLines = null;

    AttachmentBundle() {
    }

    AttachmentBundle(MimePart m) throws MessagingException {
	setHeaderSource(m);
    }
    
    void addAll(AttachmentBundle subBundle) {
	if (subBundle.textPart != null)
	    subBundle.textPart.setHeaderSource(subBundle);

	if (textPart == null)
	    textPart = subBundle.textPart;
	else if (subBundle.textPart != null)
	    allAttachments.add(subBundle.textPart);

	allAttachments.addAll(subBundle.allAttachments);
    }


    /**
     * This gets the Text part of a message.  This is useful if you want
     * to display just the 'body' of the message without the attachments.
     */
    public String getTextPart(boolean withHeaders, boolean showFullHeaders, int maxLength, String truncationMessage) throws IOException {
      StringBuffer retVal = new StringBuffer();
      
      if (withHeaders)
	retVal.append(getHeaderInformation(showFullHeaders, false));
      
      String text = null;
      if (textPart != null) {
	text = textPart.getText(withHeaders, showFullHeaders, maxLength, truncationMessage);
      }
	
      if (text != null) {
	retVal.append(text);
	return retVal.toString();
      } else
	return null;
    }
  
    /**
     * This gets the Html part of a message.  This is useful if you want
     * to display just the 'body' of the message without the attachments.
     */
    public String getHtmlPart(boolean withHeaders, boolean showFullHeaders, int maxLength, String truncationMessage) throws IOException {
	StringBuffer retVal = new StringBuffer();

	retVal.append("<html><body>");

	if (withHeaders)
	    retVal.append(getHeaderInformation(showFullHeaders, true));

	if (textPart != null)
	    retVal.append(textPart.getHtml(withHeaders, showFullHeaders, maxLength, truncationMessage));

	retVal.append("</body></html>");
	return retVal.toString();
    }

    /**
     * This returns the Attachments (basically, all the Parts in a Multipart
     * except for the main body of the message).
     */
    public Vector getAttachments() {
	return allAttachments;
    }

    /**
     * This returns the Attachments (basically, all the Parts in a Multipart
     * except for the main body of the message) using the given 
     * messageLength to determine whether or not the main text part is an
     * attachment or not.
     */
    public Vector getAttachments(int maxLength) {
	if (textPart != null && textPart.getSize() >= maxLength) {
	    if (attachmentsAndTextPart != null)
		return attachmentsAndTextPart;
	    else {
		attachmentsAndTextPart = new Vector();
		attachmentsAndTextPart.add(textPart);
		attachmentsAndTextPart.addAll(allAttachments);
		return attachmentsAndTextPart;
	    }
	} else
	    return allAttachments;
    }
    
    /**
     * This method returns the Message Text plus the text inline attachments.
     * The attachments are separated by the separator flag.
     */
    public String getTextAndTextInlines(String separator, boolean withHeaders, boolean showFullHeaders, int maxLength, String truncationMessage) throws IOException {
	StringBuffer returnValue = new StringBuffer();

	if (withHeaders)
	    returnValue.append(getHeaderInformation(showFullHeaders, false));

	if (textPart != null)
	    returnValue.append(textPart.getText(withHeaders, showFullHeaders, maxLength, truncationMessage));
	
	if (allAttachments != null && allAttachments.size() > 0) {
	    for (int i = 0; i < allAttachments.size() ; i++) {
		Attachment attach = (Attachment) allAttachments.elementAt(i);
		if (attach.isPlainText()) {
		    returnValue.append(separator);
		    returnValue.append(attach.getText(withHeaders, showFullHeaders, maxLength, truncationMessage));
		}
	    }
	}

	return returnValue.toString();
    }	
    

    /**
     * This method returns the Message HTML plus the text inline attachments.
     * The attachments are separated by the separator flag.
     */
    public String getHtmlAndTextInlines(String separator, boolean withHeaders, boolean showFullHeaders, int maxLength, String truncationMessage) throws IOException {
	StringBuffer returnValue = new StringBuffer();

	returnValue.append("<html><body>");

	if (withHeaders)
	    returnValue.append(getHeaderInformation(showFullHeaders, true));

	if (textPart != null)
	    returnValue.append(textPart.getHtml(withHeaders, showFullHeaders, maxLength, truncationMessage));
	
	if (allAttachments != null && allAttachments.size() > 0) {
	    for (int i = 0; i < allAttachments.size() ; i++) {
		Attachment attach = (Attachment) allAttachments.elementAt(i);
		if (attach.isPlainText()) {
		    returnValue.append(separator);
		    returnValue.append(attach.getText(withHeaders, showFullHeaders, maxLength, truncationMessage));
		}
	    }
	}

	returnValue.append("</body></html>");
	return returnValue.toString();
    }	
    
    public void setHeaderSource(MimePart headerSource) throws MessagingException {
	headers = parseHeaders(headerSource.getAllHeaders());
	headerLines = parseHeaderLines(headerSource.getAllHeaderLines());
    }

  /**
   * This returns the formatted header information for a message.
   */
  public StringBuffer getHeaderInformation (boolean showFullHeaders, boolean useHtml) {
    if (headers != null) {
      StringBuffer headerText = new StringBuffer();
      
      if (showFullHeaders) {
	Enumeration allHdrs = headers.getAllHeaderLines();
	while (allHdrs.hasMoreElements()) {
	  headerText.append(MailUtilities.decodeText((String) allHdrs.nextElement()));
	}		
      } else {
	StringTokenizer tokens = new StringTokenizer(Pooka.getProperty("MessageWindow.Header.DefaultHeaders", "From:To:CC:Date:Subject"), ":");
	String hdrLabel,currentHeader = null;
	String hdrValue = null;
	
	while (tokens.hasMoreTokens()) {
	  currentHeader=tokens.nextToken();
	  hdrLabel = Pooka.getProperty("MessageWindow.Header." + currentHeader + ".label", currentHeader);
	  hdrValue = MailUtilities.decodeText((String) headers.getHeader(Pooka.getProperty("MessageWindow.Header." + currentHeader + ".MIMEHeader", currentHeader), ":"));
	  if (hdrValue != null) {
	    if (useHtml) {
	      headerText.append("<b>" + hdrLabel + ":</b><nbsp><nbsp>");
	      headerText.append(MailUtilities.escapeHtml(hdrValue));
	      
	      headerText.append("<br>\n");
	    } else {
	      headerText.append(hdrLabel + ":  ");
	      headerText.append(hdrValue);
	      
	      headerText.append("\n");
	    }
	  }
	}
      } 
      if (useHtml) {
	String separator = Pooka.getProperty("MessageWindow.htmlSeparator", "<hr><br>");
	headerText.append(separator);
      } else {
	String separator = Pooka.getProperty("MessageWindow.separator", "");
	if (separator.equals(""))
	  headerText.append("\n\n");
	else
	  headerText.append(separator);
      }

      return headerText;
    } else {
      return new StringBuffer();
    }
  }
  
    /**
     * Parses the Enumeration of Header objects into a HashMap.
     */
    private InternetHeaders parseHeaders(Enumeration enum) {
	InternetHeaders retVal = new InternetHeaders();
	while (enum.hasMoreElements()) {
	    Header hdr = (Header) enum.nextElement();
	    retVal.addHeader(hdr.getName(), hdr.getValue());
	}
	return retVal;
    }

    /**
     * Parses the Enumeration of header lines into a Vector.
     */
    private Vector parseHeaderLines(Enumeration enum) {
	Vector retVal = new Vector();
	while (enum.hasMoreElements())
	    retVal.add(enum.nextElement());
	return retVal;
    }

    /**
     * Returns whether or not this attachment has an HTML version available.
     */
    public boolean containsHtml() {
	if (textPart != null) {
	    if (textPart instanceof AlternativeAttachment) {
		return true;
	    } else {
		return textPart.getMimeType().match("text/html");
	    }
	} else 
	    return false;
    }

    /**
     * Returns true if the main content of this message exists only as
     * HTML.
     */
    public boolean isHtml() {
	if (textPart != null) {
	    if (textPart instanceof AlternativeAttachment)
		return false;
	    else
		return (textPart.getMimeType().match("text/html"));
	} else
	    return false;
    }
}
