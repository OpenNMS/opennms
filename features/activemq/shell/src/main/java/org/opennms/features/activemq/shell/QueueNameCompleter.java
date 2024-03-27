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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.CommandLine;
import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.completers.StringsCompleter;
import org.opennms.features.activemq.broker.api.ManagedBroker;
import org.opennms.features.activemq.broker.api.ManagedDestination;

@Service
public class QueueNameCompleter implements Completer {

    @Reference(optional = true)
    private ManagedBroker broker;

    @Override
    public int complete(Session session, CommandLine commandLine, List<String> candidates) {
        StringsCompleter delegate = new StringsCompleter();
        // Gather the list of known queue name
        delegate.getStrings().addAll(getQueueNames());
        return delegate.complete(session, commandLine, candidates);
    }

    private Set<String> getQueueNames() {
        if (broker == null) {
            return Collections.emptySet();
        }
        return broker.getDestinations().stream()
                .filter(ManagedDestination::isQueue)
                .map(ManagedDestination::getName)
                .collect(Collectors.toSet());
    }
}
