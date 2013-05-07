package soadev.ext.adf.query;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import oracle.adf.view.rich.model.AttributeDescriptor;
import oracle.adf.view.rich.model.ConjunctionCriterion;
import oracle.adf.view.rich.model.QueryDescriptor;

/**
 * @author pino
 * Holds constants
 */
public class Constants {
    //data types
    public static final String TIMESTAMP_TYPE = "java.sql.Timestamp";
    public static final String DATE_TYPE = "java.util.Date";
    public static final String INTEGER_TYPE = "java.lang.Integer";
    public static final String LONG_TYPE = "java.lang.Long";
    public static final String DOUBLE_TYPE = "java.lang.Double";
    public static final String NUMBER_TYPE = "java.lang.Number";
    public static final String BIG_DECIMAL_TYPE = "java.math.BigDecimal";
    public static final String STRING_TYPE = "java.lang.String";
    public static final String BOOLEAN_TYPE = "java.lang.Boolean";

    //jpql
    //_ (Underscore) prefix means trailing space
    public static final String SELECT = " SELECT";
    public static final String _OBJECT_ALIAS = " o";
    public static final String _FROM_ = " FROM ";
    public static final String _WHERE_CLAUSE = " WHERE";
    public static final String SPACE = " ";
    public static final String BLANK = "";
    public static final String _AND = " AND";
    public static final String _OR = " OR";
    public static final String DOT = ".";
    public static final String OPEN_BRACKET = "(";
    public static final String CLOSE_BRACKET = ")";
    public static final String _UPPER_FUNCTION = " UPPER";
    public static final String _IS_NULL = " IS NULL";
    public static final String _IS_NOT_NULL = " IS NOT NULL";

