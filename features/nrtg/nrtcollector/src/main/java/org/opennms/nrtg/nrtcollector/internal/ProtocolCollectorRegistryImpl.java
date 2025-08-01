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

    private List<ProtocolCollector> protocolCollectors = new ArrayList<>();

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
