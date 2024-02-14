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
package org.opennms.netmgt.trapd.jmx;

import org.opennms.netmgt.daemon.BaseOnmsMBean;

/**
 * <p>TrapdMBean interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface TrapdMBean extends BaseOnmsMBean {
    /** @return The number of traps received since Trapd was last started */
    public long getTrapsReceived();
    
    /** @return The number of SNMPv1 traps received since Trapd was last started */
    public long getV1TrapsReceived();
    
    /** @return The number of SNMPv2c traps received since Trapd was last started */
    public long getV2cTrapsReceived();
    
    /** @return The number of SNMPv3 traps received since Trapd was last started */
    public long getV3TrapsReceived();
    
    /** @return The number of traps with an unknown SNMP protocol version received since Trapd was last started */
    public long getVUnknownTrapsReceived();
    
    /** @return The number of traps discarded, at user request, since Trapd was last started */
    public long getTrapsDiscarded();
    
    /** @return The number of traps not processed due to errors since Trapd was last started */
    public long getTrapsErrored();
}
