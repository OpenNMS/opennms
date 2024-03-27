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
 * Defines a runtime exception when the program attempts to send a SnmpPduPacket
 * and there is no default handler defined. This is considered a runtime
 * exception since if there isn't a handler registered yet, is there likely to
 * be one after the exception?
 * 
 */
public class SnmpHandlerNotDefinedException extends RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = -5889651086542092511L;

    /**
     * The exception constructor
     * 
     * @param why
     *            The reason the exception is being raised
     * 
     */
    public SnmpHandlerNotDefinedException(String why) {
        super(why);
    }

    /**
     * Default exception constructor
     * 
     */
    public SnmpHandlerNotDefinedException() {
        super();
    }
}
