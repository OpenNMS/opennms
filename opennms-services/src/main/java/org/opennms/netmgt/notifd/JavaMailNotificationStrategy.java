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

import java.util.List;

import org.opennms.javamail.JavaMailer;
import org.opennms.javamail.JavaMailerException;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.model.notifd.Argument;
import org.opennms.netmgt.model.notifd.NotificationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements NotificationStragey pattern used to send notifications via the
 * Java Mail API.
 *
 * @author <A HREF="mailto:david@opennms.org">David Hustace </A>
 * @version $Id: $
 */
public class JavaMailNotificationStrategy implements NotificationStrategy {
    
    
    private static final Logger LOG = LoggerFactory.getLogger(JavaMailNotificationStrategy.class);

    /**
     * <p>Constructor for JavaMailNotificationStrategy.</p>
     */
    public JavaMailNotificationStrategy() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opennms.netmgt.notifd.NotificationStrategy#send(java.util.List)
     */
    /** {@inheritDoc} */
    @Override
    public int send(List<Argument> arguments) {
        LOG.debug("In the JavaMailNotification class.");

        try {
            JavaMailer jm = buildMessage(arguments);
            jm.mailSend();
        } catch (JavaMailerException e) {
            LOG.error("send: Error sending notification.", e);
            return 1;
        }
        return 0;
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
            LOG.debug("Current arg switch: {} of {} is: {}", i, arguments.size(), arg.getSwitch());
            LOG.debug("Current arg  value: {} of {} is: {}", i, arguments.size(), arg.getValue());

            /*
             * Note: The recipient gets set by whichever of the two switches:
             * (PARAM_EMAIL or PARAM_PAGER_EMAIL) are specified last in the
             * notificationCommands.xml file
             * 
             * And the message body will get set to whichever is set last
             * (PARAM_NUM_MSG or PARAM_TEXT_MSG)
             */
            if (NotificationManager.PARAM_EMAIL.equals(arg.getSwitch())) {
                LOG.debug("Found: PARAM_EMAIL");
                jm.setTo(arg.getValue());
            } else if (NotificationManager.PARAM_PAGER_EMAIL.equals(arg.getSwitch())) {
                LOG.debug("Found: PARAM_PAGER_EMAIL");
                jm.setTo(arg.getValue());
            } else if (NotificationManager.PARAM_SUBJECT.equals(arg.getSwitch())) {
                LOG.debug("Found: PARAM_SUBJECT");
                jm.setSubject(arg.getValue());
            } else if (NotificationManager.PARAM_NUM_MSG.equals(arg.getSwitch())) {
                LOG.debug("Found: PARAM_NUM_MSG");
                jm.setMessageText(arg.getValue());
            } else if (NotificationManager.PARAM_TEXT_MSG.equals(arg.getSwitch())) {
                LOG.debug("Found: PARAM_TEXT_MSG");
                jm.setMessageText(arg.getValue());
            }
        }

        return jm;
    }

}
