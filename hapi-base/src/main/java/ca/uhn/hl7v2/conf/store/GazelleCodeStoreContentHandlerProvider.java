/**
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the
 * specific language governing rights and limitations under the License.
 *
 * The Original Code is "GazelleCodeStoreContentHandlerProvider.java".  Description:
 * "Content handler provider for reading in table schemas made by the NIST tool."
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
package ca.uhn.hl7v2.conf.store;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ca.uhn.hl7v2.conf.store.AbstractSimpleCodeStore.CodeAndName;

/**
 * Content handler provider for reading in table schemas made by the NIST tool.
 *
 * @author michael.i.calderero
 */
public class GazelleCodeStoreContentHandlerProvider implements CodeStoreContentHandlerProvider {

    public GazelleCodeStoreContentHandlerProvider() {
        // noop
    }

    @Override
    public CodeStoreContentHandler provideInstance() {
        return new Handler();
    }

    /**
     * Content handler for reading in table schemas made by the NIST tool.
     *
     * @author michael.i.calderero
     */
    private class Handler extends DefaultHandler implements CodeStoreContentHandler {

        private CodeAndName currentKey;
        private Map<CodeAndName, Map<String, CodeAndName>> loadedCodes = new HashMap<CodeAndName, Map<String, CodeAndName>>();

        public Handler() {
            // noop
        }

        public Map<CodeAndName, Map<String, CodeAndName>> getLoadedCodes() {
            return loadedCodes;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if ("hl7table".equals(qName)) {
                String code = StringEscapeUtils.escapeXml(attributes.getValue("codeSys"));
                String name = StringEscapeUtils.escapeXml(attributes.getValue("name"));
                currentKey = new CodeAndName(code, name);
                loadedCodes.put(currentKey, new HashMap<String, CodeAndName>());
            } else if ("tableElement".equals(qName)) {
                String code = StringEscapeUtils.escapeXml(attributes.getValue("code"));
                String name = StringEscapeUtils.escapeXml(attributes.getValue("displayName"));
                loadedCodes.get(currentKey).put(code, new CodeAndName(code, name));
            }
        }
    }
}
