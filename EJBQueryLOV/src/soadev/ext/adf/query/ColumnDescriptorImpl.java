package soadev.ext.adf.query;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;

import oracle.adf.view.rich.model.AttributeDescriptor;
import oracle.adf.view.rich.model.ColumnDescriptor;
import oracle.adf.view.rich.model.ListOfValuesModel;

import static soadev.ext.adf.query.Constants.COMPONENT_MAP;
import static soadev.ext.adf.query.Constants.DATE_TYPE;
import static soadev.ext.adf.query.Constants.TIMESTAMP_TYPE;

import soadev.view.utils.JSFUtils;


public final class ColumnDescriptorImpl extends ColumnDescriptor {
    private AttributeDef _attrDef;
    private Object _supplementalModel;

    public ColumnDescriptorImpl(AttributeDef attrDef) {
        _attrDef = attrDef;
    }

    public ColumnDescriptorImpl(AttributeDef attrDef,
                                Object supplementalModel) {
        this._attrDef = attrDef;
        this._supplementalModel = supplementalModel;

    }


    public String getDescription() {
        //            return _attrDef.getDescription(); not yet localized
        return null;
    }

    public Converter getConverter() {
        Application application =
            FacesContext.getCurrentInstance().getApplication();
        Class type = getType();
        if (Number.class.isAssignableFrom(type)) {
            return application.createConverter(Number.class);
        }
        if (Date.class.isAssignableFrom(type)) {
            return application.createConverter(Date.class);
        }
        return null;
    }

    public String getFormat() {
        return _attrDef.getFormat();
    }

    public String getLabel() {
        String attrLabel = _attrDef.getLabel();
        String accessorName = _attrDef.getAccessorName();
        if (_attrDef.getPropertiesBundle() != null) {
            String resourceLabel =
                JSFUtils.getStringFromBundle(_attrDef.getPropertiesBundle(),
                                             attrLabel, attrLabel);

            return (accessorName != null) ?
                   AttributeDef.concatWithDot(accessorName, resourceLabel) :
                   resourceLabel;
        }
        return (accessorName != null) ?
               AttributeDef.concatWithDot(accessorName, attrLabel) : attrLabel;
    }

    /**
     * Returns true if criterion is indexed and not required.
     *
     * @return true if criterion is indexed and not required else false.
     */
    public boolean isIndexed() {
        //only when criterion is indexed but not required it will be considered as indexed
        return (_attrDef.isIndexed() && !_attrDef.isMandatory());
    }

    public boolean isLOV() {
        AttributeDescriptor.ComponentType componentType = getComponentType();
        return componentType.equals(AttributeDescriptor.ComponentType.inputListOfValues) ||
            componentType.equals(AttributeDescriptor.ComponentType.inputComboboxListOfValues) ||
            componentType.equals(AttributeDescriptor.ComponentType.selectManyChoice) ||
            componentType.equals(AttributeDescriptor.ComponentType.selectOneChoice) ||
            componentType.equals(AttributeDescriptor.ComponentType.selectOneListbox);
    }


    /**
     * Based on the component type of the attribute, it returns an appropriate model object expected
     * of that component
     * @return
     */
    public Object getModel() {
        Object model = this._supplementalModel;
        if (model != null) {
            return model;
        }
        AttributeDescriptor.ComponentType compType = getComponentType();
        return getModel(compType);
    }

    public Object getModel(AttributeDescriptor.ComponentType compType) {
        if (compType.equals(AttributeDescriptor.ComponentType.selectOneChoice) ||
            compType.equals(AttributeDescriptor.ComponentType.selectManyChoice)) {
            List selectItems = new ArrayList();
            //use custom properties as select items
            for (Map.Entry<String, String> entry :
                 _attrDef.getCustomProperties().entrySet()) {
                selectItems.add(new SelectItem(entry.getValue(),
                                               entry.getKey()));
            }
            return selectItems;
        }
        return null;
    }

    public String getName() {
        return _attrDef.getName();
    }


    public Set<AttributeDescriptor.Operator> getSupportedOperators() {
        List<OperatorDef> operatorList = _attrDef.getSupportedOperators();
        Set<AttributeDescriptor.Operator> optrSet =
            new HashSet<AttributeDescriptor.Operator>();
        for (OperatorDef operator : operatorList) {
            AttributeDescriptor.Operator optr = new OperatorImpl(operator);
            optrSet.add(optr);
        }
        return optrSet;
    }

