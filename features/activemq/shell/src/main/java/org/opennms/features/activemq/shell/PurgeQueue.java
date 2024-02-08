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
package org.opennms.features.activemq.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.features.activemq.broker.api.ManagedBroker;
import org.opennms.features.activemq.broker.api.ManagedDestination;

@Command(scope = "opennms", name = "activemq-purge-queue", description = "Purge the content of an ActiveMQ queue.")
@Service
public class PurgeQueue implements Action {

    @Reference(optional = true)
    private ManagedBroker broker;

    @Argument(name = "queue name", required = true)
    @Completion(QueueNameCompleter.class)
    private String queueName;

    @Override
    public Object execute() throws Exception {
        if (broker == null) {
            System.out.println("(No broker available.)");
            return null;
        }

        final ManagedDestination queue = broker.getDestinations().stream()
                .filter(ManagedDestination::isQueue)
                .filter(q -> queueName.trim().equalsIgnoreCase(q.getName()))
                .findFirst()
                .orElse(null);

        if (queue == null) {
            System.out.printf("No queue named '%s' found.\n", queueName);
        } else {
            System.out.println("Purging: " + queue.getName());
            queue.purge();
        }
        return null;
    }
}
