package net.suberic.pooka.gui;
import net.suberic.pooka.*;
import java.util.*;
import java.awt.print.*;
import java.awt.Graphics;
import java.awt.Font;
import javax.mail.internet.MimeMessage;
import javax.swing.*;

public class MessagePrinter implements Printable {
    
    private MessageProxy message;
    private PageFormat pf;
    private ArrayList pages;
    private Font font= new Font ("TimesRoman", Font.PLAIN, 12);
    
    /**
     * This creates a new MessagePrinter for the given MessageProxy.
     */
    public MessagePrinter(MessageProxy mp) {
	message = mp;
    }

    /**
     * This calculates the number of pages using the given PageFormat
     * that it will take to print the message.
     */
    public int getPageCount(PageFormat format) {
	if (pf != format) {
	    pf = format;
	    pages = calculatePages();
	}
	return pages.size();
    }

    private ArrayList calculatePages() {
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
	if (pageFormat != pf) {
	    pf = pageFormat;
	    pages = calculatePages();
	}

	if (pageIndex >= pages.size ()) {
	    return Printable.NO_SUCH_PAGE;
	}

	renderPage (graphics, pf, pageIndex);
	return Printable.PAGE_EXISTS;
    }
}
