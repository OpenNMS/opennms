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
                // TODO: Fetch all alerts
            } else {
                params['id'].each { aval ->
                    def a = man.findAlertById(aval.toInteger())
                    def attribs = [:]
                    attribs['fixed'] = a.fixed
                    attribs['id'] = a.id
                    attribs['state'] = a.stateId
                    if(a.ackedBy != null) { attribs['ackedBy'] =  a.ackedBy }
                    //'alert'(fixed: a.fixed, id: a.id, state: a.stateId, 'ackUser': a.ackedBy )
                    'alert'(attribs)
                }
            }
        }
        xml
    }
}
