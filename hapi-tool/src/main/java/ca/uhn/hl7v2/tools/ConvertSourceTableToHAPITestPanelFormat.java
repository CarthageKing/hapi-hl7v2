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
            "Path to the file to convert",
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
