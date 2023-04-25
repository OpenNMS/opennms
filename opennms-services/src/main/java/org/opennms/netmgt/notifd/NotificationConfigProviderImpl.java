/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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
