package net.suberic.pooka;

import java.util.Vector;
import java.util.StringTokenizer;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.List;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import net.suberic.crypto.EncryptionManager;
import net.suberic.crypto.EncryptionUtils;

public class MailUtilities {

  public MailUtilities() {
  }
  
  /**
   * This returns the value of an array of Address objects as a String.
   */
  public static String decodeAddressString(Address[] addresses) {
    if (addresses == null)
      return null;
    
    StringBuffer returnValue = new StringBuffer();
    for (int i = 0; i < addresses.length; i++) {
      if (addresses[i] != null) {
	if (i > 0)
	  returnValue.append(", ");
	if (addresses[i] instanceof javax.mail.internet.InternetAddress)
	  returnValue.append(((javax.mail.internet.InternetAddress)addresses[i]).toUnicodeString());
	else
	  returnValue.append(addresses[i].toString());
      }
    }

    return returnValue.toString();
  }

  /**
   * This decoded an RFC 2047 encoded string.  If there are any errors
   * in decoding the string, the raw string is returned.
   */
  public static String decodeText(String encodedString) {
    if (encodedString == null)
      return null;

    String  value = null;
    try {
      value = javax.mail.internet.MimeUtility.decodeText(encodedString);
    } catch (UnsupportedEncodingException e) {
      // Don't care
      value = encodedString;
    }
    return value;
  }

  /**
   * This parses the message given into an AttachmentBundle.
   */
  public static AttachmentBundle parseAttachments(Message m) throws MessagingException, java.io.IOException {
    AttachmentBundle bundle = new AttachmentBundle((MimeMessage)m);

    handlePart((MimeMessage)m, bundle);

    /*
    String encryptionType = EncryptionManager.checkEncryptionType((MimeMessage)m);
    EncryptionUtils utils = null;
    if (encryptionType != null) {
      try {
	utils = EncryptionManager.getEncryptionUtils(encryptionType);
      } catch (java.security.NoSuchProviderException nspe) {
      }
    }

    if (utils != null) {
      int encryptionStatus = utils.getEncryptionStatus((MimeMessage) m);
      if (encryptionStatus == EncryptionUtils.ENCRYPTED) {
	Attachment newAttach = new net.suberic.pooka.crypto.CryptoAttachment((MimeMessage)m);
	
	bundle.addAttachment(newAttach);
      } else if (encryptionStatus == EncryptionUtils.SIGNED) {
	// in the case of signed attachments, we should get the wrapped body.

	Attachment newAttach = new net.suberic.pooka.crypto.SignedAttachment((MimeMessage)m);
	MimeBodyPart signedMbp = ((net.suberic.pooka.crypto.SignedAttachment) newAttach).getSignedPart();

	if (signedMbp != null) {
	  String signedContentType = signedMbp.getContentType().toLowerCase();
	  if (signedContentType.startsWith("multipart")) {
	    bundle.addAll(parseAttachments((Multipart)signedMbp.getContent()));
	  } else if (signedContentType.startsWith("message")) {
	    bundle.addAttachment(new Attachment(signedMbp));
	    Object msgContent;
	    msgContent = signedMbp.getContent();
	    
	    if (msgContent instanceof Message)
	      bundle.addAll(parseAttachments((Message)msgContent));
	    else if (msgContent instanceof java.io.InputStream)
	      bundle.addAll(parseAttachments(new MimeMessage(Pooka.getDefaultSession(), (java.io.InputStream)msgContent)));
	    else
	      System.out.println("Error:  unsupported Message Type:  " + msgContent.getClass().getName());
	    
	  } else {
	    bundle.addAttachment(new Attachment(signedMbp));
	  }
	}

	bundle.addAttachment(newAttach);
      } else if (encryptionStatus == EncryptionUtils.ATTACHED_KEYS) {
	bundle.addAttachment(new net.suberic.pooka.crypto.KeyAttachment((MimeMessage) m));
      } else {
	// FIXME
	bundle.addAttachment(new Attachment((MimeMessage)m));
      }
    } else {
      String contentType = ((MimeMessage) m).getContentType().toLowerCase();
      
      if (contentType.startsWith("multipart")) {
	ContentType ct = new ContentType(contentType);
	
	if (ct.getSubType().equalsIgnoreCase("alternative") && mp.getContent() instanceof Multipart) {
	parseAlternativeAttachment(bundle, (MimeMessage) m);
	} else if (m.getContent() instanceof Multipart) {
	bundle.addAll(parseAttachments((Multipart)m.getContent()));
	} else {
	  Attachment attachment = new Attachment((MimeMessage)m);
	  bundle.addAttachment(attachment);
	}
      } else {
	Attachment attachment = new Attachment((MimeMessage)m);
	bundle.addAttachment(attachment, new ContentType(contentType));
      }
    }
    */

    return bundle;
  }
  
