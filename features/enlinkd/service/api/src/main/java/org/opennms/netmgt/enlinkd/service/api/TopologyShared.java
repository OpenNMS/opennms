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
package org.opennms.netmgt.enlinkd.service.api;

import java.util.List;

public class TopologyShared implements Topology {

    public TopologyShared(List<BridgePort> left, List<MacPort> right,BridgePort top) {
        this.designated = top;
        this.left = left;
        this.right = right;
    }

    private MacCloud cloud;
    private final BridgePort designated;
    private final List<BridgePort> left;
    private final List<MacPort> right;

    public List<BridgePort> getBridgePorts() {
        return left;
    }

    public List<MacPort> getMacPorts() {
        return right;
    }

    public BridgePort getUpPort() {
        return designated;
    }

    public MacCloud getCloud() {
        return cloud;
    }

    public void setCloud(MacCloud cloud) {
        this.cloud = cloud;
    }

    @Override
    public String printTopology() {
        final StringBuilder strbfr = new StringBuilder();
        strbfr.append("shared -> designated bridge:[");
        strbfr.append(designated.printTopology());
        strbfr.append("]\n");
        for (BridgePort blink:  left) {
            strbfr.append("        -> port:");            
            if (blink == null) {
                strbfr.append("[null]");
            } else {
                strbfr.append(blink.printTopology());
            }
            strbfr.append("\n");
        }
        for (MacPort port: right) {
            strbfr.append("        -> macs:");
            strbfr.append(port.printTopology());
        }
        if (cloud != null) {
            strbfr.append("        -> macs:");
            strbfr.append(cloud.printTopology());
        }
        return strbfr.toString();
    }
        
}
