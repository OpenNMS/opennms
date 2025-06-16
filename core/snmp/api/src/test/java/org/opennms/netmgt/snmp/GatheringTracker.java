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
