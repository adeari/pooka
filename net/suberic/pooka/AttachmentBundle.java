package net.suberic.pooka;
import java.util.Vector;
import java.io.IOException;

/**
 * This class is here for my convenience so that I can have a single
 * set of attachment parsers that will return any set of information
 * that I want.
 */
class AttachmentBundle {
    Attachment textPart = null;
    Vector allAttachments = new Vector();
    Vector attachmentsAndTextPart = null;
    
    AttachmentBundle() {
    }
    
    void addAll(AttachmentBundle subBundle) {
	if (textPart == null)
	    textPart = subBundle.textPart;

	allAttachments.addAll(subBundle.allAttachments);
    }


    /**
     * This gets the Text part of a message.  This is useful if you want
     * to display just the 'body' of the message without the attachments.
     */
    public String getTextPart(boolean withHeaders, boolean showFullHeaders, int maxLength, String truncationMessage) throws IOException {
	if (textPart != null)
	    return textPart.getText(withHeaders, showFullHeaders, maxLength, truncationMessage);
	else
	    return null;
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
    public String getTextAndTextInlines(String separator, boolean showFullHeaders, boolean withHeaders, int maxLength, String truncationMessage) throws IOException {
	StringBuffer returnValue = new StringBuffer();
	if (textPart != null)
	    returnValue.append(textPart.getText(withHeaders, showFullHeaders, maxLength, truncationMessage));
	
	if (allAttachments != null && allAttachments.size() > 0) {
	    for (int i = 0; i < allAttachments.size() ; i++) {
		Attachment attach = (Attachment) allAttachments.elementAt(i);
		if (attach.isPlainText())
		    returnValue.append(attach.getText(withHeaders, showFullHeaders, maxLength, truncationMessage));
	    }
	}

	return returnValue.toString();
    }	
    
}
