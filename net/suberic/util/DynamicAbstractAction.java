package net.suberic.util;
import javax.swing.AbstractAction;
import javax.swing.Action;

public abstract class DynamicAbstractAction extends AbstractAction {
	
	//java.util.HashSet properties = new java.util.HashSet();

	public DynamicAbstractAction() {
		super();
	}
	
	public DynamicAbstractAction(String cmd) {
		super(cmd);
	}
	
	public Action cloneDynamicAction() throws CloneNotSupportedException {
		//DynamicAbstractAction newAction = new DynamicAbstractAction();
		
		return (Action)this.clone();
	}

	/*
	public synchronized void putValue (String key, Object newValue) {
		super.putValue(key, newValue);
		properties.add(key);
	}
	*/
	
}
