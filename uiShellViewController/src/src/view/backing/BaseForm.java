package src.view.backing;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;

import oracle.adf.model.BindingContext;
import oracle.adf.model.bean.DCDataRow;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.binding.BindingContainer;
import oracle.binding.OperationBinding;
import oracle.jbo.RowSetIterator;
import oracle.ui.pattern.dynamicShell.Tab;
import oracle.ui.pattern.dynamicShell.TabContext;

import org.apache.myfaces.trinidad.render.ExtendedRenderKitService;
import org.apache.myfaces.trinidad.util.Service;

import soadev.view.utils.JSFUtils;

public class BaseForm {
    public static final String EDIT_MODE = "editMode";
    public static final String IDENTIFIER = "identifier";

    protected void launchActivity(String title, String taskflowId,
                                  Map<String, Object> parameterMap,
                                  boolean newTab) {
        try {
            TabContext tabContext = getTabContext();
            if (newTab) { //allows multiple instance of taskflow.
                //if parameters contains identifier
                //try to select the taskflow with matching identifier
                Object identifier = parameterMap.get(IDENTIFIER);
                if (identifier != null) {
                    int index = getMatchingTabIndex(taskflowId, identifier);
                    if (index != -1) {
                        activateTab(index);
                        return;
                    }
                }
                tabContext.addTab(title, taskflowId, parameterMap);
            } else {
                tabContext.addOrSelectTab(title, taskflowId, parameterMap);
            }
        } catch (TabContext.TabOverflowException toe) {
            // causes a dialog to be displayed to the user saying that there are
            // too many tabs open - the new tab will not be opened...
            toe.handleDefault();
        }
    }
    public void phaseListener(PhaseEvent phaseEvent) {
        //need render response phase to load JavaScript
        if (phaseEvent.getPhaseId() == PhaseId.RENDER_RESPONSE) {
//            String localeStr = JSFUtils.resolveExpressionAsString("#{userSession.userPreference.locale}");
//            Locale locale = new Locale(localeStr);
//            FacesContext context = phaseEvent.getFacesContext();
//            context.getViewRoot().setLocale(locale);
            registerKeyBoardMapping();
        }
    }

    private void registerKeyBoardMapping() {
        FacesContext fctx = FacesContext.getCurrentInstance();
        ExtendedRenderKitService erks =
            Service.getRenderKitService(fctx, ExtendedRenderKitService.class);
        List<UIComponent> childComponents =
            fctx.getViewRoot().getChildren();
        //First child component in an ADF Faces page - and the
        //only child - is af:document. Thus no need to parse the child
        //components and check for their component family type
        String id =
            ((UIComponent)childComponents.get(0)).getClientId(fctx);
        StringBuffer script = new StringBuffer();
        //build JS string to invoke registry function loaded to the
        //page
        script.append("window.registerKeyBoardHandler('keyboardToServerNotify','" +
                      id + "')");
        erks.addScript(fctx, script.toString());
    }
    
    

    public void launchMenu(ActionEvent event) {
        UIComponent component = event.getComponent();
        String title = (String)component.getAttributes().get("title");
        String taskFlowId =
            (String)component.getAttributes().get("taskflowId");
        Map<String, Object> parameterMap =
            (Map<String, Object>)component.getAttributes().get("parameterMap");
        Boolean newTab = (Boolean)component.getAttributes().get("newTab");
        if (newTab == null) {
            newTab = false;
        }
        launchActivity(title, taskFlowId, parameterMap, newTab);
    }

    public Object getCurrentRowDataProvider(String iteratorName) {
        BindingContainer bindings = getBindings();
        DCIteratorBinding dcib = (DCIteratorBinding)bindings.get(iteratorName);
        RowSetIterator iter = dcib.getRowSetIterator();
        DCDataRow row = (DCDataRow)iter.getCurrentRow();
        return row.getDataProvider();
    }

    public BindingContainer getBindings() {
        return BindingContext.getCurrent().getCurrentBindingsEntry();
    }

    public void setCurrentTabDirty(boolean dirty) {
        getTabContext().markCurrentTabDirty(dirty);
    }

    public void setCurrentTabClean() {
        setCurrentTabDirty(false);
    }

    public void setCurrentTabDirty() {
        setCurrentTabDirty(true);
    }

    public Map<String, Object> getPageFlowScope() {
        return AdfFacesContext.getCurrentInstance().getPageFlowScope();
    }

    public void edit(ActionEvent event) {
        getPageFlowScope().put(EDIT_MODE, true);
    }

    protected OperationBinding getOperationBinding(String methodAction) {
        OperationBinding oper =
            getBindings().getOperationBinding(methodAction);
        if (oper == null) {
            throw new IllegalArgumentException(methodAction +
                                               " operation not found");
        }
        return oper;
    }

    public void closeCurrentTab(ActionEvent actionEvent) {
        closeCurrentTab();
    }

    public void closeCurrentTab() {
        getTabContext().removeCurrentTab();
    }

    public TabContext getTabContext() {
        return TabContext.getCurrentInstance();
    }


    protected void updateCurrentTabTitle(String title) {
        getCurrentTab().setTitle(title);
    }

    protected Tab getCurrentTab() {
        TabContext tabContext = getTabContext();
        return tabContext.getTabs().get(tabContext.getSelectedTabIndex());
    }

    public boolean isCurrentTabDirty() {
        return getTabContext().isCurrentTabDirty();
    }

    protected int getMatchingTabIndex(String taskflowId, Object identifier) {
        if (taskflowId == null || identifier == null) {
            return -1;
        }
        List<Tab> tabs = getTabContext().getTabs();
        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = tabs.get(i);
            if (tab == null || !tab.isActive())
                continue;
            if (tab.getTaskflowId().getFullyQualifiedName().equals(taskflowId)) {
                Map<String, Object> parametersMap = tab.getParameters();
                if (identifier.equals(parametersMap.get(IDENTIFIER))) {
                    return i;
                }
            }
        }
        return -1;
    }

    protected void activateTab(int index) {
        if (index == -1) {
            return;
        }
        getTabContext().setSelectedTabIndex(index);
    }
    //activates a tab with a specified taskflowID
    protected void activateTab(String taskflowId) {
        activateTab(getTabContext().getFirstTabIndex(taskflowId));
    }
}
