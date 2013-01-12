package soadev.ext.adf.query;

import java.io.Serializable;
import java.util.List;

public abstract class AbstractQueryPersistenceHelper implements Serializable{
   
    public abstract void mergeSavedSearchDef(SavedSearchDef ssd);
    
    //public abstract void persistSavedSearchDef(SavedSearchDef ssd);
    
    public abstract void removeSavedSearchDef(SavedSearchDef ssd);
    
    public abstract List<SavedSearchDef> findSavedSearchDefByQueryId(String queryId);
        
}
