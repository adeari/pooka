package net.suberic.pooka;
import javax.mail.*;
import javax.mail.internet.*;
import net.suberic.pooka.FolderInfo;
import net.suberic.pooka.Pooka;
import javax.activation.DataHandler;
import java.util.Enumeration;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

/**
 * A wrapper around a MimeMessage which can either work in real or 
 * disconnected (cached) mode.
 */

public class UIDMimeMessage extends MimeMessage {

    long uid;
    UIDFolderInfo parent;
    
    public UIDMimeMessage(UIDFolderInfo parentFolderInfo, long newUid) {
	super(Pooka.getDefaultSession());
	uid = newUid;
	parent = parentFolderInfo;
	saved=true;
	modified=false;
    }

    public int getSize() throws MessagingException {
	try {
	    return getMessage().getSize();
	} catch (FolderClosedException fce) {
	    int status = parent.getStatus();
	    if (status == FolderInfo.CONNECTED || status == FolderInfo.LOST_CONNECTION) {
		try {
		    parent.openFolder(Folder.READ_WRITE);
		} catch (MessagingException me) {
		    throw fce;
		}

		return getMessage().getSize();
		
	    } else {
		throw fce;
	    }
	}
    }

    protected InputStream getContentStream() throws MessagingException {
	throw new MessagingException("No getting the content stream!  Bad code!");
    }

    public synchronized DataHandler getDataHandler() 
		throws MessagingException {
	try {
	    return getMessage().getDataHandler();
	} catch (FolderClosedException fce) {
	    int status = parent.getStatus();
	    if (status == FolderInfo.CONNECTED || status == FolderInfo.LOST_CONNECTION) {
		try {
		    parent.openFolder(Folder.READ_WRITE);
		} catch (MessagingException me) {
		    throw fce;
		}

		return getMessage().getDataHandler();
	    } else {
		throw fce;
	    }
	}
    }

    public String[] getHeader(String name)
			throws MessagingException {
	try {
	    return getMessage().getHeader(name);
	} catch (FolderClosedException fce) {
	    int status = parent.getStatus();
	    if (status == FolderInfo.CONNECTED || status == FolderInfo.LOST_CONNECTION) {
		try {
		    parent.openFolder(Folder.READ_WRITE);
		} catch (MessagingException me) {
		    throw fce;
		}

		return getMessage().getHeader(name);
		
	    } else {
		throw fce;
	    }
	}
    }

    public String getHeader(String name, String delimiter)
				throws MessagingException {
	try {
	    return getMessage().getHeader(name, delimiter);
	} catch (FolderClosedException fce) {
	    int status = parent.getStatus();
	    if (status == FolderInfo.CONNECTED || status == FolderInfo.LOST_CONNECTION) {
		try {
		    parent.openFolder(Folder.READ_WRITE);
		} catch (MessagingException me) {
		    throw fce;
		}

		return getMessage().getHeader(name, delimiter);
		
	    } else {
		throw fce;
	    }
	}
    }

   public void setHeader(String name, String value)
       throws MessagingException {
       try {
	   getMessage().setHeader(name, value);
	} catch (FolderClosedException fce) {
	    int status = parent.getStatus();
	    if (status == FolderInfo.CONNECTED || status == FolderInfo.LOST_CONNECTION) {
		try {
		    parent.openFolder(Folder.READ_WRITE);
		} catch (MessagingException me) {
		    throw fce;
		}

		getMessage().setHeader(name, value);
		
	    } else {
		throw fce;
	    }
	}
   }

    public void addHeader(String name, String value)
                                throws MessagingException {
	try {
	    getMessage().addHeader(name, value);
	} catch (FolderClosedException fce) {
	    int status = parent.getStatus();
	    if (status == FolderInfo.CONNECTED || status == FolderInfo.LOST_CONNECTION) {
		try {
		    parent.openFolder(Folder.READ_WRITE);
		} catch (MessagingException me) {
		    throw fce;
		}

		getMessage().addHeader(name, value);
		
	    } else {
		throw fce;
	    }
	}
    }

