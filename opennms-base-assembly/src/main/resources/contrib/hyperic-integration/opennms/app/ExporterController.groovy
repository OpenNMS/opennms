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
 
/*
 * This script has the following bindings:
 *    org.hyperic.hq.events.AlertDefinitionInterface alertDef
 *    org.hyperic.hq.events.AlertInterface alert
 *    org.hyperic.hq.events.ActionExecutionInfo action
 *    org.hyperic.hq.authz.server.session.Resource resource
 *
 */
import org.hyperic.hq.hqu.rendit.BaseController

import groovy.xml.MarkupBuilder;

import java.text.*;

import org.hyperic.dao.*;
import org.hyperic.hq.appdef.server.session.*;
import org.hyperic.hq.appdef.shared.*;
import org.hyperic.hq.authz.server.session.*;
import org.hyperic.hq.events.*;
import org.hyperic.hq.dao.*;
import org.hyperic.hq.hqu.rendit.util.HQUtil

class ExporterController 
	extends BaseController
{
    def ExporterController() {
//        setTemplate('standard')  // in views/templates/standard.gsp 
    }
    
    def index(params) {

        DAOFactory daoFactory = DAOFactory.getDAOFactory();

        PlatformDAO platformDAO = daoFactory.platformDAO;

        def platforms = platformDAO.findAll_orderName(true);

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

        def writer = new StringWriter();
        def xml = new MarkupBuilder(writer);
        xml.'model-import'('foreign-source':'HQ', 'date-stamp':formatter.format(new Date())) {
            for(p in platforms) {
                node('node-label':p.fqdn, 'foreign-id':p.id) {
                    'interface'('ip-addr':p.agent.address, descr:'agent-address', status:1, 'snmp-primary':'N') {
                        'monitored-service'('service-name':'ICMP')
                        'monitored-service'('service-name':'HypericAgent')
                    }
                }
            }
        }


        render(setContentType:'text/xml', inline:writer.toString())
    }
}
