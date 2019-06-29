/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.graph.provider.bsm;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;
import org.opennms.netmgt.graph.api.generic.GenericVertex;

import com.google.common.collect.Sets;

public final class ReductionKeyVertex extends AbstractBusinessServiceVertex {

    public static final int MAX_LABEL_LENGTH = 27;
    private static final Pattern REDUCTION_KEY_LABEL_PATTERN = Pattern.compile("^.*\\/(.+?):.*:(.+)$");

    private ReductionKeyVertex(Map<String, Object> properties) {
        this(GenericVertex.builder().properties(properties).build());
    }
    
    public ReductionKeyVertex(GenericVertex genericVertex) {
        super(genericVertex);
        checkArgument(Type.ReductionKey == genericVertex.getProperty(PROPERTY_TYPE), "%s must be %s for %s", PROPERTY_TYPE, Type.ReductionKey, getClass());
    }

    protected static String getLabelFromReductionKey(String reductionKey) {
        String label;
        Matcher m = REDUCTION_KEY_LABEL_PATTERN.matcher(reductionKey);
        if (m.matches()) {
            label = String.format("%s:%s", m.group(1), m.group(2));
        } else {
            label = reductionKey;
        }
        if (label.length() > MAX_LABEL_LENGTH) {
            return label.substring(0, MAX_LABEL_LENGTH - "...".length()) + "...";
        }
        return label;
    }
    
    public final static ReductionKeyVertexBuilder builder() {
        return new ReductionKeyVertexBuilder();
    }
    
   public final static class ReductionKeyVertexBuilder extends AbstractBusinessServiceVertexBuilder<ReductionKeyVertexBuilder, ReductionKeyVertex> {
        
        public ReductionKeyVertexBuilder graphVertex(GraphVertex graphVertex) {
            reductionKey(graphVertex.getReductionKey());
            level(graphVertex.getLevel());
            return this;
        }
        
        public ReductionKeyVertexBuilder reductionKey(String reductionKey) {
            id(Type.ReductionKey + ":" + reductionKey);
            label(getLabelFromReductionKey(reductionKey)); 
            type(Type.ReductionKey);
            // ipAddress(ipAddress); // TODO MVR this is not yet supported. Maybe IpRef or something like this could be added
            isLeaf(true);
            reductionKeys(Sets.newHashSet(reductionKey));      
            return this;
        }
        
        public ReductionKeyVertex build() {
            this.type(Type.ReductionKey);
            return new ReductionKeyVertex(properties);
        }
    }

}
