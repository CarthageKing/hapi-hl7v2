package ca.uhn.hl7v2.sourcegen;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

public class DatatypeDef {

    private boolean hasTableId;
    private String type;
    private String name;
    private List<DatatypeComponentDef> mySubComponentDefs = new ArrayList<DatatypeComponentDef>();

    public DatatypeDef(String theType, String theDescription) {
        super();
        type = theType;
        name = theDescription;
        
        if (StringUtils.isEmpty(theType)) {
            throw new IllegalArgumentException("Missing datatype");
        }
        if (StringUtils.isEmpty(theDescription)) {
            throw new IllegalArgumentException("Missing name in type:" + theType);
        }
    }

    public void addSubcomponentDef(DatatypeComponentDef theDef) {
        mySubComponentDefs.add(theDef);
    }
    
    public List<DatatypeComponentDef> getSubComponentDefs() {
        return mySubComponentDefs;
    }

    public void setHasTableId(boolean hasTableId) {
        this.hasTableId = hasTableId;
    }

    public boolean isHasTableId() {
        return hasTableId;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }
    
    public String getNameEscaped() {
        return (null == name ? null : WordUtils.capitalize(name).replace("\"", "\\\""));
    }


    public boolean isIsType() {
        return getType().equals("IS");
    }

    public boolean isIdType() {
        return getType().equals("ID") || getType().equals("IS");
    }

    public boolean isSpecialCasePrimitive() {
        return type.equals("IS") || // Constant
               type.equals("ID") || // Constant
               type.equals("DT") || // Constant
               type.equals("DTM") || // Constant
               type.equals("TM");
    }

    public boolean isTextPrimitive() {
        return type.equals("ST") || // Constant
               type.equals("TX") || // Constant
               type.equals("FT");
    }

}
