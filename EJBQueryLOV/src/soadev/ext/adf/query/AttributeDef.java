package soadev.ext.adf.query;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

import java.util.Map;

import static soadev.ext.adf.query.Constants.*;

/**
 * Represents the definition of a single attribute on a page - type, label, format etc.
 * This was adopted from the AttributeDef inner class of the original QueryPageDef and was
 * modified to add additional fields mainly for representing properties of the xml files
 * of the entities.
 * - by Owie
 */
public class AttributeDef implements Serializable{
  //standard values in jdeveloper property inspector
    public static final String INPUT_DATE = "date";
    public static final String INPUT_TEXT = "inputText";
    public static final String CHECK_BOX = "check_box";
    public static final String CHOICE = "choice";
    public static final String COMBO = "combo";
    public static final String COMBO_LOV = "combo_lov";
    public static final String EDIT = "edit";
    private static final long serialVersionUID = 1L;

    private String _name;

    private String _label;

    private String _description;
    // the type of the component to display the values of this attribute
    private String _componentType;
    // the type of the attribute
    private Class _type;

    // the format for the attribute to be used when converting values
    private String _format;

    // whether the attribute is a indexed field
    private boolean _isIndexed;

    private Map<String, String> _customProperties;

    // Whether the attribute is required. A true value means that null values for the attribute are
    // not allowed.
    private boolean _isMandatory;
    // Whether the attribute is updateable. An updateable attribute allows its value to be changed.
    private boolean _isUpdateable;
    private boolean _isQueriable;
    private boolean _isPersistent;
    private String _propertiesBundle;
    private String _accessorName;


    public AttributeDef() {
    }

    // Operators for the Number dataType
    private static List<OperatorDef> NUMBER_OPERATORS =
        new ArrayList<OperatorDef>();
    static {
        NUMBER_OPERATORS.add(OperatorDef.NO_OPERATOR);
        NUMBER_OPERATORS.add(OperatorDef.EQUALS);
        NUMBER_OPERATORS.add(OperatorDef.NOT_EQUALS);
        NUMBER_OPERATORS.add(OperatorDef.GREATER_THAN);
        NUMBER_OPERATORS.add(OperatorDef.LESS_THAN);
        NUMBER_OPERATORS.add(OperatorDef.GREATER_THAN_EQUALS);
        NUMBER_OPERATORS.add(OperatorDef.LESS_THAN_EQUALS);
        NUMBER_OPERATORS.add(OperatorDef.BETWEEN);
    }

    // Operators for the Date dataType
    private static List<OperatorDef> DATE_OPERATORS =
        new ArrayList<OperatorDef>();
    static {
        DATE_OPERATORS.add(OperatorDef.NO_OPERATOR);
        DATE_OPERATORS.add(OperatorDef.BETWEEN);
        DATE_OPERATORS.add(OperatorDef.EQUALS);
        DATE_OPERATORS.add(OperatorDef.NOT_EQUALS);
        DATE_OPERATORS.add(OperatorDef.GREATER_THAN);
        DATE_OPERATORS.add(OperatorDef.GREATER_THAN_EQUALS);
        DATE_OPERATORS.add(OperatorDef.LESS_THAN);
        DATE_OPERATORS.add(OperatorDef.LESS_THAN_EQUALS);
    }

    // Operators for the String dataType
    private static List<OperatorDef> STRING_OPERATORS =
        new ArrayList<OperatorDef>();
    static {
        STRING_OPERATORS.add(OperatorDef.NO_OPERATOR);
        STRING_OPERATORS.add(OperatorDef.EQUALS);
        STRING_OPERATORS.add(OperatorDef.NOT_EQUALS);
        STRING_OPERATORS.add(OperatorDef.LIKE);
        STRING_OPERATORS.add(OperatorDef.STARTS_WITH);
        STRING_OPERATORS.add(OperatorDef.ENDS_WITH);
        STRING_OPERATORS.add(OperatorDef.CONTAINS);
        STRING_OPERATORS.add(OperatorDef.DOES_NOT_CONTAIN);
    }

    // Operators for the Boolean dataType
    private static List<OperatorDef> BOOLEAN_OPERATOR =
        new ArrayList<OperatorDef>();

