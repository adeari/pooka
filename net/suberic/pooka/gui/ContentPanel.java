package net.suberic.pooka.gui;

public interface ContentPanel extends net.suberic.pooka.UserProfileContainer, ActionContainer {
  public javax.swing.JComponent getUIComponent();
  
  public void setUIComponent(javax.swing.JComponent comp);
  
  public void showHelpScreen(String title, java.net.URL url);
  
  public void openSavedFolders(java.util.Vector folderList);
  
  public void saveOpenFolders();
  
  public void savePanelSize();
    
  public boolean isSavingOpenFolders();
}