    public void removeHeader(String name)
                                throws MessagingException {
	try {
	    getMessage().removeHeader(name);
	} catch (FolderClosedException fce) {
	    int status = parent.getStatus();
	    if (status == FolderInfo.CONNECTED || status == FolderInfo.LOST_CONNECTION) {
		try {
		    parent.openFolder(Folder.READ_WRITE);
		} catch (MessagingException me) {
		    throw fce;
		}

		getMessage().removeHeader(name);
		
	    } else {
		throw fce;
	    }
	}
    }

    public Enumeration getAllHeaders() throws MessagingException {
	try {
	    return getMessage().getAllHeaders();	
	} catch (FolderClosedException fce) {
	    int status = parent.getStatus();
	    if (status == FolderInfo.CONNECTED || status == FolderInfo.LOST_CONNECTION) {
		try {
		    parent.openFolder(Folder.READ_WRITE);
		} catch (MessagingException me) {
		    throw fce;
		}

		return getMessage().getAllHeaders();	
		
	    } else {
		throw fce;
	    }
	}
    }

    public Enumeration getMatchingHeaders(String[] names)
			throws MessagingException {
	try {
	    return getMessage().getMatchingHeaders(names);
	} catch (FolderClosedException fce) {
	    int status = parent.getStatus();
	    if (status == FolderInfo.CONNECTED || status == FolderInfo.LOST_CONNECTION) {
		try {
		    parent.openFolder(Folder.READ_WRITE);
		} catch (MessagingException me) {
		    throw fce;
		}

		return getMessage().getMatchingHeaders(names);
		
	    } else {
		throw fce;
	    }
	}
    }


    /**
     * Return non-matching headers from this Message as an
     * Enumeration of Header objects. This implementation 
     * obtains the header from the <code>headers</code> InternetHeaders object.
     *
     * @exception  MessagingException
     */
    public Enumeration getNonMatchingHeaders(String[] names)
			throws MessagingException {
	try {
	    return getMessage().getNonMatchingHeaders(names);
	} catch (FolderClosedException fce) {
	    int status = parent.getStatus();
	    if (status == FolderInfo.CONNECTED || status == FolderInfo.LOST_CONNECTION) {
		try {
		    parent.openFolder(Folder.READ_WRITE);
		} catch (MessagingException me) {
		    throw fce;
		}

		return getMessage().getNonMatchingHeaders(names);
		
	    } else {
		throw fce;
	    }
	}
    }

    /**
     * Add a raw RFC 822 header-line. 
     *
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception  	MessagingException
     */
    public void addHeaderLine(String line) throws MessagingException {
	try {
	    getMessage().addHeaderLine(line);
	} catch (FolderClosedException fce) {
	    int status = parent.getStatus();
	    if (status == FolderInfo.CONNECTED || status == FolderInfo.LOST_CONNECTION) {
		try {
		    parent.openFolder(Folder.READ_WRITE);
		} catch (MessagingException me) {
		    throw fce;
		}

		getMessage().addHeaderLine(line);
		
	    } else {
		throw fce;
	    }
	}
    }

    /**
     * Get all header lines as an Enumeration of Strings. A Header
     * line is a raw RFC 822 header-line, containing both the "name" 
     * and "value" field. 
     *
     * @exception  	MessagingException
     */
    public Enumeration getAllHeaderLines() throws MessagingException {
	try {
	    return getMessage().getAllHeaderLines();
	} catch (FolderClosedException fce) {
	    int status = parent.getStatus();
	    if (status == FolderInfo.CONNECTED || status == FolderInfo.LOST_CONNECTION) {
		try {
		    parent.openFolder(Folder.READ_WRITE);
		} catch (MessagingException me) {
		    throw fce;
		}

		return getMessage().getAllHeaderLines();
		
	    } else {
		throw fce;
	    }
	}
    }

    /**
     * Get matching header lines as an Enumeration of Strings. 
     * A Header line is a raw RFC 822 header-line, containing both 
     * the "name" and "value" field.
     *
     * @exception  	MessagingException
     */
    public Enumeration getMatchingHeaderLines(String[] names)
                                        throws MessagingException {
	try {
	    return getMessage().getMatchingHeaderLines(names);
	} catch (FolderClosedException fce) {
	    int status = parent.getStatus();
	    if (status == FolderInfo.CONNECTED || status == FolderInfo.LOST_CONNECTION) {
		try {
		    parent.openFolder(Folder.READ_WRITE);
		} catch (MessagingException me) {
		    throw fce;
		}
		
		return getMessage().getMatchingHeaderLines(names);
		
	    } else {
		throw fce;
	    }
	}
    }

