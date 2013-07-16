/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.notifd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.notificationCommands.Argument;
import org.opennms.netmgt.config.notificationCommands.Command;
import org.opennms.netmgt.config.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class holds all the data and logic for sending out a notification Each
 * notification that is sent will be accompanied by a row in the notifications
 * table. All notifications in a group will be identified with a common groupId
 * number.
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 *
 * Modification to pick an ExecuteStrategy based on the "binary" flag in
 * notificationCommands.xml by:
 * @author <A HREF="mailto:david@opennms.org">David Hustace </A>
 */
public class NotificationTask extends Thread {
    
    private static final Logger LOG = LoggerFactory.getLogger(NotificationTask.class);
    
    /**
     * The User object the notification needs to go out to
     */
    private volatile User m_user;

    /**The autoNotify info for the usersnotified table
     */
    private volatile String m_autoNotify;

    /**
     * The row id that will be used for the row inserted into the notifications
     * table
     */
    private volatile int m_notifyId;

    /**
     * The console command that will be issued to send the actual notification.
     */
    private volatile Command[] m_commands;

    /**
     */
    private final Map<String, String> m_params;

    /**
     */
    private final long m_sendTime;

    private volatile boolean m_started = false;

    private final NotificationManager m_notificationManager;

    private final UserManager m_userManager;

    /**
     * Constructor, initializes some information
     *
     * @param someParams the parameters from
     * Notify
     * @param notificationManager a {@link org.opennms.netmgt.config.NotificationManager} object.
     * @param userManager a {@link org.opennms.netmgt.config.UserManager} object.
     * @param sendTime a long.
     * @param siblings a {@link java.util.List} object.
     * @param autoNotify a {@link java.lang.String} object.
     */
    public NotificationTask(NotificationManager notificationManager, UserManager userManager, long sendTime, Map<String, String> someParams, List<NotificationTask> siblings, String autoNotify) {
        m_notificationManager = notificationManager;
        m_userManager = userManager;
        m_sendTime = sendTime;
        m_params = new HashMap<String, String>(someParams);
        m_autoNotify = autoNotify;

    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer("Send ");

        if (m_commands == null) {
            buffer.append("Null Commands");
        } else {
            for (Command command : m_commands) {
                buffer.append((command == null ? "null" : command.getName()));
                buffer.append("/");
                buffer.append("[#" + m_notifyId + "]");
            }
        }
        buffer.append(" to " + m_user.getUserId() + " at " + new Date(m_sendTime));

        return buffer.toString();
    }

    /**
     * <p>getSendTime</p>
     *
     * @return a long.
     */
    public long getSendTime() {
        return m_sendTime;
    }

    /**
     * Returns the unique id used to insert the row in the database for this
     * notification task.
     *
     * @return int, the id of the row in notifications table
     */
    public int getNotifyId() {
        return m_notifyId;
    }

    /**
     * Sets the user that the page needs to be sent to.
     *
     * @param aUser
     *            the user info
     */
    public void setUser(User aUser) {
        m_user = aUser;
    }
    
    /**
     * <p>getUser</p>
     *
     * @return a {@link org.opennms.netmgt.config.users.User} object.
     */
    public User getUser() {
        return m_user;
    }

    /**
     *Sets the autoNotify info for the usersnotified table
     *
     * @param autoNotify a {@link java.lang.String} object.
     */
    public void setAutoNotify(String autoNotify) {
        m_autoNotify = autoNotify;
    } 

    /**
     * Sets the group id that will be inserted into the row in notifications
     * table
     *
     * @param anId
     *            the group id to set for the row
     */
    public void setNoticeId(int anId) {
        m_notifyId = anId;
    }
    

    /**
     * This method will construct the command that will be issued to send the
     * actual page.
     *
     * @param commands
     *            the commands to call at the console.
     */
    public void setCommands(Command[] commands) {
        m_commands = commands;
    }
    
    /**
     * <p>getCommands</p>
     *
     * @return an array of {@link org.opennms.netmgt.config.notificationCommands.Command} objects.
     */
    public Command[] getCommands() {
        return m_commands.clone();
    }

