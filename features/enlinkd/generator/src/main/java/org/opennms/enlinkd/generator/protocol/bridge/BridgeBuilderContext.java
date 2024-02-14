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
package org.opennms.enlinkd.generator.protocol.bridge;

import java.net.InetAddress;

import org.opennms.enlinkd.generator.TopologyPersister;
import org.opennms.enlinkd.generator.util.InetAddressGenerator;
import org.opennms.enlinkd.generator.util.MacAddressGenerator;

public class BridgeBuilderContext {

    private final TopologyPersister topologyPersister;
    private final MacAddressGenerator macGenerator;
    private final InetAddressGenerator inetGenerator;

    public BridgeBuilderContext(TopologyPersister topologyPersister, MacAddressGenerator macGenerator, InetAddressGenerator inetGenerator) {
        this.topologyPersister = topologyPersister;
        this.macGenerator = macGenerator;
        this.inetGenerator = inetGenerator;
    }

    public String getNextMacAddress(){
        return macGenerator.next();
    }

    public InetAddress getNextInetAddress(){
        return inetGenerator.next();
    }

    public TopologyPersister getTopologyPersister(){
        return topologyPersister;
    }
}
