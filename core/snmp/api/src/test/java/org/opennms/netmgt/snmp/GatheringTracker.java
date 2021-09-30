/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.snmp.proxy.WalkRequest;
import org.opennms.netmgt.snmp.proxy.WalkResponse;

/**
 * Simple tracker used to gather all of the results passed to {@link #storeResult(SnmpResult)}
 *
 * @author jwhite
 */
public class GatheringTracker extends CollectionTracker {

    private List<SnmpResult> m_results = new ArrayList<>(0);

    @Override
    public List<WalkRequest> getWalkRequests() {
        return new ArrayList<>(0);
    }

    @Override
    public void handleWalkResponses(List<WalkResponse> responses) {
        // pass
    }

    @Override
    public void setMaxRepetitions(int maxRepetitions) {
        // pass
    }

    @Override
    public void setMaxRetries(int maxRetries) {
        // pass
    }

    @Override
    public ResponseProcessor buildNextPdu(PduBuilder pduBuilder) {
        return null;
    }

    protected void storeResult(SnmpResult res) {
        m_results.add(res);
    }

    public List<SnmpResult> getResults() {
        return m_results;
    }
}
