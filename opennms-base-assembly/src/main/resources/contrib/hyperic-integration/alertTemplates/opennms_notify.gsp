<%
/*
 * This script has the following bindings:
 *    org.hyperic.hq.events.AlertDefinitionInterface alertDef
 *    org.hyperic.hq.events.AlertInterface alert
 *    org.hyperic.hq.events.ActionExecutionInfo action
 *    org.hyperic.hq.authz.server.session.Resource resource
 *    org.hyperic.hq.appdef.shared.PlatformValue platform
 *    String host
 *    String ip
 *    String port
 *
 */

import groovy.xml.MarkupBuilder
import org.hyperic.hq.hqu.rendit.util.HQUtil

Date timestamp = new Date()
String hostname = InetAddress.getLocalHost().hostName

Socket socket = new Socket(host, Integer.valueOf(port))
socket.outputStream.withWriter { out ->
        
    def xml = new MarkupBuilder(out)
    
    xml.log {
        events {
            event {
                uei("uei.opennms.org/external/hyperic/alert")
                source("HypericNotify")
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
                        parmName("alert.baseURL")
                        value(HQUtil.baseURL)
                    }
                    parm {
                        parmName("alert.source")
                        value("HQ")
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
                        parmName("resource.resourceType.name")
                        value("${resource.resourceType.name}")
                    }
                }
            }
        }
    }
}

%>
${action.shortReason}
