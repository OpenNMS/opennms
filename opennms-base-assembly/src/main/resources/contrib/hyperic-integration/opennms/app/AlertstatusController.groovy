/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

import java.text.DateFormat
import java.text.SimpleDateFormat
import org.hyperic.hibernate.PageInfo
import org.hyperic.hq.hqu.rendit.BaseController
import org.hyperic.hq.appdef.server.session.PlatformManagerEJBImpl as PlatformManager
import org.hyperic.hq.events.server.session.AlertManagerEJBImpl as AlertManager
import org.hyperic.hq.events.shared.AlertValue

class AlertstatusController
    extends BaseController
{
    def AlertstatusController() {
        setXMLMethods(["list"])
    }

    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")

    def list(xml, params) {
        def man = AlertManager.one

        xml."hyperic-alert-statuses"() {
            if (params["id"] == null) {
                // This call returns a list of mixed Alert and AlertValue objects; we just need
                // the AlertValue objects since they have easy-to-parse lists of the actions
                // and escalations associated with them
                man.findAllAlerts().each { al ->
                    if (al instanceof AlertValue) {
                        try {
                            "alert"(getAlertAttribs(al))
                        } catch (e) {
                            // Skip this alert
                        }
                    }
                }
            } else {
                params["id"].each { alertId ->
                    def al = man.getById(alertId.toInteger())
                    try {
                        "alert"(getAlertAttribs(al))
                    } catch (e) {
                        // Skip this alert
                    }
                }
            }
        }
        xml
    }

    private def getAlertAttribs(alert) {
        def attribs = [:]
        if (alert.respondsTo("getId").size > 0) { 
            attribs["id"] = alert.id 
        } else {
            throw new IllegalArgumentException("Alert without ID found, must be a type mismatch: " + alert.class)
        }
        if (alert.respondsTo("isFixed").size > 0) {
            attribs["fixed"] = alert.fixed
            if (attribs["fixed"] == true) {
                // Search for "fixed" actions to extract username and timestamp information
                if (alert.respondsTo("getActionLogs").size > 0) {
                    alert.actionLogs.each { actionLog ->
                        // If the action field is null, this is either an "acknowledgement" or "fixed" action
                        if (actionLog.action == null) {
                            if (actionLog.subject != null && actionLog.subject.name != null && actionLog.subject.name != "") {
                                attribs["fixUser"] = actionLog.subject.name
                            }
                            // Non-null in Hyperic database
                            attribs["fixMessage"] = actionLog.detail
                            // Non-null in Hyperic database
                            attribs["fixTime"] = formatter.format(new Date(actionLog.timeStamp))
                        } else {
                            // Skip the action; it is not a "fixed" or "acknowledgement" action
                        }
                    }
                }
            } else {
                // Search for "acknowledgement" actions to extract username and timestamp information
                if (alert.respondsTo("getActionLogs").size > 0) {
                    alert.actionLogs.each { actionLog ->
                        // If the action field is null, this is either an "acknowledgement" or "fixed" action
                        if (actionLog.action == null) {
                            if (actionLog.subject != null && actionLog.subject.name != null && actionLog.subject.name != "") {
                                attribs["ackUser"] = actionLog.subject.name
                            }
                            // Non-null in Hyperic database
                            attribs["ackMessage"] = actionLog.detail
                            // Non-null in Hyperic database
                            attribs["ackTime"] = formatter.format(new Date(actionLog.timeStamp))
                        } else {
                            // Skip the action; it is not a "fixed" or "acknowledgement" action
                        }
                    }
                }
            }
        }
        attribs
    }
}
