/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.notifd.asterisk;

import java.util.List;

import org.opennms.core.utils.Argument;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.asterisk.agi.scripts.BaseOnmsAgiScript;
import org.opennms.netmgt.asterisk.utils.AsteriskOriginator;
import org.opennms.netmgt.asterisk.utils.AsteriskOriginatorException;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.model.notifd.NotificationStrategy;
import org.opennms.netmgt.notifd.Notifd;

/**
 * <p>AsteriskOriginateNotificationStrategy class.</p>
 *
 * @author Jeff Gehlbach <A HREF="mailto:jeffg@opennms.org">jeffg</A>
 * @version $Id: $
 */
public class AsteriskOriginateNotificationStrategy implements NotificationStrategy {

    /**
     * <p>Constructor for AsteriskOriginateNotificationStrategy.</p>
     */
    public AsteriskOriginateNotificationStrategy() {
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.notifd.NotificationStrategy#send(java.util.List)
     */
    /** {@inheritDoc} */
    public int send(List<Argument> arguments) {
        if (log().isDebugEnabled()) {
            log().debug("In the " + getClass() + " class");
        }
        
        try {
            AsteriskOriginator originator = buildOriginator(arguments);
            originator.originateCall();
        } catch (AsteriskOriginatorException aoe) {
            log().error("Error originating call for notification.", aoe);
            return 1;
        }
        return 0;
    }
    
    private AsteriskOriginator buildOriginator(List<Argument> arguments) throws AsteriskOriginatorException {
        AsteriskOriginator ao = new AsteriskOriginator();
        for (Argument arg : arguments) {
            if (NotificationManager.PARAM_WORK_PHONE.equals(arg.getSwitch())) {
                log().debug("Found: PARAM_WORK_PHONE => " + arg.getValue());
                ao.setLegAExtension(arg.getValue());
            } else if (NotificationManager.PARAM_HOME_PHONE.equals(arg.getSwitch())) {
                log().debug("Found: PARAM_HOME_PHONE => " + arg.getValue());
                ao.setLegAExtension(arg.getValue());
            } else if (NotificationManager.PARAM_MOBILE_PHONE.equals(arg.getSwitch())) {
                log().debug("Found: PARAM_MOBILE_PHONE => " + arg.getValue());
                ao.setLegAExtension(arg.getValue());
            } else if (NotificationManager.PARAM_SUBJECT.equals(arg.getSwitch())) {
                log().debug("Found: PARAM_SUBJECT => " + arg.getValue());
                ao.setSubject(arg.getValue());
                ao.setChannelVariable(BaseOnmsAgiScript.VAR_OPENNMS_NOTIFY_SUBJECT, arg.getValue());
            } else if (NotificationManager.PARAM_TEXT_MSG.equals(arg.getSwitch())) {
                log().debug("Found: PARAM_TEXT_MSG => " + arg.getValue());
                ao.setMessageText(arg.getValue());
                ao.setChannelVariable(BaseOnmsAgiScript.VAR_OPENNMS_NOTIFY_BODY, arg.getValue());
            } else if (NotificationManager.PARAM_TUI_PIN.equals(arg.getSwitch())) {
                log().debug("Found: PARAM_TUI_PIN => " + arg.getValue());
                ao.setChannelVariable(BaseOnmsAgiScript.VAR_OPENNMS_USER_PIN, arg.getValue());
            } else if (NotificationManager.PARAM_DESTINATION.equals(arg.getSwitch())) {
                log().debug("Found: PARAM_DESTINATION => " + arg.getValue());
                ao.setChannelVariable(BaseOnmsAgiScript.VAR_OPENNMS_USERNAME, arg.getValue());
            } else if (NotificationManager.PARAM_NODE.equals(arg.getSwitch())) {
                log().debug("Found: PARAM_NODE => " + arg.getValue());
                ao.setChannelVariable(BaseOnmsAgiScript.VAR_OPENNMS_NODEID, arg.getValue());
                try {
                    ao.setChannelVariable(BaseOnmsAgiScript.VAR_OPENNMS_NODELABEL, Notifd.getInstance().getNodeDao().get(arg.getValue()).getLabel());
                } catch (Throwable e) {
                    ao.setChannelVariable(BaseOnmsAgiScript.VAR_OPENNMS_NODELABEL, null);
                }
            } else if (NotificationManager.PARAM_INTERFACE.equals(arg.getSwitch())) {
                log().debug("Found: PARAM_INTERFACE => " + arg.getValue());
                ao.setChannelVariable(BaseOnmsAgiScript.VAR_OPENNMS_INTERFACE, arg.getValue());
            } else if (NotificationManager.PARAM_SERVICE.equals(arg.getSwitch())) {
                log().debug("Found: PARAM_SERVICE => " + arg.getValue());
                ao.setChannelVariable(BaseOnmsAgiScript.VAR_OPENNMS_SERVICE, arg.getValue());
            } else {
                log().debug("Unconsumed arg: " + String.valueOf(arg.getSwitch()) + " => " + String.valueOf(arg.getValue()));
            }
        }
        return ao;
    }

    /**
     * <p>log</p>
     *
     * @return a {@link org.opennms.core.utils.ThreadCategory} object.
     */
    protected ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

}
