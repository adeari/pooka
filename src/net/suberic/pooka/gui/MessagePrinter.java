package net.suberic.pooka.gui;
import net.suberic.pooka.*;
import java.util.*;
import java.awt.print.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Dimension;
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

	int displayLength = 100000;
	try {
	    displayLength = Integer.parseInt(Pooka.getProperty("Pooka.attachmentDisplayMaxLength", "100000"));
	} catch (NumberFormatException nfe) {
	}
	if (Pooka.getProperty("Pooka.displayTextAttachments", "").equalsIgnoreCase("true")) {
	    content = message.getTextAndTextInlines(Pooka.getProperty("Pooka.attachmentSeparator", "\n\n"), true, false, displayLength, "foo");
	    
        } else
	    content = message.getTextPart(true, false, displayLength, "foo");
	
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
  /*
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
  */
  JTextPane jtp = null;

  /**
   * This actually prints the given page.
   */
  public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
    try {
      if (jtp == null) {
	createTextPane();
      }
      
    /*
      System.err.println("jtp.getHeight() = " + jtp.getHeight());
      
      double height = pageFormat.getImageableHeight();
      
      if (height * (pageIndex - 1) > jtp.getHeight()) {
	return Printable.NO_SUCH_PAGE;
      } else {
	jtp.print(graphics);
	return Printable.PAGE_EXISTS;
      }
    */

    Graphics2D g2 = (Graphics2D)graphics;

    Dimension d = jtp.getSize(); //get size of document
    double panelWidth = d.width; //width in pixels
    double panelHeight = d.height; //height in pixels

    double pageHeight = pageFormat.getImageableHeight(); //height of printer page
    double pageWidth = pageFormat.getImageableWidth(); //width of printer page

    double scale = pageWidth/panelWidth;

    //double scale = 1;

    int totalNumPages = (int)Math.ceil(scale * panelHeight / pageHeight);

    //make sure not print empty pages
    if(pageIndex >= totalNumPages) { 
      return Printable.NO_SUCH_PAGE;
    }
    
    //shift Graphic to line up with beginning of print-imageable region
    g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

    //shift Graphic to line up with beginning of next page to print
    g2.translate(0f, -pageIndex*pageHeight);

    //scale the page so the width fits...
    g2.scale(scale, scale);
    
    jtp.paint(g2); //repaint the page for printing
    
    return Printable.PAGE_EXISTS;
    
    } catch (MessagingException me) {
      me.printStackTrace();
      return Printable.NO_SUCH_PAGE;
    }
  }

  /**
   * Creates the appropriate 
   */
  public void createTextPane() throws MessagingException {
    jtp = new JTextPane();

    StringBuffer messageText = new StringBuffer();

    String content = null;
    
    String contentType = "text/plain";
    
    boolean displayHtml = false;
    
    int msgDisplayMode = message.getMessageProxy().getDisplayMode();
    
    // figure out html vs. text
    if (Pooka.getProperty("Pooka.displayHtml", "").equalsIgnoreCase("true")) {
      if (message.isHtml()) {
	if (msgDisplayMode > MessageProxy.TEXT_ONLY) 
	  displayHtml = true;
	
      } else if (message.containsHtml()) {
	if (msgDisplayMode >= MessageProxy.HTML_PREFERRED)
	  displayHtml = true;
	
      } else {
	// if we don't have any html, just display as text.
      }
    }
    
    // set the content
    if (msgDisplayMode == MessageProxy.RFC_822) {
      content = message.getRawText();
    } else {
      if (displayHtml) {
	contentType = "text/html";
	
	if (Pooka.getProperty("Pooka.displayTextAttachments", "").equalsIgnoreCase("true")) {
	  content = message.getHtmlAndTextInlines(true, false);
	} else {
	  content = message.getHtmlPart(true, false);
	}
      } else {
	if (Pooka.getProperty("Pooka.displayTextAttachments", "").equalsIgnoreCase("true")) {
	  // Is there only an HTML part?  Regardless, we've determined that 
	  // we will still display it as text.
	  if (message.isHtml())
	    content = message.getHtmlAndTextInlines(true, false);
	  else
	    content = message.getTextAndTextInlines(true, false);
	} else {
	  // Is there only an HTML part?  Regardless, we've determined that 
	  // we will still display it as text.
	  if (message.isHtml())
	    content = message.getHtmlPart(true, false);
	  else
	    content = message.getTextPart(true, false);
	}
      }
    }
    
    if (content != null)
      messageText.append(content);

    jtp.setContentType(contentType);
    jtp.setText(messageText.toString());

    jtp.addNotify();
    jtp.setSize(jtp.getPreferredSize());
    jtp.setVisible(true);

  }
}
