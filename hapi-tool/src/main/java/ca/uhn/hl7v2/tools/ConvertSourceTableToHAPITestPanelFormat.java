/**
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the
 * specific language governing rights and limitations under the License.
 *
 * The Original Code is "ConvertSourceTableToHAPITestPanelFormat.java".  Description:
 * "Convert the given input file of table or valueset definitions into the table format supported by HAPI TestPanel."
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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import ca.uhn.hl7v2.conf.store.AbstractSimpleCodeStore.CodeAndName;
import ca.uhn.hl7v2.conf.store.AppendableCodeStore;
import ca.uhn.hl7v2.conf.store.GazelleCodeStoreContentHandlerProvider;

/**
 * Convert the given input file of table or valueset definitions into the table format supported by HAPI TestPanel.
 *
 * Currently, only table definitions in NIST format are supported.
 *
 * @author michael.i.calderero
 */
public class ConvertSourceTableToHAPITestPanelFormat {

    private static final String INPUTFILE = "infile";
    private static final String OUTFILENAME = "outfilename";

    public static void main(String[] args) throws Exception {
        Options options = getCommandLineOptions();
        CommandLine cl = null;
        try {
            cl = new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            displayUsageAndExitWithError(options);
        }

        String outFilename = cl.getOptionValue(OUTFILENAME);
        String inputFilePath = cl.getOptionValue(INPUTFILE);
        File inFile = new File(inputFilePath);
        InputStream istrm = new FileInputStream(inFile);

        GazelleCodeStoreContentHandlerProvider provider = new GazelleCodeStoreContentHandlerProvider();
        AppendableCodeStore acs = new AppendableCodeStore(provider);
        acs.append(istrm);
        istrm.close();

        final File OUTFOLDER = new File("output");
        final String tableTitle = "Table converted from " + inputFilePath;
        Map<CodeAndName, Map<String, CodeAndName>> map = new TreeMap<CodeAndName, Map<String, CodeAndName>>(acs.getCodesMap());
        GenerateNormativeTableXmlFiles.writeTablesInHapiTestPanelFormat(OUTFOLDER, tableTitle, map, outFilename);
    }

    private static Options getCommandLineOptions() {
        Options opts = new Options();
        Option o = null;
        String[] opta = {
            INPUTFILE,
            OUTFILENAME
        };
        String[] optd = {
            "Path to the file to convert. Table definition files in NIST format is the only format that is currently supported",
            "Output filename. Should just be the filename only and not include any paths"
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
}
