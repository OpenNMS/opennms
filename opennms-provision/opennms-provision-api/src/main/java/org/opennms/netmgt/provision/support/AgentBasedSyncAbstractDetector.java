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
package org.opennms.netmgt.provision.support;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.provision.DetectRequest;
import org.opennms.netmgt.provision.DetectResults;
import org.opennms.netmgt.provision.SyncServiceDetector;

public abstract class AgentBasedSyncAbstractDetector<T> extends AbstractDetector implements SyncServiceDetector {

    public static final String HAS_MULTIPLE_AGENT_CONFIGS = "hasMultipleAgentConfigs";

    public AgentBasedSyncAbstractDetector(String serviceName, int port, int defaultTimeout, int defaultRetries) {
        super(serviceName, port, defaultTimeout, defaultRetries);
    }

    protected AgentBasedSyncAbstractDetector(String serviceName, int port) {
        super(serviceName, port);
    }

    @Override
    public DetectResults detect(DetectRequest request) {
        Map<String, String> runTimeAttributes = request.getRuntimeAttributes();

        if (hasMultipleAgentConfigs(runTimeAttributes)) {
            return new DetectResultsImpl(isServiceDetected(request.getAddress(), getListOfAgentConfigs(request)));
        }
        return new DetectResultsImpl(isServiceDetected(request.getAddress(), getAgentConfig(request)));
    }

    public abstract T getAgentConfig(DetectRequest request);

    public abstract boolean isServiceDetected(final InetAddress address, final T agentConfig);

    /**
     * Override this if detector can support multiple agent configs.
     */
    public List<T> getListOfAgentConfigs(DetectRequest request) {
        List<T> agentConfigList = new ArrayList<T>();
        agentConfigList.add(getAgentConfig(request));
        return agentConfigList;
    }

    public boolean isServiceDetected(final InetAddress address, final List<T> agentConfigList) {
        return agentConfigList.stream().anyMatch(agentConfig -> {
            return isServiceDetected(address, agentConfig);
        });
    }

    protected static boolean hasMultipleAgentConfigs(Map<String, String> runTimeAttributes) {
        return runTimeAttributes != null &&
                runTimeAttributes.get(HAS_MULTIPLE_AGENT_CONFIGS) != null &&
                runTimeAttributes.get(HAS_MULTIPLE_AGENT_CONFIGS).equals(Boolean.toString(true));
    }


}
