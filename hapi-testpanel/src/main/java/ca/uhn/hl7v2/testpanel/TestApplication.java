package ca.uhn.hl7v2.testpanel;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.parser.XMLParser;

public class TestApplication {
	public static void main(String[] args) throws Exception {
		String msg = "MSH|^~\\&|blahblah xyz - EMR Emulator|Acme Health Inc.^280s-343s^2.16.840.1.113883.3.1666.100.4.5.1|||20161028063752-0500||ADT^A01^ADT_A01|30001|T|2.6|1\r"
				+ "EVN|A01|20161028063752.992-0500\r"
				+ "PID|||111^^^Acme Health Inc.&280s-343s&2.16.840.1.113883.3.1666.100.4.5.1||Jones^John Paul||19861105053752-0600|M||^Caucasian|29 Turnip Drive||^PRS^CP^^^^^^^^^+1-512-555-1212~^^Internet^john.smith@woohoo.com\r"
				+ "PV1||I|^^^Emergency Room 1^^^^^^300&Acme Health Inc.&280s-343s&2.16.840.1.113883.3.1666.100.4.5.1||||777^^^^^^^^Acme Health Inc.&280s-343s&2.16.840.1.113883.3.1666.100.4.5.1||||||||||||222|||||||||||||||||||||||||20161028063752-0500\r"
				+ "ROL|||AT|||||||||^^Internet^practi@org.org\r"
				+ "DG1|1||34j2^Sample diagnosis 1^http://sample.org||20161028063752.993-0500|A|||||||||1|||||10111^Acme Health Inc.^280s-343s^2.16.840.1.113883.3.1666.100.4.5.1\r"
				+ "DG1|2||9sk-342s^Sample diagnosis 2^http://warlock.de||20161028063752.993-0500|A||||||||||||||10222^Acme Health Inc.^280s-343s^2.16.840.1.113883.3.1666.100.4.5.1";

		HapiContext v2Context = v2Context = new DefaultHapiContext();
		// Create the MCF. NOTE: You can parse lower-version messages with a higher
		// version MCF
		// see
		// http://hl7api.sourceforge.net/xref/ca/uhn/hl7v2/examples/HandlingMultipleVersions.html
		CanonicalModelClassFactory mcf = new CanonicalModelClassFactory("2.6");
		v2Context.setModelClassFactory(mcf);
		PipeParser v2PipeParser = v2Context.getPipeParser();

		DefaultXMLParser v2XmlParser = new DefaultXMLParser();
		// output friendly names
		v2XmlParser.setOutputFriendlyNames(true);

		// parse the v2 text with HAPI
		Message hapiMsg = v2PipeParser.parse(msg);

		String xmlV2Message = v2XmlParser.encode(hapiMsg);
		System.out.println("xml v2 msg->\n" + xmlV2Message);
	}
}
