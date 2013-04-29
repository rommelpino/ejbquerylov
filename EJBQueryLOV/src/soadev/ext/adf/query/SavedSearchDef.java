package soadev.ext.adf.query;

import java.io.Serializable;

import java.util.ArrayList;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import java.util.Map;

import oracle.adf.view.rich.model.ConjunctionCriterion;

import static soadev.utils.MyUtils.*;

import oracle.adf.view.rich.model.QueryDescriptor;


/**
 * @author pino
 * Represents the definition for a saved search and contains a list of SearchFieldDef objects,
 * each SearchFieldDef being a kind of AttributeDef.
 */
public class SavedSearchDef implements Serializable {

    private static final long serialVersionUID = 1L;
//    private QueryLOV _query;
    private String _queryId;
    // The default conjunction to use between all search fields. This is used in place of specific
    // conjunction if one is not available
    private ConjunctionCriterion.Conjunction _defaultConjunction =
        ConjunctionCriterion.Conjunction.AND;
    // The name of the saved search. This also happens to be the display name.
    private String _name;
    // Whether the saved search is readOnly. A true value implies that the definition cannot be
    // changed once its created.
    private boolean _isReadOnly = false;
    // A list of search fields associated to this SavedSearchDef
    private List<SearchFieldDef> _searchFields;
    // AutoExecute
    private boolean _uiHintAutoExecute = false;
    // Default
    private boolean _uiHintDefault = false;
    // ResultsId
    private String _uiHintResultsId;
    // Mode
    private QueryDescriptor.QueryMode _uiHintMode =
        QueryDescriptor.QueryMode.ADVANCED;
    // Show In List
    private boolean _uiHintShowInList = true;
    // Save Results Layout
    private boolean _uiHintSaveResultsLayout = false;
    // SavedSearchDef Id
    private Long _id;

    public SavedSearchDef() {

    }

    public SavedSearchDef(String name, boolean autoExecute,
                          QueryDescriptor.QueryMode mode,
                          boolean saveResultsLayout, boolean showInList,
                          boolean readOnly) {
        this._name = name;
        this._uiHintAutoExecute = autoExecute;
        this._uiHintMode = mode;
        this._uiHintSaveResultsLayout = saveResultsLayout;
        this._uiHintShowInList = showInList;
        this._isReadOnly = readOnly;
    }

    public SavedSearchDef copy() {
        SavedSearchDef copy = new SavedSearchDef();
        copy.setAutoExecute(this._uiHintAutoExecute);
        copy.setDefaultSearch(this._uiHintDefault);
        copy.setDefaultConjunction(this._defaultConjunction);
        copy.setMode(this._uiHintMode);
        copy.setName(this._name);
        copy.setQueryId(this._queryId);
        copy.setReadOnly(this._isReadOnly);
        copy.setResultsId(this._uiHintResultsId);
        copy.setSaveResultsLayout(this._uiHintSaveResultsLayout);
        List<SearchFieldDef> searchFieldList = new ArrayList<SearchFieldDef>();
        for (SearchFieldDef sfd : this._searchFields) {
            searchFieldList.add(sfd.copy());
        }
        copy.setSearchFields(searchFieldList);
        copy.setShowInList(this._uiHintShowInList);
        return copy;
    }

    /**
     * Adds a search field definition
     * @param searchFieldDef
     */
    public void addSearchFieldDef(SearchFieldDef searchFieldDef) {
        getSearchFields().add(searchFieldDef);
//        searchFieldDef.setQuery(_query);

    }

    /**
     * Removes a search field definition
     * @param searchFieldDef
     */
    public void removeSearchFieldDef(SearchFieldDef searchFieldDef) {
        getSearchFields().remove(searchFieldDef);
    }


    /**
     * Returns the default conjunction to use between all search fields. If a default is not set,
     * this method loops through all its searchFieldDefs to see if they all use the same conjunction.
     * If they do we use that otherwise this method returns ConjunctionType.NONE.
     * @return
     */
    public ConjunctionCriterion.Conjunction getDefaultConjunction() {
        boolean hasSameConj = true;
        boolean bFirst = true;

        ConjunctionCriterion.Conjunction prevConj =
            ConjunctionCriterion.Conjunction.NONE;
        for (SearchFieldDef field : getSearchFields()) {
            ConjunctionCriterion.Conjunction currConj =
                field.getBeforeConjunction();
            if (!bFirst) {
                // If the conjunction differs between search fields return NONE
                if (!prevConj.equals(currConj)) {
                    hasSameConj = false;
                    break;
                }
            }
            prevConj = currConj;
            bFirst = false;
        }

        if (!hasSameConj) {
            return ConjunctionCriterion.Conjunction.NONE;
        } else {
            return prevConj;
        }
    }

