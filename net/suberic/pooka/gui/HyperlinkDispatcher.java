package net.suberic.pooka.gui;
import net.suberic.pooka.*;
import javax.swing.event.*;
import java.util.StringTokenizer;

/**
 * This is a simple class which implements HyperlinkListener.
 */

public class HyperlinkDispatcher implements HyperlinkListener {

    public HyperlinkDispatcher() {
    }

    /**
     * This handles HyperlinkEvents.  For now, we're just taking
     * ACTIVATED events, and dispatching the url's to the external
     * program indicated by Pooka.urlHandler.
     * 
     * Specified in javax.swing.event.HyperlinkListener.
     */
    public void hyperlinkUpdate(HyperlinkEvent e) {
	if (e.getEventType() ==  HyperlinkEvent.EventType.ACTIVATED) {
	    String parsedVerb = Pooka.getProperty("Pooka.urlHandler", "netscape %s");
	    if (parsedVerb.indexOf("%s") == -1)
		parsedVerb = parsedVerb + " %s";
	    
	    String[] cmdArray;

	    parsedVerb = ExternalLauncher.substituteString(parsedVerb, "%s", e.getURL().toString());
		
	    StringTokenizer tok = new StringTokenizer(parsedVerb);
	    cmdArray = new String[tok.countTokens()];
	    for (int i = 0; tok.hasMoreTokens(); i++) {
		String currentString = tok.nextToken();
		cmdArray[i]=currentString;
	    }
	    try {
		Runtime.getRuntime().exec(cmdArray);
	    } catch (java.io.IOException ioe) {
		System.out.println("caught error:  " + ioe);
	    }
	}
    }
}

