/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
