package net.suberic.pooka.gui;
import net.suberic.pooka.*;

public interface MessageUI extends UserProfileContainer, ErrorHandler, ActionContainer {

    public void openMessageUI();

    public void closeMessageUI();

    public MessageProxy getMessageProxy();

    public String getMessageText();

    public String getMessageContentType();

    public String showInputDialog(String inputMessage, String title);

    public String showInputDialog(Object[] inputPanels, String title);
    
    public int showConfirmDialog(String message, String title, int optionType, int messageType);

    public void setBusy(boolean newValue);

    public void setEnabled(boolean newValue);

}
