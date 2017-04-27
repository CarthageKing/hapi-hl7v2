/**
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the
 * specific language governing rights and limitations under the License.
 *
 * The Original Code is "GenerateNormativeProfileFiles.java".  Description:
 * "Generates the conformance profile for all messages from all versions of the Normative HL7 V2 standard."
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import ca.uhn.hl7v2.conf.ProfileException;
import ca.uhn.hl7v2.conf.parser.ProfileParser;
import ca.uhn.hl7v2.conf.parser.ProfileWriter;
import ca.uhn.hl7v2.model.AbstractSuperMessage;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.tools.GenerateNormativeTableXmlFiles.MyContentHandler;

/**
 * Generates the conformance profile for all messages from all versions of the Normative HL7 V2 standard.
 *
 * @author michael.i.calderero
 */
public class GenerateNormativeProfileFiles {

    private static final String OUTPUT_FORMAT = "outformat";

    private static final String OUTFORMAT_ALL_IN_ONE_FILE = "all-in-one";
    private static final String OUTFORMAT_SEPARATE_FILES = "separate";

    public static void main(String[] args) throws Exception {
        Options options = getCommandLineOptions();
        CommandLine cl = null;
        try {
            cl = new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            displayUsageAndExitWithError(options);
        }

        final File OUTFOLDER = new File("output");
        String outputFormat = cl.getOptionValue(OUTPUT_FORMAT);

        if (OUTFORMAT_ALL_IN_ONE_FILE.equals(outputFormat)) {

        } else if (OUTFORMAT_SEPARATE_FILES.equals(outputFormat)) {

        } else {
            displayUsageAndExitWithError(options);
        }

        // TODO: PD1 is an empty segment. Find out if this is a bug in the sourcegen
        String version = "2.1";
        String packageName = ca.uhn.hl7v2.model.v21.message.ADT_A01.class.getPackage().getName();
        boolean skipValidation = true;
        generateProfile(OUTFOLDER, outputFormat, version, packageName, skipValidation);

        // TODO: MFN messages have the Zxx segment, which is (1) an empty segment and (2) the profile XSD doesn't allow
        // lowercase letters in the name. But the v2.2 PDF mentions this segment so it must be valid. Find out how
        // to handle this
        version = "2.2";
        packageName = ca.uhn.hl7v2.model.v22.message.ADT_A01.class.getPackage().getName();
        skipValidation = true;
        generateProfile(OUTFOLDER, outputFormat, version, packageName, skipValidation);

        // TODO: MFN message have the GenericSegment segment which is supposed to represent the Z segment. The profile
        // XSD disallows names longer than 3. Find out how to handle this, or maybe just change the profile
        version = "2.3";
        packageName = ca.uhn.hl7v2.model.v23.message.ADT_A01.class.getPackage().getName();
        skipValidation = true;
        generateProfile(OUTFOLDER, outputFormat, version, packageName, skipValidation);

        // TODO: ADR_A19 has some seg groups which have very long names that don't match the SegGroup naming convention
        // imposed in the profile XSD. Maybe fix this to proper names. This will change the API though
        version = "2.3.1";
        packageName = ca.uhn.hl7v2.model.v231.message.ADT_A01.class.getPackage().getName();
        skipValidation = true;
        generateProfile(OUTFOLDER, outputFormat, version, packageName, skipValidation);

        // TODO: MFN/MFR messages have the Zxx segment, which is (1) an empty segment and (2) the profile XSD doesn't allow
        // lowercase letters in the name. But the v2.4 PDF mentions this segment so it must be valid. Find out how
        // to handle this
        version = "2.4";
        packageName = ca.uhn.hl7v2.model.v24.message.ADT_A01.class.getPackage().getName();
        skipValidation = true;
        generateProfile(OUTFOLDER, outputFormat, version, packageName, skipValidation);

        // TODO: MFN/MFR messages have the Hxx segment, which is (1) an empty segment and (2) the profile XSD doesn't allow
        // lowercase letters in the name. But the v2.5 PDF mentions this segment so it must be valid. Find out how
        // to handle this. Hxx refers to the same Z-segments
        version = "2.5";
        packageName = ca.uhn.hl7v2.model.v25.message.ADT_A01.class.getPackage().getName();
        skipValidation = true;
        generateProfile(OUTFOLDER, outputFormat, version, packageName, skipValidation);

        // TODO: MFN/MFR messages have the Hxx segment, which is (1) an empty segment and (2) the profile XSD doesn't allow
        // lowercase letters in the name. But the v2.5.1 PDF mentions this segment so it must be valid. Find out how
        // to handle this. Hxx refers to the same Z-segments
        version = "2.5.1";
        packageName = ca.uhn.hl7v2.model.v251.message.ADT_A01.class.getPackage().getName();
        skipValidation = true;
        generateProfile(OUTFOLDER, outputFormat, version, packageName, skipValidation);

        // TODO: MFN/MFR messages have the Hxx segment, which is (1) an empty segment and (2) the profile XSD doesn't allow
        // lowercase letters in the name. But the v2.6 PDF mentions this segment so it must be valid. Find out how
        // to handle this. Hxx refers to the same Z-segments
        version = "2.6";
        packageName = ca.uhn.hl7v2.model.v26.message.ADT_A01.class.getPackage().getName();
        skipValidation = true;
        generateProfile(OUTFOLDER, outputFormat, version, packageName, skipValidation);
    }

