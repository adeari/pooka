package net.suberic.util.swing;
import javax.swing.*;

/**
 * This is a DesktopManager for a JDesktopPane which dynamically resizes
 * when a JInternalFrame is moved out of the visible portion of the 
 * desktop.  This means that all parts of your JInteralFrames will be 
 * available via the ScrollBars at all time.
 *
 * @see javax.swing.JDesktopPane
 * @see javax.swing.JScrollBar
 * @see javax.swing.DefaultDesktopManager
 * @version 1.0 6/24/2000
 * @author Allen Petersen
 */

public class ScrollingDesktopManager extends DefaultDesktopManager {
    /**
     * <p>This extends the behaviour of DefaultDesktopManager by 
     * calling <code>updateDesktopSize(JDesktopPane pane)</code> after
     * completing its action.
     * 
     * Overrides 
     * <code>javax.swing.DefaultDesktopManager.closeFrame(JInternalFrame f).
     * </code>
     */
    public void closeFrame(JInternalFrame f) {
	super.closeFrame(f);
	JDesktopPane pane = f.getDesktopPane();
	if (f != null)
	    updateDesktopSize(pane);
    }
    
    /**
     * <p>This extends the behaviour of DefaultDesktopManager by 
     * calling <code>updateDesktopSize(JDesktopPane pane)</code> after
     * completing its action.
     * 
     * Overrides 
     * <code>javax.swing.DefaultDesktopManager.minimizeFrame(JInternalFrame f).
     * </code>
     */
    public void minimizeFrame(JInternalFrame f) {
	super.minimizeFrame(f);
	JDesktopPane pane = f.getDesktopPane();
	if (f != null)
	    updateDesktopSize(pane);

    }

    /**
     * <p>This extends the behaviour of DefaultDesktopManager by 
     * calling <code>updateDesktopSize(JDesktopPane pane)</code> after
     * completing its action.
     * 
     * Overrides 
     * <code>javax.swing.DefaultDesktopManager.iconifyFrame(JInternalFrame f).
     * </code>
     */
    public void iconifyFrame(JInternalFrame f) {
	super.iconifyFrame(f);
	JDesktopPane pane = f.getDesktopPane();
	if (f != null)
	    updateDesktopSize(pane);
    }
    
    /**
     * <p>This extends the behaviour of DefaultDesktopManager by 
     * calling <code>updateDesktopSize(JDesktopPane pane)</code> after
     * completing its action.
     * 
     * Overrides 
     * <code>javax.swing.DefaultDesktopManager.deiconifyFrame(JInternalFrame f).
     * </code>
     */
    public void deiconifyFrame(JInternalFrame f) {
	super.deiconifyFrame(f);
	JDesktopPane pane = f.getDesktopPane();
	if (f != null)
	    updateDesktopSize(pane);
    }

    /**
     * <p>This extends the behaviour of DefaultDesktopManager by 
     * calling <code>updateDesktopSize(JDesktopPane pane)</code> after
     * completing its action.
     * 
     * Overrides 
     * <code>javax.swing.DefaultDesktopManager.endDraggingFrame(JComponent f).
     * </code>
     */
    public void endDraggingFrame(JComponent f) {
	super.endDraggingFrame(f);

	// JComponent????  and just when is this _not_ going to be a
	// JInternalFrame?  hmph. -akp

	if (f instanceof JComponent) {
	    JDesktopPane pane = ((JInternalFrame)f).getDesktopPane();
	    if (f != null)
		updateDesktopSize(pane);
	} else {
	    // search up the parent list until we find a JDesktopPane,
	    // i suppose.
	    java.awt.Component current = f;
	    while (current != null && ! (current instanceof JDesktopPane)) 
		current = current.getParent();

	    if (current != null)
		updateDesktopSize((JDesktopPane)current);
	}
    }

    /**
     * <p>This extends the behaviour of DefaultDesktopManager by 
     * calling <code>updateDesktopSize(JDesktopPane pane)</code> after
     * completing its action.
     * 
     * Overrides 
     * <code>javax.swing.DefaultDesktopManager.endResizingFrame(JComponent f).
     * </code>
     */
    public void endResizingFrame(JComponent f) {
	super.endResizingFrame(f);
	// JComponent????  and just when is this _not_ going to be a
	// JInternalFrame?  hmph. -akp

	if (f instanceof JComponent) {
	    JDesktopPane pane = ((JInternalFrame)f).getDesktopPane();
	    if (f != null)
		updateDesktopSize(pane);
	} else {
	    // search up the parent list until we find a JDesktopPane,
	    // i suppose.
	    java.awt.Component current = f;
	    while (current != null && ! (current instanceof JDesktopPane)) 
		current = current.getParent();

	    if (current != null)
		updateDesktopSize((JDesktopPane)current);
	}
    }

