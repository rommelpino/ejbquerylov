package soadev.ext.adf.query.helper;


import java.io.Serializable;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import oracle.binding.OperationBinding;

import soadev.utils.MyUtils;

import soadev.view.utils.ADFUtils;


public class SelectItemsHelper implements Serializable{
    private String operationBinding;
    private List<String> displayAttributes;
    private String attributeValue;
    private transient List<SelectItem> items;

    public void setOperationBinding(String expression) {
        this.operationBinding = expression;
    }

    public String getOperationBinding() {
        if (operationBinding == null) {
            throw new IllegalStateException("No value set for attribute operationBinding");
        }
        return operationBinding;
    }

    public List<SelectItem> getItems() {
        if (items == null) {
            OperationBinding oper =
                ADFUtils.getBindings().getOperationBinding(getOperationBinding());
            List results = (List)oper.execute();
            items = new ArrayList<SelectItem>();
            for (Object obj : results) {
                StringBuffer buffer = new StringBuffer();
                int index = 0;
                for (String attr : getDisplayAttributes()) {
                    if (index > 0)
                        buffer.append(" ");
                    buffer.append(MyUtils.getProperty(obj, attr));
                }
                Object value = null;
                if (attributeValue != null) {
                    value = MyUtils.getProperty(obj, attributeValue);
                } else {
                    value = obj;
                }
                items.add(new SelectItem(value, buffer.toString()));
            }
        }
        return items;
    }

    public String createValueExpression(String expression) {
        StringBuffer buff = new StringBuffer("#{");
        buff.append(expression);
        buff.append("}");
        return buff.toString();
    }

    public void setDisplayAttributes(List<String> displayAttributes) {
        this.displayAttributes = displayAttributes;
    }

    public List<String> getDisplayAttributes() {
        return displayAttributes;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public String getAttributeValue() {
        return attributeValue;
    }
}
