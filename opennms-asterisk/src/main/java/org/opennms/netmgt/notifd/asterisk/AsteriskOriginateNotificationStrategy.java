/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.notifd.asterisk;

import java.util.List;

import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.asterisk.agi.scripts.BaseOnmsAgiScript;
import org.opennms.netmgt.asterisk.utils.AsteriskOriginator;
import org.opennms.netmgt.asterisk.utils.AsteriskOriginatorException;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.notifd.Argument;
import org.opennms.netmgt.model.notifd.NotificationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>AsteriskOriginateNotificationStrategy class.</p>
 *
 * @author Jeff Gehlbach <A HREF="mailto:jeffg@opennms.org">jeffg</A>
 * @version $Id: $
 */
public class AsteriskOriginateNotificationStrategy implements NotificationStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(AsteriskOriginateNotificationStrategy.class);

    /**
     * <p>Constructor for AsteriskOriginateNotificationStrategy.</p>
     */
    public AsteriskOriginateNotificationStrategy() {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.notifd.NotificationStrategy#send(java.util.List)
     */
    /** {@inheritDoc} */
    @Override
    public final int send(final List<Argument> arguments) {
        LOG.debug("In the {} class", getClass());

        try {
            buildOriginator(arguments).originateCall();
        } catch (final AsteriskOriginatorException aoe) {
            LOG.error("Error originating call for notification.", aoe);
            return 1;
        }
        return 0;
    }

    private AsteriskOriginator buildOriginator(final List<Argument> arguments) throws AsteriskOriginatorException {
        final AsteriskOriginator ao = new AsteriskOriginator();
        for (final Argument arg : arguments) {
            final String argSwitch = arg.getSwitch();
            final String argValue = arg.getValue();

            if (NotificationManager.PARAM_WORK_PHONE.equals(argSwitch)) {
                LOG.debug("Found: PARAM_WORK_PHONE => {}", argValue);
                ao.setLegAExtension(argValue);
            } else if (NotificationManager.PARAM_HOME_PHONE.equals(argSwitch)) {
                LOG.debug("Found: PARAM_HOME_PHONE => {}", argValue);
                ao.setLegAExtension(argValue);
            } else if (NotificationManager.PARAM_MOBILE_PHONE.equals(argSwitch)) {
                LOG.debug("Found: PARAM_MOBILE_PHONE => {}", argValue);
                ao.setLegAExtension(argValue);
            } else if (NotificationManager.PARAM_SUBJECT.equals(argSwitch)) {
                LOG.debug("Found: PARAM_SUBJECT => {}", argValue);
                ao.setSubject(argValue);
                ao.setChannelVariable(BaseOnmsAgiScript.VAR_OPENNMS_NOTIFY_SUBJECT, argValue);
            } else if (NotificationManager.PARAM_TEXT_MSG.equals(argSwitch)) {
                LOG.debug("Found: PARAM_TEXT_MSG => {}", argValue);
                ao.setMessageText(argValue);
                ao.setChannelVariable(BaseOnmsAgiScript.VAR_OPENNMS_NOTIFY_BODY, argValue);
            } else if (NotificationManager.PARAM_TUI_PIN.equals(argSwitch)) {
                LOG.debug("Found: PARAM_TUI_PIN => {}", argValue);
                ao.setChannelVariable(BaseOnmsAgiScript.VAR_OPENNMS_USER_PIN, argValue);
            } else if (NotificationManager.PARAM_DESTINATION.equals(argSwitch)) {
                LOG.debug("Found: PARAM_DESTINATION => {}", argValue);
                ao.setChannelVariable(BaseOnmsAgiScript.VAR_OPENNMS_USERNAME, argValue);
            } else if (NotificationManager.PARAM_NODE.equals(argSwitch)) {
                LOG.debug("Found: PARAM_NODE => {}", argValue);
                ao.setChannelVariable(BaseOnmsAgiScript.VAR_OPENNMS_NODEID, argValue);
                try {
                    final NodeDao nodeDao = BeanUtils.getBean("notifdContext", "nodeDao", NodeDao.class);
                    ao.setChannelVariable(BaseOnmsAgiScript.VAR_OPENNMS_NODELABEL, nodeDao.get(argValue).getLabel());
                } catch (final Throwable t) {
                    ao.setChannelVariable(BaseOnmsAgiScript.VAR_OPENNMS_NODELABEL, null);
                }
            } else if (NotificationManager.PARAM_INTERFACE.equals(argSwitch)) {
                LOG.debug("Found: PARAM_INTERFACE => {}", argValue);
                ao.setChannelVariable(BaseOnmsAgiScript.VAR_OPENNMS_INTERFACE, argValue);
            } else if (NotificationManager.PARAM_SERVICE.equals(argSwitch)) {
                LOG.debug("Found: PARAM_SERVICE => {}", argValue);
                ao.setChannelVariable(BaseOnmsAgiScript.VAR_OPENNMS_SERVICE, argValue);
            } else {
                LOG.debug("Unconsumed arg: {} => {}", String.valueOf(argSwitch), String.valueOf(argValue));
            }
        }
        return ao;
    }
}
