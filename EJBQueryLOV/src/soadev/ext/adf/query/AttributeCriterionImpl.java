package soadev.ext.adf.query;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import oracle.adf.view.rich.model.AttributeCriterion;
import oracle.adf.view.rich.model.AttributeDescriptor;
import oracle.adf.view.rich.model.ConjunctionCriterion;
import oracle.adf.view.rich.model.ListOfValuesModel;

import static soadev.ext.adf.query.Constants.DATE_TYPE;
import static soadev.ext.adf.query.Constants.TIMESTAMP_TYPE;


public class AttributeCriterionImpl extends AttributeCriterion {
    private AttributeDef _attrDef;
    private AttributeDescriptor.Operator _operator;
    private List _values;
    private boolean _removable;
    private AttributeDescriptor _attrDescriptor;
    private ConjunctionCriterion.Conjunction _beforeConjunction;


    public AttributeCriterionImpl(AttributeDef attributeDef,
                                  OperatorDef operatorDef, List values,
                                  boolean removable) {
        this(attributeDef, null, operatorDef, values, removable);
    }
    
    public AttributeCriterionImpl(AttributeDef attributeDef, ConjunctionCriterion.Conjunction beforeConjunction,
                                  OperatorDef operatorDef, List values,
                                  boolean removable) {
        this._beforeConjunction = beforeConjunction;
        this._attrDef = attributeDef;
        this._values = values;
        this._removable = removable;
        this._attrDescriptor = new ColumnDescriptorImpl(attributeDef);
        this._operator =
                ((ColumnDescriptorImpl)_attrDescriptor).getOperator(operatorDef);
    }


    public AttributeDescriptor getAttribute() {
        return _attrDescriptor;
    }

    public AttributeDescriptor.Operator getOperator() {
        return _operator;
    }


    /**
     * The list of operators returned for an AttributeCriterion could be different from those
     * returned for an AttributeDescriptor. For e.g., for an LOV attribute that supports multiple
     * selection it makes more sense to support "In" and "Not In" operators rather than "Equals /
     * Not Equals".
     * @return
     */
    public Map<String, AttributeDescriptor.Operator> getOperators() {

        Map<String, AttributeDescriptor.Operator> optrMap =
            new LinkedHashMap<String, AttributeDescriptor.Operator>();
        if (((ColumnDescriptorImpl)getAttribute()).isLOV()) {
            //inputListOfValues cannot support mutiple selection
            if (!getAttribute().getComponentType().equals(AttributeDescriptor.ComponentType.inputListOfValues)) {
                AttributeDescriptor.Operator in =
                    ((ColumnDescriptorImpl)_attrDescriptor).getOperator(OperatorDef.IN);
                optrMap.put(in.getLabel(), in);
                AttributeDescriptor.Operator notIn =
                    ((ColumnDescriptorImpl)_attrDescriptor).getOperator(OperatorDef.NOT_IN);
                optrMap.put(notIn.getLabel(), notIn);
            }
        }
        //plus the default supported operators
        List<OperatorDef> operatorList = _attrDef.getSupportedOperators();
        for (OperatorDef operator : operatorList) {
            AttributeDescriptor.Operator optr =
                ((ColumnDescriptorImpl)_attrDescriptor).getOperator(operator);
            optrMap.put(optr.getLabel(), optr);
        }
        return optrMap;
    }

    public List getValues() {
        if (_values != null) {
            return _values;
        }
        return Collections.emptyList();
    }

    public void setValues(List values) {
        this._values = values;
    }

    public boolean isRemovable() {
        return this._removable;
    }

    public void setOperator(AttributeDescriptor.Operator operator) {
        this._operator = operator;
    }

    /**
     * Returns the ComponentType specific to an AttributeCriterion. This may be different from the
     * ComponentType of the AttributeDescriptor, based on the operator chosen or whether
     * multi-select is enabled or not. For e.g., for LOV attributes, <br/>
     * - when multi-select is enabled, <br/>
     *   - for operator 'In / Not In', the component type will always be selectManyChoice regardless
     *     of the default type or dataType. <br/>
     *   - For all other operators it's an inputText (inputDate or inputNumberSpinbox for Date and
     *     Number datatypes respectively.)
     * - when multi-select is disabled, <br/>
     *   - for operator Equals/Not Equals, it will be the default type (of the attribute)<br/>
     *   - for all other operators, an inputText (or inputDate or inputNumberSpinbox)
     *
     * @param operator the operator for which the component type needs to be determined
     * @return a ComponentType
     */
    @Override
    public AttributeDescriptor.ComponentType getComponentType(AttributeDescriptor.Operator operator) {
        ColumnDescriptorImpl attrDesc = (ColumnDescriptorImpl)getAttribute();
        boolean isMultiSelectOper = attrDesc.hasVariableOperands(operator);
        if (attrDesc.isLOV()) {
            Object model = getAttribute().getModel();
            if (isMultiSelectOper) {
                // always return selectManyChoice for multiSelect enabled operators
                return (model instanceof ListOfValuesModel) ?
                       AttributeDescriptor.ComponentType.inputComboboxListOfValues :
                       AttributeDescriptor.ComponentType.selectManyChoice;
            }
            if (operator.getValue().equals(OperatorDef.EQUALS) ||
                operator.getValue().equals(OperatorDef.NOT_EQUALS)) {
                return (model instanceof ListOfValuesModel) ?
                       AttributeDescriptor.ComponentType.inputListOfValues :
                       AttributeDescriptor.ComponentType.selectOneChoice;
            }
        }
        // the base component type is used (based on the datatype)
        String typeName = attrDesc.getType().getName();
        if (AttributeDef.isNumericType(typeName)) {
            return AttributeDescriptor.ComponentType.inputNumberSpinbox;
        }
        if (typeName.equals(DATE_TYPE) || typeName.equals(TIMESTAMP_TYPE)) {
            return AttributeDescriptor.ComponentType.inputDate;
        }
        return AttributeDescriptor.ComponentType.inputText;
    }

    public void setBeforeConjunction(ConjunctionCriterion.Conjunction _beforeConjunction) {
        this._beforeConjunction = _beforeConjunction;
    }

    public ConjunctionCriterion.Conjunction getBeforeConjunction() {
        return _beforeConjunction;
    }


    public AttributeDef getAttributeDef() {
        return _attrDef;
    }
}
