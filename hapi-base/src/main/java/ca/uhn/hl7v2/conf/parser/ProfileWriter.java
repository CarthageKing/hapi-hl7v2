/**
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the
 * specific language governing rights and limitations under the License.
 *
 * The Original Code is "ProfileWriter.java".  Description:
 * "Creates a conformance profile from a given message object. Useful if you just have the generated classes from
 * the conformance profile but not the actual conformance profile itself. Case in point: classes generated from
 * the Normative database."
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
package ca.uhn.hl7v2.conf.parser;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.Location;
import ca.uhn.hl7v2.conf.ProfileException;
import ca.uhn.hl7v2.model.AbstractSegment;
import ca.uhn.hl7v2.model.Composite;
import ca.uhn.hl7v2.model.GenericSegment;
import ca.uhn.hl7v2.model.Group;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.MessageVisitor;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.model.Structure;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.primitive.NULLDT;
import ca.uhn.hl7v2.parser.ModelClassFactory;

/**
 * Creates a conformance profile from a given message object. Useful if you just have the generated classes from
 * the conformance profile but not the actual conformance profile itself. Case in point: classes generated from
 * the Normative database.
 *
 * @author michael.i.calderero
 */
public class ProfileWriter {

    public enum Behavior {
        /**
         * Throw an exception if the writer will generate an invalid profile, like creating a sub-subcomponent.
         * This happens for example in HL7v 2.6 ORC-7 where the CQ datatype (i.e. component) contains CWE datatype
         * (i.e. subcomponent). CWE is a composite datatype itself so its children will become sub-subcomponents.
         */
        INVALID_PROFILE_ERROR,

        /**
         * In the case of HL7v2.6 ORC-7, will generate the sub-subcomponents. The generated profile may fail to
         * parse with corresponding profile parsers.
         */
        ALLOW_GENERATE_INVALID_PROFILE,

        /**
         * In the case of HL7v2.6 ORC-7, only generate up-to the subcomponent level.
         */
        MAKE_VALID_PROFILE;
    }

    private static final String NO_URI = XMLConstants.XML_NS_URI;
    //private static final Attributes EMPTY_ATTRIBUTES = new AttributesImpl();

    // Refer to v2.8.2 section 2.B.7.7 Conformance Usage
    private static final String USAGE_REQUIRED = "R";
    //private static final String USAGE_REQUIRED_EMPTY = "RE";
    private static final String USAGE_OPTIONAL = "O";
    //private static final String USAGE_CONDITIONAL = "C";
    //private static final String USAGE_NOT_SUPPORTED = "X";
    private static final String USAGE_BACKWARD_COMPATIBILITY = "B";
    private static final String USAGE_WITHDRAWN = "W";

    // according to javadoc of AbstractSegment.add(), 0 means "no limit"
    private static final int UNLIMITED_REPS_INTEGER = 0;
    private static final String UNLIMITED_REPETITIONS = "*";

    private static final String DATATYPE_WITHDRAWN = "-";

    private final Behavior behavior;

    public ProfileWriter() {
        this(Behavior.MAKE_VALID_PROFILE);
    }

    public ProfileWriter(Behavior theBehavior) {
        behavior = theBehavior;
    }

    public Behavior getBehavior() {
        return behavior;
    }

    public void writeHeader(String hl7v2Version, String metadataName, ContentHandler w) throws Exception {
        se(w, "HL7v2xConformanceProfile", "HL7Version", hl7v2Version, "ProfileType", "HL7");

        se(w, "MetaData", "Name", metadataName, "OrgName", "HL7").ws(w).ee(w, "MetaData");
        se(w, "UseCase").ws(w).ee(w, "UseCase");

        se(w, "Encodings");
        se(w, "Encoding").ws(w, "ER7").ee(w, "Encoding");
        ee(w, "Encodings");

        se(w, "DynamicDef").ws(w).ee(w, "DynamicDef");
    }

    public void writeMessageDefinition(Message msg, ContentHandler w) throws Exception {
        String fullMsgName = msg.getClass().getSimpleName();
        String[] pair = fullMsgName.split("_");
        String msgType = null;
        String evtType = null;

        if (2 == pair.length) {
            msgType = pair[0];
            evtType = pair[1];
        } else {
            msgType = fullMsgName;
            evtType = fullMsgName;
        }

        se(w, "HL7v2xStaticDef", "MsgType", msgType, "EventType", evtType, "MsgStructID", fullMsgName, "EventDesc", (null == msg.getFriendlyName() ? fullMsgName : msg.getFriendlyName()), "Identifier", msg.getClass().getName());
        // MetaData
        writeGroup(msg, w, msg);
        ee(w, "HL7v2xStaticDef");
    }

    public void writeFooter(ContentHandler w) throws Exception {
        ee(w, "HL7v2xConformanceProfile");
    }

