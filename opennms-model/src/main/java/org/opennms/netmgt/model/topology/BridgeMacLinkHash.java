/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.topology;

import org.opennms.netmgt.model.BridgeMacLink;

public class BridgeMacLinkHash {
    final Integer nodeid;
    final Integer bridgeport;
    final String mac;
    public BridgeMacLinkHash(BridgeMacLink maclink) {
        super();
        nodeid = maclink.getNode().getId();
        bridgeport = maclink.getBridgePort();
        mac = maclink.getMacAddress();
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((bridgeport == null) ? 0 : bridgeport.hashCode());
        result = prime * result + ((mac == null) ? 0 : mac.hashCode());
        result = prime * result + ((nodeid == null) ? 0 : nodeid.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BridgeMacLinkHash other = (BridgeMacLinkHash) obj;
        if (bridgeport == null) {
            if (other.bridgeport != null)
                return false;
        } else if (!bridgeport.equals(other.bridgeport))
            return false;
        if (mac == null) {
            if (other.mac != null)
                return false;
        } else if (!mac.equals(other.mac))
            return false;
        if (nodeid == null) {
            if (other.nodeid != null)
                return false;
        } else if (!nodeid.equals(other.nodeid))
            return false;
        return true;
    }
    

}
