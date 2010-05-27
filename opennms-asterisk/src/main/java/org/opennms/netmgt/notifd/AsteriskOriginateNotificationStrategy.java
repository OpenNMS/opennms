/**
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * 2009 Jan 16: Created file - jeffg@opennms.org
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.notifd;

import java.util.List;

import org.apache.log4j.Logger;
import org.opennms.core.utils.Argument;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.asterisk.agi.scripts.BaseOnmsAgiScript;
import org.opennms.netmgt.asterisk.utils.AsteriskOriginator;
import org.opennms.netmgt.asterisk.utils.AsteriskOriginatorException;
import org.opennms.netmgt.config.NotificationManager;

/**
 * @author Jeff Gehlbach <A HREF="mailto:jeffg@opennms.org">jeffg</A>
 *
 */
public class AsteriskOriginateNotificationStrategy implements NotificationStrategy {

    public AsteriskOriginateNotificationStrategy() {
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.notifd.NotificationStrategy#send(java.util.List)
     */
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
                } catch (Exception e) {
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

    protected ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

}
