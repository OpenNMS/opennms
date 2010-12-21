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
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.poller.InetNetworkInterface;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;

/**
 * <p>PolledService class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class PolledService implements MonitoredService, Serializable, Comparable<PolledService> {
    
    private static final long serialVersionUID = 2L;

    private InetNetworkInterface m_netInterface;
    private Map<String,Object> m_monitorConfiguration;
    private OnmsPollModel m_pollModel;
    private Integer m_serviceId;
    private Integer m_nodeId;
    private String m_nodeLabel;
    private String m_svcName;
	
	/**
	 * <p>Constructor for PolledService.</p>
	 *
	 * @param monitoredService a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
	 * @param monitorConfiguration a {@link java.util.Map} object.
	 * @param pollModel a {@link org.opennms.netmgt.poller.remote.OnmsPollModel} object.
	 */
	public PolledService(final OnmsMonitoredService monitoredService, final Map<String,Object> monitorConfiguration, final OnmsPollModel pollModel) {
        m_serviceId = monitoredService.getId();
        m_nodeId = monitoredService.getNodeId();
        m_nodeLabel = monitoredService.getIpInterface().getNode().getLabel();
        m_svcName = monitoredService.getServiceName();
        m_netInterface = new InetNetworkInterface(monitoredService.getIpInterface().getInetAddress());
		m_monitorConfiguration = monitorConfiguration;
		m_pollModel = pollModel;
	}
	
	/**
	 * <p>getServiceId</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getServiceId() {
		return m_serviceId;
	}

    /**
     * <p>getAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getAddress() {
        return m_netInterface.getAddress();
    }

    /**
     * <p>getIpAddr</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddr() {
        return m_netInterface.getAddress().getHostAddress();
    }

    /**
     * <p>getNetInterface</p>
     *
     * @return a {@link org.opennms.netmgt.poller.NetworkInterface} object.
     */
    public NetworkInterface<InetAddress> getNetInterface() {
        return m_netInterface;
    }

    /**
     * <p>getNodeId</p>
     *
     * @return a int.
     */
    public int getNodeId() {
        return m_nodeId;
    }

    /**
     * <p>getNodeLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNodeLabel() {
        return m_nodeLabel;
    }

    /**
     * <p>getSvcName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSvcName() {
        return m_svcName;
    }
	
	/**
	 * <p>getMonitorConfiguration</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<String,Object> getMonitorConfiguration() {
        return m_monitorConfiguration;
    }
    
    /**
     * <p>getPollModel</p>
     *
     * @return a {@link org.opennms.netmgt.poller.remote.OnmsPollModel} object.
     */
    public OnmsPollModel getPollModel() {
        return m_pollModel;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return getNodeId()+":"+getIpAddr()+":"+getSvcName();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 57)
        .append(this.getNodeLabel())
        .append(this.getIpAddr())
        .append(this.getNodeId())
        .append(this.getSvcName())
        .append(this.getServiceId())
        .toHashCode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object o) {
        if (o == null) return false;
        if (!(o instanceof PolledService)) {
            return false;
        }
        final PolledService that = (PolledService)o;
        return new EqualsBuilder()
            .append(this.getServiceId(), that.getServiceId())
            .append(this.getNodeId(), that.getNodeId())
            .append(this.getIpAddr(), that.getIpAddr())
            .isEquals();
    }

    /**
     * <p>compareTo</p>
     *
     * @param that a {@link org.opennms.netmgt.poller.remote.PolledService} object.
     * @return a int.
     */
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