    /**
     * <p>This overrides maximizeFrame() such that it only maximizes to the
     * size of the Viewport, rather than to the entire size of the 
     * JDesktopPane.</p>
     *
     * Overrides 
     * <code>javax.swing.DefaultDesktopManager.maximizeFrame(JInternalFrame f)
     * </code>
     */
    public void maximizeFrame(JInternalFrame f) {
	JDesktopPane pane = f.getDesktopPane();
	JScrollPane scrollPane = getParentScrollPane(pane);
	if (scrollPane != null) {
	    java.awt.Dimension newSize = scrollPane.getViewport().getSize();
	    pane.setSize(newSize);
	    pane.setPreferredSize(newSize);
	}
	
	super.maximizeFrame(f);

    }

    /**
     * This actually does the updating of the parent JDesktopPane.
     */
    public void updateDesktopSize(JDesktopPane pane) {
	JScrollPane scrollPane = getParentScrollPane(pane);
	
	if (scrollPane != null) {
	    JScrollBar hsb = scrollPane.getHorizontalScrollBar();
	    JScrollBar vsb = scrollPane.getVerticalScrollBar();
	    
	    // calculate the min and max locations for all the frames.
	    JInternalFrame[] allFrames = pane.getAllFrames();
	    int min_x = 0, min_y = 0, max_x = 0, max_y = 0;
	    // add to this the current viewable area.
	    
	    java.awt.Rectangle bounds = scrollPane.getViewport().getViewRect();
	    min_x = bounds.x;
	    min_y = bounds.y;
	    max_x = bounds.width + bounds.x;
	    max_y = bounds.height + bounds.y;
	    
	    bounds = null;
	    for (int i = 0; i < allFrames.length; i++) {
		bounds = allFrames[i].getBounds(bounds);
		min_x = Math.min(min_x, bounds.x);
		min_y = Math.min(min_y, bounds.y);
		max_x = Math.max(max_x, bounds.width + bounds.x);
		max_y = Math.max(max_y, bounds.height + bounds.y);
	    }
	    
	    bounds = pane.getBounds(bounds);
	    int xdiff = 0;
	    int ydiff = 0;
	    if (min_x != bounds.x || min_y != bounds.y) {
		xdiff = bounds.x + hsb.getValue() - min_x;
		ydiff = bounds.y + vsb.getValue() - min_y;
		
		hsb = scrollPane.getHorizontalScrollBar();
		vsb = scrollPane.getVerticalScrollBar();
		
		min_x = min_x + xdiff;
		min_y = min_y + ydiff;
		max_x = max_x + xdiff;
		max_y = max_y + ydiff;
		for (int i = 0; i < allFrames.length; i++) {
		    allFrames[i].setLocation(allFrames[i].getX() + xdiff, allFrames[i].getY() + ydiff);
		}
		
	    }
	    
	    hsb = scrollPane.getHorizontalScrollBar();
	    vsb = scrollPane.getVerticalScrollBar();
	    
	    int hval = hsb.getValue();
	    int vval = vsb.getValue();
	    
	    pane.setSize(max_x - min_x, max_y - min_y);
	    pane.setPreferredSize(pane.getSize());
	    scrollPane.validate();
	    
	    hsb = scrollPane.getHorizontalScrollBar();
	    vsb = scrollPane.getVerticalScrollBar();
	    
	    if (hval + xdiff + hsb.getModel().getExtent() > hsb.getMaximum())
		hsb.setMaximum(hval + xdiff + hsb.getModel().getExtent());
	    if (vval + ydiff + vsb.getModel().getExtent() > vsb.getMaximum())
		vsb.setMaximum(vval + ydiff + vsb.getModel().getExtent());
	    
	    hsb.setValue(hval + xdiff);
	    vsb.setValue(vval + ydiff);
	}
    }
 
    /**
     * This returns the parent JScrollPane for the given JDesktopPane, or
     * null if the JDesktopPane isn't a child of a JScrollPane.
     */
    public JScrollPane getParentScrollPane(JDesktopPane pane) {
	java.awt.Component current = pane;
	while (current != null && ! (current instanceof JScrollPane)) 
	    current = current.getParent();

	return (JScrollPane)current;
    }
}
