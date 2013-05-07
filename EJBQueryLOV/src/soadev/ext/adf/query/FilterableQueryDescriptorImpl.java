package soadev.ext.adf.query;

import java.io.StringWriter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public AttributeDef getAttributeDescriptor(String attrName) {
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


    private List<AttributeCriterion> _parseCriteria(String key, String value) {
        List<AttributeCriterion> criteriaItems = null;
        value = value.trim();
        int charCount = value.length();
        if (charCount < 1) {
            return criteriaItems;
        }
        criteriaItems = new ArrayList<AttributeCriterion>();
        Pattern pattern1 = Pattern.compile("(?i)\\band\\b"), pattern2;
        Matcher m = pattern1.matcher(value);
        boolean andMatches = m.find(), orMatches = false;

        if (!andMatches) {
            pattern2 = Pattern.compile("(?i)\\bor\\b");
            m = pattern2.matcher(value);
            orMatches = m.find();
        }

        if (andMatches || orMatches) {
            String[] tokenValues = value.split(" ");
            List<String> bufferedValues = new ArrayList<String>();
            ConjunctionCriterion.Conjunction lastConjunction =
                ConjunctionCriterion.Conjunction.AND;
            for (int i = 0; i < tokenValues.length; i++) {
                String tokenValue = tokenValues[i];
                if (tokenValue.equalsIgnoreCase("AND")) {
                    AttributeCriterion vcItem =
                        _parseAttributeCriterion(key, _joinStrings(' ',
                                                                   bufferedValues),
                                                 lastConjunction);
                    if (vcItem != null)
                        criteriaItems.add(vcItem);
                    lastConjunction = ConjunctionCriterion.Conjunction.AND;
                    bufferedValues.clear();
                } else if (tokenValue.equalsIgnoreCase("OR")) {
                    AttributeCriterion vcItem =
                        _parseAttributeCriterion(key, _joinStrings(' ',
                                                                   bufferedValues),
                                                 lastConjunction);
                    if (vcItem != null)
                        criteriaItems.add(vcItem);
                    lastConjunction = ConjunctionCriterion.Conjunction.OR;
                    bufferedValues.clear();
                } else {
                    bufferedValues.add(tokenValue);
                }
            }
            if (bufferedValues.size() > 0) {
                AttributeCriterion vcItem =
                    _parseAttributeCriterion(key, _joinStrings(' ',
                                                               bufferedValues),
                                             lastConjunction);
                if (vcItem != null)
                    criteriaItems.add(vcItem);
            }
        } else {
            AttributeCriterion vcItem =
                _parseAttributeCriterion(key, value, ConjunctionCriterion.Conjunction.AND);
            if (vcItem != null)
                criteriaItems.add(vcItem);

        }
        return criteriaItems;
    }

    private static String _joinStrings(char token,
                                       List<String> bufferedValues) {
        StringWriter sb = new StringWriter();
        int i = 0, count = bufferedValues.size();
        for (String string : bufferedValues) {
            sb.append(string);
            if (i < count - 1)
                sb.append(token);
            i++;
        }

        return (sb.toString());
    }

    // QBE related methods

    public ConjunctionCriterion getFilterConjunctionCriterion() {
        Map<String, Object> filterCriteria = getFilterCriteria();

        List<Criterion> criterionList = new ArrayList<Criterion>();
        if (filterCriteria != null && !filterCriteria.isEmpty()) {
            Set<String> keySet = filterCriteria.keySet();
            for (String key : keySet) {
                Object filterValue = filterCriteria.get(key);
                if (filterValue == null) {
                    continue;
                }
                String strValue = filterValue.toString();
                List<AttributeCriterion> items = _parseCriteria(key, strValue);
                if (items != null) {
                    criterionList.addAll(items);
                }
            }
        }
        ConjunctionCriterion conjunctionCriterion =
            new ConjunctionCriterionImpl(ConjunctionCriterion.Conjunction.AND,
                                         criterionList);
        return conjunctionCriterion;
    }

    private AttributeCriterion _parseAttributeCriterion(String key,
                                                        String value,
                                                        ConjunctionCriterion.Conjunction conjunction) {
        value = value.trim();
        int charCount = value.length();
        OperatorDef operator = null;
        if (charCount > 0) {
            char firstChar = value.charAt(0);
            if (firstChar == '>') {
                char secondChar = charCount > 1 ? value.charAt(1) : '\0';
                if (secondChar == '=') {
                    if (charCount > 2) {
                        operator = OperatorDef.GREATER_THAN_EQUALS;
                        value = value.substring(2);
                    }
                } else if (charCount > 1) {
                    operator = OperatorDef.GREATER_THAN;
                    value = value.substring(1);
                }
            } else if (firstChar == '<') {
                char secondChar = charCount > 1 ? value.charAt(1) : '\0';
                if (secondChar == '=') {
                    if (charCount > 2) {
                        operator = OperatorDef.LESS_THAN_EQUALS;
                        value = value.substring(2);
                    }
                } else if (charCount > 1) {
                    operator = OperatorDef.LESS_THAN;
                    value = value.substring(1);
                }
            } else if (firstChar == '*') {
                char lastChar = value.charAt(charCount - 1);
                if (charCount > 1 && lastChar == '*') {
                    operator = OperatorDef.CONTAINS;
                    value = value.substring(1, charCount - 1);
                } else if (charCount > 1) {
                    operator = OperatorDef.ENDS_WITH;
                    value = value.substring(1);
                }
            } else if (firstChar == '=') {
                operator = OperatorDef.EQUALS;
                if (charCount > 1) {
                    value = value.substring(1);
                }
            } else {
                char lastChar = value.charAt(charCount - 1);
                if (lastChar == '*') {
                    if (charCount > 1) {
                        // TODO: Cannot assume operator to be STARTS_WITH as it's only supported on String
                        // type attributes and if user enter * on a Number attribute, a NPE is thrown in
                        // getSqlString() as the operator never gets stored
                        operator = OperatorDef.STARTS_WITH;
                        value = value.substring(0, charCount - 1);
                    }
                } else {
                    operator = OperatorDef.NO_OPERATOR;
                }
            }
        }

        if (operator != null) {
            List list = new ArrayList();
            list.add(value);
            AttributeCriterionImpl criterion =
                new AttributeCriterionImpl(getAttributeDescriptor(key),
                                           conjunction, operator, list, false);
            return criterion;
        } else {
            return null;
        }

    }
}
