package net.suberic.pooka.gui;
import net.suberic.pooka.*;

public interface MessageUI extends UserProfileContainer, ErrorHandler {

    public void closeMessageUI();

    public MessageProxy getMessageProxy();

    public String getMessageText();

    public String getMessageContentType();

    public String showInputDialog(String inputMessage, String title);

    public void setBusy(boolean newValue);

    public void setEnabled(boolean newValue);
}
