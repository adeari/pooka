package net.suberic.pooka.gui;
import java.awt.print.*;
import java.awt.Graphics;
import javax.swing.*;

public class MessagePrinter implements Printable {
    
    private MessageProxy message;

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
	return 1;
    }

    /**
     * This actually prints the given page.
     */
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
	return Printable.PAGE_EXISTS;
    }
}
