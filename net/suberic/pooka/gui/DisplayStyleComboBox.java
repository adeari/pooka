package net.suberic.pooka.gui;
import java.util.*;

/**
 * A ConfigurableComboBox that should remain in sync with the display
 * style.
 */
public class DisplayStyleComboBox extends net.suberic.util.gui.ConfigurableComboBox {
  /*
  ReadMessageDisplayPanel messagePanel;
  
  HashMap commandToItemMap = new HashMap();
  */
  
  public boolean displayStyle = false;
  public boolean headerStyle = false;

  /**
   * This configures the ComboBox using the given buttonID and 
   * VariableBundle.
   *
   * As defined in interface net.suberic.util.gui.ConfigurableUI.
   */
  public void configureComponent(String key, net.suberic.util.VariableBundle vars) {
    super.configureComponent(key, vars);

    // set whether this is a header combo, a display combo, or both.
    displayStyle = true;

    /*
    Set keys = selectionMap.keySet();
    Iterator keyIter = keys.iterator();
    while (keyIter.hasNext()) {
      Object currentKey = keyIter.next();
      Object value = selectionMap.get(currentKey);
      System.err.println("adding " + value + ", " + currentKey + " to commandToItemMap.");
      commandToItemMap.put(value, currentKey);
    }
    */
  } 

  /**
   * Called when either style is updated.
   */
  public void styleUpdated(int newDisplayStyle, int newHeaderStyle) {
    System.err.println("style updated.");
    // find out which of the items we have corresponds to the given display
    // and/or header style.

    for (int i = 0; i < getItemCount(); i++) {
      String cmd = (String) selectionMap.get(getItemAt(i));
      if (cmd != null) {
	javax.swing.Action currentAction = getAction(cmd);
	if (currentAction != null && currentAction instanceof MessageProxy.OpenAction) {
	  MessageProxy.OpenAction oa = (MessageProxy.OpenAction) currentAction;
	  if (((displayStyle && (oa.getDisplayModeValue() == newDisplayStyle)) || !displayStyle) && ((headerStyle && (oa.getHeaderModeValue() == newHeaderStyle)) || !headerStyle)) {
	    if (getSelectedIndex() != i) {
	      setSelectedIndex(i);
	    }
	  }
	}
      }
    }
  }

}
