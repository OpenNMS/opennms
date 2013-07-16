/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;
import java.util.Set;

import org.opennms.features.poller.remote.gwt.client.utils.CompareToBuilder;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * <p>GWTMonitoredService class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class GWTMonitoredService implements Serializable, IsSerializable, Comparable<GWTMonitoredService> {

	private static final long serialVersionUID = -8529053406347958362L;

	private int m_id;

    private int m_ifIndex;

    private int m_ipInterfaceId;

    private String m_ipAddress;

    private String m_hostname;

    private int m_nodeId;

    private String m_serviceName;

    private Set<String> m_applications;

    /**
     * <p>Constructor for GWTMonitoredService.</p>
     */
    public GWTMonitoredService() {
    }

    /**
     * <p>setId</p>
     *
     * @param id a int.
     */
    public void setId(final int id) {
        m_id = id;
    }

    /**
     * <p>getId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getId() {
        return m_id;
    }

    /**
     * <p>setIfIndex</p>
     *
     * @param ifIndex a {@link java.lang.Integer} object.
     */
    public void setIfIndex(final Integer ifIndex) {
        m_ifIndex = ifIndex;
    }

    /**
     * <p>getIfIndex</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getIfIndex() {
        return m_ifIndex;
    }

    /**
     * <p>setIpInterfaceId</p>
     *
     * @param id a {@link java.lang.Integer} object.
     */
    public void setIpInterfaceId(final Integer id) {
        m_ipInterfaceId = id;
    }

    /**
     * <p>getIpInterfaceId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getIpInterfaceId() {
        return m_ipInterfaceId;
    }

    /**
     * <p>setIpAddress</p>
     *
     * @param ipAddress a {@link java.lang.String} object.
     */
    public void setIpAddress(final String ipAddress) {
        m_ipAddress = ipAddress;
    }

    /**
     * <p>getIpAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddress() {
        return m_ipAddress;
    }

    /**
     * <p>setHostname</p>
     *
     * @param hostname a {@link java.lang.String} object.
     */
    public void setHostname(final String hostname) {
        m_hostname = hostname;
    }

    /**
     * <p>getHostname</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHostname() {
        return m_hostname;
    }

    /**
     * <p>setNodeId</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     */
    public void setNodeId(final Integer nodeId) {
        m_nodeId = nodeId;
    }

    /**
     * <p>getNodeId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getNodeId() {
        return m_nodeId;
    }

    /**
     * <p>setServiceName</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     */
    public void setServiceName(final String serviceName) {
        m_serviceName = serviceName;
    }

    /**
     * <p>getServiceName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceName() {
        return m_serviceName;
    }

    /**
     * <p>getApplications</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<String> getApplications() {
        return m_applications;
    }

    /**
     * <p>setApplications</p>
     *
     * @param applications a {@link java.util.Set} object.
     */
    public void setApplications(final Set<String> applications) {
        m_applications = applications;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
        @Override
    public String toString() {
        return "GWTMonitoredService[id=" + m_id + ",service=" + m_serviceName + ",nodeId=" + m_nodeId + ",ipInterfaceId=" + m_ipInterfaceId
                + "]";
    }

    /**
     * <p>compareTo</p>
     *
     * @param that a {@link org.opennms.features.poller.remote.gwt.client.GWTMonitoredService} object.
     * @return a int.
     */
        @Override
    public int compareTo(GWTMonitoredService that) {
        return new CompareToBuilder()
        .append(this.getServiceName(), that.getServiceName())
        .append(this.getNodeId(), that.getNodeId())
        .append(this.getIpInterfaceId(), that.getIpInterfaceId())
        .append(this.getApplications(), that.getApplications())
        .append(this.getId(), that.getId())
        .toComparison();
    }

}
