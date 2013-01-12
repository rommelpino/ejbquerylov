package soadev.component;

import javax.el.ELContext;
import javax.el.MethodExpression;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import oracle.adf.view.rich.component.rich.fragment.RichDeclarativeComponent;

import soadev.component.model.PaginationModel;


public abstract class Pagination extends RichDeclarativeComponent{
    
    public void first(ActionEvent event){
        getModel().setPageNumber(1);
        invokeExecute(event);
    }
    
    public void previous(ActionEvent event){
        getModel().decrementPageNumber();
        invokeExecute(event);
        
    }
    public void next(ActionEvent event){
        getModel().incrementPageNumber();
        invokeExecute(event);
        
    }
    public void last(ActionEvent event){
        getModel().setPageNumber((int)getModel().getMaxPage());
        invokeExecute(event);
        
    }
    
    public void maxResultsValueChanged(ValueChangeEvent event){
        getModel().setPageNumber(1);
        invokeExecute(null);
    }

    private void invokeExecute(ActionEvent event) {
        ELContext elc = FacesContext.getCurrentInstance().getELContext();
        getExecute().invoke(elc, new Object[]{event});
    }
    

    public abstract PaginationModel getModel();
    public abstract MethodExpression getExecute();

}
