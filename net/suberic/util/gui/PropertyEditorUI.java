package net.suberic.util.gui;
import net.suberic.util.VariableBundle;

public interface PropertyEditorUI {

    public void configureEditor(PropertyEditorFactory factory, String propertyName, String templateType, VariableBundle bundle, boolean isEnabled);

    public void configureEditor(String propertyName, VariableBundle bundle, boolean isEnabled);

    public void configureEditor(String propertyName, VariableBundle bundle);

    public void setValue();

    public void resetDefaultValue();

    public java.util.Properties getValue();

    public void setEnabled(boolean newValue);

    public boolean isEnabled();
}
