package net.suberic.pooka.gui;
import net.suberic.pooka.*;

public interface FolderDisplayUI extends UserProfileContainer, ErrorHandler, ActionContainer {
    
    public void openFolderDisplay();
    
    public void closeFolderDisplay();

    public FolderInfo getFolderInfo();

    public void setEnabled(boolean newValue);

    public void setBusy(boolean newValue);

    public String showInputDialog(String inputMessage, String title);
}
