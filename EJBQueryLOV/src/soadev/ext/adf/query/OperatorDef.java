package soadev.ext.adf.query;

/**
 * List of all available operators.
 * This was taken out of the original QueryPageDef to make it accessible to
 * other classes aside from QueryPageDef
 * By Owie
 * _symbol attribute was removed because its of no good use;
 */
public enum OperatorDef {
    NO_OPERATOR(""),
    EQUALS("Equals"),
    GREATER_THAN("Greater Than"),
    GREATER_THAN_EQUALS("Greater Than Equals"),
    LESS_THAN("Less Than"),
    LESS_THAN_EQUALS("Less Than Equals"),
    NOT_EQUALS("Not Equals"),
    LIKE("Like"),
    BETWEEN("Between"),
    CONTAINS("Contains"),
    DOES_NOT_CONTAIN("Does not Contain"),
    STARTS_WITH("Starts With"),
    ENDS_WITH("Ends With"),
    IN("In"),
    NOT_IN("Not In");
    
    
    private String _label;

    OperatorDef(String label) {
        _label = label;
    }

    public String getLabel() {
        return _label;
    }
    
    private int parseIndex(String value){
        try {
             return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }

    /**
     * Returns the number of operands required by an OperatorType instance. This may be useful in
     * determining the number of input components to display for the operator and attribute.
     * @return an int
     */
    public int getOperandCount() {
        switch (this) {
        case BETWEEN:
            return 2;
        case IN:
        case NOT_IN:
            return -1;
        default:
            return 1;
        }
    }

    public boolean hasVariableOperands() {
        return (getOperandCount() == -1);
    }


}
