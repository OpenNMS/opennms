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
package org.opennms.netmgt.filters.shell;


import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LocationUtils;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceRef;
import org.opennms.netmgt.dao.api.ServiceTracker;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsNode;

@Command(scope = "opennms", name = "track-service", description = "Track services that match a given filter")
@Service
public class TrackServiceCommand implements Action {

    @Reference
    private NodeDao nodeDao;

    @Reference
    private SessionUtils sessionUtils;

    @Reference
    private ServiceTracker serviceTracker;

    @Argument(index = 0, description = "Service name", required = true)
    private String serviceName;

    @Argument(index = 1, description = "Filter Rule", required = false)
    private String filterRule;

    @Override
    public Object execute() throws IOException {
        System.out.printf("Tracking service named %s%s. Press CTRL+C to stop.\n", serviceName,
                filterRule != null ? String.format(" with filter rule \"%s\"", filterRule) : "");

        // Callback may occur from other threads, but we can't use those to print the results
        // since output isn't redirected to the console. To work around this we push messages to a queue
        // and pull/print these from the command execution thread
        BlockingQueue<String> messageQueue = new LinkedBlockingDeque<>();
        Closeable session = serviceTracker.trackServiceMatchingFilterRule(serviceName, filterRule, new ServiceTracker.ServiceListener() {
            @Override
            public void onServiceMatched(ServiceRef serviceRef) {
                messageQueue.add(String.format("FOUND: %s\n", getDescription(serviceRef)));
            }

            @Override
            public void onServiceStoppedMatching(ServiceRef serviceRef) {
                messageQueue.add(String.format("REMOVED: %s\n", getDescription(serviceRef)));
            }
        });

        while (true) {
            try {
                System.out.print(messageQueue.take());
            } catch (InterruptedException e) {
                break;
            }
        }

        session.close();
        return null;
    }

    private String getDescription(ServiceRef serviceRef) {
        return sessionUtils.withReadOnlyTransaction(() -> {
            String nodeLabel = "<unknown label>";
            String nodeLocation = "<unknown location>";
            OnmsNode node = nodeDao.get(serviceRef.getNodeId());
            if (node != null) {
                nodeLabel = node.getLabel();
                if (node.getLocation() != null) {
                    nodeLocation = node.getLocation().getLocationName();
                } else {
                    nodeLocation = LocationUtils.DEFAULT_LOCATION_NAME;
                }
            }
            return String.format("%s on interface %s on node %s (id=%d) at location %s.",
                    serviceRef.getServiceName(), InetAddressUtils.str(serviceRef.getIpAddress()),
                    nodeLabel, serviceRef.getNodeId(), nodeLocation);
        });
    }
}
