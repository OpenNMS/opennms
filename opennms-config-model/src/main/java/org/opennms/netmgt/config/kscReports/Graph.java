/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.kscReports;


import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Class Graph.
 */
@XmlRootElement(name = "Graph")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("ksc-performance-reports.xsd")
public class Graph implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "title", required = true)
    private String m_title;

    @XmlAttribute(name = "resourceId")
    private String m_resourceId;

    @XmlAttribute(name = "nodeId")
    private String m_nodeId;

    @XmlAttribute(name = "nodeSource")
    private String m_nodeSource;

    @XmlAttribute(name = "domain")
    private String m_domain;

    @XmlAttribute(name = "interfaceId")
    private String m_interfaceId;

    @XmlAttribute(name = "timespan", required = true)
    private String m_timespan;

    @XmlAttribute(name = "graphtype", required = true)
    private String m_graphtype;

    @XmlAttribute(name = "extlink")
    private String m_extlink;

    public Graph() {
    }

    public String getTitle() {
        return m_title;
    }

    public void setTitle(final String title) {
        m_title = ConfigUtils.assertNotNull(title, "title");
    }

    public Optional<String> getResourceId() {
        return Optional.ofNullable(m_resourceId);
    }

    public void setResourceId(final String resourceId) {
        m_resourceId = ConfigUtils.normalizeString(resourceId);
    }

    public Optional<String> getNodeId() {
        return Optional.ofNullable(m_nodeId);
    }

    public void setNodeId(final String nodeId) {
        m_nodeId = ConfigUtils.normalizeString(nodeId);
    }

    public Optional<String> getNodeSource() {
        return Optional.ofNullable(m_nodeSource);
    }

    public void setNodeSource(final String nodeSource) {
        m_nodeSource = ConfigUtils.normalizeString(nodeSource);
    }

    public Optional<String> getDomain() {
        return Optional.ofNullable(m_domain);
    }

    public void setDomain(final String domain) {
        m_domain = ConfigUtils.normalizeString(domain);
    }

    public Optional<String> getInterfaceId() {
        return Optional.ofNullable(m_interfaceId);
    }

    public void setInterfaceId(final String interfaceId) {
        m_interfaceId = ConfigUtils.normalizeString(interfaceId);
    }

    public String getTimespan() {
        return m_timespan;
    }

    public void setTimespan(final String timespan) {
        m_timespan = ConfigUtils.assertNotEmpty(timespan, "timespan");
    }

    public String getGraphtype() {
        return m_graphtype;
    }

    public void setGraphtype(final String graphtype) {
        m_graphtype = ConfigUtils.assertNotEmpty(graphtype, "graphtype");
    }

    public Optional<String> getExtlink() {
        return Optional.ofNullable(m_extlink);
    }

    public void setExtlink(final String extlink) {
        m_extlink = ConfigUtils.normalizeString(extlink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_title, 
                            m_resourceId, 
                            m_nodeId, 
                            m_nodeSource, 
                            m_domain, 
                            m_interfaceId, 
                            m_timespan, 
                            m_graphtype, 
                            m_extlink);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Graph) {
            final Graph that = (Graph)obj;
            return Objects.equals(this.m_title, that.m_title)
                    && Objects.equals(this.m_resourceId, that.m_resourceId)
                    && Objects.equals(this.m_nodeId, that.m_nodeId)
                    && Objects.equals(this.m_nodeSource, that.m_nodeSource)
                    && Objects.equals(this.m_domain, that.m_domain)
                    && Objects.equals(this.m_interfaceId, that.m_interfaceId)
                    && Objects.equals(this.m_timespan, that.m_timespan)
                    && Objects.equals(this.m_graphtype, that.m_graphtype)
                    && Objects.equals(this.m_extlink, that.m_extlink);
        }
        return false;
    }

}
