package soadev.ext.adf.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oracle.adf.view.rich.model.AttributeCriterion;
import oracle.adf.view.rich.model.ConjunctionCriterion;
import oracle.adf.view.rich.model.Criterion;
import oracle.adf.view.rich.model.FilterableQueryDescriptor;
import oracle.adf.view.rich.model.QueryDescriptor;

import static soadev.ext.adf.query.Constants.BLANK;

import static soadev.utils.MyUtils.unbox;


public class FilterableQueryDescriptorImpl extends FilterableQueryDescriptor {
    private SavedSearchDef _savedSearchDef; //retain copy for use during reset
    private Map<String, AttributeDef> _attributes;
    private Map<String, Object> _filterCriteria;
    private AttributeCriterion _currentCriterion;
    private Map<String, Object> _uiHintsMap;
    private ConjunctionCriterion _conjunctionCriterion;
    //List for hidden criterion so that they can be added back unchanged.
    private List<Criterion> _hiddenCriterions;

    public FilterableQueryDescriptorImpl(SavedSearchDef savedSearchDef,
                                         Map<String, AttributeDef> attributes) {
        this._savedSearchDef = savedSearchDef;
        this._attributes = attributes;
        initialize();
    }

    public SavedSearchDef generateSavedSearch() {
        SavedSearchDef ssd = new SavedSearchDef();
        ssd.setAutoExecute(unbox((Boolean)getUIHints().get(UIHINT_AUTO_EXECUTE)));
        ssd.setDefaultSearch(unbox((Boolean)getUIHints().get(UIHINT_DEFAULT)));
        ssd.setDefaultConjunction(_conjunctionCriterion.getConjunction());
        ssd.setName(_savedSearchDef.getName() + "_copy");
        ssd.setMode((QueryDescriptor.QueryMode)getUIHints().get(UIHINT_MODE));
        ssd.setReadOnly(unbox((Boolean)getUIHints().get(UIHINT_IMMUTABLE)));
        ssd.setResultsId((String)getUIHints().get(UIHINT_RESULTS_COMPONENT_ID));
        ssd.setSaveResultsLayout(unbox((Boolean)getUIHints().get(UIHINT_SAVE_RESULTS_LAYOUT)));
        //            ssd.setShowInList(unbox((Boolean) getUIHints().get(UIHINT_SHOW_IN_LIST)));
        List<SearchFieldDef> searchFields = new ArrayList<SearchFieldDef>();
        for (Criterion criterion : _conjunctionCriterion.getCriterionList()) {
            AttributeCriterionImpl attrCriterion =
                (AttributeCriterionImpl)criterion;
            SearchFieldDef sfd = new SearchFieldDef();
            sfd.setAttrName(attrCriterion.getAttribute().getName());
            sfd.setOperator((OperatorDef)attrCriterion.getOperator().getValue());
            sfd.setRemovable(attrCriterion.isRemovable());
            //copy list to support proper reset;
            List copy = copy(attrCriterion.getValues());
            sfd.setValues(copy);
            searchFields.add(sfd);
        }
        ssd.setSearchFields(searchFields);
        return ssd;
    }

    private AttributeDef getAttributeDescriptor(String attrName) {
        if (_attributes != null)
            return _attributes.get(attrName);

        return null;
    }

    public void initialize() {
        SavedSearchDef savedSearchDef =
            _savedSearchDef.copy(); //work with a copy
        List<Criterion> criterionList = new ArrayList<Criterion>();
        for (SearchFieldDef searchFieldDef :
             savedSearchDef.getSearchFields()) {
            //shallow copy list to support reset
            List values = copy(searchFieldDef.getValues());
            //ensure values are not null to avoid el property exception isReadOnly blah blah...
            if (values.isEmpty()) {
                values.add(BLANK);
                values.add(BLANK);
            }
            Criterion criterion =
                new AttributeCriterionImpl(getAttributeDescriptor(searchFieldDef.getAttribute()),
                                           searchFieldDef.getOperator(),
                                           values,
                                           searchFieldDef.isRemovable());
            criterionList.add(criterion);
        }

        _conjunctionCriterion =
                new ConjunctionCriterionImpl(savedSearchDef.getDefaultConjunction(),
                                             criterionList);
        _currentCriterion =
                (AttributeCriterion)_conjunctionCriterion.getCriterionList().get(0);
        _hiddenCriterions = new ArrayList<Criterion>();
        _uiHintsMap = new HashMap<String, Object>();
        _uiHintsMap.put(UIHINT_AUTO_EXECUTE, savedSearchDef.isAutoExecute());
        _uiHintsMap.put(UIHINT_MODE, savedSearchDef.getMode());
        _uiHintsMap.put(UIHINT_NAME, savedSearchDef.getName());
        _uiHintsMap.put(UIHINT_SAVE_RESULTS_LAYOUT,
                        savedSearchDef.isSaveResultsLayout());
        _uiHintsMap.put(UIHINT_SHOW_IN_LIST, savedSearchDef.isShowInList());
        _uiHintsMap.put(UIHINT_IMMUTABLE, savedSearchDef.isReadOnly());
        _uiHintsMap.put(UIHINT_RESULTS_COMPONENT_ID,
                        savedSearchDef.getResultsId());
        _uiHintsMap.put(UIHINT_DEFAULT, savedSearchDef.isDefaultSearch());
    }

    private List copy(List list) {
        //shallow copy
        List copy = new ArrayList();
        copy.addAll(list);
        return copy;
    }

    public void addCriterion(String name) {
        List values = new ArrayList();
        values.add(BLANK);
        values.add(BLANK);
        AttributeDef attrDef = getAttributeDescriptor(name);
        Criterion criterion =
            new AttributeCriterionImpl(attrDef, attrDef.getDefaultOperator(),
                                       values, true);
        ((ConjunctionCriterionImpl)_conjunctionCriterion).addCriterion(criterion);

    }


    public void changeMode(QueryDescriptor.QueryMode mode) {
        _uiHintsMap.put(UIHINT_MODE, mode);
    }

    public SavedSearchDef getSavedSearchDef() {
        return _savedSearchDef;
    }

    public ConjunctionCriterion getConjunctionCriterion() {
        return _conjunctionCriterion;
    }

    public void setConjunctionCriterion(ConjunctionCriterion criterion) {
        _conjunctionCriterion = criterion;
    }

    public String getName() {
        return (String)getUIHints().get(UIHINT_NAME);
    }

    public void removeCriterion(Criterion criterion) {
        ((ConjunctionCriterionImpl)_conjunctionCriterion).removeCriterion(criterion);
    }

    public Map<String, Object> getUIHints() {
        return _uiHintsMap;
    }

    public AttributeCriterion getCurrentCriterion() {
        return _currentCriterion;
    }

    public void setCurrentCriterion(AttributeCriterion attrCriterion) {
        this._currentCriterion = attrCriterion;
    }

    @Override
    public Map<String, Object> getFilterCriteria() {
        return _filterCriteria;
    }

    @Override
    public void setFilterCriteria(Map<String, Object> filterCriteria) {
        _filterCriteria = filterCriteria;
    }

    public void addHiddenCriterion(AttributeCriterionImpl criterion) {
        _hiddenCriterions.add(criterion);
    }

    public void removeHiddenCriterion(AttributeCriterionImpl criterion) {
        _hiddenCriterions.remove(criterion);
    }

}
