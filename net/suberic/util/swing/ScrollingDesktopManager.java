package net.suberic.util.swing;
import javax.swing.*;
import java.awt.event.*;

/**
 * <p>
 * This is a DesktopManager for a JDesktopPane which dynamically resizes
 * when a JInternalFrame is moved out of the visible portion of the 
 * desktop.  This means that all parts of your JInteralFrames will be 
 * available via the ScrollBars at all time.
 * </p>
 *
 * <p>
 * Currently, to use this class you need to set it as the DesktopManager
 * of your JDesktopPane, and also register the JDesktopPane and its
 * JScrollPane with this ScrollingDesktopManager:
 * </p>
 *
 * <pre>
 * JDesktopPane desktopPane = new JDesktopPane();
 * JScrollPane scrollPane = new JScrollPane(desktopPane, 
 *   JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED, 
 *   JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 * ScrollingDesktopManager manager = new ScrollingDesktopManager(desktopPane,
 *   scrollPane);
 * desktopPane.setDesktopManager(manager);
 * </pre>
 *
 * <p>
 * Note that it only makes sense to use this class with the 
 * SCROLLBAR_AS_NEEDED and SCROLLBAR_ALWAYS options on the JScrollPane.
 * </p>
 *
 * @see javax.swing.JDesktopPane
 * @see javax.swing.JScrollBar
 * @see javax.swing.DefaultDesktopManager
 * @version 1.0 6/28/2000
 * @author Allen Petersen
 */

