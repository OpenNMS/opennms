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

package org.opennms.netmgt.notifd.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.logging.Logging;
import org.opennms.netmgt.notifd.api.NotificationConfigProvider;
import org.opennms.netmgt.notifd.api.NotificationTester;

import java.util.List;

@Command(scope = "opennms", name = "destination-path-trigger", description="Trigger test notifications to destination path.")
@Service
public class DestinationPathTrigger implements Action {
    @Argument(name = "destinationPath", description = "Destination path", required = true)
    @Completion(DestinationPathNameCompleter.class)
    String destinationPathName;

    @Option(name = "escalate", description = "Include escalations when triggering the path.")
    boolean includeEscalations = false;

    @Reference
    public NotificationConfigProvider notificationConfigProvider;

    @Reference
    public NotificationTester notificationTester;

    @Override
    public Object execute() {
        List<String> targetNames = notificationConfigProvider.getTargetNames(destinationPathName, includeEscalations);
        if (targetNames.isEmpty()) {
            System.out.printf("No path with name '%s' found.\n", destinationPathName);
            return null;
        }

        for (String targetName : targetNames) {
            triggerTarget(targetName);
        }

        System.out.println("Done triggering targets and commands. See notifd.log for details.");
        return null;
    }

    private void triggerTarget(String targetName) {
        for (String command : notificationConfigProvider.getCommands(destinationPathName, targetName, includeEscalations)) {
            System.out.printf("Triggering command='%s' for target='%s'.\n", command, targetName);
            try(Logging.MDCCloseable ignored = Logging.withPrefixCloseable("notifd")) {
                notificationTester.triggerNotificationsForTarget(targetName, command);
            }
        }
    }

}
