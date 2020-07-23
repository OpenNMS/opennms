/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
