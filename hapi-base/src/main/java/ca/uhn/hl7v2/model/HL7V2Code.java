/**
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the
 * specific language governing rights and limitations under the License.
 *
 * The Original Code is "HL7V2Code.java".  Description:
 * "Interface representing a single code from a given V2 table"
 *
 * The Initial Developer of the Original Code is Accenture LLP. Copyright (C)
 * 2017.  All Rights Reserved.
 *
 * Contributor(s): ______________________________________.
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

package ca.uhn.hl7v2.model;

/**
 * <p>
 * Interface representing a single code from a given V2 table.
 * </p>
 * 
 * @author michael.i.calderero
 */
public interface HL7V2Code {

    /**
     * @return the string representation of the code in the given code system (e.g. M, F).
     */
    String getCode();

    /**
     * @return the HL7 V2 version (e.g. 2.1, 2.5.1, 2.6).
     */
    String getVersion();

    /**
     * @return friendly name for the code (e.g. Male, Female).
     */
    String getDescription();

    /**
     * @return table number for this code's code system.
     */
    String getCodeSystemTableNumber();

    /**
     * @return the OID for this code's code system if available (e.g. 2.16.840.1.113883.12.1).
     */
    String getCodeSystemOID();

    /**
     * @return friendly name for this code's code system (e.g. Administrative Sex).
     */
    String getCodeSystemName();
}