    public AttributeDef(String name,  Class type,
                        String format, String componentType) {
        super();
        this._name = name;
        this._componentType = componentType;
        this._type = type;
        this._format = format;
    }
    static {
        BOOLEAN_OPERATOR.add(OperatorDef.NO_OPERATOR);
        BOOLEAN_OPERATOR.add(OperatorDef.EQUALS);
        BOOLEAN_OPERATOR.add(OperatorDef.NOT_EQUALS);
    }

    public String getName() {
        return _name;
    }
    
  public static String concatWithDot(String... values) {
      StringBuilder builder = new StringBuilder();
      int counter = 0;
      for (String value : values) {
          if (counter > 0) {
              builder.append(DOT);
          }
          builder.append(value);
          counter++;
      }
      return builder.toString();
  }

    public String getLabel() {
        return _label;
    }

    public String getDescription() {
        return _description;
    }


    /**
     * Returns the list of supported operators for this attribute.
     * @return a List&lt;OperatorType&gt;
     */
    public final List<OperatorDef> getSupportedOperators() {
        String typeName = _type.getName();
        if (isNumericType(typeName))
            return NUMBER_OPERATORS;
        else if (isDateType(typeName))
            return DATE_OPERATORS;
        else if (typeName.equals(BOOLEAN_TYPE))
            return BOOLEAN_OPERATOR;
        else
            return STRING_OPERATORS;
    }
    
    public OperatorDef getDefaultOperator(){
        String typeName = _type.getName();
        if (isNumericType(typeName))
            return OperatorDef.EQUALS;
        else if (isDateType(typeName))
            return OperatorDef.BETWEEN;
        else if (typeName.equals(BOOLEAN_TYPE))
            return OperatorDef.EQUALS;
        else
            return OperatorDef.CONTAINS;
    }

    public Class getType() {
        return _type;
    }

    public String getComponentType() {
        return _componentType;
    }


    /**
     * By default returns a certain length.
     * @return
     */
    public int getLength() {
        if (_componentType.equals(INPUT_DATE)) {
            return 10;
        } else
            return 0;
    }

    public int getMaximumLength() {
        if (_componentType.equals(INPUT_TEXT))
            return 100;
        else
            return 0;
    }

    public String getFormat() {
        return _format;
    }


    public boolean isIndexed() {
        return _isIndexed;
    }

    public boolean isMandatory() {
        return _isMandatory;
    }

    public boolean isUpdateable() {
        return _isUpdateable;
    }

    public void setFormat(String format) {
        _format = format;
    }

    public void setQueriable(boolean isQueriable) {
        this._isQueriable = isQueriable;
    }

    public boolean isQueriable() {
        return _isQueriable;
    }

    public void setPersistent(boolean isPersistent) {
        this._isPersistent = isPersistent;
    }

    public boolean isPersistent() {
        return _isPersistent;
    }


    public void setUpdateable(boolean isUpdateable) {
        this._isUpdateable = isUpdateable;
    }

    public void setMandatory(boolean isMandatory) {
        this._isMandatory = isMandatory;
    }

    public void setName(String _name) {
        this._name = _name;
    }

    public void setLabel(String _label) {
        this._label = _label;
    }

    public void setDescription(String _description) {
        this._description = _description;
    }

    public void setType(Class _type) {
        this._type = _type;
    }

    public void setIndexed(boolean isIndexed) {
        this._isIndexed = isIndexed;
    }

    public void setComponentType(String componentType) {
        this._componentType = componentType;
    }

    public void setCustomProperties(Map<String, String> customProperties) {
        this._customProperties = customProperties;
    }

    public Map<String, String> getCustomProperties() {
        return _customProperties;
    }
    
  public void setPropertiesBundle(String _propertiesBundle) {
      this._propertiesBundle = _propertiesBundle;
  }

  public String getPropertiesBundle() {
      return _propertiesBundle;
  }
      
    public static boolean isNumericType(String type){
        return type.equals(NUMBER_TYPE)||type.equals(INTEGER_TYPE)||type.equals(LONG_TYPE)||type.equals(DOUBLE_TYPE)||type.equals(BIG_DECIMAL_TYPE);
    }
    
    public static boolean isDateType(String type){
        return type.equals(DATE_TYPE)||type.equals(TIMESTAMP_TYPE)||type.equals("java.sql.Date");//more types should be added
    }


    public void setAccessorName(String accessorName) {
        this._accessorName = accessorName;
    }

    public String getAccessorName() {
        return _accessorName;
    }

}
