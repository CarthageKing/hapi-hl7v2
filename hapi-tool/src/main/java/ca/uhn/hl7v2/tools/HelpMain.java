package ca.uhn.hl7v2.tools;

import java.io.PrintWriter;

public class HelpMain {

    public static void main(String[] args) throws Exception {
        PrintWriter pw = new PrintWriter(System.out);
        pw.println("Tools Available:");
        pw.println("================");
        pw.println();
        pw.println(ConvertSourceTableToHAPITestPanelFormat.class.getName());
        pw.println(GenerateNormativeTableXmlFiles.class.getName());
        pw.println();
        pw.println("To run a specific command, you can invoke it via the following command: java -cp hapi-tool.jar <command name>");
        pw.println();
        pw.println("Example: java -cp hapi-tool.jar " + ConvertSourceTableToHAPITestPanelFormat.class.getName());
        pw.flush();
    }
}
