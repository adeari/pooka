package net.suberic.pooka;
import javax.mail.*;
import java.util.Vector;
import java.util.Enumeration;
import java.io.*;
import javax.activation.*;
import javax.mail.internet.*;

/**
 * A MessageInfo representing a new message.
 */
public class NewMessageInfo extends MessageInfo {

    public NewMessageInfo(Message newMessage) {
	message = newMessage;
    }

    /**
     * Sends the new message, using the given Profile, the given 
     * InternetHeaders, the given messageText, the given ContentType, and 
     * the attachments already set for this object.
     */
    public void sendMessage(UserProfile profile, InternetHeaders headers, String messageText, String messageContentType) throws MessagingException {

	MimeMessage mMsg = (MimeMessage) message;

	URLName urlName = null;
	
	if (profile != null) {
	    profile.populateMessage(mMsg);
	    urlName = profile.getSendMailURL();
	}

	Enumeration individualHeaders = headers.getAllHeaders();
	while(individualHeaders.hasMoreElements()) {
	    Header currentHeader = (Header) individualHeaders.nextElement();
	    message.setHeader(currentHeader.getName(), currentHeader.getValue());
	}
	
	if (Pooka.getProperty("Pooka.lineWrap", "").equalsIgnoreCase("true"))
	    messageText=net.suberic.pooka.MailUtilities.wrapText(messageText);
	
	if (urlName != null) {
	    Vector attachments = getAttachments();
	    if (attachments != null && attachments.size() > 0) {
		MimeBodyPart mbp = new MimeBodyPart();
		mbp.setContent(messageText, messageContentType);
		MimeMultipart multipart = new MimeMultipart();
		multipart.addBodyPart(mbp);
		for (int i = 0; i < attachments.size(); i++) 
		    multipart.addBodyPart((BodyPart)attachments.elementAt(i));
		multipart.setSubType("mixed");
		getMessage().setContent(multipart);
		getMessage().saveChanges();
	    } else {
		getMessage().setContent(messageText, messageContentType);
	    }
	    
	    Pooka.getMainPanel().getMailQueue().sendMessage(getMessage(), urlName);
	    
	    if (profile.getSentFolder() != null && profile.getSentFolder().getFolder() != null) {
		getMessage().setSentDate(java.util.Calendar.getInstance().getTime());
		profile.getSentFolder().getFolder().appendMessages(new Message[] {getMessage()});
	    }
	} else {
	    throw new MessagingException(Pooka.getProperty("error.noMailURL", "Error sending Message:  No mail URL."));
	}
    }

    /**
     * Saves the NewMessageInfo to the sentFolder associated with the 
     * given Profile, if any.
     */
    public void saveToSentFolder(UserProfile profile) throws MessagingException {
	if (profile.getSentFolder() != null && profile.getSentFolder().getFolder() != null) {
	    getMessage().setSentDate(java.util.Calendar.getInstance().getTime());
	    profile.getSentFolder().getFolder().appendMessages(new Message[] {getMessage()});
	}
    }

    /**
     * Returns the attachments for the new messages.
     */
    public Vector getAttachments() {
	return attachments;
    }

    /**
     * Adds an attachment to this message.
     */
    public void addAttachment(BodyPart part) {
	if (attachments == null) 
	    attachments = new Vector();
	attachments.add(part);
    }

    /**
     * Removes an attachment from this message.
     */
    public int removeAttachment(BodyPart part) {
	if (attachments != null) {
	    int index = attachments.indexOf(part);	
	    attachments.remove(index);
	    return index;
	}
	
	return -1;
    }

    /**
     * Attaches the given File to the message.
     */
    public void attachFile(File f) throws MessagingException {
	// borrowing liberally from ICEMail here.
	
	MimeBodyPart mbp = new MimeBodyPart();
	
	FileDataSource fds = new FileDataSource(f);
	
	DataHandler dh = new DataHandler(fds);
	
	mbp.setFileName(f.getName());
	
	if (Pooka.getMimeTypesMap().getContentType(f).startsWith("text"))
	    mbp.setDisposition(Part.ATTACHMENT);
	else
	    mbp.setDisposition(Part.INLINE);
	
	mbp.setDescription(f.getName());
	    
	mbp.setDataHandler( dh );
	
	addAttachment(mbp);
    }

    /**
     * Returns the given header on the wrapped Message.
     */
    public String getHeader(String headerName, String delimeter) throws MessagingException {
	return ((MimeMessage)getMessage()).getHeader(headerName, delimeter);
    }

    /**
     * Gets the text part of the wrapped message.
     */
    public String getTextPart(boolean showFullHeaders) {
	return net.suberic.pooka.MailUtilities.getTextPart(getMessage(), showFullHeaders);
    }
}
