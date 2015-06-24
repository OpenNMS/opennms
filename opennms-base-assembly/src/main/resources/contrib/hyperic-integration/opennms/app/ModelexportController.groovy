/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

/*
 * This script has the following bindings:
 *    org.hyperic.hq.events.AlertDefinitionInterface alertDef
 *    org.hyperic.hq.events.AlertInterface alert
 *    org.hyperic.hq.events.ActionExecutionInfo action
 *    org.hyperic.hq.authz.server.session.Resource resource
 *
 */
import org.hyperic.hq.hqu.rendit.BaseController

import java.text.SimpleDateFormat
import org.hyperic.hibernate.PageInfo
import org.hyperic.hq.authz.server.session.ResourceSortField
import org.hyperic.hq.appdef.server.session.PlatformManagerEJBImpl as PlatformManager

class ModelexportController 
    extends BaseController
{
    def ModelexportController() {
        setXMLMethods(['list'])
    }

    def list(xml, params) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        def platforms = resourceHelper.findPlatforms(new PageInfo(ResourceSortField.NAME, true));
        def man = PlatformManager.one
        xml.'model-import'('xmlns':'http://xmlns.opennms.org/xsd/config/model-import', 'foreign-source':'HQ', 'date-stamp':formatter.format(new Date())) {
            platforms.each { res ->   
                def p = man.findPlatformById(res.instanceId)

                node('node-label':p.fqdn, 'foreign-id':p.id) {
                   p.ips.each { ip ->
                    'interface'('ip-addr': ip.address, descr: ip.macAddress,
                        status: 1) {
                            'monitored-service'('service-name': 'ICMP')
                            if(ip.address == p.agent.address) {
                                'monitored-service'('service-name': 'HypericAgent')
                            }
                        }
                    }
                }
            }
        }

        xml
    }
}
