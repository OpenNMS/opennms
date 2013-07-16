/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.nrtg.nrtcollector.internal;

import org.opennms.nrtg.api.ProtocolCollector;

import java.util.ArrayList;
import java.util.List;

/**
 * OSGI Implementation
 *
 * @author Simon Walter
 */
public class ProtocolCollectorRegistryImpl implements ProtocolCollectorRegistry {

    private List<ProtocolCollector> protocolCollectors = new ArrayList<ProtocolCollector>();

    @Override
    public ProtocolCollector getProtocolCollector(String protocol) {
        for (ProtocolCollector pc : protocolCollectors) {
            if (pc.getProtcol().equals(protocol))
                return pc;
        }
        throw new RuntimeException("Unknown protocol! " + protocol);
    }

    public void setProtocolCollectors(List<ProtocolCollector> protocolCollectors) {
        this.protocolCollectors = protocolCollectors;
    }

    public List<ProtocolCollector> getProtocolCollectors() {
        return protocolCollectors;
    }

}
