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