    public void setDefaultConjunction(ConjunctionCriterion.Conjunction conjunction) {
        _defaultConjunction = conjunction;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    public String getResultsId() {
        return _uiHintResultsId;
    }

    public List<SearchFieldDef> getSearchFields() {
        if (_searchFields == null) {
            _searchFields = new ArrayList<SearchFieldDef>();
        }
        return _searchFields;
    }

    public boolean isReadOnly() {
        return _isReadOnly;
    }

    public boolean isAutoExecute() {
        return _uiHintAutoExecute;
    }

    public QueryDescriptor.QueryMode getMode() {
        return _uiHintMode;
    }

    public boolean isDefaultSearch() {
        return _uiHintDefault;
    }

    public boolean isShowInList() {
        return _uiHintShowInList;
    }

    public boolean isSaveResultsLayout() {
        return _uiHintSaveResultsLayout;
    }

    public void setAutoExecute(boolean autoExecute) {
        _uiHintAutoExecute = autoExecute;
    }

    /**
     * Determines if the definition is readOnly and hence cannot be changed.
     * @param readOnly
     */
    public void setReadOnly(boolean readOnly) {
        _isReadOnly = readOnly;
    }

    public void setShowInList(boolean showInList) {
        _uiHintShowInList = showInList;
    }

    public void setDefaultSearch(boolean isDefault) {
        _uiHintDefault = isDefault;
    }

    public void setResultsId(String resultsId) {
        _uiHintResultsId = resultsId;
    }

    public void setSaveResultsLayout(boolean saveLayout) {
        _uiHintSaveResultsLayout = saveLayout;
    }

    public void setMode(QueryDescriptor.QueryMode mode) {
        _uiHintMode = mode;
    }

    /**
     *  Sets the Unique identifier for the SavedSearchDef
     * @param _id
     */
    public void setId(Long _id) {
        this._id = _id;
    }

    public Long getId() {
        return _id;
    }

    /**
     * Sets the conjunction selected by the user for this saved search. This overrides the search
     * field specific conjunction.
     * @param selectedConjunction
     */
    //        public void setSelectedConjunction(ConjunctionCriterion.Conjunction selectedConjunction) {
    //            this._selectedConjunction = selectedConjunction;
    //        }

//    public Map<String, AttributeDef> getAttributes() {
////        return getQuery().getAttributes();
//        if(_attributes == null){
//            return Collections.EMPTY_MAP;
//        }
//        return _attributes;
//    }
//    
//    public void setAttributes(Map<String, AttributeDef> attributes) {
//        this._attributes = attributes;
//    }

    public void setSearchFields(List<SearchFieldDef> searchFields) {
        for (SearchFieldDef sfd : searchFields) {
            addSearchFieldDef(sfd);
        }
    }

    public void setQueryId(String queryId) {
        this._queryId = queryId;
    }

    public String getQueryId() {
        return _queryId;
    }

    public void update(Map<String, Object> uiHints) {
        for (Map.Entry<String, Object> entry : uiHints.entrySet()) {
            if (entry.getKey().equals(QueryDescriptor.UIHINT_NAME)) {
                setName((String)entry.getValue());
            }
            if (entry.getKey().equals(QueryDescriptor.UIHINT_DEFAULT)) {
                setDefaultSearch(unbox((Boolean)entry.getValue()));
            }
            if (entry.getKey().equals(QueryDescriptor.UIHINT_AUTO_EXECUTE)) {
                setAutoExecute(unbox((Boolean)entry.getValue()));
            }
            if (entry.getKey().equals(QueryDescriptor.UIHINT_RESULTS_COMPONENT_ID)) {
                setResultsId((String)entry.getValue());
            }
            //            if (entry.getKey().equals(QueryDescriptor.UIHINT_SHOW_IN_LIST)){
            //                setShowInList(unbox((Boolean)entry.getValue()));
            //            }
            if (entry.getKey().equals(QueryDescriptor.UIHINT_SAVE_RESULTS_LAYOUT)) {
                setSaveResultsLayout(unbox((Boolean)entry.getValue()));
            }


        }
    }


}

