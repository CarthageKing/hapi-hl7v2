/**
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/ 
Software distributed under the License is distributed on an "AS IS" basis, 
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the 
specific language governing rights and limitations under the License. 

The Original Code is "CodeTableGenerator.java".  Description: 
"Generates enums for HL7 V2 tables classes based on the HL7 database" 

The Initial Developer of the Original Code is Accenture LLP. Copyright (C) 
2001.  All Rights Reserved. 

Contributor(s): michael.i.calderero 

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.database.NormativeDatabase;
import ca.uhn.hl7v2.parser.DefaultModelClassFactory;
import ca.uhn.hl7v2.sourcegen.CodeTableDef.CodeEntryDef;
import ca.uhn.hl7v2.sourcegen.util.VelocityFactory;

/**
 * Generates enums for HL7 V2 tables classes based on the HL7 database.
 *
 * @author michael.i.calderero
 */
public class CodeTableGenerator {

    private static final String NO_DESCRIPTION = "##No description defined##";
    private static final String REMOVE_ME_DESCRIPTION = "##remove me##";

    private static final Logger log = LoggerFactory.getLogger(CodeTableGenerator.class);

    /**
     * Creates enums for all coding tables found in the normative database.
     * 
     * @throws Exception
     */
    public static void makeAll(String baseDirectory, String version, String theTemplatePackage, String theFileExt) throws Exception {
        //make base directory
        if (!(baseDirectory.endsWith("\\") || baseDirectory.endsWith("/"))) {
            baseDirectory = baseDirectory + "/";
        }
        File targetDir = SourceGenerator.makeDirectory(baseDirectory + DefaultModelClassFactory.getVersionPackagePath(version) + "table");
        List<CodeTableDef> tables = getCodeTables(version);

        log.info("Generating {} coding tables for version {}", tables.size(), version);
        if (tables.size() == 0) {
            log.warn("No version {} coding tables found in database {}", version, System.getProperty("ca.on.uhn.hl7.database.url"));
        }

        String basePackage = DefaultModelClassFactory.getVersionPackageName(version);

        for (CodeTableDef ctd : tables) {
            makeEnum(targetDir, ctd, version, basePackage, theTemplatePackage, theFileExt);
        }

        makeUtil(targetDir, tables, version, basePackage, theTemplatePackage, theFileExt);
    }

    public static List<CodeTableDef> getCodeTables(String version) throws SQLException {
        List<CodeTableDef> tables = new ArrayList<CodeTableDef>();
        NormativeDatabase normativeDatabase = NormativeDatabase.getInstance();
        Connection conn = normativeDatabase.getConnection();
        Statement stmt = conn.createStatement();
        // Get all the coding tables in the database
        String sql = "select a.table_id, a.description, a.table_type, a.oid_table from HL7Tables a, HL7Versions b where a.version_id=b.version_id and b.hl7_version='" + version + "'";
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            CodeTableDef ctd = new CodeTableDef();
            ctd.setTableNumber(rs.getInt(1));

            boolean shouldAdd = shouldAddCodeTable(version, ctd);

            if (shouldAdd) {
                ctd.setName(StringUtils.trimToNull(rs.getString(2)));
                if (StringUtils.isBlank(ctd.getName())) {
                    fixTableName(ctd, version);
                }
                ctd.setClassName(generateValidJavaClassName(ctd.getTableNumber(), ctd.getName()));
                ctd.setVersion(version);
                ctd.setOid(fixOid(StringUtils.trimToNull(rs.getString(4))));
                tables.add(ctd);
            }
        }

        stmt.close();

