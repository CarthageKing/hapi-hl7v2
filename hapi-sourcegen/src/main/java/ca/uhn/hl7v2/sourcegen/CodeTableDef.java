/**
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/ 
Software distributed under the License is distributed on an "AS IS" basis, 
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the 
specific language governing rights and limitations under the License. 

The Original Code is "CodeTableDef.java".  Description: 
"A structure for storing information about a HL7 V2 table" 

The Initial Developer of the Original Code is Accenture LLP. Copyright (C) 
2017.  All Rights Reserved. 

Contributor(s): ______________________________________. 

Alternatively, the contents of this file may be used under the terms of the 
GNU General Public License (the  �GPL�), in which case the provisions of the GPL are 
applicable instead of those above.  If you wish to allow use of your version of this 
file only under the terms of the GPL and not to allow others to use your version 
of this file under the MPL, indicate your decision by deleting  the provisions above 
and replace  them with the notice and other provisions required by the GPL License.  
If you do not delete the provisions above, a recipient may use your version of 
this file under either the MPL or the GPL. 

 */
package ca.uhn.hl7v2.sourcegen;

import java.util.ArrayList;
import java.util.List;

/**
 * @author michael.i.calderero
 */
public class CodeTableDef {

    private int tableNumber;
    private String name;
    private String className;
    private String version;
    private String oid;
    private List<CodeEntryDef> entries = new ArrayList<>();

    public CodeTableDef() {
        // noop
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public List<CodeEntryDef> getEntries() {
        return entries;
    }

    public void setEntries(List<CodeEntryDef> entries) {
        this.entries = entries;
    }

    public static class CodeEntryDef {

        private String enumName;
        private String code;
        private String description;

        public CodeEntryDef() {
            // noop
        }

        public String getEnumName() {
            return enumName;
        }

        public void setEnumName(String enumName) {
            this.enumName = enumName;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDescriptionJavaEscaped() {
            return description.replace("\"", "\\\"");
        }

        @Override
        public String toString() {
            return code;
        }
    }
}
