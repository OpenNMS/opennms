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
            return List.of(user);
        }

        if (target.indexOf('@') >= 0) {
            // Create a user with the given e-mail
            user = new User();
            user.setUserId(target);
            Contact contact = new Contact();
            contact.setType("email");
            contact.setInfo(target);
            user.addContact(contact);
            return List.of(user);
        }

        // Nothing found :(
        return Collections.emptyList();
    }

}
