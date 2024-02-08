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
package org.opennms.netmgt.alarmd.northbounder.snmptrap;

/**
 * The Class SnmpTrapHelperException.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class SnmpTrapException extends Exception {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1817203327929032238L;

    /**
     * Instantiates a new SNMP trap helper exception.
     */
    public SnmpTrapException() {
        super();
    }

    /**
     * Instantiates a new SNMP trap helper exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public SnmpTrapException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new SNMP trap helper exception.
     *
     * @param message the message
     */
    public SnmpTrapException(String message) {
        super(message);
    }

    /**
     * Instantiates a new SNMP trap helper exception.
     *
     * @param cause the cause
     */
    public SnmpTrapException(Throwable cause) {
        super(cause);
    }

}
