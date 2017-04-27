package ca.uhn.hl7v2.conf.parser;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.conf.ProfileException;
import ca.uhn.hl7v2.conf.check.DefaultValidator;
import ca.uhn.hl7v2.conf.parser.ProfileWriter.Behavior;
import ca.uhn.hl7v2.conf.spec.RuntimeProfile;
import ca.uhn.hl7v2.conf.store.AppendableCodeStore;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.tools.GenerateNormativeTableXmlFiles.MyContentHandler;

public class ProfileWriterTest {

    private ProfileWriter testObj;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        testObj = new ProfileWriter();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test_generateORU_R01_Profile() throws Exception {
        ca.uhn.hl7v2.model.v26.message.ORU_R01 tmpInstance = new ca.uhn.hl7v2.model.v26.message.ORU_R01();
        StringWriter sw = new StringWriter();
        MyContentHandler handler = new MyContentHandler(sw);

        testObj = new ProfileWriter(Behavior.ALLOW_GENERATE_INVALID_PROFILE);
        testObj.writeComplete(tmpInstance, handler);
        //System.out.println(sw.toString());

        // Test the generated profile
        ProfileParser parser = new ProfileParser(true);
        try {
            parser.parse(sw.toString());
            Assert.fail("Did not throw expected exception");
        } catch (ProfileException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid content was found starting with element 'SubComponent'. One of '{ImpNote, Description, Reference, Predicate, DataValues}' is expected"));
        }

        sw = new StringWriter();
        handler = new MyContentHandler(sw);
        testObj = new ProfileWriter(Behavior.INVALID_PROFILE_ERROR);
        try {
            testObj.writeComplete(tmpInstance, handler);
            Assert.fail("Did not throw expected exception");
        } catch (ProfileException e) {
            Assert.assertTrue(e.getMessage().contains("still had components for a SubComponent"));
        }

        sw = new StringWriter();
        handler = new MyContentHandler(sw);
        testObj = new ProfileWriter(Behavior.MAKE_VALID_PROFILE);
        testObj.writeComplete(tmpInstance, handler);
        //System.out.println(sw.toString());

        HapiContext hapiCtx = new DefaultHapiContext();

        // Get a profile
        RuntimeProfile profile = parser.parse(sw.toString());
        // Get a code store
        AppendableCodeStore acs = ca.uhn.hl7v2.model.v26.table.CodingTableUtil.createTestPanelCompatibleCodeStore(null);
        // Setup validation
        DefaultValidator validator = new DefaultValidator(hapiCtx);
        validator.setCodeStore(acs);

        String v23Msg = "MSH|^~\\&|brawld||||20011105133010||ORU^R01|33ssj33|P|2.3\r"
            + "EVN|1|20170415010203\r"
            + "PID|1||601321^^^^x~7623454789^^^^y~4355678^^^^z||ROBERTS^RICHARD^^^MR||19560520|M|||20 THE AVENUE^^NEWTOWN^MIDSHIRE^SO74 3ZZ||01442411411|||M\r"
            + "NTE|1|L|Laboratory comment appears here\r"
            + "OBR|1||B0004321^biolab|Bab_RU\\T\\E^Urea \\T\\ Electrolytes^L||| 200111030800||||||?Dehydrated|200111030840|| ^SMITH^RICHARD^^^DR.||||||200111031400||CH|||^^^200111030800^^S|^JONES^JACK^^^DR.~^ASKHAM^DAVID^^^DR.\r"
            + "NTE|1|L|U\\T\\E Request Item comment appears here"
            + "OBX|2|SN|^8123^Sodium^LN||150|mmol/l|136-148|H|||F";

        System.out.println("Validating 2.3 message");
        PipeParser pparser = hapiCtx.getPipeParser();
        // disable the internal validation because we're going to validate the message with our custom profile
        pparser.getParserConfiguration().setValidating(false);
        Message msg = pparser.parse(v23Msg);
        // Validate!!!
        List<HL7Exception> errlst = Arrays.asList(validator.validate(msg, profile.getMessage()));
        for (int i = 0; i < errlst.size(); i++) {
            System.out.println(errlst.get(i));
        }
        Assert.assertEquals(13, errlst.size());

        String v26Msg = "MSH|^~\\&|brawld||||20011105133010||ORU^R01|33ssj33|P|2.6\r"
            + "PID|||2123||testsurname\r"
            + "ORC|CN\r"
            + "OBR||||3zdf\r"
            + "EVN|1|20170415010203\r"
            + "PID|1||601321^^^^x~7623454789^^^^y~4355678^^^^z||ROBERTS^RICHARD^^^MR||19560520|M|||20 THE AVENUE^^NEWTOWN^MIDSHIRE^SO74 3ZZ||01442411411|||M\r"
            + "NTE|1|L|Laboratory comment appears here\r"
            + "OBR|1||B0004321^biolab|Bab_RU\\T\\E^Urea \\T\\ Electrolytes^L||| 200111030800||||||?Dehydrated|200111030840|| ^SMITH^RICHARD^^^DR.||||||200111031400||CH|||^^^200111030800^^S|^JONES^JACK^^^DR.~^ASKHAM^DAVID^^^DR.\r"
            + "NTE|1|L|U\\T\\E Request Item comment appears here\r"
            + "OBX|2|SN|^8123^Sodium^LN||150|mmol/l|136-148|H|||F";

        System.out.println("Validating 2.6 message");
        msg = pparser.parse(v26Msg);
        errlst = Arrays.asList(validator.validate(msg, profile.getMessage()));
        for (int i = 0; i < errlst.size(); i++) {
            System.out.println(errlst.get(i));
        }
    }

