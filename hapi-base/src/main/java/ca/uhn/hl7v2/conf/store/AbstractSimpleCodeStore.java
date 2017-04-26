/**
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the
 * specific language governing rights and limitations under the License.
 *
 * The Original Code is "AbstractSimpleCodeStore.java".  Description:
 * "A code store backed by a map implementation. The keys will be the table ids (e.g. table numbers) and
 * the value per key will be a list of the valid codes for that table."
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ca.uhn.hl7v2.conf.ProfileException;

/**
 * A code store backed by a map implementation. The keys will be the table ids (e.g. table numbers) and
 * the value per key will be a list of the valid codes for that table.
 * 
 * @author michael.i.calderero
 */
public abstract class AbstractSimpleCodeStore extends AbstractCodeStore {

    protected Map<CodeAndName, Map<String, CodeAndName>> codes = new HashMap<CodeAndName, Map<String, CodeAndName>>();

    protected AbstractSimpleCodeStore() {
        // noop
    }

    /**
     * @return a read-only view of the underlying map of the code store.
     */
    public Map<CodeAndName, Map<String, CodeAndName>> getCodesMap() {
        return Collections.unmodifiableMap(codes);
    }

    @Override
    public String[] getValidCodes(String codeSystem) throws ProfileException {
        Map<String, CodeAndName> result = getCodeTable(codeSystem);
        if (result == null) {
            throw new ProfileException("Unknown code system: " + codeSystem);
        }
        String[] arr = new String[result.size()];
        int counter = 0;
        for (Iterator<CodeAndName> iter = result.values().iterator(); iter.hasNext();) {
            arr[counter] = iter.next().getCode();
            counter++;
        }
        return arr;
    }

    @Override
    public boolean knowsCodes(String codeSystem) {
        try {
            return getCodeTable(codeSystem) != null;
        } catch (ProfileException e) {
            return false;
        }
    }

    /**
     * @param codeSystem
     *            code system (i.e. table id/sequence number) to use.
     *
     * @return list of codes tied to the given id.
     *
     * @throws ProfileException
     *             if the input parameter is <tt>null</tt>.
     */
    protected Map<String, CodeAndName> getCodeTable(String codeSystem) throws ProfileException {
        if (codeSystem == null) {
            throw new ProfileException("The input codeSystem parameter cannot be null");
        }
        return codes.get(new CodeAndName(codeSystem, null));
    }

    public static class CodeAndName implements Comparable<CodeAndName> {

        private final String code;
        private final String name;

        public CodeAndName(String theCode, String theName) {
            code = theCode;
            name = theName;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        @Override
        public int compareTo(CodeAndName o) {
            return code.compareTo(o.code);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((code == null) ? 0 : code.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof CodeAndName)) {
                return false;
            }
            CodeAndName other = (CodeAndName) obj;
            if (code == null) {
                if (other.code != null) {
                    return false;
                }
            } else if (!code.equals(other.code)) {
                return false;
            }
            return true;
        }
    }
}
