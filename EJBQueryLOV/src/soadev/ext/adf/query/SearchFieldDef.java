package soadev.ext.adf.query;

import java.io.Serializable;

import java.util.ArrayList;

import java.util.List;

import oracle.adf.view.rich.model.ConjunctionCriterion;

/**
 * @author pino
 * DemoSearchFieldDef represents a search field that embellishes an attributeDef by providing
 * info such as the default operator to use for the attribute when it is used is a search
 * criteria etc.
 */
public class SearchFieldDef implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    // The unique identifying field for SearchFieldDef
    private Long _id;

    // the name of the attribute that this search field references
    private String _attrName;
    // The conjunction to use before this search field.
    private ConjunctionCriterion.Conjunction _beforeConjunction =
        ConjunctionCriterion.Conjunction.AND;
    // default operator to use with this search field
    private OperatorDef _operator;

    // if the search field has dependent fields
    private boolean[] _hasDependentFields = new boolean[] { false, false };

    // the entered values. it could be a String literal or a Number
    private List _values;
    private boolean _isRemovable = true;
    private boolean _isMultiSelect = false;

    public SearchFieldDef() {

    }

    public SearchFieldDef(String attrName,
                          ConjunctionCriterion.Conjunction beforeConjunction,
                          OperatorDef operator, boolean[] hasDependentFields,
        List values, boolean isRemovable, boolean isMultiSelect) {
        super();
        this._attrName = attrName;
        this._beforeConjunction = beforeConjunction;
        this._operator = operator;
        this._hasDependentFields = hasDependentFields;
        this._values = values;
        this._isRemovable = isRemovable;
        this._isMultiSelect = isMultiSelect;
    }


    public SearchFieldDef copy() {
        SearchFieldDef copy = new SearchFieldDef();
        copy.setAttrName(this._attrName);
        copy.setBeforeConjunction(this._beforeConjunction);
        copy.setOperator(this._operator);
        copy.setHasDependentFields(this._hasDependentFields);
        copy.setId(this._id);
        copy.setMultiSelect(this._isMultiSelect);
        copy.setRemovable(this._isRemovable);
        copy.setValues(this._values);
        return copy;
    }

    public boolean equals(String attrName) {
        if (attrName != null)
            return (this.getAttribute().equals(attrName));
        return false;
    }

    /**
     * Gets the name of the attribute
     * @return
     */
    public String getAttribute() {
        return _attrName;
    }

    /**
     * Gets the conjunction to use before this search field.
     * @return
     */
    public ConjunctionCriterion.Conjunction getBeforeConjunction() {
        return _beforeConjunction;
    }

    /**
         * Gets the selected (if changed by user) or the default operator to use with this search field
         * @return
         */

    /**
     * Returns the list of operators for this search field. Basically it loops through the supported
     * list of operators for the attribute and replace Equals and Not Equals with In and Not In, for
     * LOV attribute types
     * @return a List&lt;OperatorDef&gt;
     */
//    public final List<OperatorDef> getOperators() {
//        return getAttributeDef().getSupportedOperators();
//    }


    public OperatorDef getOperator() {
        return _operator;
    }

    public boolean isRemovable() {
        return _isRemovable;
    }

    /**
     * Gets the last saved values.
     * @return
     */
    public List getValues() {
        if (_values == null) {
            _values = new ArrayList();
        }
        return _values;
    }


    public void setRemovable(boolean removable) {
        _isRemovable = removable;
    }

    /**
     * Returns true, if this criterion has dependent field for given value map index.
     * @param index the value map index of the search value field
     * @return boolean
     */
    public boolean hasDependentField(int index) {
        return _hasDependentFields[index];
    }

    public boolean[] getHasDependentFields() {
        return _hasDependentFields;
    }

    public boolean isMultiSelect() {
        return _isMultiSelect;
    }

    /**
     * Sets the unique identifier for SearchFieldDef
     * @param _id
     */
    public void setId(Long _id) {
        this._id = _id;
    }

    /**
     *
     * @return returns the unique identifier for SearchFieldDef
     */
    public Long getId() {
        return _id;
    }


    public void setAttrName(String attrName) {
        this._attrName = attrName;
    }

    public void setBeforeConjunction(ConjunctionCriterion.Conjunction beforeConjunction) {
        this._beforeConjunction = beforeConjunction;
    }

    public void setMultiSelect(boolean isMultiSelect) {
        this._isMultiSelect = isMultiSelect;
    }

    public void setOperator(OperatorDef operator) {
        this._operator = operator;
    }

    public void setHasDependentFields(boolean[] hasDependentFields) {
        this._hasDependentFields = hasDependentFields;
    }

    public void setValues(List values) {
        this._values = values;
    }

}