        for (Iterator<CodeTableDef> iter = tables.iterator(); iter.hasNext();) {
            CodeTableDef ctd = iter.next();
            // Get the codes for each table
            sql = "select a.table_value, a.description from HL7TableValues a, HL7Versions b where a.version_id=b.version_id and a.table_id=" + ctd.getTableNumber() + " and b.hl7_version='" + version + "'";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                CodeTableDef.CodeEntryDef ced = new CodeTableDef.CodeEntryDef();
                ced.setCode(StringUtils.trimToNull(rs.getString(1)));
                ced.setDescription(StringUtils.trimToNull(rs.getString(2)));

                if (!StringUtils.isBlank(ced.getCode())) {
                    fixCodeEntryCode(version, ctd, ced);
                    ctd.getEntries().add(ced);
                    ced.setEnumName(generateValidEnumName(version, ctd, ced, ced.getCode()));

                    if (StringUtils.isBlank(ced.getDescription())) {
                        fixCodeEntryDescription(ced, ctd, version);
                    }
                }
            }
            stmt.close();
            cleanupCodeTable(version, ctd);
            // remove this def if it has no codes in it
            if (ctd.getEntries().isEmpty()) {
                log.debug("Removed coding table version=[{}], number=[{}], name=[{}] because it had no defined codes", version, ctd.getTableNumber(), ctd.getName());
                iter.remove();
            } else {
                // Sort the enums for easier browsing of the generated code
                Collections.sort(ctd.getEntries(), new Comparator<CodeTableDef.CodeEntryDef>() {

                    @Override
                    public int compare(CodeEntryDef o1, CodeEntryDef o2) {
                        return String.CASE_INSENSITIVE_ORDER.compare(o1.getEnumName(), o2.getEnumName());
                    }
                });
            }
        }
        normativeDatabase.returnConnection(conn);
        return tables;
    }

    private static boolean shouldAddCodeTable(String version, CodeTableDef ctd) {
        if ("2.2".equals(version)) {
            if (18 == ctd.getTableNumber()) {
                // Patient Type
                // Access database defines some values without descriptions. The PDF specification says
                // "no suggested values"
                return false;
            }
        } else if ("2.3".equals(version)) {
            if (18 == ctd.getTableNumber()) {
                // Patient Type
                // Access database defines some values without descriptions. The PDF specification says
                // "no suggested values"
                return false;
            } else if (33 == ctd.getTableNumber()) {
                // ???
                // v2.3 PDF states a table 0033 for DB1-2 but the detailed field description refers to table 0334.
                // The Access database also doesn't define any codes for 0033. We treat this as a publishing error
                // and not generate this table
                // TODO: Check if the HAPI-generated code for v2.3 DB1 refers to the correct table or not...
                return false;
            }
        } else if ("2.6".equals(version)) {
            if (836 == ctd.getTableNumber()) {
                // Problem Severity
                // no values defined so exclude this table from code generation
                return false;
            } else if (838 == ctd.getTableNumber()) {
                // Problem Perspective
                // no values defined so exclude this table from code generation
                return false;
            }
        }
        return true;
    }

    private static void cleanupCodeTable(String version, CodeTableDef ctd) {
        // As per examination of generated coding tables, ... seems to indicate sample values or "no suggested values"
        // and not actual usable codes. We remove all instances of these across all versions
        removeEntries(ctd, "...");
        if ("2.3.1".equals(version)) {
            if (153 == ctd.getTableNumber()) {
                // Value Code
                // Remove duplicate/overlapping codes
                removeEntries(ctd, "70 ... 72", "75 ... 79");
            }
        } else if ("2.4".equals(version)) {
            if (153 == ctd.getTableNumber()) {
                // Value Code
                // Remove duplicate/overlapping codes
                removeEntries(ctd, "70 ... 72", "75 ... 79");
            } else if (418 == ctd.getTableNumber()) {
                // Procedure priority
                // Remove the ...
                log.warn("procedure priority entries: {}", ctd.getEntries());
            } else if (455 == ctd.getTableNumber()) {
                // Type of bill code
                // Remove the ...
                removeEntries(ctd, "...");
            } else if (456 == ctd.getTableNumber()) {
                // Revenue code
                // Remove the ...
                removeEntries(ctd, "...");
            } else if (458 == ctd.getTableNumber()) {
                // OCE edit code
                // Remove the ...
                removeEntries(ctd, "...");
            } else if (466 == ctd.getTableNumber()) {
                // Ambulatory payment classification code
                // Remove the ...
                removeEntries(ctd, "...");
            }
        } else if ("2.5".equals(version)) {
            if (418 == ctd.getTableNumber()) {
                // Procedure priority
                // Remove the ...
                log.warn("procedure priority entries: {}", ctd.getEntries());
            }
        } else if ("2.5.1".equals(version)) {
            if (210 == ctd.getTableNumber()) {
                // Access database is missing the OR value. Generate it here
                CodeTableDef.CodeEntryDef ced = new CodeTableDef.CodeEntryDef();
                ced.setCode("OR");
                ced.setDescription(NO_DESCRIPTION);
                ced.setEnumName(ced.getCode());
                ctd.getEntries().add(ced);
            } else if (418 == ctd.getTableNumber()) {
                // Procedure priority
                // Remove the ...
                log.warn("procedure priority entries: {}", ctd.getEntries());
            }
        } else if ("2.6".equals(version)) {
            if (359 == ctd.getTableNumber()) {
                // Diagnosis Priority Code
                // Remove the ...
                removeEntries(ctd, "...");
            } else if (418 == ctd.getTableNumber()) {
                // Procedure Priority
                // Remove the ...
                removeEntries(ctd, "...");
            } else if (458 == ctd.getTableNumber()) {
                // OCE edit code
                // Remove the ...
                removeEntries(ctd, "...");
            } else if (466 == ctd.getTableNumber()) {
                // Ambulatory payment classification code
                // Remove the ...
                removeEntries(ctd, "...");
            }
        }

        removeEntriesWithRemoveDescription(ctd);
    }

    private static void removeEntriesWithRemoveDescription(CodeTableDef ctd) {
        for (Iterator<CodeTableDef.CodeEntryDef> iter = ctd.getEntries().iterator(); iter.hasNext();) {
            CodeTableDef.CodeEntryDef ced = iter.next();
            if (REMOVE_ME_DESCRIPTION.equals(ced.getDescription())) {
                log.warn("Removed the code [{}]", ced.getCode());
            }
        }
    }

    private static void removeEntries(CodeTableDef ctd, String... toRemove) {
        for (Iterator<CodeTableDef.CodeEntryDef> iter = ctd.getEntries().iterator(); iter.hasNext();) {
            CodeTableDef.CodeEntryDef ced = iter.next();
            for (String tr : toRemove) {
                if (tr.equals(ced.getCode())) {
                    iter.remove();
                    break;
                }
            }
        }
    }

    private static void fixTableName(CodeTableDef ctd, String version) {
        boolean skip = false;
        if ("2.2".equals(version)) {
            // names were retrieved from HL7 OID registry using the OID
            switch (ctd.getTableNumber()) {
            case 182:
                skip = true;
                ctd.setName("Staff Type");
                break;

            case 184:
                skip = true;
                ctd.setName("Department");
                break;

            case 188:
                skip = true;
                ctd.setName("Operator ID");
                break;

            case 189:
                skip = true;
                ctd.setName("Ethnic Group");
                break;

            default:
                // do nothing
                break;
            }
        }
        if (!skip) {
            throw new RuntimeException("Coding table number " + ctd.getTableNumber() + " from version " + version + " had no name");
        }
    }

    private static void fixCodeEntryCode(String version, CodeTableDef ctd, CodeEntryDef ced) {
        // Cleanup trailing " ..."
        final String trailingDots = " ...";
        boolean cleanupTrailingDots = false;
        if ("2.4".equals(version) || "2.5".equals(version) || "2.5.1".equals(version)) {
            if (359 == ctd.getTableNumber()) {
                if (ced.getCode().endsWith(trailingDots)) {
                    cleanupTrailingDots = true;
                }
            }
        }
        if (cleanupTrailingDots) {
            int idx = ced.getCode().indexOf(" ...");
            ced.setCode(ced.getCode().substring(0, idx));
        }
        // Remove (obsolete) marker from code and put it in description
        if ("2.6".equals(version)) {
            if (396 == ctd.getTableNumber()) {
                // Coding System
                final String txt = " (obsolete)";
                if (ced.getCode().endsWith(txt)) {
                    int idx = ced.getCode().indexOf(txt);
                    ced.setCode(ced.getCode().substring(0, idx));
                    ced.setDescription(ced.getDescription() + txt);
                }
            }
        }
        // Separate out L,M,N into distinct codes
        if (301 == ctd.getTableNumber()) {
            // Universal ID Type
            final String txt = "L,M,N";
            if (ced.getCode().equals(txt)) {
                String desc = ced.getDescription();
                ced.setCode("L");
                CodeTableDef.CodeEntryDef tc = new CodeTableDef.CodeEntryDef();
                tc.setCode("M");
                tc.setDescription(desc);
                tc.setEnumName(generateValidEnumName(version, ctd, tc, tc.getCode()));
                ctd.getEntries().add(tc);
                tc = new CodeTableDef.CodeEntryDef();
                tc.setCode("N");
                tc.setDescription(desc);
                tc.setEnumName(generateValidEnumName(version, ctd, tc, tc.getCode()));
                ctd.getEntries().add(tc);
            }
        }
        // Separate out '99zzz or L' into distinct codes
        if (396 == ctd.getTableNumber()) {
            // Coding System
            final String txt = "99zzz or L";
            if (ced.getCode().equals(txt)) {
                ced.setCode("99zzz");
                CodeTableDef.CodeEntryDef tc = new CodeTableDef.CodeEntryDef();
                tc.setCode("L");
                // Set a custom description since the original description is not appropriate
                tc.setDescription("Local general code");
                tc.setEnumName(generateValidEnumName(version, ctd, tc, tc.getCode()));
                ctd.getEntries().add(tc);
            }
        }
        // Expand
        if (141 == ctd.getTableNumber()) {
            // Military Rank Grade
            if (ced.getCode().equals("E1... E9") || ced.getCode().equals("E1 ... E9") /* 2.3.1 and 2.4 */) {
                expandCode(version, ctd, ced, "E", 1, 9);
            } else if (ced.getCode().equals("O1 ... O9")) {
                expandCode(version, ctd, ced, "O", 1, 9);
            } else if (ced.getCode().equals("O1 ... O10")) {
                expandCode(version, ctd, ced, "O", 1, 10);
            } else if (ced.getCode().equals("W1 ... W4")) {
                expandCode(version, ctd, ced, "W", 1, 4);
            }
        }
        if (350 == ctd.getTableNumber()) {
            // Occurrence Code
            if ("2.3.1".equals(version) || "2.4".equals(version)) {
                if ("47 ... 49".equals(ced.getCode())) {
                    expandCode(version, ctd, ced, "", 47, 49);
                } else if ("70 ... 99".equals(ced.getCode())) {
                    expandCode(version, ctd, ced, "", 70, 99);
                }
            }
        }
        if (112 == ctd.getTableNumber()) {
            // Discharge Disposition
            if ("10 ...19".equals(ced.getCode()) || "10 ... 19".equals(ced.getCode())) {
                expandCode(version, ctd, ced, "", 10, 19);
            } else if ("21 ... 29".equals(ced.getCode())) {
                expandCode(version, ctd, ced, "", 21, 29);
            } else if ("31 ... 39".equals(ced.getCode())) {
                expandCode(version, ctd, ced, "", 31, 39);
            }
        }
        if (43 == ctd.getTableNumber()) {
            // Condition Code
            if ("12 ... 16".equals(ced.getCode())) {
                expandCode(version, ctd, ced, "", 12, 16);
            }
        }
    }

    private static void expandCode(String version, CodeTableDef ctd, CodeEntryDef primaryEntry, String prefix, int startNum, int endNum) {
        String desc = primaryEntry.getDescription();
        primaryEntry.setCode(prefix + startNum);

        for (int i = startNum + 1; i <= endNum; i++) {
            CodeTableDef.CodeEntryDef tc = new CodeTableDef.CodeEntryDef();
            tc.setCode(prefix + i);
            tc.setDescription(desc);
            tc.setEnumName(generateValidEnumName(version, ctd, tc, tc.getCode()));
            ctd.getEntries().add(tc);
        }
    }

    private static void fixCodeEntryDescription(CodeEntryDef ced, CodeTableDef ctd, String version) {
        boolean skip = false;
        boolean setDefaultEmptyDescription = false;
        if ("2.2".equals(version)) {
            if (11 == ctd.getTableNumber() && "R".equals(ced.getCode())) {
                // definition retrieved from HL7 v2.2 PDF; for some reason this wasn't set in the Access database
                ced.setDescription("System that received and processed the order");
                skip = true;
            } else if (16 == ctd.getTableNumber() && "0".equals(ced.getCode())) {
                // definition retrieved from HL7 v2.2 PDF; for some reason this wasn't set in the Access database
                ced.setDescription("Antibiotic Resistance Precautions");
                skip = true;
            }
        } else if ("2.3".equals(version)) {
            if (210 == ctd.getTableNumber() && "OR".equals(ced.getCode())) {
                // PDF and Access database both don't define a description
                setDefaultEmptyDescription = true;
                skip = true;
            }
        } else if ("2.3.1".equals(version)) {
            if (210 == ctd.getTableNumber() && "OR".equals(ced.getCode())) {
                // PDF and Access database both don't define a description
                setDefaultEmptyDescription = true;
                skip = true;
            } else if (354 == ctd.getTableNumber()) {
                // Access defines these extra entries that are not mentioned in the PDF
                // TODO: Consider removing these from the enum codes
                if ("ACK".equals(ced.getCode())) {
                    setDefaultEmptyDescription = true;
                    skip = true;
                } else if ("OMD_O01".equals(ced.getCode())) {
                    setDefaultEmptyDescription = true;
                    skip = true;
                } else if ("OMN_O01".equals(ced.getCode())) {
                    setDefaultEmptyDescription = true;
                    skip = true;
                } else if ("OMS_O01".equals(ced.getCode())) {
                    setDefaultEmptyDescription = true;
                    skip = true;
                } else if ("ORD_O02".equals(ced.getCode())) {
                    setDefaultEmptyDescription = true;
                    skip = true;
                } else if ("ORN_O02".equals(ced.getCode())) {
                    setDefaultEmptyDescription = true;
                    skip = true;
                } else if ("ORS_O02".equals(ced.getCode())) {
                    setDefaultEmptyDescription = true;
                    skip = true;
                } else if ("RDO_O01".equals(ced.getCode())) {
                    setDefaultEmptyDescription = true;
                    skip = true;
                } else if ("RRO_O02".equals(ced.getCode())) {
                    setDefaultEmptyDescription = true;
                    skip = true;
                }
            }
        } else if ("2.4".equals(version)) {
            if (210 == ctd.getTableNumber() && "OR".equals(ced.getCode())) {
                // PDF and Access database both don't define a description
                setDefaultEmptyDescription = true;
                skip = true;
            } else if (391 == ctd.getTableNumber() && "etc".equals(ced.getCode())) {
                // PDF and Access database both don't define a description
                setDefaultEmptyDescription = true;
                skip = true;
            } else if (393 == ctd.getTableNumber()) {
                if ("LINKSOFT_2.01".equals(ced.getCode())) {
                    // Access doesn't define a description but PDF does
                    ced.setDescription("Proprietary algorithm for LinkSoft v2.01");
                    skip = true;
                } else if ("MATCHWARE_1.2".equals(ced.getCode())) {
                    // Access doesn't define a description but PDF does
                    ced.setDescription("Proprietary algorithm for MatchWare v1.2");
                    skip = true;
                }
            } else if (418 == ctd.getTableNumber()) {
                // Garbage code ... will be removed in cleanupCodeTable()
                ced.setDescription(REMOVE_ME_DESCRIPTION);
                skip = true;
            } else if (455 == ctd.getTableNumber() && "...".equals(ced.getCode())) {
                // Will be removed in cleanupCodeTable()
                skip = true;
            } else if (456 == ctd.getTableNumber() && "...".equals(ced.getCode())) {
                // Will be removed in cleanupCodeTable()
                skip = true;
            } else if (458 == ctd.getTableNumber() && "...".equals(ced.getCode())) {
                // Will be removed in cleanupCodeTable()
                skip = true;
            } else if (466 == ctd.getTableNumber() && "...".equals(ced.getCode())) {
                // Will be removed in cleanupCodeTable()
                skip = true;
            }
        } else if ("2.5".equals(version)) {
            if (210 == ctd.getTableNumber() && "OR".equals(ced.getCode())) {
                // PDF and Access database both don't define a description
                setDefaultEmptyDescription = true;
                skip = true;
            } else if (418 == ctd.getTableNumber()) {
                // Garbage code ... will be removed in cleanupCodeTable()
                ced.setDescription(REMOVE_ME_DESCRIPTION);
                skip = true;
            } else if (485 == ctd.getTableNumber()) {
                if ("TD<integer>".equals(ced.getCode())) {
                    // Access doesn't define a description and PDF doesn't either. However, PDF shows a comment.
                    // We use the comment as the description
                    ced.setDescription("Timing critical within <integer> days.");
                    skip = true;
                } else if ("TH<integer>".equals(ced.getCode())) {
                    // Access doesn't define a description and PDF doesn't either. However, PDF shows a comment.
                    // We use the comment as the description
                    ced.setDescription("Timing critical within <integer> hours.");
                    skip = true;
                } else if ("TL<integer>".equals(ced.getCode())) {
                    // Access doesn't define a description and PDF doesn't either. However, PDF shows a comment.
                    // We use the comment as the description
                    ced.setDescription("Timing critical within <integer> months.");
                    skip = true;
                } else if ("TM<integer>".equals(ced.getCode())) {
                    // Access doesn't define a description and PDF doesn't either. However, PDF shows a comment.
                    // We use the comment as the description
                    ced.setDescription("Timing critical within <integer> minutes.");
                    skip = true;
                } else if ("TS<integer>".equals(ced.getCode())) {
                    // Access doesn't define a description and PDF doesn't either. However, PDF shows a comment.
                    // We use the comment as the description
                    ced.setDescription("Timing critical within <integer> seconds.");
                    skip = true;
                } else if ("TW<integer>".equals(ced.getCode())) {
                    // Access doesn't define a description and PDF doesn't either. However, PDF shows a comment.
                    // We use the comment as the description
                    ced.setDescription("Timing critical within <integer> weeks.");
                    skip = true;
                }
            }
        } else if ("2.5.1".equals(version)) {
            if (418 == ctd.getTableNumber()) {
                // Garbage code ... will be removed in cleanupCodeTable()
                ced.setDescription(REMOVE_ME_DESCRIPTION);
                skip = true;
            }
        } else if ("2.6".equals(version)) {
            if (210 == ctd.getTableNumber() && "OR".equals(ced.getCode())) {
                // PDF and Access database both don't define a description
                setDefaultEmptyDescription = true;
                skip = true;
            } else if (359 == ctd.getTableNumber() && "...".equals(ced.getCode())) {
                // Will be removed in cleanupCodeTable()
                skip = true;
            } else if (391 == ctd.getTableNumber()) {
                // Everything here has no description or comment both in the PDF and Access
                // TODO: Once HL7 defines the descriptions, perhaps in a later HL7 v2 spec, manually code
                // the description back to this spec
                // For now, generate empty description
                setDefaultEmptyDescription = true;
                skip = true;
            } else if (418 == ctd.getTableNumber()) {
                // Will be removed in cleanupCodeTable()
                skip = true;
            } else if (458 == ctd.getTableNumber()) {
                // Will be removed in cleanupCodeTable()
                skip = true;
            } else if (466 == ctd.getTableNumber()) {
                // Will be removed in cleanupCodeTable()
                skip = true;
            } else if (485 == ctd.getTableNumber()) {
                if ("TD<integer>".equals(ced.getCode())) {
                    // Access doesn't define a description and PDF doesn't either. However, PDF shows a comment.
                    // We use the comment as the description
                    ced.setDescription("Timing critical within <integer> days.");
                    skip = true;
                } else if ("TH<integer>".equals(ced.getCode())) {
                    // Access doesn't define a description and PDF doesn't either. However, PDF shows a comment.
                    // We use the comment as the description
                    ced.setDescription("Timing critical within <integer> hours.");
                    skip = true;
                } else if ("TL<integer>".equals(ced.getCode())) {
                    // Access doesn't define a description and PDF doesn't either. However, PDF shows a comment.
                    // We use the comment as the description
                    ced.setDescription("Timing critical within <integer> months.");
                    skip = true;
                } else if ("TM<integer>".equals(ced.getCode())) {
                    // Access doesn't define a description and PDF doesn't either. However, PDF shows a comment.
                    // We use the comment as the description
                    ced.setDescription("Timing critical within <integer> minutes.");
                    skip = true;
                } else if ("TS<integer>".equals(ced.getCode())) {
                    // Access doesn't define a description and PDF doesn't either. However, PDF shows a comment.
                    // We use the comment as the description
                    ced.setDescription("Timing critical within <integer> seconds.");
                    skip = true;
                } else if ("TW<integer>".equals(ced.getCode())) {
                    // Access doesn't define a description and PDF doesn't either. However, PDF shows a comment.
                    // We use the comment as the description
                    ced.setDescription("Timing critical within <integer> weeks.");
                    skip = true;
                }
            }
        }
        if (setDefaultEmptyDescription) {
            ced.setDescription(NO_DESCRIPTION);
        }
        if (!skip) {
            throw new RuntimeException("Coding table number " + ctd.getTableNumber() + " name '" + ctd.getName() + "' code '" + ced.getCode() + "' from version " + version + " had no description");
        }
    }

    private static String fixOid(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        // The oids in the Access database have space in-between. Remove them
        StringTokenizer st = new StringTokenizer(str, " ");
        StringBuilder sb = new StringBuilder();
        while (st.hasMoreTokens()) {
            sb.append(st.nextToken());
        }
        return sb.toString();
    }

    private static String generateValidJavaClassName(int tableNumber, String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        StringBuilder sb = new StringBuilder(value);
        for (int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);
            if (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c)) {
                sb.setCharAt(i, '_');
            }
        }
        String str = sb.toString();
        sb.setLength(0);
        sb.append("Table").append(tableNumber).append("_");
        StringTokenizer st = new StringTokenizer(str, " ");
        while (st.hasMoreTokens()) {
            sb.append(WordUtils.capitalizeFully(st.nextToken()));
        }
        return sb.toString();
    }

    private static String generateValidEnumName(String version, CodeTableDef ctd, CodeEntryDef ced, String value) {
        StringBuilder sb = new StringBuilder();

        if (78 == ctd.getTableNumber()) {
            if ("2.2".equals(version) || "2.3".equals(version) || "2.3.1".equals(version) || "2.4".equals(version)
                || "2.5".equals(version) || "2.5.1".equals(version) || "2.6".equals(version)) {
                // Abnormal flags
                if ("<".equals(value)) {
                    value = "BELOW_ABSOLUTE_LOW_OFF_INSTRUMENT_SCALE";
                } else if (">".equals(value)) {
                    value = "ABOVE_ABSOLUTE_HIGH_OFF_INSTRUMENT_SCALE";
                }
            }
        } else if (505 == ctd.getTableNumber()) {
            if ("2.5".equals(version) || "2.5.1".equals(version) || "2.6".equals(version)) {
                // Cyclic Entry/Exit Indicator
                if ("*".equals(value)) {
                    value = "FIRST_SERVICE_REQUEST";
                } else if ("#".equals(value)) {
                    value = "LAST_SERVICE_REQUEST";
                }
            }
        } else if (391 == ctd.getTableNumber()) {
            if ("2.6".equals(version)) {
                // Access and PDF define somewhat similar codes (e.g. ENCODED ORDER and ENCODED_ORDER). Append
                // a '_' suffix at the end for the underscore variant
                if ("ENCODED_ORDER".equals(ced.getCode()) || "FINANCIAL_COMMON_ORDER".equals(ced.getCode())
                    || "FINANCIAL_TIMING_QUANTITY".equals(ced.getCode()) || "GENERAL_RESOURCE".equals(ced.getCode())
                    || "LOCATION_RESOURCE".equals(ced.getCode()) || "PATIENT_VISIT".equals(ced.getCode())
                    || "PERSONNEL_RESOURCE".equals(ced.getCode())) {
                    value = value + "_";
                }
            }
        }

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            if (!Character.isLetterOrDigit(c)) {
                c = '_';
            }

            sb.append(c);
        }

        String newval = sb.toString();

        if (!(Character.isLetter(newval.charAt(0)) || '_' == newval.charAt(0))) {
            newval = '_' + newval;
        }

        return newval.toUpperCase();
    }

    private static void makeEnum(File targetDirectory, CodeTableDef codeTableDef, String version, String basePackage, String theTemplatePackage, String theFileExt) throws Exception {
        //make sure that targetDirectory is a directory ... 
        if (!targetDirectory.isDirectory()) {
            throw new IOException("Can't create file in " + targetDirectory.toString() + " - it is not a directory.");
        }

        String fileName = targetDirectory.toString() + "/" + codeTableDef.getClassName() + "." + theFileExt;
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName, false), SourceGenerator.ENCODING));

        theTemplatePackage = theTemplatePackage.replace(".", "/");
        Template template = VelocityFactory.getClasspathTemplateInstance(theTemplatePackage + "/hl7code_table.vsm");
        VelocityContext ctx = new VelocityContext();
        ctx.put("codeTable", codeTableDef);
        ctx.put("basePackageName", basePackage);

        template.merge(ctx, out);

        out.flush();
        out.close();
    }

    private static void makeUtil(File targetDirectory, List<CodeTableDef> tables, String version, String basePackage, String theTemplatePackage, String theFileExt) throws Exception {
        //make sure that targetDirectory is a directory ... 
        if (!targetDirectory.isDirectory()) {
            throw new IOException("Can't create file in " + targetDirectory.toString() + " - it is not a directory.");
        }

        String className = "CodingTableUtil";
        String fileName = targetDirectory.toString() + "/" + className + "." + theFileExt;
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName, false), SourceGenerator.ENCODING));

        theTemplatePackage = theTemplatePackage.replace(".", "/");
        Template template = VelocityFactory.getClasspathTemplateInstance(theTemplatePackage + "/hl7code_table_util.vsm");
        VelocityContext ctx = new VelocityContext();
        ctx.put("className", className);
        ctx.put("codeTables", tables);
        ctx.put("basePackageName", basePackage);

        template.merge(ctx, out);

        out.flush();
        out.close();
    }
}
