/**
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the
 * specific language governing rights and limitations under the License.
 *
 * The Original Code is "GenerateNormativeTableXmlFiles.java".  Description:
 * "Generates the valueset or table XML files in either NIST or HAPI TestPanel format. The source of the data will be
 * all the versions of the Normative HL7 V2 standard."
 *
 * The Initial Developer of the Original Code is Accenture LLP. Copyright (C)
 * 2017.  All Rights Reserved.
 *
 * Contributor(s): michael.i.calderero
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * GNU General Public License (the "GPL"), in which case the provisions of the GPL are
 * applicable instead of those above.  If you wish to allow use of your version of this
 * file only under the terms of the GPL and not to allow others to use your version
 * of this file under the MPL, indicate your decision by deleting  the provisions above
 * and replace  them with the notice and other provisions required by the GPL License.
 * If you do not delete the provisions above, a recipient may use your version of
 * this file under either the MPL or the GPL.
 *
 */
package ca.uhn.hl7v2.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import javax.xml.XMLConstants;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import ca.uhn.hl7v2.conf.store.AbstractSimpleCodeStore.CodeAndName;
import ca.uhn.hl7v2.conf.store.AppendableCodeStore;
import ca.uhn.hl7v2.model.HL7V2Code;

/**
 * Generates the valueset or table XML files in either NIST or HAPI TestPanel format. The source of the data will be
 * all the versions of the Normative HL7 V2 standard.
 *
 * @author michael.i.calderero
 */
public class GenerateNormativeTableXmlFiles {

    private static final String OUTPUT_FORMAT = "outformat";

    private static final String OUTFORMAT_NIST = "NIST";
    private static final String OUTFORMAT_HAPI_TESTPANEL = "HAPITestPanel";

    public static void main(String[] args) throws Exception {
        Options options = getCommandLineOptions();
        CommandLine cl = null;
        try {
            cl = new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            displayUsageAndExitWithError(options);
        }

        String outputFormat = cl.getOptionValue(OUTPUT_FORMAT);

        if (OUTFORMAT_NIST.equals(outputFormat)) {

        } else if (OUTFORMAT_HAPI_TESTPANEL.equals(outputFormat)) {

        } else {
            displayUsageAndExitWithError(options);
        }

        final File OUTFOLDER = new File("output");
        AppendableCodeStore acs = null;
        HL7V2Code info = null;

        info = ca.uhn.hl7v2.model.v21.table.Table1_Sex.M;
        acs = ca.uhn.hl7v2.model.v21.table.CodingTableUtil.createTestPanelCompatibleCodeStore(null);
        write(OUTFOLDER, info, acs, outputFormat);

        info = ca.uhn.hl7v2.model.v22.table.Table1_Sex.M;
        acs = ca.uhn.hl7v2.model.v22.table.CodingTableUtil.createTestPanelCompatibleCodeStore(null);
        write(OUTFOLDER, info, acs, outputFormat);

        info = ca.uhn.hl7v2.model.v23.table.Table1_Sex.M;
        acs = ca.uhn.hl7v2.model.v23.table.CodingTableUtil.createTestPanelCompatibleCodeStore(null);
        write(OUTFOLDER, info, acs, outputFormat);

        info = ca.uhn.hl7v2.model.v231.table.Table1_Sex.M;
        acs = ca.uhn.hl7v2.model.v231.table.CodingTableUtil.createTestPanelCompatibleCodeStore(null);
        write(OUTFOLDER, info, acs, outputFormat);

        info = ca.uhn.hl7v2.model.v24.table.Table1_AdministrativeSex.M;
        acs = ca.uhn.hl7v2.model.v24.table.CodingTableUtil.createTestPanelCompatibleCodeStore(null);
        write(OUTFOLDER, info, acs, outputFormat);

        info = ca.uhn.hl7v2.model.v25.table.Table1_AdministrativeSex.M;
        acs = ca.uhn.hl7v2.model.v25.table.CodingTableUtil.createTestPanelCompatibleCodeStore(null);
        write(OUTFOLDER, info, acs, outputFormat);

        info = ca.uhn.hl7v2.model.v251.table.Table1_AdministrativeSex.M;
        acs = ca.uhn.hl7v2.model.v251.table.CodingTableUtil.createTestPanelCompatibleCodeStore(null);
        write(OUTFOLDER, info, acs, outputFormat);

        info = ca.uhn.hl7v2.model.v26.table.Table1_AdministrativeSex.M;
        acs = ca.uhn.hl7v2.model.v26.table.CodingTableUtil.createTestPanelCompatibleCodeStore(null);
        write(OUTFOLDER, info, acs, outputFormat);
    }

    private static Options getCommandLineOptions() {
        Options opts = new Options();
        Option o = null;
        String[] opta = {
            OUTPUT_FORMAT
        };
        String[] optd = {
            "Format of the output: '" + OUTFORMAT_NIST + "' or '" + OUTFORMAT_HAPI_TESTPANEL + "'"
        };

        assert (opta.length == optd.length);

        for (int i = 0; i < opta.length; i++) {
            o = new Option(opta[i], true, optd[i]);
            o.setRequired(true);
            opts.addOption(o);
        }

        return opts;
    }

