package net.suberic.pooka.gui;
import net.suberic.pooka.*;
import java.util.*;
import java.awt.print.*;
import java.awt.Graphics;
import java.awt.Font;
import javax.mail.internet.MimeMessage;
import javax.swing.*;
import javax.mail.MessagingException;

public class MessagePrinter implements Printable {
    
    private MessageInfo message;
    private PageFormat pf;
    private ArrayList pages;
    private Font font= new Font ("TimesRoman", Font.PLAIN, 12);
    private int offset;
    
    /**
     * This creates a new MessagePrinter for the given MessageInfo.
     */
    public MessagePrinter(MessageInfo mi, int newOffset) {
	message = mi;
	offset = newOffset;
    }

    public MessagePrinter(MessageInfo mi) {
	this(mi, 0);
    }

    /**
     * This calculates the number of pages using the given PageFormat
     * that it will take to print the message.
     */
    public int getPageCount(PageFormat format) throws MessagingException {
	if (pf != format) {
	    pf = format;
	    pages = calculatePages();
	}
	return pages.size();
    }

    private ArrayList calculatePages() throws MessagingException {
	// step through articles, creating pages of lines
	int maxh = (int) pf.getImageableHeight ();
	int lineh = font.getSize ();
	ArrayList pgs = new ArrayList ();
	String content;

	if (Pooka.getProperty("Pooka.displayTextAttachments", "").equalsIgnoreCase("true")) 
	    content = net.suberic.pooka.MailUtilities.getTextAndTextInlines((MimeMessage)message.getMessage(), Pooka.getProperty("Pooka.attachmentSeparator", "\n\n"), false, true);
	
        else
	    content = net.suberic.pooka.MailUtilities.getTextPart((MimeMessage)message.getMessage(), false, true);
	
	StringTokenizer st = new StringTokenizer (content, "\n");

	ArrayList page = new ArrayList ();
	int pageh = 0;
	
	while (st.hasMoreTokens ()) {
	    String line = st.nextToken ();
	    if (pageh + lineh > maxh) {
		// need new page
		pgs.add (page);
		page = new ArrayList ();
		pageh = 0;
	    } 
	    page.add (line);
	    pageh += lineh;
	}
	pgs.add (page);
	
	return pgs;
    }

    private void renderPage(Graphics g, PageFormat pf, int idx) {
	// render the lines from the pages list
	int xo = (int) pf.getImageableX ();
	int yo = (int) pf.getImageableY ();
	int y = font.getSize ();
	ArrayList page = (ArrayList) pages.get (idx);
	Iterator it = page.iterator ();
	while (it.hasNext ()) {
	    String line = (String) it.next ();
	    g.drawString (line, xo, y + yo);
	    y += font.getSize ();
	}
    }

    /**
     * This actually prints the given page.
     */
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
	try {
	    if (pageFormat != pf) {
		pf = pageFormat;
		pages = calculatePages();
	    }
	    
	    if (pageIndex - offset >= pages.size ()) {
		return Printable.NO_SUCH_PAGE;
	    }
	    
	    renderPage (graphics, pf, pageIndex - offset);
	    return Printable.PAGE_EXISTS;
	} catch (MessagingException me) {
	    return Printable.NO_SUCH_PAGE;
	}
    }
}
