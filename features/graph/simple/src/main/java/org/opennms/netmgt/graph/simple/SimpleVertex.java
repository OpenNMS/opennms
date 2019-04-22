/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.simple;

import java.util.Objects;

import org.opennms.netmgt.graph.api.Vertex;
import org.opennms.netmgt.graph.api.aware.LocationAware;
import org.opennms.netmgt.graph.api.aware.NodeAware;
import org.opennms.netmgt.graph.api.generic.GenericProperties;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.info.NodeInfo;

/**
 * Acts as a domain specific view on a Vertex.
 * Can be extended by domain specific Vertex classes.
 * It contains no data of it's own but operates on the data of it's wrapped GenericVertex.
 */
public class SimpleVertex implements Vertex, NodeAware, LocationAware {

    protected final GenericVertex delegate;

    public SimpleVertex(GenericVertex genericVertex) {
        this.delegate = genericVertex;
    }

    public SimpleVertex(String namespace, String id) {
        this.delegate = new GenericVertex(namespace, id);
    }

    public SimpleVertex(SimpleVertex copyMe) {
        // copy the delegate to have a clone down to the properties maps
        this(new GenericVertex(copyMe.asGenericVertex()));
    }

    @Override
    public String getNamespace() {
        return delegate.getNamespace();
    }

    public void setNamespace(String namespace) {
        delegate.setNamespace(namespace);
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    public String getLabel() {
        return delegate.getProperty(GenericProperties.LABEL);
    }

    public void setLabel(String label) {
        this.delegate.setProperty(GenericProperties.LABEL, label);
    }

    @Override
    public final GenericVertex asGenericVertex() {
        return delegate;
    }

    public NodeInfo getNodeInfo() {
        return delegate.getNodeInfo();
    }

    public void setNodeInfo(NodeInfo nodeInfo) {
        this.delegate.setProperty(GenericProperties.NODE_INFO, nodeInfo);
    }

    public String getNodeRefString() {
        return delegate.getProperty(GenericProperties.NODE_REF);
    }

    public void setNodeRefString(String nodeRefString) {
        delegate.setProperty(GenericProperties.NODE_REF, nodeRefString);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public String getLocation() {
        if (getNodeInfo() != null) {
            return getNodeInfo().getLocation();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleVertex that = (SimpleVertex) o;
        return Objects.equals(this.delegate, that.delegate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(delegate);
    }
}
