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
  private int offset;
  private MessagePrinterDisplay mDisplay = null;
  
  JTextPane jtp = null;

  int mPageCount = 0;
  double[] mPageBreaks = null;
  double mScale = 1;

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
  public int getPageCount() {
    return mPageCount;
  }

  /**
   * Calculates the page breaks for this component.
   */
  public void doPageCalculation(PageFormat pageFormat) {
    double pageHeight = pageFormat.getImageableHeight();
    double pageWidth = pageFormat.getImageableWidth(); 

    boolean needsResize = false;

    java.awt.Dimension minSize = jtp.getMinimumSize();

    double newWidth = Math.max(minSize.getWidth(), pageWidth);
    
    java.awt.Dimension newSize = new java.awt.Dimension();
    newSize.setSize(newWidth, jtp.getSize().getHeight());
    jtp.setSize(newSize);
    
    if (jtp.getSize().getHeight() < jtp.getMinimumSize().getHeight()) {
      java.awt.Dimension finalSize = new java.awt.Dimension();
      finalSize.setSize(jtp.getSize().getWidth(), jtp.getMinimumSize().getHeight());
      jtp.setSize(finalSize);
    }

    java.awt.Dimension d = jtp.getSize();

    double panelWidth = d.getWidth(); 
    double panelHeight = d.getHeight(); 
    
    jtp.setVisible(true);

    // don't scale below 1.
    mScale = Math.min(1,pageWidth/panelWidth);

    //mScale = 1;
    
    mPageCount = (int)Math.ceil(mScale * panelHeight / pageHeight);
    
    java.util.List breakList = new java.util.ArrayList();
    
    int counter = 0;

    mPageBreaks = new double[mPageCount];

    for (int i = 0 ; i < mPageCount; i++) {
      mPageBreaks[i] = i*pageHeight;
    }

    if (mDisplay != null) {
      mDisplay.setPageCount(mPageCount);
    }

  }

  /**
   * This actually prints the given page.
   */
  public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
    try {
      if (jtp == null) {
	createTextPane();
      }

      if (mPageBreaks == null) {
	doPageCalculation(pageFormat);
      }

      //make sure not print empty pages
      if(pageIndex >= mPageCount) { 
	return Printable.NO_SUCH_PAGE;
      }
      
      if (mDisplay != null) {
	mDisplay.setCurrentPage(pageIndex + 1);
      }
      
      Graphics2D g2 = (Graphics2D)graphics;
      
      //shift Graphic to line up with beginning of print-imageable region
      g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
      
      //shift Graphic to line up with beginning of next page to print
      //g2.translate(0f, -pageIndex*pageHeight);
      g2.translate(0f, -mPageBreaks[pageIndex]);
      
      //scale the page so the width fits...
      g2.scale(mScale, mScale);
      
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

    //jtp.addNotify();
    jtp.setSize(jtp.getPreferredSize());

    //jtp.setVisible(true);
    
  }

  /**
   * Sets the PrinterDisplay for this MessagePrinter.
   */
  public void setDisplay(MessagePrinterDisplay pDisplay) {
    mDisplay = pDisplay;
  }

  /**
   * Returns the JTextPane for this Printer.
   */
  public JTextPane getTextPane() {
    return jtp;
  }
}
