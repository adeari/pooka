package net.suberic.pooka.gui.propedit;
import net.suberic.util.gui.propedit.*;
import net.suberic.util.VariableBundle;
import net.suberic.pooka.gui.filechooser.*;
import net.suberic.pooka.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import javax.swing.filechooser.FileSystemView;

/**
 * This displays the currently selected folder (if any), along with a 
 * button which will bring up a dialog to select another folder.
 */

public class FolderSelectorPane extends SwingPropertyEditor {

  JLabel label;
  JTextField valueDisplay;
  JButton inputButton;
  VariableBundle sourceBundle;
  //boolean enabled;
  
  /**
   * @param propertyName The property to be edited.  
   * @param template The property that will define the layout of the 
   *                 editor.
   * @param manager The PropertyEditorManager that will manage the
   *                   changes.
   * @param isEnabled Whether or not this editor is enabled by default. 
   */
  public void configureEditor(String propertyName, String template, PropertyEditorManager newManager, boolean isEnabled) {
    property=propertyName;
    manager=newManager;
    editorTemplate = template;
    originalValue = manager.getProperty(property, "");

    if (debug) {
      System.out.println("property is " + property + "; editorTemplate is " + editorTemplate);
    }
    
    label = createLabel();
    
    valueDisplay = new JTextField(originalValue);
    
    inputButton = createInputButton();
    
    valueDisplay.setPreferredSize(new java.awt.Dimension(150 - inputButton.getPreferredSize().width, valueDisplay.getMinimumSize().height));
    
    this.add(label);
    labelComponent = label;
    JPanel tmpPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0,0));
    tmpPanel.add(valueDisplay);
    tmpPanel.add(inputButton);
    tmpPanel.setPreferredSize(new java.awt.Dimension(150, valueDisplay.getMinimumSize().height));
    valueComponent = tmpPanel;
    //this.add(valueDisplay);
    //this.add(inputButton);
    this.add(tmpPanel);
    
    this.setEnabled(isEnabled);
    
  }
  
  /**
   * Creates a button that will bring up a way to select a folder.
   */
  public JButton createInputButton() {
    if (Pooka.isDebug())
      System.out.println("creating an input button.");
    try {
      java.net.URL url = this.getClass().getResource(sourceBundle.getProperty("FolderSelectorPane.inputButton.image", "images/More.gif"));
      if (url != null) {
	if (Pooka.isDebug())
	  System.out.println("url isn't null.");
	
	ImageIcon icon = new ImageIcon(url);
	
	JButton newButton = new JButton(icon);
	if (Pooka.isDebug())
	  System.out.println("new button is created.");
	
	newButton.setPreferredSize(new java.awt.Dimension(icon.getIconHeight(), icon.getIconWidth()));
	newButton.addActionListener(new AbstractAction() {
	    public void actionPerformed(ActionEvent e) {
	      selectNewFolder();
	    }
	  });
	
	if (Pooka.isDebug())
	  System.out.println("returning button.");
	
	return newButton;
      }
    } catch (java.util.MissingResourceException mre) {
    }
    
    if (Pooka.isDebug())
      System.out.println("error - creating a blank button.");
    
    JButton newButton = new JButton();
    newButton.addActionListener(new AbstractAction() {
	public void actionPerformed(ActionEvent e) {
	  selectNewFolder();
	}
      });
    
    return newButton;
  }
  
  /**
   * This actually brings up a JFileChooser to select a new Folder for 
   * the value of the property.
   */
  public void selectNewFolder() {

    FileSystemView mfsv = createFileSystemView();
    
    String defaultRoot = valueDisplay.getText();
    if (defaultRoot.equals(""))
      defaultRoot = "/";
    
    JFileChooser jfc =
      new JFileChooser(defaultRoot, mfsv);
    jfc.setMultiSelectionEnabled(false);
    jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    
    int returnValue =
      jfc.showDialog(Pooka.getMainPanel(),
		     Pooka.getProperty("FolderEditorPane.Select",
				       "Select"));
    
    if (returnValue == JFileChooser.APPROVE_OPTION) {
      java.io.File wrapper = jfc.getSelectedFile();
      valueDisplay.setText(wrapper.getAbsolutePath());
    }
    
  }
  
  /**
   * Creates the FileSystemView appropriate for this file chooser.  This
   * can either be a view of all the stores and their corresponding 
   * folders, or just the folders of a single store.  This is determined
   * by the 'selectionRoot' subproperty of the edited property's template.
   */
  public FileSystemView createFileSystemView() {
    
    FileSystemView returnValue = null;
    boolean justSubscribed = sourceBundle.getProperty(editorTemplate + ".onlySubscribed", "false").equalsIgnoreCase("true");
    if (sourceBundle.getProperty(editorTemplate + ".selectionRoot", "allStores").equals("allStores")) {
      if (justSubscribed) 
	returnValue = new PookaFileSystemView();
      else 
	returnValue = new MailFileSystemView();
    } else {
      int prefixSize = sourceBundle.getProperty(editorTemplate + ".namePrefix", "Store.").length();
      int suffixSize = sourceBundle.getProperty(editorTemplate + ".nameSuffix", "trashFolder").length();
      String currentStoreName = property.substring(prefixSize, property.length() - suffixSize);
      net.suberic.pooka.StoreInfo currentStore = Pooka.getStoreManager().getStoreInfo(currentStoreName);
      if (currentStore != null) {
	if (justSubscribed)
	  returnValue = new PookaFileSystemView(currentStore);
	else 
	  returnValue = new MailFileSystemView(currentStore);
      }
    }
    
    return returnValue;
  }
  
  //  as defined in net.suberic.util.gui.PropertyEditorUI
  
  public void setValue() {
    if (Pooka.isDebug())
      System.out.println("calling fsp.setValue.  isEnabled() = " + isEnabled() + "; isChanged() = " + isChanged());
    if (isEnabled() && isChanged())
      sourceBundle.setProperty(property, (String)valueDisplay.getText());
  }
  
  public java.util.Properties getValue() {
    java.util.Properties retProps = new java.util.Properties();
    
    retProps.setProperty(property, (String)valueDisplay.getText());
    
    return retProps;
  }
  
  public void resetDefaultValue() {
    valueDisplay.setText(originalValue);
  }
  
  public boolean isChanged() {
    return (!(originalValue.equals(valueDisplay.getText())));
  }
  
  public void setEnabled(boolean newValue) {
    if (Pooka.isDebug())
      System.out.println("calling fsp.setEnabled(" + newValue + ")");
    if (inputButton != null) {
      inputButton.setEnabled(newValue);
      enabled=newValue;
      if (Pooka.isDebug())
	System.out.println("set enabled to " + newValue);
      
    }
  }
  
}
