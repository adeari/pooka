package net.suberic.pooka.gui;
import javax.swing.text.WrappedPlainView;
import javax.swing.text.Segment;
import javax.swing.text.Element;
import net.suberic.pooka.MailUtilities;

/**
 * This class extends javax.swing.text.WrappedPlainView.  Its primary
 * purpose is to change the word wrapping methodology so that the text
 * of new messages is wrapped on the screen at a character boundary, just
 * as it will be when it's actually sent.  So in other words, this 
 * class makes it so, if you have your mail client set to line wrap at 72
 * characters, your mail entry window will also wrap at 72 characters, no
 * matter what the size of your font or your window is.
 */
public class MailWrappedView extends WrappedPlainView {

    public MailWrappedView(Element elem) {
	super(elem);
    }

    public MailWrappedView(Element elem, boolean wordWrap) {
	super(elem, wordWrap);
    }

    private int characterLength = 72;

    private Segment lineBuffer = new Segment();

    /**
     * This implementation will break the line at the character length
     * returned by getCharacterLength().
     *
     * Overrides <code>WrappedPlainView.calculateBreakPosition</code>
     */
    protected int calculateBreakPosition(int p0, int p1) {
	int p;
	try {
	    getDocument().getText(p0, p1 - p0, lineBuffer);
	    return MailUtilities.getBreakPosition(lineBuffer.array, p0, p1, getCharacterLength());
	} catch (javax.swing.text.BadLocationException ble) {
	    return p1;
	}
    } 

    public int getCharacterLength() {
	return characterLength;
    }

    public void setCharacterLength(int newCharacterLength) {
	characterLength = newCharacterLength;
    }
}

