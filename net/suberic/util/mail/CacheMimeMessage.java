package net.suberic.util.mail;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * This is an implementation of MimeMessage which operates on top of a 
 * cache.  
 */

public class CacheMimeMessage extends MimeMessage {
    // the wrapped MimeMessage, if available.
    MimeMessage wrappedMessage;
    
    // the uid for this message
    long uid;

    // boolean 


    public CacheMimeMessage(long messageUid, CacheFolder sourceFolder) {
	uid =  messageUid;
	folder = sourceFolder;
    }

    /**
     *
     */
    public abstract Address[] getFrom()
	throws MessagingException {
	
    }


    public abstract void setFrom()
	throws MessagingException {
    }

    public abstract void setFrom(Address address)
	throws MessagingException {
    }

    public void setFrom()
	throws MessagingException {
    }


    public abstract void addFrom(Address[] addresses)
	throws MessagingException {

    }

    public abstract Address[] getRecipients(Message.RecipientType type)
	throws MessagingException {

    }

    public Address[] getAllRecipients()
	throws MessagingException {

    }

    public abstract void setRecipients(Message.RecipientType type,
				       Address[] addresses)
    throws MessagingException {
    }
    
    
    public void setRecipient(Message.RecipientType type,
			     Address address)
	throws MessagingException {

    }

    public abstract void addRecipients(Message.RecipientType type,
				       Address[] addresses)
	throws MessagingException {
    }

    public void addRecipient(Message.RecipientType type,
                         Address address)
	throws MessagingException {
    }

    public Address[] getReplyTo()
	throws MessagingException {
    }

    public void setReplyTo(Address[] addresses)
	throws MessagingException {
    } 


    public abstract java.lang.String getSubject()
	throws MessagingException {
    }

    public abstract void setSubject(java.lang.String subject)
	throws MessagingException {
    }


    public void setSubject(java.lang.String subject,
                       java.lang.String charset)
	throws MessagingException {
    }

    public abstract java.util.Date getSentDate()
	throws MessagingException {
    }

    public abstract void setSentDate(java.util.Date date)
	throws MessagingException {
    }

    
    public abstract java.util.Date getReceivedDate()
	throws MessagingException {
    }

    public int getSize()
	throws MessagingException {
    }

    public int getLineCount()
	throws MessagingException {
    }

    public java.lang.String getContentType()
	throws MessagingException {
    }

    public boolean isMimeType(java.lang.String mimeType)
	throws MessagingException {
    }

    public java.lang.String getDisposition()
	throws MessagingException Z{
    }

    public void setDisposition(java.lang.String disposition)
	throws MessagingException {
    }

    public java.lang.String getEncoding()
	throws MessagingException {
    }

    public java.lang.String getContentID()
	throws MessagingException {
    }

    public void setContentID(java.lang.String cid)
	throws MessagingException {
    }

    public java.lang.String getContentMD5()
	throws MessagingException {
    }

    public void setContentMD5(java.lang.String md5)
	throws MessagingException {
    }

    public java.lang.String getDescription()
	throws MessagingException {
    }

    public void setDescription(java.lang.String description)
	throws MessagingException {
    }

    public void setDescription(java.lang.String description,
                           java.lang.String charset)
	throws MessagingException {
    }

    public java.lang.String[] getContentLanguage()
	throws MessagingException {
    }

    public void setContentLanguage(java.lang.String[] languages)
	throws MessagingException {
    }

    public java.lang.String getMessageID()
	throws MessagingException {
    }

    public java.lang.String getFileName()
	throws MessagingException {
    }

    public void setFileName(java.lang.String filename)
	throws MessagingException {
    }

    public java.io.InputStream getInputStream()
	throws java.io.IOException,
	       MessagingException {
    }

    protected java.io.InputStream getContentStream()
	throws MessagingException {
    }

    public javax.activation.DataHandler getDataHandler()
	throws MessagingException {
    }

    public java.lang.Object getContent()
	throws java.io.IOException,
	       MessagingException {
    }

    

    public abstract Flags getFlags()
	throws MessagingException {
    }

    public boolean isSet(Flags.Flag flag)
	throws MessagingException {
    }

    public abstract void setFlags(Flags flag,
                              boolean set)
	throws MessagingException {
    }

    public void setFlag(Flags.Flag flag,
                    boolean set)
	throws MessagingException {
    }

    public int getMessageNumber() {
    }

    protected void setMessageNumber(int msgnum) {
    }

    public Folder getFolder() {
    }

    public boolean isExpunged() {
    }

    protected void setExpunged(boolean expunged) {
    }

    public abstract Message reply(boolean replyToAll)
	throws MessagingException {
    }

    public abstract void saveChanges()
	throws MessagingException {
    }

    public boolean match(SearchTerm term)
	throws MessagingException {
    }

}
