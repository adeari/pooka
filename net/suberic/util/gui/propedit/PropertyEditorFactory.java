package net.suberic.util.gui.propedit;
import javax.swing.*;
import net.suberic.util.*;
import java.awt.*;
import java.util.Vector;

/**
 * A factory which can be used to create PropertyEditorUI's.
 */
public class PropertyEditorFactory {
  
  // the VariableBundle that holds both the properties and the editor
  // definitions.

  VariableBundle sourceBundle;

  /**
   * Creates a PropertyEditorFactory using the given VariableBundle as
   * a source.
   */
  public PropertyEditorFactory(VariableBundle bundle) {
    sourceBundle = bundle;
  }
  
}


    



