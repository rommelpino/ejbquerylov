package src.view.listeners;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

import oracle.ui.pattern.dynamicShell.TabContext;

public class DirtyListener implements ValueChangeListener {

    public void processValueChange(ValueChangeEvent valueChangeEvent) throws AbortProcessingException {
        TabContext tabContext = TabContext.getCurrentInstance();
        if (tabContext != null) {
            tabContext.markCurrentTabDirty(true);
        }
    }
}
