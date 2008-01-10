/*
 * Modifications:
 *
 * 2007 Apr 13: Genericize List passed to send method. - dj@opennms.org
 * 2004 Sep 08: Created this file.
 *
 * Copyright (C) 2005, The OpenNMS Group, Inc..
 */
package org.opennms.netmgt.notifd;

import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.Argument;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.utils.JavaMailer;
import org.opennms.netmgt.utils.JavaMailerException;

/**
 * Implements NotificationStragey pattern used to send notifications via the
 * Java Mail API.
 * 
 * @author <A HREF="mailto:david@opennms.org">David Hustace </A>
 * 
 */
public class JavaMailNotificationStrategy implements NotificationStrategy {

    /**
     * 
     */
    public JavaMailNotificationStrategy() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opennms.netmgt.notifd.NotificationStrategy#send(java.util.List)
     */
    public int send(List<Argument> arguments) {
        log().debug("In the JavaMailNotification class.");

        try {
            JavaMailer jm = buildMessage(arguments);
            jm.mailSend();
        } catch (JavaMailerException e) {
            log().error("send: Error sending notification.", e);
            return 1;
        }
        return 0;
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    /**
     * This method extracts the to, subject, and message text from the
     * parameters passed in the notification.
     * 
     * @param arguments
     * @throws JavaMailerException 
     */
    private JavaMailer buildMessage(List<Argument> arguments) throws JavaMailerException {

        JavaMailer jm = new JavaMailer();

        for (int i = 0; i < arguments.size(); i++) {

            Argument arg = arguments.get(i);
            log().debug("Current arg switch: " + i + " of " + arguments.size() + " is: " + arg.getSwitch());
            log().debug("Current arg  value: " + i + " of " + arguments.size() + " is: " + arg.getValue());

            /*
             * Note: The recipient gets set by whichever of the two switches:
             * (PARAM_EMAIL or PARAM_PAGER_EMAIL) are specified last in the
             * notificationCommands.xml file
             */
            if (NotificationManager.PARAM_EMAIL.equals(arg.getSwitch())) {
                log().debug("Found: PARAM_EMAIL");
                jm.setTo(arg.getValue());
            } else if (NotificationManager.PARAM_PAGER_EMAIL.equals(arg.getSwitch())) {
                log().debug("Found: PARAM_PAGER_EMAIL");
                jm.setTo(arg.getValue());
            } else if (NotificationManager.PARAM_SUBJECT.equals(arg.getSwitch())) {
                log().debug("Found: PARAM_SUBJECT");
                jm.setSubject(arg.getValue());
            } else if (NotificationManager.PARAM_TEXT_MSG.equals(arg.getSwitch())) {
                log().debug("Found: PARAM_TEXT_MSG");
                jm.setMessageText(arg.getValue());
            }
        }

        return jm;
    }

}