    public Class getType() {
        return _attrDef.getType();
    }

    public AttributeDescriptor.ComponentType getComponentType() {
        Object model = _supplementalModel;
        if (model != null) {
            if (model instanceof ListOfValuesModel) {
                //inputComboboxListOfValues should return at least an empty list.
                return ((ListOfValuesModel)model).getItems() == null ?
                       AttributeDescriptor.ComponentType.inputListOfValues :
                       AttributeDescriptor.ComponentType.inputComboboxListOfValues;
            }
            if (model instanceof List) {
                return AttributeDescriptor.ComponentType.selectManyChoice;
            }
            //undefined model continue with the usual below;
        }
        AttributeDescriptor.ComponentType componentType =
            COMPONENT_MAP.get(_attrDef.getComponentType());
        return (componentType != null) ? componentType :
               ComponentType.inputText;
    }

    public boolean isReadOnly() {
        return false;
    }

    /**
     * Returns true if this criterion is a required field or is the only indexed field in the
     * criterion list.
     *
     * @return true if single indexed criterion exist else false
     */
    public boolean isRequired() {
        //building of the JPQL String could throw exception if appropriate controls are not marks as required.
        //handle with care :)
        String type = _attrDef.getType().getName();
        return false; //_attrDef.isMandatory() ||
        //                AttributeDef.isNumericType(type) ||
        //                AttributeDef.isDateType(type) || isLOV();
    }

    public AttributeDescriptor.Operator getOperator(OperatorDef operator) {
        return new OperatorImpl(operator);
    }

    public int getLength() {
        return _attrDef.getLength();
    }

    public int getMaximumLength() {
        return _attrDef.getMaximumLength();
    }

    /**
     * Determines if the passed in operator can be used with multiple values, like the IN, Not IN
     * @param operator
     * @return boolean
     */
    public boolean hasVariableOperands(AttributeDescriptor.Operator operator) {
        if (operator == null)
            return false;
        OperatorImpl operImpl = (OperatorImpl)operator;
        return operImpl.getOperatorDef().hasVariableOperands();
    }

    /**
     * Determines if the operator requires showing the default component type. The default component
     * type of the attribute is used for operator Equals / Not Equals.
     * @param operator
     * @return
     */
    public boolean useDefaultComponentType(AttributeDescriptor.Operator operator) {
        OperatorImpl operImpl = (OperatorImpl)operator;
        return (operImpl.getOperatorDef().equals(OperatorDef.EQUALS) ||
                operImpl.getOperatorDef().equals(OperatorDef.NOT_EQUALS) ||
                operImpl.getOperatorDef().equals(OperatorDef.NO_OPERATOR));
    }

    public int getWidth() {
        return 0;
    }

    public String getAlign() {
        String type = _attrDef.getType().getName();
        if (type.equals(TIMESTAMP_TYPE) || type.equals(DATE_TYPE)) {
            return "center";
        }
        return "start";
    }

    public class OperatorImpl extends AttributeDescriptor.Operator {
        private OperatorDef _operator;

        public OperatorImpl(OperatorDef operator) {
            if (operator != null)
                _operator = operator;
            else
                _operator = OperatorDef.NO_OPERATOR;
        }

        public String getLabel() {
            return _operator.getLabel();
        }

        public Object getValue() {
            return _operator;
        }

        // Returns the default operandCount for the operator. Except when the operator is null it
        // returns 1 and if operandCount is -1, indicating unlimited operands, it returns 1, as from
        // the UI standpoint a selectmanyChoice will be rendered.

        public int getOperandCount() {
            if (_operator != null) {
                int operandCount = _operator.getOperandCount();
                if (operandCount != -1)
                    return operandCount;
            }
            return 1;
        }

        @Override
        public boolean equals(Object operator) {
            if (operator != null && operator instanceof OperatorImpl) {
                return (this._operator.equals(((OperatorImpl)operator).getOperatorDef()));
            }
            return false;
        }

        @Override
        public String toString() {
            return _operator.getLabel();
        }

        private OperatorDef getOperatorDef() {
            return _operator;
        }
    }
}
