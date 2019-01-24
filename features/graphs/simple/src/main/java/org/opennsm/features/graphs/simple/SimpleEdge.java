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

package org.opennsm.features.graphs.simple;

import org.opennms.features.graph.api.AbstractEdge;
import org.opennms.features.graph.api.generic.GenericEdge;
import org.opennms.features.graph.api.generic.GenericProperties;

public class SimpleEdge extends AbstractEdge<SimpleVertex> {

    public SimpleEdge(SimpleVertex source, SimpleVertex target) {
        super(source, target);
    }

    public SimpleEdge(SimpleEdge e) {
        super(e.getSource(), e.getTarget());
        setLabel(e.getLabel());
    }

    @Override
    public GenericEdge asGenericEdge() {
        final GenericEdge genericEdge = new GenericEdge(getSource(), getTarget());
        if (getLabel() != null) {
            genericEdge.setProperty(GenericProperties.LABEL, getLabel());
        } else {
            genericEdge.setProperty(GenericProperties.LABEL, String.format("connection:%s:%s", getSource().getId(), getTarget().getId()));
        }
        // TODO MVR Tooltip?
//        if (getTooltip() != null) {
//            genericEdge.setProperty(GenericProperties.TOOLTIP, getTooltip());
//        }
        return genericEdge;
    }
}