    @Test
    public void test_MultipleMessages() throws Exception {
        String profileString = null;
        {
            StringWriter sw = new StringWriter();
            MyContentHandler handler = new MyContentHandler(sw);
            testObj.writeHeader("2.6", "various", handler);
            testObj.writeMessageDefinition(new ca.uhn.hl7v2.model.v26.message.ORU_R01(), handler);
            testObj.writeMessageDefinition(new ca.uhn.hl7v2.model.v26.message.ADT_A01(), handler);
            testObj.writeMessageDefinition(new ca.uhn.hl7v2.model.v26.message.ADT_A05(), handler);
            testObj.writeMessageDefinition(new ca.uhn.hl7v2.model.v26.message.BAR_P12(), handler);
            testObj.writeFooter(handler);
            profileString = sw.toString();
        }

        //System.out.println(profileString);
        // Test the generated profile
        ProfileParser parser = new ProfileParser(true);
        parser.parse(profileString);
    }

    @Test
    public void test_WithdrawnFields() throws Exception {
        String inputMsg = "MSH|^~\\&|dd||||20011105133010||ORU^R01|fg|P|2.6\r"
            + "PID|||x||d\r"
            + "ORC|CA\r"
            + "OBR||||d\r"
            + "FT1||||20170427a000036+0000||CD|d|transaction description^x^y|transaction desc alt";

        String profileString = null;
        {
            ca.uhn.hl7v2.model.v26.message.ORU_R01 tmpInstance = new ca.uhn.hl7v2.model.v26.message.ORU_R01();
            StringWriter sw = new StringWriter();
            MyContentHandler handler = new MyContentHandler(sw);
            testObj.writeComplete(tmpInstance, handler);
            profileString = sw.toString();
        }

        HapiContext hapiCtx = new DefaultHapiContext();
        ProfileParser parser = new ProfileParser(true);
        RuntimeProfile profile = parser.parse(profileString);
        AppendableCodeStore acs = ca.uhn.hl7v2.model.v26.table.CodingTableUtil.createTestPanelCompatibleCodeStore(null);
        DefaultValidator validator = new DefaultValidator(hapiCtx);
        validator.setCodeStore(acs);
        PipeParser pparser = hapiCtx.getPipeParser();
        pparser.getParserConfiguration().setValidating(false);
        Message msg = pparser.parse(inputMsg);
        List<HL7Exception> errlst = Arrays.asList(validator.validate(msg, profile.getMessage()));
        for (int i = 0; i < errlst.size(); i++) {
            System.out.println(errlst.get(i));
        }
        Assert.assertEquals(4, errlst.size());
        Assert.assertTrue(errlst.get(0).getMessage().equals("Message structure null doesn't match profile type of ORU_R01"));
        Assert.assertTrue(errlst.get(1).getMessage().equals("Required element Message Structure is missing at MSH-9(0)"));
        Assert.assertTrue(errlst.get(2).getMessage().contains("\"Transaction Description\" is present but specified as withdrawn"));
        Assert.assertTrue(errlst.get(3).getMessage().contains("\"Transaction Description - Alt\" is present but specified as withdrawn"));
    }
}
