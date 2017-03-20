/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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


import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class Graph.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "Graph")
@XmlAccessorType(XmlAccessType.FIELD)
public class Graph implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "title", required = true)
    private String title;

    @XmlAttribute(name = "resourceId")
    private String resourceId;

    @XmlAttribute(name = "nodeId")
    private String nodeId;

    @XmlAttribute(name = "nodeSource")
    private String nodeSource;

    @XmlAttribute(name = "domain")
    private String domain;

    @XmlAttribute(name = "interfaceId")
    private String interfaceId;

    @XmlAttribute(name = "timespan", required = true)
    private String timespan;

    @XmlAttribute(name = "graphtype", required = true)
    private String graphtype;

    @XmlAttribute(name = "extlink")
    private String extlink;

    public Graph() {
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof Graph) {
            Graph temp = (Graph)obj;
            boolean equals = Objects.equals(temp.title, title)
                && Objects.equals(temp.resourceId, resourceId)
                && Objects.equals(temp.nodeId, nodeId)
                && Objects.equals(temp.nodeSource, nodeSource)
                && Objects.equals(temp.domain, domain)
                && Objects.equals(temp.interfaceId, interfaceId)
                && Objects.equals(temp.timespan, timespan)
                && Objects.equals(temp.graphtype, graphtype)
                && Objects.equals(temp.extlink, extlink);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'domain'.
     * 
     * @return the value of field 'Domain'.
     */
    public Optional<String> getDomain() {
        return Optional.ofNullable(this.domain);
    }

    /**
     * Returns the value of field 'extlink'.
     * 
     * @return the value of field 'Extlink'.
     */
    public Optional<String> getExtlink() {
        return Optional.ofNullable(this.extlink);
    }

    /**
     * Returns the value of field 'graphtype'.
     * 
     * @return the value of field 'Graphtype'.
     */
    public String getGraphtype() {
        return this.graphtype;
    }

    /**
     * Returns the value of field 'interfaceId'.
     * 
     * @return the value of field 'InterfaceId'.
     */
    public Optional<String> getInterfaceId() {
        return Optional.ofNullable(this.interfaceId);
    }

    /**
     * Returns the value of field 'nodeId'.
     * 
     * @return the value of field 'NodeId'.
     */
    public Optional<String> getNodeId() {
        return Optional.ofNullable(this.nodeId);
    }

    /**
     * Returns the value of field 'nodeSource'.
     * 
     * @return the value of field 'NodeSource'.
     */
    public Optional<String> getNodeSource() {
        return Optional.ofNullable(this.nodeSource);
    }

    /**
     * Returns the value of field 'resourceId'.
     * 
     * @return the value of field 'ResourceId'.
     */
    public Optional<String> getResourceId() {
        return Optional.ofNullable(this.resourceId);
    }

    /**
     * Returns the value of field 'timespan'.
     * 
     * @return the value of field 'Timespan'.
     */
    public String getTimespan() {
        return this.timespan;
    }

    /**
     * Returns the value of field 'title'.
     * 
     * @return the value of field 'Title'.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            title, 
            resourceId, 
            nodeId, 
            nodeSource, 
            domain, 
            interfaceId, 
            timespan, 
            graphtype, 
            extlink);
        return hash;
    }

    /**
     * Sets the value of field 'domain'.
     * 
     * @param domain the value of field 'domain'.
     */
    public void setDomain(final String domain) {
        this.domain = domain;
    }

    /**
     * Sets the value of field 'extlink'.
     * 
     * @param extlink the value of field 'extlink'.
     */
    public void setExtlink(final String extlink) {
        this.extlink = extlink;
    }

    /**
     * Sets the value of field 'graphtype'.
     * 
     * @param graphtype the value of field 'graphtype'.
     */
    public void setGraphtype(final String graphtype) {
        if (graphtype == null) {
            throw new IllegalArgumentException("'graphtype' is a required attribute!");
        }
        this.graphtype = graphtype;
    }

    /**
     * Sets the value of field 'interfaceId'.
     * 
     * @param interfaceId the value of field 'interfaceId'.
     */
    public void setInterfaceId(final String interfaceId) {
        this.interfaceId = interfaceId;
    }

    /**
     * Sets the value of field 'nodeId'.
     * 
     * @param nodeId the value of field 'nodeId'.
     */
    public void setNodeId(final String nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * Sets the value of field 'nodeSource'.
     * 
     * @param nodeSource the value of field 'nodeSource'.
     */
    public void setNodeSource(final String nodeSource) {
        this.nodeSource = nodeSource;
    }

    /**
     * Sets the value of field 'resourceId'.
     * 
     * @param resourceId the value of field 'resourceId'.
     */
    public void setResourceId(final String resourceId) {
        this.resourceId = resourceId;
    }

    /**
     * Sets the value of field 'timespan'.
     * 
     * @param timespan the value of field 'timespan'.
     */
    public void setTimespan(final String timespan) {
        if (timespan == null) {
            throw new IllegalArgumentException("'timespan' is a required attribute!");
        }
        this.timespan = timespan;
    }

    /**
     * Sets the value of field 'title'.
     * 
     * @param title the value of field 'title'.
     */
    public void setTitle(final String title) {
        if (title == null) {
            throw new IllegalArgumentException("'title' is a required attribute!");
        }
        this.title = title;
    }

}