    /**
     * Get non-matching header lines as an Enumeration of Strings. 
     * A Header line is a raw RFC 822 header-line, containing both 
     * the "name" and "value" field.
     *
     * @exception  	MessagingException
     */
    public Enumeration getNonMatchingHeaderLines(String[] names)
                                        throws MessagingException {
	try {
	    return getMessage().getNonMatchingHeaderLines(names);
	} catch (FolderClosedException fce) {
	    int status = parent.getStatus();
	    if (status == FolderInfo.CONNECTED || status == FolderInfo.LOST_CONNECTION) {
		try {
		    parent.openFolder(Folder.READ_WRITE);
		} catch (MessagingException me) {
		    throw fce;
		}

		return getMessage().getNonMatchingHeaderLines(names);
		
	    } else {
		throw fce;
	    }
	}
    }

    /**
     * Return a <code>Flags</code> object containing the flags for 
     * this message. <p>
     *
     * Note that a clone of the internal Flags object is returned, so
     * modifying the returned Flags object will not affect the flags
     * of this message.
     *
     * @return          Flags object containing the flags for this message
     * @exception  	MessagingException
     * @see 		javax.mail.Flags
     */
    public synchronized Flags getFlags() throws MessagingException {
	try {
	    return getMessage().getFlags();
	} catch (FolderClosedException fce) {
	    int status = parent.getStatus();
	    if (status == FolderInfo.CONNECTED || status == FolderInfo.LOST_CONNECTION) {
		try {
		    parent.openFolder(Folder.READ_WRITE);
		} catch (MessagingException me) {
		    throw fce;
		}

		return getMessage().getFlags();
		
	    } else {
		throw fce;
	    }
	}
    }

    /**
     * Check whether the flag specified in the <code>flag</code>
     * argument is set in this message. <p>
     *
     * This implementation checks this message's internal 
     * <code>flags</code> object.
     *
     * @param flag	the flag
     * @return		value of the specified flag for this message
     * @see 		javax.mail.Flags.Flag
     * @see		javax.mail.Flags.Flag#ANSWERED
     * @see		javax.mail.Flags.Flag#DELETED
     * @see		javax.mail.Flags.Flag#DRAFT
     * @see		javax.mail.Flags.Flag#FLAGGED
     * @see		javax.mail.Flags.Flag#RECENT
     * @see		javax.mail.Flags.Flag#SEEN
     * @exception       MessagingException
     */
    public synchronized boolean isSet(Flags.Flag flag)
				throws MessagingException {
	try {
	    return getFlags().contains(flag);
	} catch (FolderClosedException fce) {
	    int status = parent.getStatus();
	    if (status == FolderInfo.CONNECTED || status == FolderInfo.LOST_CONNECTION) {
		try {
		    parent.openFolder(Folder.READ_WRITE);
		} catch (MessagingException me) {
		    throw fce;
		}

		return getFlags().contains(flag);
		
	    } else {
		throw fce;
	    }
	}
    }

    /**
     * Set the flags for this message. <p>
     *
     * This implementation modifies the <code>flags</code> field.
     *
     * @exception	IllegalWriteException if the underlying
     *			implementation does not support modification
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception  	MessagingException
     */
    public synchronized void setFlags(Flags flag, boolean set)
			throws MessagingException {
	try {
	    getMessage().setFlags(flag, set);
	} catch (FolderClosedException fce) {
	    int status = parent.getStatus();
	    if (status == FolderInfo.CONNECTED || status == FolderInfo.LOST_CONNECTION) {
		try {
		    parent.openFolder(Folder.READ_WRITE);
		} catch (MessagingException me) {
		    throw fce;
		}

		getMessage().setFlags(flag, set);
		
	    } else {
		throw fce;
	    }
	}
    }

    public long getUID() {
	return uid;
    }

    public long getUIDValidity() {
	return parent.getUIDValidity();
    }

    public MimeMessage getMessage() throws MessagingException {
	return parent.getRealMessageById(uid);
    }
}




