package soadev.view.utils;

import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import javax.faces.event.FacesEvent;

import oracle.adf.model.BindingContext;
import oracle.adf.model.bean.DCDataRow;
import oracle.adf.model.binding.DCBindingContainer;
import oracle.adf.model.binding.DCIteratorBinding;

import oracle.adf.model.events.EventProducer;
import oracle.adf.view.rich.component.rich.RichPopup;

import oracle.adf.view.rich.component.rich.fragment.RichRegion;

import oracle.binding.BindingContainer;
import oracle.binding.OperationBinding;

import oracle.jbo.RowSetIterator;

import oracle.jbo.uicli.binding.JUCtrlActionBinding;

import org.apache.myfaces.trinidad.context.RequestContext;
import org.apache.myfaces.trinidad.event.AttributeChangeEvent;
import org.apache.myfaces.trinidad.render.ExtendedRenderKitService;
import org.apache.myfaces.trinidad.util.Service;

public class ADFUtils {
    public static RowSetIterator getRowSetIterator(String iterator) {
        return getIteratorBinding(iterator).getRowSetIterator();
    }

    public static DCIteratorBinding getIteratorBinding(String iterator) {
        DCIteratorBinding dcib =
            (DCIteratorBinding)getBindings().get(iterator);
        return dcib;
    }

    public static OperationBinding getOperationBinding(String methodAction) {
        OperationBinding oper =
            getBindings().getOperationBinding(methodAction);
        return oper;
    }

    public static Map<String, Object> getPageFlowScope() {
        return RequestContext.getCurrentInstance().getPageFlowScope();
    }

    public static BindingContainer getBindings() {
        return BindingContext.getCurrent().getCurrentBindingsEntry();
    }

    public static void showDialog(RichPopup popup) {
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuilder script = new StringBuilder();
        script.append("var popup = AdfPage.PAGE.findComponent('"). // NOTRANS
            append(popup.getClientId(context)).append("'); "). // NOTRANS
            append("if (!popup.isPopupVisible()) { "). // NOTRANS
            append("var hints = {}; "). // NOTRANS
            append("popup.show(hints);}"); // NOTRANS
        ExtendedRenderKitService erks =
            Service.getService(context.getRenderKit(),
                               ExtendedRenderKitService.class);
        erks.addScript(context, script.toString());
    }

    public static void closeDialog(RichPopup popup) {
        FacesContext context = FacesContext.getCurrentInstance();
        String popupId = popup.getClientId(context);
        StringBuilder jscript = new StringBuilder();
        jscript.append("var popup = AdfPage.PAGE.findComponent('").append(popupId).append("'); ").append("if (popup.isPopupVisible()==true) { ").append("popup.hide();}");
        ExtendedRenderKitService erks =
            Service.getService(context.getRenderKit(),
                               ExtendedRenderKitService.class);
        erks.addScript(context, jscript.toString());
    }
    //for compatibility

    public static Object getIteratorCurrentRowDataProvider(String iteratorName) {
        return getCurrentRowDataProvider(iteratorName);
    }

    public static Object getCurrentRowDataProvider(String iteratorName) {
        RowSetIterator iter = getRowSetIterator(iteratorName);
        DCDataRow row = (DCDataRow)iter.getCurrentRow();
        if(row != null){
            return row.getDataProvider();
        }
        return null;
    }

    //eager style

    public static void fireEvent(EventProducer eventProducer, Object payload) {
        BindingContainer bindings = getBindings();
        ((DCBindingContainer)bindings).getEventDispatcher().fireEvent(eventProducer,
                                                                      payload);
    }
    //more convenient

    public static void fireEvent(String eventProducer, Object payload) {
        fireEvent(getEventProducer(eventProducer), payload);
    }

    //lazy style: queue first then process

    public static void queueEvent(EventProducer eventProducer,
                                  Object payload) {
        BindingContainer bindings = getBindings();
        ((DCBindingContainer)bindings).getEventDispatcher().queueEvent(eventProducer,
                                                                       payload);
    }

    public static void processContextualEvents() {
        BindingContainer bindings = getBindings();
        ((DCBindingContainer)bindings).getEventDispatcher().processContextualEvents();
    }


    //accepts valid actionBinding

    public static EventProducer getEventProducer(String producer) {
        BindingContainer bindings = getBindings();
        JUCtrlActionBinding actionBinding =
            (JUCtrlActionBinding)bindings.getControlBinding(producer);

        return actionBinding.getEventProducer();
    }

    public static void broadcastAsRegionEvent(FacesEvent event) {
        RichRegion region = findImmediateContainingRegion(event);
        if (region != null) {
            AttributeChangeEvent dummyRegionEvent =
                new AttributeChangeEvent(region, "dummy", null, null);
            region.broadcast(dummyRegionEvent);
        }
    }

    public static RichRegion findImmediateContainingRegion(FacesEvent event) {
        UIComponent comp = event.getComponent();
        while (!(comp instanceof RichRegion)) {
            comp = comp.getParent();
        }
        if (comp instanceof RichRegion) {
            return (RichRegion)comp;
        }
        return null;
    }


}


