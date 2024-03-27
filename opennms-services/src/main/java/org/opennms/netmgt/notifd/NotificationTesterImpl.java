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

import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.NotificationCommandManager;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.notificationCommands.Command;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.config.users.Contact;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.notifd.api.NotificationTester;
import org.opennms.netmgt.xml.event.Event;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

public class NotificationTesterImpl implements NotificationTester {

    private final BroadcastEventProcessor broadcastEventProcessor;
    private final NotificationManager notificationManager;
    private final NotificationCommandManager notificationCommandManager;
    private final GroupManager groupManager;
    private final UserManager userManager;
    private static final Executor rejectingExecutor = command -> {
        throw new RejectedExecutionException();
    };

    @Inject
    public NotificationTesterImpl(BroadcastEventProcessor broadcastEventProcessor, NotificationManager notificationManager,
                                  NotificationCommandManager notificationCommandManager,
                                  GroupManager groupManager, UserManager userManager) {
        this.broadcastEventProcessor = Objects.requireNonNull(broadcastEventProcessor);
        this.notificationManager = Objects.requireNonNull(notificationManager);
        this.notificationCommandManager = Objects.requireNonNull(notificationCommandManager);
        this.groupManager = Objects.requireNonNull(groupManager);
        this.userManager = Objects.requireNonNull(userManager);
    }

    @Override
    public void triggerNotificationsForTarget(String targetName, String commandName) {
        // Determine the users associated with the given target
        List<User> users;
        try {
            users = getUsersForTarget(targetName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (users.isEmpty()) {
            throw new RuntimeException(String.format("No users for target='%s' found.", targetName));
        }

        // Find the command
        Command command = notificationCommandManager.getCommand(commandName);
        if (command == null) {
            throw new RuntimeException(String.format("No command with name='%s' found.", commandName));
        }

        // Build a fake notification
        Notification notification = new Notification();
        notification.setTextMessage("THIS IS A TEST MESSAGE");
        notification.setNumericMessage("TEST NUMERIC");
        notification.setSubject("THIS IS A TEST SUBJECT");

        // Build a fake event
        Event event = new Event();
        event.setUei(EventConstants.NODE_LOST_SERVICE_EVENT_UEI);
        event.setDbid(0);

        // Generate the parameter map
        Map<String, String> parameterMap = broadcastEventProcessor.buildParameterMap(notification, event, 0);

        // Trigger the notification for each user
        for (User user : users) {
            triggerNotificationTask(user, command, parameterMap);
        }
    }

    private void triggerNotificationTask(User user, Command command, Map<String, String> parameterMap) {
        // Build the notification task
        NotificationTask task = new NotificationTask(notificationManager,
                userManager,
                System.currentTimeMillis(),
                parameterMap,
                Collections.emptyList(),
                Boolean.FALSE.toString(),
                rejectingExecutor);
        task.setUser(user);
        task.setCommands(new Command[]{command});

        // Execute it
        ExecutorStrategy strategy = NotificationTask.getExecutorStrategy(command);
        strategy.execute(command.getExecute(), task.getArgumentList(command));
    }

    public List<User> getUsersForTarget(String target) throws IOException {
        List<User> users = new LinkedList<>();

        // First, we see if there is a group with a matching name
        Group group = groupManager.getGroup(target);
        if (group != null) {
            List<String> groupUsers = group.getUsers();
            for (String groupUser : groupUsers) {
                User user = userManager.getUser(groupUser);
                if (user != null) {
                    users.add(user);
                }
            }
            return users;
        }

        // Next, we check and see if there is a "role" with a matching name
        if (userManager.hasOnCallRole(target)) {
            String[] userNames = userManager.getUsersScheduledForRole(target, new Date());
            for (String userName : userNames) {
                User user = userManager.getUser(userName);
                if (user != null) {
                    users.add(user);
                }
            }
            return users;
        }

        // Next, we check and see if there is a user
        User user = userManager.getUser(target);
        if (user != null) {
            return Collections.singletonList(user);
        }

        if (target.indexOf('@') >= 0) {
            // Create a user with the given e-mail
            user = new User();
            user.setUserId(target);
            Contact contact = new Contact();
            contact.setType("email");
            contact.setInfo(target);
            user.addContact(contact);
            return Collections.singletonList(user);
        }

        // Nothing found :(
        return Collections.emptyList();
    }

}
