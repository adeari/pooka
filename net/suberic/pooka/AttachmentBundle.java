package net.suberic.pooka;
import java.util.Vector;

/**
 * This class is here for my convenience so that I can have a single
 * set of attachment parsers that will return any set of information
 * that I want.
 */
class AttachmentBundle {
    StringBuffer textPart = null;
    StringBuffer allText = new StringBuffer();
    Vector textAttachments = new Vector();
    Vector nonTextAttachments = new Vector();
    Vector allAttachments = new Vector();
    
    AttachmentBundle() {
    }
    
    void addAll(AttachmentBundle subBundle) {
	if (textPart == null)
	    textPart = subBundle.textPart;
	else if (subBundle.textPart != null) {
	    javax.mail.internet.MimeBodyPart mbp = new javax.mail.internet.MimeBodyPart();
	    try {
		mbp.setText(subBundle.textPart.toString());
	    } catch (javax.mail.MessagingException me) {
	    }
	    textAttachments.add(mbp);
	    allAttachments.add(mbp);
	}

	allText.append(subBundle.allText);
	textAttachments.addAll(subBundle.textAttachments);
	nonTextAttachments.addAll(subBundle.nonTextAttachments);
	allAttachments.addAll(subBundle.allAttachments);
    }
}
