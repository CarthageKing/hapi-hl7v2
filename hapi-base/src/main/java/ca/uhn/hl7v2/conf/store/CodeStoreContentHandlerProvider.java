/**
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the
 * specific language governing rights and limitations under the License.
 *
 * The Original Code is "CodeStoreContentHandlerProvider.java".  Description:
 * "Content handler provider interface used by AppendableCodeStore."
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

import java.util.Map;

import org.xml.sax.ContentHandler;

import ca.uhn.hl7v2.conf.store.AbstractSimpleCodeStore.CodeAndName;

/**
 * Content handler provider interface used by AppendableCodeStore.
 *
 * @author michael.i.calderero
 */
public interface CodeStoreContentHandlerProvider {

    /**
     * @return a new instance of a {@link CodeStoreContentHandler}.
     */
    CodeStoreContentHandler provideInstance();

    /**
     * Content handler interface used by AppendableCodeStore.
     *
     * @author michael.i.calderero
     */
    interface CodeStoreContentHandler extends ContentHandler {

        /**
         * @return the codes loaded from the input source.
         */
        Map<CodeAndName, Map<String, CodeAndName>> getLoadedCodes();
    }
}