    public void writeComplete(Message msg, ContentHandler w) throws Exception {
        writeHeader(msg.getVersion(), msg.getClass().getSimpleName(), w);
        writeMessageDefinition(msg, w);
        writeFooter(w);
    }

    private void writeGroup(Group grp, ContentHandler w, Message msg) throws Exception {
        String[] segOrGrpNames = grp.getNames();
        String[] friendlyNames = grp.getFriendlyNames();

        for (int i = 0; i < segOrGrpNames.length; i++) {
            String sogn = segOrGrpNames[i];
            boolean isRequired = grp.isRequired(sogn);
            String requiredStr = getUsage(isRequired);
            String minStr = getMinimum(isRequired);
            String maxStr = getMaximum(grp.isRepeating(sogn));
            if (grp.isGroup(sogn)) {
                se(w, "SegGroup", "Name", sogn, "LongName", grp.getClass(sogn).getSimpleName(), "Usage", requiredStr, "Min", minStr, "Max", maxStr);
                Group childGrp = createGroup(grp.getClass(sogn), msg);
                if (childGrp.getNames().length < 1) {
                    ws(w);
                } else {
                    try {
                        writeGroup(childGrp, w, msg);
                    } catch (StackOverflowError e) {
                        throw new RuntimeException("msg '" + msg.getClass().getName() + "' grp '" + grp.getClass().getName() + "' childgrp '" + childGrp.getClass().getName() + "' stack overflow", e);
                    }
                }
                ee(w, "SegGroup");
            } else {
                se(w, "Segment", "Name", grp.getClass(sogn).getSimpleName(), "LongName", friendlyNames[i], "Usage", requiredStr, "Min", minStr, "Max", maxStr);
                Segment childSeg = createSegment(grp.getClass(sogn), msg);
                if (childSeg.getNames().length < 1) {
                    ws(w);
                } else {
                    writeSegment(childSeg, w, msg);
                }
                ee(w, "Segment");
            }
        }
    }

    private void writeSegment(Segment seg, ContentHandler w, Message msg) throws Exception {
        String[] fieldNames = seg.getNames();
        for (int i = 0; i < fieldNames.length; i++) {
            String fn = fieldNames[i];
            int offset = i + 1;
            boolean isRequired = seg.isRequired(offset);
            String minStr = getMinimum(isRequired);
            String maxStr = String.valueOf(seg.getMaxCardinality(offset));
            if (maxStr.equals(String.valueOf(UNLIMITED_REPS_INTEGER))) {
                maxStr = UNLIMITED_REPETITIONS;
            }
            String lenStr = String.valueOf(seg.getLength(offset));
            Type type = ((AbstractSegment) seg).createNewTypeWithoutReflection(i);
            String dataType = type.getClass().getSimpleName();
            String usage = USAGE_OPTIONAL;

            if (null != type.getOptionality()) {
                usage = type.getOptionality();
            }
            if (type instanceof NULLDT) {
                usage = USAGE_WITHDRAWN;
                dataType = DATATYPE_WITHDRAWN;
                lenStr = null;
            } else if (isRequired) {
                usage = USAGE_REQUIRED;
            } else if (seg.getLength(offset) < 1) {
                usage = USAGE_BACKWARD_COMPATIBILITY;
                // XXX: ProfileParser XSD does not allow length of zero so we change it to 1
                lenStr = "1";
            }

            List<String> params = new ArrayList<String>();
            addParam(params, "Datatype", dataType);
            addParam(params, "Usage", usage);

            if (null != minStr) {
                addParam(params, "Min", minStr);
            }
            if (null != maxStr) {
                addParam(params, "Max", maxStr);
            }
            if (null != lenStr) {
                addParam(params, "Length", lenStr);
            }
            String tbl = formatTableId(type.getTableId());
            if (null != tbl) {
                addParam(params, "Table", tbl);
            }

            addParam(params, "Name", fn);

            se(w, "Field", params.toArray(new String[0]));

            if (type instanceof Composite) {
                writeComposite((Composite) type, w, msg, "Component", 0);
            } else {
                ws(w);
            }

            ee(w, "Field");
        }
    }

