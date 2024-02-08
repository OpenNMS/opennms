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
package org.opennms.netmgt.notifd;

import org.opennms.netmgt.config.DestinationPathManager;
import org.opennms.netmgt.config.destinationPaths.Escalate;
import org.opennms.netmgt.config.destinationPaths.Path;
import org.opennms.netmgt.config.destinationPaths.Target;
import org.opennms.netmgt.notifd.api.NotificationConfigProvider;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class NotificationConfigProviderImpl implements NotificationConfigProvider {

    private final DestinationPathManager destinationPathManager;

    @Inject
    public NotificationConfigProviderImpl(DestinationPathManager destinationPathManager) {
        this.destinationPathManager = Objects.requireNonNull(destinationPathManager);
    }

    @Override
    public List<String> getDestinationPathNames() {
        try {
            return destinationPathManager.getPaths().keySet().stream().sorted().collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getTargetNames(String destinationPathName, boolean includeEscalations) {
        return getTargets(destinationPathName, includeEscalations).stream()
                .map(Target::getName)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getCommands(String destinationPathName, String targetName, boolean includeEscalations) {
        return getTargets(destinationPathName, includeEscalations).stream()
                .filter(t -> Objects.equals(targetName, t.getName()))
                .findFirst()
                .map(Target::getCommands)
                .orElse(Collections.emptyList());
    }

    private List<Target> getTargets(String destinationPathName, boolean includeEscalations) {
        try {
            Path path = destinationPathManager.getPath(destinationPathName);
            if (path == null) {
                return Collections.emptyList();
            }

            // add the direct targets
            List<Target> targets = new LinkedList<>(path.getTargets());

            // add the escalations
            if (includeEscalations) {
                for (Escalate escalation : path.getEscalates()) {
                    targets.addAll(escalation.getTargets());
                }
            }

            return targets;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
