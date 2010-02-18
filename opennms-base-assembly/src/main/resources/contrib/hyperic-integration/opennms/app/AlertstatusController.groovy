/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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

import java.text.SimpleDateFormat
import org.hyperic.hibernate.PageInfo
import org.hyperic.hq.hqu.rendit.BaseController
import org.hyperic.hq.appdef.server.session.PlatformManagerEJBImpl as PlatformManager
import org.hyperic.hq.events.server.session.AlertManagerEJBImpl as AlertManager
import org.hyperic.hq.events.shared.AlertValue
import org.hyperic.hq.authz.server.session.RoleManagerEJBImpl as RoleManager

class AlertstatusController
    extends BaseController
{
    def AlertstatusController() {
        setXMLMethods(['list'])
    }

    def list(xml, params) {
        def man = AlertManager.one

        xml.'hyperic-alert-statuses'() {
            if (params['id'] == null) {
                // This call returns a list of mixed Alert and AlertValue objects; we just need
                // the AlertValue objects since they have easy-to-parse lists of the actions
                // and escalations associated with them
                man.findAllAlerts().each { al ->
                    if (al instanceof AlertValue) {
                        'alert'(getAlertAttribs(al))
                    }
                }
            } else {
                params['id'].each { alertId ->
                    def al = man.getById(alertId.toInteger())
                    'alert'(getAlertAttribs(al))
                }
            }
        }
        xml
    }

    private def getAlertAttribs(alert) {
        def attribs = [:]
        if (alert.respondsTo('getId').size > 0) { attribs['id'] = alert.id }
        if (alert.respondsTo('isFixed').size > 0) {
            attribs['fixed'] = alert.fixed
            if (attribs['fixed'] == true) {
                if (alert.respondsTo('getActionLogs').size > 0) {
                    alert.actionLogs.each { actionLog ->
                        // If the action field is null, this is either an acknowledgement or fixed message
                        if (actionLog.action == null) {
                            attribs['fixUserId'] == actionLog.subject.fullName
                        } else {
                        }
                        attribs['actionDetail'] = actionLog.detail
                    }
                }
            }
            // if (alert.stateId != null) { attribs['state'] = alert.stateId }
            /*
            if(alert.ackedBy != null) { 
                def roleMan = RoleManager.one
                attribs['ackedBy'] = roleMan.findRoleById(alert.ackedBy).name 
            }
            */
        }
        attribs
    }
}
