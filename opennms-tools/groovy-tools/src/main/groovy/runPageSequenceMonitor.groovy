#!/usr/bin/env groovy

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

@GrabResolver(name='opennms-repo', root='http://maven.opennms.org/content/groups/opennms.org-release')

@Grab(group='org.slf4j', module='slf4j-simple', version='1.7.5')

@Grab(group='org.opennms.dependencies', module='spring-dependencies', type='pom', version='1.13.1')
import org.springframework.util.*;

@Grab(group='org.opennms', module='opennms-model', version='1.13.1')
import org.opennms.netmgt.model.*;

@Grab(group='org.opennms', module='opennms-services', version='1.13.1')
import org.opennms.netmgt.poller.*;
import org.opennms.netmgt.poller.monitors.*;


public class runPageSequenceMonitor implements MonitoredService {
    private int m_nodeId = 21;
    private String m_nodeLabel = "nodeLabel";
    private String m_ipAddr;
    private String m_svcName = "PSMTest";
    private InetAddress m_inetAddr;

    public void setSvcName(String svcName) {
        m_svcName = svcName;
    }

    public String getSvcName() {
        return m_svcName;
    }

    public String getIpAddr() {
        return m_ipAddr;
    }

    public int getNodeId() {
        return m_nodeId;
    }

    public String getNodeLabel() {
        return m_nodeLabel;
    }

    public NetworkInterface getNetInterface() {
        return new InetNetworkInterface(getAddress());
    }

    public String getSvcUrl() { return null }

    public InetAddress getAddress() {
        return m_inetAddr;
    }

    public void setIpAddr(String ipAddr) throws UnknownHostException {
        m_ipAddr = ipAddr;
        m_inetAddr = InetAddress.getByName(ipAddr);
    }

    public void setNodeId(int nodeId) {
        m_nodeId = nodeId;
    }

    public void setNodeLabel(String nodeLabel) {
        m_nodeLabel = nodeLabel;
    }


    public static void main(String[] args) {
        
        if (args.length != 2) {
            System.err.println "usage runPageSequenceMonitor <ip-address> <page-sequence-file>"
            System.exit(1);
        }

        String ipAddr = args[0];
        println ipAddr;
        MonitoredService monSvc = new runPageSequenceMonitor(ipAddr:ipAddr);	  
        String pageSequenceConfig = new File(args[1]).getText();
        println pageSequenceConfig;
    
        Map parms = [retry:'1', timeout:'20000', 'page-sequence':pageSequenceConfig];
	  
        PageSequenceMonitor monitor = new PageSequenceMonitor();
	  
        monitor.initialize(parms);
        monitor.initialize(monSvc);
        PollStatus status = monitor.poll(monSvc, parms);
        println "${status} : ${status.reason}";
    }
}
