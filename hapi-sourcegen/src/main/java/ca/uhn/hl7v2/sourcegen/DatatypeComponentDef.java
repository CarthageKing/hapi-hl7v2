package ca.uhn.hl7v2.sourcegen;

public class DatatypeComponentDef extends DatatypeDef {

    private int table;
    private String parentType;
    private int indexWithinParent;
    private Integer maxLength;

    public DatatypeComponentDef(String theParentType, int theIndexWithinParent, String theType, String theName, int theTable) {
        super(theType, theName);
        parentType = theParentType;
        indexWithinParent = theIndexWithinParent;
        table = theTable;
    }

    public int getTable() {
        return table;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public Integer getMaxLength() {
        return maxLength;
    }
    
    public String getAccessorName() {
        return SourceGenerator.makeAccessorName(getName(), parentType);
    }

    public String getAlternateAccessorName() {
        return SourceGenerator.makeAlternateAccessorName(getName(), parentType, indexWithinParent + 1);
    }

}