    private void writeComposite(Composite type, ContentHandler w, Message msg, String tag, int level) throws Exception {
        if (level > 1) {
            switch (behavior) {
            case INVALID_PROFILE_ERROR:
                throw new ProfileException("Message '" + msg.getClass() + "' still had components for a SubComponent: " + type.getClass());

            case MAKE_VALID_PROFILE:
                ws(w);
                return;

            default:
                break;
            }
        }

        Type[] components = type.getComponents();

        for (int i = 0; i < components.length; i++) {
            Type t = components[i];
            String dataType = t.getName();
            // TODO: required?
            String usage = USAGE_OPTIONAL;
            if (null != t.getOptionality()) {
                usage = t.getOptionality();
            }
            String lenStr = (null == t.getMaxLength() ? null : t.getMaxLength().toString());
            if (null != t.getMaxLength() && t.getMaxLength() < 1) {
                usage = USAGE_BACKWARD_COMPATIBILITY;
                lenStr = null;
            }
            if (t instanceof NULLDT) {
                usage = USAGE_WITHDRAWN;
                lenStr = null;
                dataType = DATATYPE_WITHDRAWN;
            }

            List<String> params = new ArrayList<String>();
            addParam(params, "Datatype", dataType);
            addParam(params, "Usage", usage);

            if (null != lenStr) {
                addParam(params, "Length", lenStr);
            }
            String tbl = formatTableId(t.getTableId());
            if (null != tbl) {
                addParam(params, "Table", formatTableId(t.getTableId()));
            }

            addParam(params, "Name", t.getFriendlyName());

            se(w, tag, params.toArray(new String[0]));

            if (t instanceof Composite) {
                writeComposite((Composite) t, w, msg, "SubComponent", level + 1);
            } else {
                ws(w);
            }

            ee(w, tag);
        }
    }

    private void addParam(List<String> params, String k, String v) {
        params.add(k);
        params.add(v);
    }

    private String formatTableId(String tableId) {
        if (null == tableId) {
            return null;
        }
        try {
            return String.format("%04d", Integer.parseInt(tableId));
        } catch (NumberFormatException e) {
            return tableId;
        }
    }

    private Segment createSegment(Class<? extends Structure> clzz, Message msg) throws Exception {
        if (GenericSegment.class.isAssignableFrom(clzz)) {
            Constructor<? extends Structure> ctor = clzz.getConstructor(Group.class, String.class);
            return (Segment) ctor.newInstance(new GroupAdapter(msg), (String) null);
        } else {
            Constructor<? extends Structure> ctor = clzz.getConstructor(Group.class, ModelClassFactory.class);
            return (Segment) ctor.newInstance(new GroupAdapter(msg), (ModelClassFactory) null);
        }
    }

    private Group createGroup(Class<? extends Structure> clzz, Message msg) throws Exception {
        Constructor<? extends Structure> ctor = clzz.getConstructor(Group.class, ModelClassFactory.class);
        return (Group) ctor.newInstance(new GroupAdapter(msg), (ModelClassFactory) null);
    }

    private String getMaximum(boolean repeating) {
        return (repeating ? UNLIMITED_REPETITIONS : "1");
    }

    private String getMinimum(boolean isRequired) {
        return (isRequired ? "1" : "0");
    }

    private String getUsage(boolean isRequired) {
        return (isRequired ? USAGE_REQUIRED : USAGE_OPTIONAL);
    }

    //private ProfileWriter se(ContentHandler w, String tag) throws SAXException {
    //    w.startElement(NO_URI, null, tag, EMPTY_ATTRIBUTES);
    //    return this;
    //}

    private ProfileWriter se(ContentHandler w, String tag, String... attrPairs) throws SAXException {
        if (0 != (attrPairs.length % 2)) {
            throw new IllegalArgumentException("Expected even number of attrPairs.length");
        }
        AttributesImpl atts = new AttributesImpl();
        for (int i = 0; i < attrPairs.length; i += 2) {
            String k = attrPairs[i];
            String v = attrPairs[i + 1];
            atts.addAttribute(NO_URI, null, k, null, v);
        }
        w.startElement(NO_URI, null, tag, atts);
        return this;
    }

    private ProfileWriter ws(ContentHandler w, String string) throws SAXException {
        char[] arr = string.toCharArray();
        w.characters(arr, 0, arr.length);
        return this;
    }

    private ProfileWriter ws(ContentHandler w) throws SAXException {
        char[] arr = "".toCharArray();
        w.characters(arr, 0, arr.length);
        return this;
    }

    private ProfileWriter ee(ContentHandler w, String tag) throws SAXException {
        w.endElement(NO_URI, null, tag);
        return this;
    }

    class GroupAdapter implements Group {

        private static final long serialVersionUID = 7267813081189186454L;

        private final Message msg;

        public GroupAdapter(Message theMsg) {
            msg = theMsg;
        }

        @Override
        public Message getMessage() {
            return msg;
        }

        @Override
        public String getName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getFriendlyName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Group getParent() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean accept(MessageVisitor visitor, Location currentLocation) throws HL7Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        public Location provideLocation(Location parentLocation, int index, int repetition) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEmpty() throws HL7Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        public Structure[] getAll(String name) throws HL7Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        public Structure get(String name) throws HL7Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        public Structure get(String name, int rep) throws HL7Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isRequired(String name) throws HL7Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isRepeating(String name) throws HL7Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isChoiceElement(String name) throws HL7Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isGroup(String name) throws HL7Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        public String[] getNames() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String[] getFriendlyNames() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Class<? extends Structure> getClass(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String addNonstandardSegment(String name) throws HL7Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        public String addNonstandardSegment(String name, int theIndex) throws HL7Exception {
            throw new UnsupportedOperationException();
        }
    }
}
