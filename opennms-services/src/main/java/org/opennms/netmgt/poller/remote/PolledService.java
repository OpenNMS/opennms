/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller.remote;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Map;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.opennms.core.utils.InetAddressUtils;
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

    private final InetNetworkInterface m_netInterface;
    private final Map<String,Object> m_monitorConfiguration;
    private final OnmsPollModel m_pollModel;
    private final Integer m_serviceId;
    private final Integer m_nodeId;
    private final String m_nodeLabel;
    private final String m_svcName;
	
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
        m_netInterface = new InetNetworkInterface(monitoredService.getIpInterface().getIpAddress());
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
    @Override
    public InetAddress getAddress() {
        return m_netInterface.getAddress();
    }

    /**
     * <p>getIpAddr</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getIpAddr() {
        return InetAddressUtils.str(m_netInterface.getAddress());
    }

    /**
     * <p>getNetInterface</p>
     *
     * @return a {@link org.opennms.netmgt.poller.NetworkInterface} object.
     */
    @Override
    public NetworkInterface<InetAddress> getNetInterface() {
        return m_netInterface;
    }

    /**
     * <p>getNodeId</p>
     *
     * @return a int.
     */
    @Override
    public int getNodeId() {
        return m_nodeId;
    }

    /**
     * <p>getNodeLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getNodeLabel() {
        return m_nodeLabel;
    }

    /**
     * <p>getSvcName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
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
        .append(this.getNodeId())
        .append(this.getIpAddr())
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
            .append(this.getNodeId(), that.getNodeId())
            .append(this.getIpAddr(), that.getIpAddr())
            .append(this.getServiceId(), that.getServiceId())
            .isEquals();
    }

    /**
     * <p>compareTo</p>
     *
     * @param that a {@link org.opennms.netmgt.poller.remote.PolledService} object.
     * @return a int.
     */
    @Override
    public int compareTo(final PolledService that) {
        if (that == null) return -1;
        return new CompareToBuilder()
            .append(this.getNodeId(), that.getNodeId())
            .append(this.getNodeLabel(), that.getNodeLabel())
            .append(this.getIpAddr(), that.getIpAddr())
            .append(this.getSvcName(), that.getSvcName())
            .append(this.getServiceId(), that.getServiceId())
            .toComparison();
    }

    @Override
    public String getSvcUrl() {
        return null;
    }
}
