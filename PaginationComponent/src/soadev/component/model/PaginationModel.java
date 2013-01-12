package soadev.component.model;

import java.io.Serializable;

import javax.faces.context.FacesContext;

import oracle.adf.model.BindingContext;
import oracle.adf.share.logging.ADFLogger;

import oracle.binding.BindingContainer;
import oracle.binding.OperationBinding;

import soadev.pattern.util.Observer;
import soadev.pattern.util.Subject;


public class PaginationModel implements Observer, Serializable{
    private static ADFLogger _logger =
        ADFLogger.createADFLogger(PaginationModel.class);
    private static final long NO_COUNT = -2L;
    private int _pageNumber = 1;
    private long _maxResults = 0L;
    private long _totalCount = -1L;
    private String projectionOper = "getResultCount";
    
    public void reset(){
        _pageNumber = 1;
        _totalCount = 0L;
    }
    
    public void setPageNumber(int pageNumber) {
        this._pageNumber = pageNumber;
    }

    public int getPageNumber() {
        return _pageNumber;
    }

    public void setMaxResults(long maxResults) {
        this._maxResults = maxResults;
    }

    public long getMaxResults() {
        if(_maxResults == 0L ){
            FacesContext fc = FacesContext.getCurrentInstance();
            String maxResultsStr = fc.getExternalContext().getInitParameter("maxResults");
            if(maxResultsStr!= null){
                try {
                    _maxResults = Long.decode(maxResultsStr);
                } catch (NumberFormatException e) {
                    _maxResults = 50L;//Default
                }
            }else{
                _maxResults = 50L;//Default
            }
        }
        return _maxResults;
    }
    
    public int getFirstResult(){
        long maxResults = getMaxResults();
        int pageNumber = getPageNumber();
        return (int)maxResults*(pageNumber - 1);
    }

    public void setTotalCount(long _totalCount) {
        this._totalCount = _totalCount;
    }

    public long getTotalCount() {
        if(_totalCount == -1L){
            executeCount();
        }
        return _totalCount;
    }

    public void incrementPageNumber(){
        _pageNumber++;
    }
    
    public void decrementPageNumber(){
        _pageNumber--;
    }
    
    public long getMaxPage(){
        double maxResults = getMaxResults();
        double totalCount = getTotalCount();
        if(totalCount == NO_COUNT){
            return _pageNumber + 1;
        }
        if (totalCount <= maxResults){
            return 1;
        }
        return (long)Math.ceil(totalCount/maxResults);
    }
    
    public String getPageInfo(){
        _logger.fine("PaginationModel.getPageInfo");
        long maxResults = getMaxResults();
        int pageNumber = getPageNumber();
        long totalCount = getTotalCount();
        StringBuilder sb = new StringBuilder();
        if(totalCount == 0L){
            sb.append(0);
        }else{
            sb.append(maxResults*(pageNumber - 1)+1);
        }
        sb.append(" - ");
        long lastRecord = maxResults*(pageNumber);
        if(lastRecord > totalCount && totalCount != NO_COUNT){
            lastRecord = totalCount;
        }
        sb.append(lastRecord);
        sb.append(" of ");
        if (totalCount == NO_COUNT){
            sb.append("unknown");
        }else{
            sb.append(totalCount);
        }
        return sb.toString();
    }

    public void update(Subject subject, Object object) {
        reset();
        executeCount();
//        String jpqlStmt = ((QueryLOV)subject).getJpqlStmt();
//        executeCount(jpqlStmt);
    }
    private void executeCount() {
        OperationBinding oper = getOperationBinding(projectionOper);
        if(oper == null){
            setTotalCount(NO_COUNT);
            return;
        }
        Long count = (Long) oper.execute();
        if(!oper.getErrors().isEmpty()){
            for(Object obj: oper.getErrors()){
                _logger.severe(obj.toString());
            }
        }
        if(count != null){
            setTotalCount(count);
        }else{
            setTotalCount(0L);
        }
    }

//    private void executeCount(String jpqlStmt) {
//        OperationBinding oper = getOperationBinding(projectionOper);
//        Long count = (Long) oper.execute();
//        if(!oper.getErrors().isEmpty()){
//            for(Object obj: oper.getErrors()){
//                _logger.severe(obj.toString());
//            }
//        }
//        if(count != null){
//            setTotalCount(count);
//        }else{
//            setTotalCount(0L);
//        }
//    }
    
    
    public OperationBinding getOperationBinding(String operation) {
        BindingContainer bindings = BindingContext.getCurrent().getCurrentBindingsEntry();
        return bindings.getOperationBinding(operation);    
    }

    public void setProjectionOper(String projectionOper) {
        this.projectionOper = projectionOper;
    }

    public String getProjectionOper() {
        return projectionOper;
    }
}