    private static void generateProfile(final File OUTFOLDER, String outputFormat, String version, String packageName, boolean skipValidation) throws IOException, ClassNotFoundException, UnsupportedEncodingException, FileNotFoundException, Exception, InstantiationException, IllegalAccessException, ProfileException {
        System.out.println("Generating profiles for version " + version);
        List<Class<?>> loadedClazzes = findallClasses(packageName);

        if (OUTFORMAT_ALL_IN_ONE_FILE.equals(outputFormat)) {
            File outFile = new File(OUTFOLDER, "HL7_" + version + "_AllMessageProfiles.xml");
            Writer bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8"));
            MyContentHandler handler = new MyContentHandler(bw);
            ProfileWriter pwriter = new ProfileWriter();
            pwriter.writeHeader(version, "All HL7v" + version + "-defined messages", handler);
            for (int i = 0; i < loadedClazzes.size(); i++) {
                Class<?> c = loadedClazzes.get(i);
                Message msg = (Message) c.newInstance();
                if (msg instanceof AbstractSuperMessage) {
                    // do not generate because not a valid message in the Normative database
                } else {
                    pwriter.writeMessageDefinition(msg, handler);
                }
            }
            pwriter.writeFooter(handler);
            bw.flush();
            bw.close();
            if (!skipValidation) {
                // Verify that profile is parseable
                ProfileParser pparser = new ProfileParser(true);
                pparser.parse(readAllIntoString(outFile));
            }
        } else if (OUTFORMAT_SEPARATE_FILES.equals(outputFormat)) {
            File verFolder = new File(OUTFOLDER, "HL7_" + version + "_AllMessageProfiles");
            verFolder.mkdirs();
            for (int i = 0; i < loadedClazzes.size(); i++) {
                Class<?> c = loadedClazzes.get(i);
                Message msg = (Message) c.newInstance();
                if (msg instanceof AbstractSuperMessage) {
                    // do not generate because not a valid message in the Normative database
                } else {
                    File outFile = new File(verFolder, msg.getClass().getSimpleName() + "_v" + version + "_MessageProfile.xml");
                    Writer bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8"));
                    MyContentHandler handler = new MyContentHandler(bw);
                    ProfileWriter pwriter = new ProfileWriter();
                    pwriter.writeComplete(msg, handler);
                    bw.flush();
                    bw.close();
                    // TODO: validation
                }
            }
        }
    }

    private static String readAllIntoString(File f) throws IOException {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line = null;
        while (null != (line = br.readLine())) {
            pw.println(line);
        }
        br.close();
        return sw.toString();
    }

    private static List<Class<?>> findallClasses(String packageName) throws IOException, ClassNotFoundException {
        List<Class<?>> loadedClazzes = new ArrayList<Class<?>>();
        // Solution from: http://stackoverflow.com/questions/520328/can-you-find-all-classes-in-a-package-using-reflection
        ClassLoader clazzLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> urls = clazzLoader.getResources(packageName.replace('.', '/'));
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            URLConnection urlConn = url.openConnection();
            if (urlConn instanceof JarURLConnection) {
                JarURLConnection jarConn = (JarURLConnection) urlConn;
                JarFile jf = jarConn.getJarFile();
            } else if (urlConn.getURL().toExternalForm().startsWith("file:/")) {
                File folder = new File(url.getFile());
                File[] clazzFiles = folder.listFiles(new FileFilter() {

                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isFile() && pathname.getName().endsWith(".class");
                    }
                });
                for (File f : clazzFiles) {
                    String basefilename = f.getName();
                    int idx = basefilename.lastIndexOf('.');
                    if (idx > 0) {
                        String clazzName = basefilename.substring(0, idx);
                        loadedClazzes.add(Class.forName(packageName + "." + clazzName));
                    }
                }
            }
        }
        return loadedClazzes;
    }

    private static Options getCommandLineOptions() {
        Options opts = new Options();
        Option o = null;
        String[] opta = {
            OUTPUT_FORMAT
        };
        String[] optd = {
            "Format of the output: '" + OUTFORMAT_ALL_IN_ONE_FILE + "' or '" + OUTFORMAT_SEPARATE_FILES + "'"
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