  /**
   * This parses a Mulitpart object into an AttachmentBundle.
   */
  public static AttachmentBundle parseAttachments(Multipart mp) throws MessagingException, java.io.IOException {
    AttachmentBundle bundle = new AttachmentBundle();
    for (int i = 0; i < mp.getCount(); i++) {
      MimeBodyPart mbp = (MimeBodyPart)mp.getBodyPart(i);
      
      handlePart(mbp, bundle);
    }
    return bundle;
  }

  /**
   * Handles a MimeBodyPart.
   */
  public static void handlePart(MimePart mp, AttachmentBundle bundle) throws MessagingException, java.io.IOException {
    
    String encryptionType = EncryptionManager.checkEncryptionType(mp);
    
    EncryptionUtils utils = null;
    if (encryptionType != null) {
      try {
	utils = EncryptionManager.getEncryptionUtils(encryptionType);
      } catch (java.security.NoSuchProviderException nspe) {
      }
    }
    
    if (utils != null) {
      
      int encryptionStatus = utils.getEncryptionStatus(mp);

      if (encryptionStatus == EncryptionUtils.ENCRYPTED) {
	Attachment newAttach = new net.suberic.pooka.crypto.CryptoAttachment(mp);
	bundle.addAttachment(newAttach);
      } else if (encryptionStatus == EncryptionUtils.SIGNED) {
	// in the case of signed attachments, we should get the wrapped body.
	
	Attachment newAttach = new net.suberic.pooka.crypto.SignedAttachment(mp);

	MimeBodyPart signedMbp = ((net.suberic.pooka.crypto.SignedAttachment) newAttach).getSignedPart();
	
	if (signedMbp != null) {
	  /*
	  String signedContentType = signedMbp.getContentType().toLowerCase();
	  if (signedContentType.startsWith("multipart")) {
	    bundle.addAll(parseAttachments((Multipart)signedMbp.getContent()));
	  } else if (signedContentType.startsWith("message")) {
	    bundle.addAttachment(new Attachment(signedMbp));
	    Object msgContent;
	    msgContent = signedMbp.getContent();
	    
	    if (msgContent instanceof Message)
	      bundle.addAll(parseAttachments((Message)msgContent));
	    else if (msgContent instanceof java.io.InputStream)
	      bundle.addAll(parseAttachments(new MimeMessage(Pooka.getDefaultSession(), (java.io.InputStream)msgContent)));
	    else
	      System.out.println("Error:  unsupported Message Type:  " + msgContent.getClass().getName());
	    
	  } else {
	    bundle.addAttachment(new Attachment(signedMbp));
	  }
	  */
	  handlePart(signedMbp, bundle);
	}
	
	bundle.addAttachment(newAttach);
	
      } else if (encryptionStatus == EncryptionUtils.ATTACHED_KEYS) {
	  bundle.addAttachment(new net.suberic.pooka.crypto.KeyAttachment(mp));
      } else {
	// FIXME
	  bundle.addAttachment(new Attachment(mp));
      }
      
    } else {
      ContentType ct = new ContentType(mp.getContentType());
      if (ct.getPrimaryType().equalsIgnoreCase("multipart")) {
	if (ct.getSubType().equalsIgnoreCase("alternative") && mp.getContent() instanceof Multipart) {
	  parseAlternativeAttachment(bundle, mp);
	} else if (mp.getContent() instanceof Multipart) {
	  bundle.addAll(parseAttachments((Multipart)mp.getContent()));
	} else {
	  Attachment attachment = new Attachment(mp);
	  bundle.addAttachment(attachment);
	}
      } else if (ct.getPrimaryType().equalsIgnoreCase("Message")) {
	bundle.addAttachment(new Attachment(mp));
	Object msgContent;
	msgContent = mp.getContent();
	
	if (msgContent instanceof Message)
	  bundle.addAll(parseAttachments((Message)msgContent));
	else if (msgContent instanceof java.io.InputStream)
	  bundle.addAll(parseAttachments(new MimeMessage(Pooka.getDefaultSession(), (java.io.InputStream)msgContent)));
	else
	  System.out.println("Error:  unsupported Message Type:  " + msgContent.getClass().getName());
	
      } else {
	bundle.addAttachment(new Attachment(mp), ct);
      }
    }
  }

