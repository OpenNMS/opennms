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
package org.opennms.netmgt.provision;

import java.net.InetAddress;

/**
 * <p>ScanContext interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface ScanContext {

    /**
     * Return the preferred address used to talk to the agent of type type provided
     *
     * e.g.  use getAgentAddress("SNMP") to find the InetAddress for the SNMP Agent for the node being scanned.
     *
     * @param agentType the type of agent to search for
     * @return the InetAddress for the agent or null if no such agent exists
     */
    public InetAddress getAgentAddress(String agentType);
    
    /**
     * <p>updateSysObjectId</p>
     *
     * @param sysObjectId a {@link java.lang.String} object.
     */
    public void updateSysObjectId(String sysObjectId);
    /**
     * <p>updateSysName</p>
     *
     * @param sysName a {@link java.lang.String} object.
     */
    public void updateSysName(String sysName);
    /**
     * <p>updateSysDescription</p>
     *
     * @param sysDescription a {@link java.lang.String} object.
     */
    public void updateSysDescription(String sysDescription);
    /**
     * <p>updateSysLocation</p>
     *
     * @param sysLocation a {@link java.lang.String} object.
     */
    public void updateSysLocation(String sysLocation);
    /**
     * <p>updateSysContact</p>
     *
     * @param sysContact a {@link java.lang.String} object.
     */
    public void updateSysContact(String sysContact);

}