    /**
     * <p>run</p>
     */
    @Override
    public void run() {
        boolean outstanding = false;
        try {
            outstanding = getNotificationManager().noticeOutstanding(m_notifyId);
        } catch (Throwable e) {
            LOG.error("Unable to get response status on notice #{}", m_notifyId, e);
        }

        // check to see if someone has responded, if so remove all the brothers
        if (outstanding) {
            try {
                if (getUserManager().isUserOnDuty(m_user.getUserId(), Calendar.getInstance())) {

                    // send the notice
                    ExecutorStrategy strategy = null;
                    String cntct = "";

                    for (Command command : m_commands) {
                        try {
                            cntct = getContactInfo(command.getName());
                            try {
                                getNotificationManager().updateNoticeWithUserInfo(m_user.getUserId(), m_notifyId, command.getName(), cntct, m_autoNotify);
                            } catch (Throwable e) {
                                LOG.error("Could not insert notice info into database, aborting send notice", e);
                                continue;
                            }
                            String binaryCommand = command.getBinary();
                            if (binaryCommand == null) {
                                LOG.error("binary flag not set for command: {}.  Guessing false.", command.getExecute());
                                binaryCommand = "false";
                            }
                            if (binaryCommand.equals("true")) {
                                strategy = new CommandExecutor();
                            } else {
                                strategy = new ClassExecutor();
                            }
                            LOG.debug("Class created is: {}", command.getClass());

                            int returnCode = strategy.execute(command.getExecute(), getArgumentList(command));
                            LOG.debug("command {} return code = {}", command.getName(), returnCode);
                        } catch (Throwable e) {
                            LOG.warn("Notification command failed: {}", command.getName(), e);
                        }
                    }
                } else {
                    LOG.debug("User {} is not on duty, skipping", m_user.getUserId());
                }
            } catch (IOException e) {
                LOG.warn("Could not get user duty schedule information: ", e);
            } catch (MarshalException e) {
                LOG.warn("Could not get user duty schedule information: ", e);
            } catch (ValidationException e) {
                LOG.warn("Could not get user duty schedule information: ", e);
            }
        } else {
            // remove all the related notices that have yet to be sent
            //for (int i = 0; i < m_siblings.size(); i++) {
            //    NotificationTask task = (NotificationTask) m_siblings.get(i);

            // FIXME: Reported on discuss list and not found to ever
            // be initialized anywhere.
            // m_notifTree.remove(task);
            //}
        }
    }

    private NotificationManager getNotificationManager() {
        return m_notificationManager;
    }

    private UserManager getUserManager() {
        return m_userManager;
    }

    private String getContactInfo(String cmdName) throws IOException, MarshalException, ValidationException {
        return getUserManager().getContactInfo(m_user, cmdName);
    }

    /**
     */
    private List<org.opennms.core.utils.Argument> getArgumentList(Command command) {
        Collection<Argument> notifArgs = getArgumentsForCommand(command);
        List<org.opennms.core.utils.Argument> commandArgs = new ArrayList<org.opennms.core.utils.Argument>();

        for (Argument curArg : notifArgs) {
            LOG.debug("argument: {} {} '{}' {}", curArg.getSwitch(), curArg.getSubstitution(), getArgumentValue(curArg.getSwitch()), Boolean.valueOf(curArg.getStreamed()).booleanValue());

            commandArgs.add(new org.opennms.core.utils.Argument(curArg.getSwitch(), curArg.getSubstitution(), getArgumentValue(curArg.getSwitch()), Boolean.valueOf(curArg.getStreamed()).booleanValue()));
        }

        return commandArgs;
    }

    private List<Argument> getArgumentsForCommand(Command command) {
        return command.getArgumentCollection();
    }

    /**
     * 
     */
    private String getArgumentValue(String aSwitch) {
        String value = "";

        try {
            if (NotificationManager.PARAM_DESTINATION.equals(aSwitch)) {
                value = m_user.getUserId();
            } else if (NotificationManager.PARAM_EMAIL.equals(aSwitch)) {
                value = getEmail();
            } else if (NotificationManager.PARAM_TUI_PIN.equals(aSwitch)) {
                value = getTuiPin();
            } else if (NotificationManager.PARAM_PAGER_EMAIL.equals(aSwitch)) {
                value = getUserManager().getPagerEmail(m_user.getUserId());
            } else if (NotificationManager.PARAM_XMPP_ADDRESS.equals(aSwitch)) {
            	value = getUserManager().getXMPPAddress(m_user.getUserId());
            } else if (NotificationManager.PARAM_TEXT_PAGER_PIN.equals(aSwitch)) {
                value = getUserManager().getTextPin(m_user.getUserId());
            } else if (NotificationManager.PARAM_NUM_PAGER_PIN.equals(aSwitch)) {
                value = getUserManager().getNumericPin(m_user.getUserId());
            } else if (NotificationManager.PARAM_WORK_PHONE.equals(aSwitch)) {
                value = getUserManager().getWorkPhone(m_user.getUserId());
            } else if (NotificationManager.PARAM_MOBILE_PHONE.equals(aSwitch)) {
                value = getUserManager().getMobilePhone(m_user.getUserId());
            } else if (NotificationManager.PARAM_HOME_PHONE.equals(aSwitch)) {
                value = getUserManager().getHomePhone(m_user.getUserId());
            } else if (NotificationManager.PARAM_MICROBLOG_USERNAME.equals(aSwitch)) {
                value = getUserManager().getMicroblogName(m_user.getUserId());
            } else if (m_params.containsKey(aSwitch)) {
                value = m_params.get(aSwitch);
            }
        } catch (Throwable e) {
            LOG.error("unable to get value for parameter {}", aSwitch);
        }

        return value;
    }

    /**
     * <p>getEmail</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public String getEmail() throws IOException, MarshalException, ValidationException {
        return getContactInfo("email");
    }
    
    /**
     * <p>getTuiPin</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public String getTuiPin() throws MarshalException, ValidationException, IOException {
        return getContactInfo("tuiPin");
    }

    /**
     * <p>start</p>
     */
    @Override
    public synchronized void start() {
        m_started = true;
        super.start();
    }

    /**
     * <p>isStarted</p>
     *
     * @return a boolean.
     */
    public boolean isStarted() {
        return m_started;
    }

}
