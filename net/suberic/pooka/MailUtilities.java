package net.suberic.pooka;

import javax.mail.*;
import javax.mail.internet.*;

public class MailUtilities {
    public MailUtilities() {
    }

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

}
