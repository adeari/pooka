package net.suberic.util.gui;

public interface AkpPropertyEditor {
    public void setValue();

    public void resetDefaultValue();

    public java.util.Properties getValue();

    public void setEnabled(boolean newValue);

    public boolean isEnabled();
}
