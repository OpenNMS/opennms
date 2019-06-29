/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.provider.bsm;

import java.util.Objects;
import java.util.Set;

import org.opennms.netmgt.graph.api.generic.GenericProperties;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.info.NodeInfo;
import org.opennms.netmgt.graph.simple.AbstractDomainVertex;

public abstract class AbstractBusinessServiceVertex extends AbstractDomainVertex {

    enum Type {
        BusinessService,
        IpService,
        ReductionKey,
        Application
    }

    private final static String PROPERTY_LEVEL = "level";
    protected final static String PROPERTY_TYPE = "type";
    private final static String PROPERTY_REDUCTION_KEYS = "reductionKeys";
    private final static String PROPERTY_IS_LEAF = "isLeaf";
    
    public AbstractBusinessServiceVertex(GenericVertex genericVertex) {
        super(genericVertex);
        // specific checks for this class:
        Objects.requireNonNull(getLevel(), "getLevel() cannot be null.");
        Objects.requireNonNull(isLeaf(), "isLeaf() cannot be null.");
        Objects.requireNonNull(getType(), "getType() cannot be null.");
        Objects.requireNonNull(getReductionKeys(), "getReductionKeys() cannot be null.");
    }

    public int getLevel() {
        return this.delegate.getProperty(PROPERTY_LEVEL);
    }

    public boolean isLeaf(){
        return this.delegate.getProperty(PROPERTY_IS_LEAF);
    }

    public Type getType() {
        return this.delegate.getProperty(PROPERTY_TYPE);
    }

    public Set<String> getReductionKeys() {
        return this.delegate.getProperty(PROPERTY_REDUCTION_KEYS);
    }

    public final static AbstractBusinessServiceVertex from(GenericVertex genericVertex) {
        // TODO: Patrick. I don't really like this piece of code: the super class knows about its children but I haven't found a better
        // solution yet
        Type type = genericVertex.getProperty(PROPERTY_TYPE);
        if (Type.Application == type) {
            return new ApplicationVertex(genericVertex);
        } else if (Type.BusinessService == type) {
            return new BusinessServiceVertex(genericVertex);
        } else if (Type.IpService == type) {
            return new IpServiceVertex(genericVertex);
        } else if (Type.ReductionKey == type) {
            return new ReductionKeyVertex(genericVertex);
        } else {
            throw new IllegalArgumentException("Unknown type of AbstractBusinessServiceVertex: "  + type);
        }
    }


    public abstract static class AbstractBusinessServiceVertexBuilder<T extends AbstractDomainVertexBuilder<?>, V extends AbstractBusinessServiceVertex>
        extends AbstractDomainVertexBuilder<T> { 
        
        public T nodeInfo(NodeInfo nodeInfo) {
            this.properties.put(GenericProperties.NODE_INFO, nodeInfo);
            return (T)this;
        }
        
        public T reductionKeys(Set<String> reductionKeys) {
            this.properties.put(PROPERTY_REDUCTION_KEYS, reductionKeys);// TODO MVR collections cannot be persisted
            return (T)this;
        }
        
        public T type(Type type) {
            this.properties.put(PROPERTY_TYPE, type);
            return (T)this;
        }
        
        public T isLeaf(boolean isLeaf){
            this.properties.put(PROPERTY_IS_LEAF, isLeaf);
            return (T)this;
        }
        
        /** level the level of the vertex in the Business Service Hierarchy. The root element is level 0. */
        public T level(int level){
            this.properties.put(PROPERTY_LEVEL, level);
            return (T)this;
        }
        
        public abstract V build();
    }
}
