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
	allText.append(subBundle.allText);
	textAttachments.addAll(subBundle.textAttachments);
	nonTextAttachments.addAll(subBundle.nonTextAttachments);
	allAttachments.addAll(subBundle.allAttachments);
    }
}
