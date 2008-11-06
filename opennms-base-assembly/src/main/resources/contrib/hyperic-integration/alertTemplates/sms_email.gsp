
<%
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

import groovy.xml.MarkupBuilder;
import org.hyperic.dao.*;
import org.hyperic.hq.appdef.server.session.*;
import org.hyperic.hq.appdef.shared.*;
import org.hyperic.hq.authz.server.session.*;
import org.hyperic.hq.events.*;
import org.hyperic.hq.dao.*;
import org.hyperic.hq.hqu.rendit.util.HQUtil

def getPlatformForAlertDef(def alertDef) {
    PlatformManagerLocal pltMan = PlatformManagerEJBImpl.getOne();
    
    switch (alertDef.appdefType) {
        case AppdefEntityConstants.APPDEF_TYPE_PLATFORM:
            return alertDef.appdefId 
        case AppdefEntityConstants.APPDEF_TYPE_SERVER:
            return pltMan.getPlatformIdByServer(alertDef.appdefId);
        case AppdefEntityConstants.APPDEF_TYPE_SERVICE:
            return pltMan.getPlatformIdByService(alertDef.appdefId);
        default:
            return null;
    }
      
}

def getOpenNMSTargetHosts(def alertDef) {

    def pattern = ~/opennms@(.*)/

    def recipients = alertDef.escalation.actions.action.initializedAction.collect { it.lookupEmailAddr().collect { it.address.address }; }.flatten();

    def hosts = recipients.grep { it =~ pattern }.collect { (it =~ pattern)[0][1] }

    System.out.println("recipients is "+recipients);
    System.out.println("hosts is "+hosts);

    return hosts;
}

def hosts = getOpenNMSTargetHosts(alertDef);

def platId = getPlatformForAlertDef(alertDef);


DAOFactory daoFactory = DAOFactory.getDAOFactory();

PlatformDAO platformDAO = daoFactory.platformDAO;

System.out.println("platId is "+platId);


def platform = null;
if (platId != null) {
    platform = platformDAO.get(platId);
}

Date timestamp = new Date();
String hostname = InetAddress.getLocalHost().hostName;

for(openNmsHost in hosts) {

    Socket socket = new Socket(openNmsHost, 5817);
    socket.outputStream.withWriter { out ->
            
            def xml = new MarkupBuilder(out);
            
            xml.log {
                events {
                    event {
                        uei("uei.opennms.org/external/hyperic/alert")
                        source("hq-server")
                        time(timestamp)
                        host(hostname)
                        parms {
                            if (platform != null) {
                                parm {
                                    parmName("platform.id")
                                    value(platform.id)
                                }
                                parm {
                                    parmName("platform.commentText")
                                    value("${platform.commentText}")
                                }
                                parm {
                                    parmName("platform.platformType.os")
                                    value("${platform.platformType.os}")
                                }
                                parm {
                                    parmName("platform.platformType.osVersion")
                                    value("${platform.platformType.osVersion}")
                                }
                                parm {
                                    parmName("platform.platformType.arch")
                                    value("${platform.platformType.arch}")
                                }
                                parm {
                                    parmName("platform.agent.address")
                                    value("${platform.agent.address}")
                                }
                                parm {
                                    parmName("platform.agent.port")
                                    value("${platform.agent.port}")
                                }
                                parm {
                                    parmName("platform.fqdn")
                                    value("${platform.fqdn}")
                                }
                                parm {
                                    parmName("platform.name")
                                    value("${platform.name}")
                                }
                                parm {
                                    parmName("platform.description")
                                    value("${platform.description}")
                                }
                                parm {
                                    parmName("platform.location")
                                    value("${platform.location}")
                                }
                            }
                            parm {
                                parmName("alert.id")
                                value("${alert.id}")
                            }
                            parm {
                                parmName("alert.fixed")
                                value("${alert.fixed}")
                            }
                            parm {
                                parmName("alert.ctime")
                                value("${alert.ctime}")
                            }
                            parm {
                                parmName("alert.timestamp")
                                value("${alert.timestamp}")
                            }
                            parm {
                                parmName("alert.ackedBy")
                                value("${alert.ackedBy}")
                            }
                            parm {
                                parmName("alert.stateId")
                                value("${alert.stateId}")
                            }
                            parm {
                                parmName("alert.url")
                                value(HQUtil.baseURL + alert.urlFor('alert'))
                            }
                            parm {
                                parmName("alertDef.id")
                                value("${alertDef.id}")
                            }
                            parm {
                                parmName("alertDef.name")
                                value("${alertDef.name}")
                            }
                            parm {
                                parmName("alertDef.description")
                                value("${alertDef.description}")
                            }
                            parm {
                                parmName("alertDef.priority")
                                value("${alertDef.priority}")
                            }
                            parm {
                                parmName("alertDef.appdefType")
                                value("${alertDef.appdefType}")
                            }
                            parm {
                                parmName("alertDef.appdefId")
                                value("${alertDef.appdefId}")
                            }
                            parm {
                                parmName("alertDef.notifyFiltered")
                                value("${alertDef.notifyFiltered}")
                            }
                            parm {
                                parmName("action.shortReason")
                                value("${action.shortReason}")
                            }
                            parm {
                                parmName("action.longReason")
                                value("${action.longReason}")
                            }
                            parm {
                                parmName("resource.instanceId")
                                value("${resource.instanceId}")
                            }
                            parm {
                                parmName("resource.name")
                                value("${resource.name}")
                            }
                            parm {
                                parmName("resource.url")
                                value(HQUtil.baseURL + resource.urlFor('inventory'))
                            }
                            parm {
                                parmName("resource.resourceType.cid")
                                value("${resource.resourceType.cid}")
                            }
                            parm {
                                parmName("resource.resourceType.name")
                                value("${resource.resourceType.name}")
                            }
                        }
                    }
                }
            }
        }
        
        
    }
%>

${action.shortReason}
