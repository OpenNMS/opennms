/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: August 17, 2006
 *
 * Copyright (C) 2006-2007 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.poller.remote;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Map;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.poller.IPv4NetworkInterface;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class PolledService implements MonitoredService, Serializable, Comparable<PolledService> {
    
    private static final long serialVersionUID = 2L;

    private IPv4NetworkInterface m_netInterface;
    private Map<String,Object> m_monitorConfiguration;
    private OnmsPollModel m_pollModel;
    private Integer m_serviceId;
    private Integer m_nodeId;
    private String m_nodeLabel;
    private String m_svcName;
	
	public PolledService(OnmsMonitoredService monitoredService, Map<String,Object> monitorConfiguration, OnmsPollModel pollModel) {
        m_serviceId = monitoredService.getId();
        m_nodeId = monitoredService.getNodeId();
        m_nodeLabel = monitoredService.getIpInterface().getNode().getLabel();
        m_svcName = monitoredService.getServiceName();
        m_netInterface = new IPv4NetworkInterface(monitoredService.getIpInterface().getInetAddress());
		m_monitorConfiguration = monitorConfiguration;
		m_pollModel = pollModel;
	}
	
	public Integer getServiceId() {
		return m_serviceId;
	}

    public InetAddress getAddress() {
        return m_netInterface.getInetAddress();
    }

    public String getIpAddr() {
        return m_netInterface.getInetAddress().getHostAddress();
    }

    public NetworkInterface getNetInterface() {
        return m_netInterface;
    }

    public int getNodeId() {
        return m_nodeId;
    }

    public String getNodeLabel() {
        return m_nodeLabel;
    }

    public String getSvcName() {
        return m_svcName;
    }
	
	public Map<String,Object> getMonitorConfiguration() {
        return m_monitorConfiguration;
    }
    
    public OnmsPollModel getPollModel() {
        return m_pollModel;
    }

    @Override
    public String toString() {
        return getNodeId()+":"+getIpAddr()+":"+getSvcName();
    }

    public int compareTo(final PolledService that) {
        if (that == null) return -1;
        return new CompareToBuilder()
            .append(this.getNodeLabel(), that.getNodeLabel())
            .append(this.getIpAddr(), that.getIpAddr())
            .append(this.getNodeId(), that.getNodeId())
            .append(this.getSvcName(), that.getSvcName())
            .append(this.getServiceId(), that.getServiceId())
            .toComparison();
    }
}