    public static final Date BLANK_STARTING_DATE; // 1/1/1900
    public static final Date BLANK_ENDING_DATE; // 31/12/2099
    static {
        Calendar cal = Calendar.getInstance();
        cal.set(1900, Calendar.JANUARY, 1);
        BLANK_STARTING_DATE = cal.getTime();
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2099, Calendar.DECEMBER, 31);
        BLANK_ENDING_DATE = cal2.getTime();
    }

    public static final Map<String, QueryDescriptor.QueryMode> QUERY_MODE_MAP =
        new HashMap<String, QueryDescriptor.QueryMode>();
    static {
        QUERY_MODE_MAP.put("ADVANCED", QueryDescriptor.QueryMode.ADVANCED);
        QUERY_MODE_MAP.put("BASIC", QueryDescriptor.QueryMode.BASIC);
    }

    public Map getQueryModeMap() {
        return QUERY_MODE_MAP;
    }


    public static final Map<QueryDescriptor.QueryMode, String> INVERSE_QUERY_MODE_MAP =
        new HashMap<QueryDescriptor.QueryMode, String>();
    static {
        INVERSE_QUERY_MODE_MAP.put(QueryDescriptor.QueryMode.ADVANCED,
                                   "ADVANCED");
        INVERSE_QUERY_MODE_MAP.put(QueryDescriptor.QueryMode.BASIC, "BASIC");
    }

    public static final Map<String, ConjunctionCriterion.Conjunction> CONJUNCTION_MAP =
        new HashMap<String, ConjunctionCriterion.Conjunction>();
    static {
        CONJUNCTION_MAP.put("AND", ConjunctionCriterion.Conjunction.AND);
        CONJUNCTION_MAP.put("OR", ConjunctionCriterion.Conjunction.OR);
        CONJUNCTION_MAP.put("NONE", ConjunctionCriterion.Conjunction.NONE);
    }

    public Map getConjunctionMap() {
        return CONJUNCTION_MAP;
    }

    public static final Map<ConjunctionCriterion.Conjunction, String> INVERSE_CONJUNCTION_MAP =
        new HashMap<ConjunctionCriterion.Conjunction, String>();
    static {
        INVERSE_CONJUNCTION_MAP.put(ConjunctionCriterion.Conjunction.AND,
                                    "AND");
        INVERSE_CONJUNCTION_MAP.put(ConjunctionCriterion.Conjunction.OR, "OR");
        INVERSE_CONJUNCTION_MAP.put(ConjunctionCriterion.Conjunction.NONE,
                                    "NONE");
    }

    public static final Map<String, OperatorDef> OPERATOR_MAP =
        new HashMap<String, OperatorDef>();
    static {
        OPERATOR_MAP.put("BETWEEN", OperatorDef.BETWEEN);
        OPERATOR_MAP.put("EQUALS", OperatorDef.EQUALS);
        OPERATOR_MAP.put("GREATER_THAN", OperatorDef.GREATER_THAN);
        OPERATOR_MAP.put("GREATER_THAN_EQUALS",
                         OperatorDef.GREATER_THAN_EQUALS);
        OPERATOR_MAP.put("LESS_THAN", OperatorDef.LESS_THAN);
        OPERATOR_MAP.put("LESS_THAN_EQUALS", OperatorDef.LESS_THAN_EQUALS);
        OPERATOR_MAP.put("LIKE", OperatorDef.LIKE);
        OPERATOR_MAP.put("NO_OPERATOR", OperatorDef.NO_OPERATOR);
        OPERATOR_MAP.put("NOT_EQUALS", OperatorDef.NOT_EQUALS);
        OPERATOR_MAP.put("CONTAINS", OperatorDef.CONTAINS);
        OPERATOR_MAP.put("STARTS_WITH", OperatorDef.STARTS_WITH);
        OPERATOR_MAP.put("ENDS_WITH", OperatorDef.ENDS_WITH);
        OPERATOR_MAP.put("DOES_NOT_CONTAIN", OperatorDef.DOES_NOT_CONTAIN);
        OPERATOR_MAP.put("IN", OperatorDef.IN);
        OPERATOR_MAP.put("NOT_IN", OperatorDef.NOT_IN);
    }

    public Map getOperatorMap() {
        return OPERATOR_MAP;
    }

    public static final Map<OperatorDef, String> INVERSE_OPERATOR_MAP =
        new HashMap<OperatorDef, String>();
    static {
        INVERSE_OPERATOR_MAP.put(OperatorDef.BETWEEN, "BETWEEN");
        INVERSE_OPERATOR_MAP.put(OperatorDef.EQUALS, "EQUALS");
        INVERSE_OPERATOR_MAP.put(OperatorDef.GREATER_THAN, "GREATER_THAN");
        INVERSE_OPERATOR_MAP.put(OperatorDef.GREATER_THAN_EQUALS,
                                 "GREATER_THAN_EQUALS");
        INVERSE_OPERATOR_MAP.put(OperatorDef.LESS_THAN, "LESS_THAN");
        INVERSE_OPERATOR_MAP.put(OperatorDef.LESS_THAN_EQUALS,
                                 "LESS_THAN_EQUALS");
        INVERSE_OPERATOR_MAP.put(OperatorDef.LIKE, "LIKE");
        INVERSE_OPERATOR_MAP.put(OperatorDef.NO_OPERATOR, "NO_OPERATOR");
        INVERSE_OPERATOR_MAP.put(OperatorDef.CONTAINS, "CONTAINS");
        INVERSE_OPERATOR_MAP.put(OperatorDef.STARTS_WITH, "STARTS_WITH");
        INVERSE_OPERATOR_MAP.put(OperatorDef.ENDS_WITH, "ENDS_WITH");
        INVERSE_OPERATOR_MAP.put(OperatorDef.DOES_NOT_CONTAIN,
                                 "DOES_NOT_CONTAIN");
        INVERSE_OPERATOR_MAP.put(OperatorDef.IN, "IN");
        INVERSE_OPERATOR_MAP.put(OperatorDef.NOT_IN, "NOT_IN");
    }
    public static final Map<String, AttributeDescriptor.ComponentType> COMPONENT_MAP;
    static {
        COMPONENT_MAP =
                new HashMap<String, AttributeDescriptor.ComponentType>();
        //values in jdeveloper property inspector
        COMPONENT_MAP.put(AttributeDef.CHECK_BOX,
                          AttributeDescriptor.ComponentType.selectBooleanCheckbox);
        COMPONENT_MAP.put(AttributeDef.CHOICE,
                          AttributeDescriptor.ComponentType.selectOneListbox);
        COMPONENT_MAP.put(AttributeDef.COMBO,
                          AttributeDescriptor.ComponentType.selectManyChoice);
        COMPONENT_MAP.put(AttributeDef.COMBO_LOV,
                          AttributeDescriptor.ComponentType.inputComboboxListOfValues);
        COMPONENT_MAP.put(AttributeDef.INPUT_DATE,
                          AttributeDescriptor.ComponentType.inputDate);
        COMPONENT_MAP.put(AttributeDef.INPUT_TEXT,
                          AttributeDescriptor.ComponentType.inputText);
    }
}
