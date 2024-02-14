/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.protocols.snmp;

/**
 * This class is thrown by the SNMP classes when an encoding exception occurs at
 * the SNMP level and not via the AsnEncoder class.
 * 
 * @see org.opennms.protocols.snmp.asn1.AsnEncoder
 */
public class SnmpPduEncodingException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 4251105118752643982L;

    /**
     * The default exception constructor
     * 
     */
    public SnmpPduEncodingException() {
        super();
    }

    /**
     * The exception constructor
     * 
     * @param why
     *            The reason the exception is being raised
     * 
     */
    public SnmpPduEncodingException(String why) {
        super(why);
    }
}
