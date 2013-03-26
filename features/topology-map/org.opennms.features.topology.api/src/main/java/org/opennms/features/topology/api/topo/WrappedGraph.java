/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api.topo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="graph")
@XmlAccessorType(XmlAccessType.FIELD)
public class WrappedGraph {
    
    @XmlElements({
            @XmlElement(name="vertex", type=WrappedLeafVertex.class),
            @XmlElement(name="group", type=WrappedGroup.class)
    })
    public List<WrappedVertex> m_vertices = new ArrayList<WrappedVertex>();
    
    @XmlElement(name="edge")
    public List<WrappedEdge> m_edges = new ArrayList<WrappedEdge>();
    
    @XmlAttribute(name="namespace")
    public String m_namespace;
    
    /**
     * No-arg constructor for JAXB.
     */
    public WrappedGraph() {}

    public WrappedGraph(String namespace, List<WrappedVertex> vertices, List<WrappedEdge> edges) {
    	m_namespace = namespace;
        m_vertices = vertices;
        m_edges = edges;
    }
    
    /**
     * This getter-setter pair is required so that we can use bean introspection to find 
     * the namespace when we are deserializing child classes.
     *
     * @see WrappedVertex#afterUnmarshal(javax.xml.bind.Unmarshaller, Object)
     * @see WrappedEdge#afterUnmarshal(javax.xml.bind.Unmarshaller, Object)
     */
    public String getNamespace() {
        return m_namespace;
    }

    /**
     * This getter-setter pair is required so that we can use bean introspection to find 
     * the namespace when we are deserializing child classes.
     *
     * @see WrappedVertex#afterUnmarshal(javax.xml.bind.Unmarshaller, Object)
     * @see WrappedEdge#afterUnmarshal(javax.xml.bind.Unmarshaller, Object)
     */
    public void setNamespace(String namespace) {
        m_namespace = namespace;
    }
}