    private static void displayUsageAndExitWithError(Options options) {
        new HelpFormatter().printHelp("thecmd", options);
        System.exit(1);
    }

    private static void write(File outFolder, HL7V2Code info, AppendableCodeStore acs, String outputFormat) throws IOException, SAXException {
        outFolder.mkdirs();

        Map<CodeAndName, Map<String, CodeAndName>> map = new TreeMap<CodeAndName, Map<String, CodeAndName>>(acs.getCodesMap());

        if (OUTFORMAT_NIST.equals(outputFormat)) {

        } else if (OUTFORMAT_HAPI_TESTPANEL.equals(outputFormat)) {
            String filename = "Tables_" + info.getVersion() + "_" + OUTFORMAT_HAPI_TESTPANEL + ".xml";
            writeTablesInHapiTestPanelFormat(outFolder, "TableFile for HL7 Normative version " + info.getVersion(), map, filename);
        }
    }

    public static void writeTablesInHapiTestPanelFormat(File outFolder, String tableTitle, Map<CodeAndName, Map<String, CodeAndName>> map, String filename) throws UnsupportedEncodingException, FileNotFoundException, SAXException, IOException {
        final Attributes EMPTY_ATTRIBUTES = new AttributesImpl();
        final String NULL_URI = XMLConstants.NULL_NS_URI;
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outFolder, filename)), "UTF-8"));
        MyContentHandler handler = new MyContentHandler(writer);
        handler.startElement(NULL_URI, null, "tableFile", EMPTY_ATTRIBUTES);

        handler.startElement(NULL_URI, null, "id", EMPTY_ATTRIBUTES);
        writeStringCharacters(handler, UUID.randomUUID().toString());
        handler.endElement(NULL_URI, null, "id");

        handler.startElement(NULL_URI, null, "name", EMPTY_ATTRIBUTES);
        writeStringCharacters(handler, tableTitle);
        handler.endElement(NULL_URI, null, "name");

        for (Entry<CodeAndName, Map<String, CodeAndName>> entry : map.entrySet()) {
            handler.startElement(NULL_URI, null, "table", EMPTY_ATTRIBUTES);

            handler.startElement(NULL_URI, null, "id", EMPTY_ATTRIBUTES);
            writeStringCharacters(handler, entry.getKey().getCode());
            handler.endElement(NULL_URI, null, "id");

            handler.startElement(NULL_URI, null, "name", EMPTY_ATTRIBUTES);
            writeStringCharacters(handler, entry.getKey().getName());
            handler.endElement(NULL_URI, null, "name");

            Map<String, CodeAndName> innermap = new TreeMap<String, CodeAndName>(entry.getValue());

            for (Entry<String, CodeAndName> innerEntry : innermap.entrySet()) {
                handler.startElement(NULL_URI, null, "code", EMPTY_ATTRIBUTES);

                handler.startElement(NULL_URI, null, "code", EMPTY_ATTRIBUTES);
                writeStringCharacters(handler, innerEntry.getValue().getCode());
                handler.endElement(NULL_URI, null, "code");

                handler.startElement(NULL_URI, null, "displayName", EMPTY_ATTRIBUTES);
                writeStringCharacters(handler, innerEntry.getValue().getName());
                handler.endElement(NULL_URI, null, "displayName");

                handler.endElement(NULL_URI, null, "code");
            }

            handler.endElement(NULL_URI, null, "table");
        }

        handler.endElement(NULL_URI, null, "tableFile");
        writer.flush();
        writer.close();
    }

    private static void writeStringCharacters(MyContentHandler handler, String string) throws SAXException {
        char[] arr = string.toCharArray();
        handler.characters(arr, 0, arr.length);
    }

    private static class MyContentHandler extends DefaultHandler {

        private static final int INDENT_INCREMENT = 4;

        protected boolean wroteCharacters;
        protected boolean startedElement;
        protected int currIndent;
        protected PrintWriter pwriter;

        public MyContentHandler(final Writer w) {
            pwriter = new PrintWriter(w);
        }

        private void writeIndent() {
            if (currIndent > 0) {
                pwriter.printf("%" + currIndent + "s", " ");
            }
        }

        private void incrementIndent() {
            currIndent += INDENT_INCREMENT;
        }

        private void decrementIndent() {
            currIndent -= INDENT_INCREMENT;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if (startedElement) {
                pwriter.println();
            }
            writeIndent();
            pwriter.print("<");
            pwriter.print(qName);
            for (int i = 0; i < atts.getLength(); i++) {
                String k = atts.getQName(i);
                String v = StringEscapeUtils.escapeXml(atts.getValue(i));

                pwriter.print(" ");
                pwriter.print(k);
                pwriter.print("=\"");
                pwriter.print(v);
                pwriter.print("\"");
            }
            pwriter.print(">");
            incrementIndent();
            startedElement = true;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String s = new String(ch, start, length);
            s = StringEscapeUtils.escapeXml(s);
            pwriter.print(s);
            wroteCharacters = true;
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            decrementIndent();
            if (!wroteCharacters) {
                writeIndent();
            }
            pwriter.print("</");
            pwriter.print(qName);
            pwriter.println(">");
            startedElement = false;
            wroteCharacters = false;
        }
    }
}