  /**
   * Creates an AlternativeAttachment and adds it properly to the
   * given AttachmentBundle.
   */
  public static void parseAlternativeAttachment(AttachmentBundle bundle, MimePart mbp) throws MessagingException, java.io.IOException {
    Multipart amp = (Multipart) mbp.getContent();
    
    MimeBodyPart altTextPart = null;
    MimeBodyPart altHtmlPart = null;
    List extraList = new LinkedList();
    
    for (int j = 0; j < amp.getCount(); j++) {
      MimeBodyPart current = (MimeBodyPart)amp.getBodyPart(j);
      ContentType ct2 = new ContentType(current.getContentType());
      if (ct2.match("text/plain") && altTextPart == null)
	altTextPart = current;
      else if (ct2.match("text/html") && altHtmlPart == null)
	altHtmlPart = current;
      else
	extraList.add(new Attachment(current));
    }
    
    if (altHtmlPart != null && altTextPart != null) {
      Attachment attachment = new AlternativeAttachment(altTextPart, altHtmlPart);
      bundle.addAttachment(attachment);
      Iterator it = extraList.iterator();
      while (it.hasNext()) {
	bundle.addAttachment((Attachment) it.next());
      }
    } else {
      // hurm
      bundle.addAll(parseAttachments(amp));
    }
  }
  
  /**
   * This method takes a given character array and returns the offset
   * position at which a line break should occur.
   *
   * If no break is necessary, the <code>finish</code> value is returned.
   * 
   */
  public static int getBreakOffset(String buffer, int breakLength, int tabSize) {
    // what we'll do is to modify the break length to make it fit tabs.
    
    int nextTab = buffer.indexOf('\t');
    int tabAccumulator = 0;
    int tabAddition = 0;
    while (nextTab >=0 && nextTab < breakLength) {
      tabAddition = tabSize - ((tabSize +  nextTab + tabAccumulator + 1) % tabSize);
      breakLength=breakLength - tabAddition;
      tabAccumulator = tabAccumulator + tabAddition;
      if (nextTab + 1 < buffer.length())
	nextTab = buffer.indexOf('\t', nextTab + 1);
      else
	nextTab = -1;
    }
    
    
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
   * This takes a String and word wraps it at length wrapLength.
   */
  public static String wrapText(String originalText, int wrapLength, char lineBreak, int tabSize) {
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
	newBreak = getBreakOffset(wrappedText.substring(lastReal +1, nextReal), wrapLength, tabSize) + lastReal + 1;
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
    int tabSize;
    try {
      String wrapLengthString = Pooka.getProperty("Pooka.lineLength");
      wrapLength = Integer.parseInt(wrapLengthString);
    } catch (Exception e) {
      wrapLength = 72;
    }
    
    try {
      String tabSizeString = Pooka.getProperty("Pooka.tabSize", "8");
      tabSize = Integer.parseInt(tabSizeString);
    } catch (Exception e) {
      tabSize = 8;
    }
    return wrapText(originalText, wrapLength, '\n', tabSize);
  }
  
  /**
   * Escapes html special characters.
   */
  public static String escapeHtml(String input) {
    char[] characters = input.toCharArray();
    StringBuffer retVal = new StringBuffer();
    for (int i = 0; i < characters.length; i++) {
      if (characters[i] == '&')
	retVal.append("&amp;");
      else if (characters[i] == '<')
	retVal.append("&lt;");
      else if (characters[i] == '>')
	retVal.append("&gt;");
      else
	retVal.append(characters[i]);
    }
    return retVal.toString();
  }
}