public class ScrollingDesktopManager extends DefaultDesktopManager 
    implements ContainerListener, AdjustmentListener {

    private JScrollPane scrollPane = null;
    
    private JDesktopPane pane = null;

    private boolean updating = false;

    /**
     * <p>
     * This creates a ScrollingDesktopManager for JDesktopPane pane
     * in JScrollPane scrollPane.
     * </p>
     */
    public ScrollingDesktopManager(JDesktopPane pane, JScrollPane scrollPane) {
	super();
	setDesktopPane(pane);
	setScrollPane(scrollPane);
    }

    /**
     * <p>This extends the behaviour of DefaultDesktopManager by 
     * calling <code>updateDesktopSize()</code> after
     * completing its action.
     * 
     * Overrides 
     * <code>javax.swing.DefaultDesktopManager.closeFrame(JInternalFrame f).
     * </code>
     */
    public void closeFrame(JInternalFrame f) {
	super.closeFrame(f);
	updateDesktopSize();
    }
    
    /**
     * <p>This extends the behaviour of DefaultDesktopManager by 
     * calling <code>updateDesktopSize()</code> after
     * completing its action.
     * 
     * Overrides 
     * <code>javax.swing.DefaultDesktopManager.minimizeFrame(JInternalFrame f).
     * </code>
     */
    public void minimizeFrame(JInternalFrame f) {
	super.minimizeFrame(f);
	updateDesktopSize();

    }

    /**
     * <p>This extends the behaviour of DefaultDesktopManager by 
     * calling <code>updateDesktopSize()</code> after
     * completing its action.
     * 
     * Overrides 
     * <code>javax.swing.DefaultDesktopManager.iconifyFrame(JInternalFrame f).
     * </code>
     */
    public void iconifyFrame(JInternalFrame f) {
	super.iconifyFrame(f);
	updateDesktopSize();
    }
    
    /**
     * <p>This extends the behaviour of DefaultDesktopManager by 
     * calling <code>updateDesktopSize()</code> after
     * completing its action.
     * 
     * Overrides 
     * <code>javax.swing.DefaultDesktopManager.deiconifyFrame(JInternalFrame f).
     * </code>
     */
    public void deiconifyFrame(JInternalFrame f) {
	super.deiconifyFrame(f);
	updateDesktopSize();
    }

    /**
     * <p>This extends the behaviour of DefaultDesktopManager by 
     * calling <code>updateDesktopSize()</code> after
     * completing its action.
     * 
     * Overrides 
     * <code>javax.swing.DefaultDesktopManager.endDraggingFrame(JComponent f).
     * </code>
     */
    public void endDraggingFrame(JComponent f) {
	super.endDraggingFrame(f);

	updateDesktopSize();
    }

    /**
     * <p>This extends the behaviour of DefaultDesktopManager by 
     * calling <code>updateDesktopSize()</code> after
     * completing its action.
     * 
     * Overrides 
     * <code>javax.swing.DefaultDesktopManager.endResizingFrame(JComponent f).
     * </code>
     */

    public void endResizingFrame(JComponent f) {
	super.endResizingFrame(f);
	updateDesktopSize();
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
	if (scrollPane != null) {
	    java.awt.Dimension newSize = scrollPane.getViewport().getSize();
	    pane.setSize(newSize);
	    pane.setPreferredSize(newSize);
	}
	
	super.maximizeFrame(f);

    }

    /**
     * Implements componentRemoved() to call updateDesktopSize().
     */
    public void componentRemoved(java.awt.event.ContainerEvent e) {
	updateDesktopSize();
    }

    /**
     * Implements componentAdded() to call updateDesktopSize().
     */
    public void componentAdded(java.awt.event.ContainerEvent e) {
	updateDesktopSize();
    }

    /**
     * Implements adjustmentValueChanged() to call updateDesktopSize().
     */
    public void adjustmentValueChanged(java.awt.event.AdjustmentEvent e) {
	updateDesktopSize();
    }

    /**
     * This actually does the updating of the parent JDesktopPane.
     */
    public void updateDesktopSize() {
	if (!updating && scrollPane != null) {
	    updating = true;
	    // boolean oldBackingEnabled = scrollPane.getViewport().isBackingStoreEnabled();
	    // scrollPane.getViewport().setBackingStoreEnabled(false);

	    int oldScrollMode = scrollPane.getViewport().getScrollMode();
	    scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);


	    JScrollBar hsb = scrollPane.getHorizontalScrollBar();
	    JScrollBar vsb = scrollPane.getVerticalScrollBar();
	    
	    // calculate the min and max locations for all the frames.
	    JInternalFrame[] allFrames = pane.getAllFrames();
	    int min_x = 0, min_y = 0, max_x = 0, max_y = 0;
	    java.awt.Rectangle bounds = null;
	    // add to this the current viewable area.
	    
	    if (allFrames.length > 0) {
		bounds = allFrames[0].getBounds(bounds);
		min_x = bounds.x;
		min_y = bounds.y;
		max_x = bounds.width + bounds.x;
		max_y = bounds.height + bounds.y;
		for (int i = 1; i < allFrames.length; i++) {
		    bounds = allFrames[i].getBounds(bounds);
		    min_x = Math.min(min_x, bounds.x);
		    min_y = Math.min(min_y, bounds.y);
		    max_x = Math.max(max_x, bounds.width + bounds.x);
		    max_y = Math.max(max_y, bounds.height + bounds.y);
		}
	    }
	    
	    int windowsWidth = max_x;
	    int windowsHeight = max_y;
	    
	    bounds = scrollPane.getViewport().getViewRect();
	    min_x = Math.min(min_x, bounds.x);
	    min_y = Math.min(min_y, bounds.y);
	    max_x = Math.max(max_x, bounds.width + bounds.x);
	    max_y = Math.max(max_y, bounds.height + bounds.y);

	    printstats(pane, scrollPane, "pre; min_x = " + min_x + ", min_y = " + min_y  + ", max_x  = " + max_x + ", max_y =" + max_y);
	    
	    if (min_x != 0 || min_y != 0) {
		for (int i = 0; i < allFrames.length; i++) {
		    allFrames[i].setLocation(allFrames[i].getX() - min_x, allFrames[i].getY() - min_y);
		    
		    /*
		    System.out.println("moving frame " + i);
		    try {
			Thread.sleep(5000);
		    } catch (Exception e) {
		    }
		    */
		}

		windowsWidth = windowsWidth - min_x;
		windowsHeight = windowsHeight - min_y;
	    }

	    int hval = hsb.getValue();
	    int vval = vsb.getValue();
	    
	    bounds = scrollPane.getViewport().getViewRect();
	    int oldViewWidth = bounds.width + hval;
	    int oldViewHeight = bounds.height + vval;

	    int portWidth = scrollPane.getViewport().getSize().width;
	    int portHeight = scrollPane.getViewport().getSize().height;

	    java.awt.Dimension dim = pane.getSize();
	    int oldWidth = dim.width;
	    int oldHeight = dim.height;

	    pane.setSize(max_x - min_x, max_y - min_y);

	    /*
	    System.out.println("reset size.");
	    try {
		Thread.sleep(5000);
	    } catch (Exception e) {
	    }
	    */

	    /*********************************/

	    int prefWidth = max_x - min_x;
	    int prefHeight = max_y - min_y;

	    System.out.println("newWidth = " + prefWidth + ", newHeight = " + prefHeight);

	    boolean hasVsb = false, needsVsb = false, hasHsb = false, needsHsb = false;
	    // if a scrollbar is added, check to see if the space covered
	    // by the scrollbar is whitespace or not.  if not, remove that
	    // whitespace from the preferredsize.

	    if (vsb.isVisible()) {
		hasVsb = true;
	    } else {
		hasVsb = false;
	    }
	    
	    if (hsb.isVisible()) {
		hasHsb = true;
	    } else {
		hasHsb = false;
	    }

	    if (max_x - min_x > scrollPane.getViewport().getViewRect().width)
		needsHsb = true;
	    else
		needsHsb = false;

	    if (max_y - min_y > scrollPane.getViewport().getViewRect().height)
		needsVsb = true;
	    else
		needsVsb = false;
	
	    System.out.println("has/needs hsb, has/needs vsb:  " + hasHsb + needsHsb + hasVsb + needsVsb);

	    if (hasVsb == false && needsVsb == true) {
		if (windowsWidth < bounds.width - vsb.getPreferredSize().width)
		    prefWidth-=vsb.getPreferredSize().width;
	    } else if (hasVsb == true && needsVsb == false) {
		if (max_x <= bounds.width)
		    prefWidth+= vsb.getPreferredSize().width;
	    }

	    if (hasHsb == false && needsHsb == true) {
		if (windowsHeight < bounds.height - hsb.getPreferredSize().height) {
		    prefHeight-=hsb.getPreferredSize().height;
		}
	    } else if (hasHsb == true && needsHsb == false) {
		if (max_y <= bounds.height) {
		    System.out.println("changing prefHeight from " + prefHeight + " to (" + prefHeight + " + " + hsb.getPreferredSize().height + ").  ");
		    prefHeight+= hsb.getPreferredSize().height;
		}
	    }
		    
	    /**************************************/
	    
	    System.out.println("\nprefWidth = " + prefWidth + ", prefHeight = " + prefHeight + "\n\n");
	    
	    pane.setPreferredSize(new java.awt.Dimension(prefWidth, prefHeight));
	    scrollPane.validate();
	    
	    /*
	    System.out.println("did validate.");
	    try {
		Thread.sleep(5000);
	    } catch (Exception e) {
	    }	    
	    */
	    
	    hsb = scrollPane.getHorizontalScrollBar();
	    vsb = scrollPane.getVerticalScrollBar();
	    
	    if (min_x != 0 && hval - min_x + hsb.getModel().getExtent() > hsb.getMaximum()) {
		hsb.setMaximum(hval - min_x + hsb.getModel().getExtent());
	    }
	    
	    if (min_y != 0 && vval - min_y + vsb.getModel().getExtent() > vsb.getMaximum()) {
		vsb.setMaximum(vval - min_y + vsb.getModel().getExtent());
	    }

	    /*
	    System.out.println("about to reset scrollbars.");
	    try {
		Thread.sleep(5000);
	    } catch (Exception e) {
	    }
	    */

	    hsb.setValue(hval - min_x);
	    
	    vsb.setValue(vval - min_y);

	    /*
	    System.out.println("done resetting scrollbars.");
	    try {
		Thread.sleep(5000);
	    } catch (Exception e) {
	    }
	    */

	    scrollPane.getViewport().setScrollMode(oldScrollMode);
	    //	    scrollPane.getViewport().setBackingStoreEnabled(oldBackingEnabled);

	    updating = false;
	}
    }
 
    public void printstats(JDesktopPane pane, JScrollPane scrollPane, String message) {
	System.out.println("\n" + message);
	JViewport viewport = scrollPane.getViewport();
	JInternalFrame[] allFrames = pane.getAllFrames();
	
	System.out.println("getBounds() of MessagePanel is " + pane.getBounds());
	System.out.println("getPreferredSize() of MessagePanel is " + pane.getPreferredSize());
	    System.out.println("getViewRect() is JViewport is " + viewport.getViewRect());
	    System.out.println("getSize() is JViewport is " + viewport.getSize());
	    System.out.println("HSB.getValue() is " + scrollPane.getHorizontalScrollBar().getValue() + ", hsb.isVisible = " + scrollPane.getHorizontalScrollBar().isVisible());
	    System.out.println("VSB.getValue() is " + scrollPane.getVerticalScrollBar().getValue() + ", VSB.isVisible() = " + scrollPane.getVerticalScrollBar().isVisible());
	    for (int i = 0; i < allFrames.length; i++) {
		System.out.println("allFrames[i] = " + allFrames[i].getLocation());
		System.out.println("convertPoint(allFrames[" + i + "]) = " + SwingUtilities.convertPoint(allFrames[i], 0, 0, pane));
	    } 
    }

    public void setScrollPane(JScrollPane newScrollPane) {
	if (scrollPane != null) {
	    scrollPane.getHorizontalScrollBar().removeAdjustmentListener(this);
	    scrollPane.getVerticalScrollBar().removeAdjustmentListener(this);
	}
	scrollPane = newScrollPane;
	scrollPane.getHorizontalScrollBar().addAdjustmentListener(this);
	scrollPane.getVerticalScrollBar().addAdjustmentListener(this);
    }

    public JScrollPane getScrollPane() {
	return scrollPane;
    }

    public void setDesktopPane(JDesktopPane newDesktopPane) {
	if (pane != null) 
	    pane.removeContainerListener(this);
	pane = newDesktopPane;
	pane.addContainerListener(this);
    }

    public JDesktopPane getDesktopPane() {
	return pane;
    }
}


