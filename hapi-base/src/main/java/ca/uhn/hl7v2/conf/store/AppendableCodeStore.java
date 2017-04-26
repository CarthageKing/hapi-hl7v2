/**
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the
 * specific language governing rights and limitations under the License.
 *
 * The Original Code is "AppendableCodeStore.java".  Description:
 * "A mutable code store. New information appended to the code store will overwrite existing codes. Useful for
 * stores wherein its state will be comprised of the tables defined by the standard plus any other tables
 * from conformance profiles."
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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import ca.uhn.hl7v2.conf.ProfileException;
import ca.uhn.hl7v2.model.HL7V2Code;

/**
 * A mutable code store. New information appended to the code store will overwrite existing codes. Useful for
 * stores wherein its state will be comprised of the tables defined by the standard plus any other tables
 * from conformance profiles.
 *
 * Note: This class is not thread-safe.
 *
 * @author michael.i.calderero
 */
public class AppendableCodeStore extends AbstractSimpleCodeStore {

    private final SAXParserFactory saxParserFactory;
    private final CodeStoreContentHandlerProvider contentHandlerProvider;

    public AppendableCodeStore(CodeStoreContentHandlerProvider theContentHandlerProvider) {
        this(SAXParserFactory.newInstance(), theContentHandlerProvider);
    }

    public AppendableCodeStore(SAXParserFactory theSaxParserFactory, CodeStoreContentHandlerProvider theContentHandlerProvider) {
        saxParserFactory = theSaxParserFactory;
        contentHandlerProvider = theContentHandlerProvider;
    }

    public AppendableCodeStore append(URL path) throws ProfileException {
        InputStream is = null;
        try {
            is = path.openStream();
            return append(new InputStreamReader(is, "UTF-8"));
        } catch (IOException e) {
            throw new ProfileException("Error reading profile at url: " + path, e);
        } finally {
            closeFully(is);
        }
    }

    public AppendableCodeStore append(InputStream is) throws ProfileException {
        return append(new InputSource(is));
    }

    public AppendableCodeStore append(Reader rdr) throws ProfileException {
        return append(new InputSource(rdr));
    }

    public AppendableCodeStore append(InputSource isrc) throws ProfileException {
        try {
            XMLReader xr = saxParserFactory.newSAXParser().getXMLReader();
            CodeStoreContentHandlerProvider.CodeStoreContentHandler handler = contentHandlerProvider.provideInstance();
            xr.parse(isrc);
            upsertCodes(codes, handler.getLoadedCodes());
        } catch (ParserConfigurationException e) {
            throw new ProfileException("Error reading profile", e);
        } catch (SAXException e) {
            throw new ProfileException("Error reading profile", e);
        } catch (IOException e) {
            throw new ProfileException("Error reading profile", e);
        }
        return this;
    }

    public AppendableCodeStore append(String codeSystem, String codeSystemName, HL7V2Code[] entries) {
        return append(codeSystem, codeSystemName, Arrays.asList(entries));
    }

    public AppendableCodeStore append(String codeSystem, String codeSystemName, List<HL7V2Code> entries) {
        CodeAndName key = new CodeAndName(codeSystem, codeSystemName);
        Map<String, CodeAndName> map = codes.get(key);
        if (null == map) {
            map = new HashMap<String, CodeAndName>();
            codes.put(key, map);
        }
        for (int i = 0; i < entries.size(); i++) {
            HL7V2Code hl7v2code = entries.get(i);
            map.put(hl7v2code.getCode(), new CodeAndName(hl7v2code.getCode(), hl7v2code.getDescription()));
        }
        return this;
    }

    private void upsertCodes(Map<CodeAndName, Map<String, CodeAndName>> store, Map<CodeAndName, Map<String, CodeAndName>> newCodes) {
        for (Entry<CodeAndName, Map<String, CodeAndName>> entry : newCodes.entrySet()) {
            Map<String, CodeAndName> exist = store.get(entry.getKey());
            if (null != exist) {
                exist.putAll(entry.getValue());
            } else {
                store.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public static void closeFully(Closeable c) {
        try {
            if (null != c) {
                c.close();
            }
        } catch (Exception e) {
            // do nothing
        }
    }
}
