/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.browsers;

import org.opennms.core.criteria.restrictions.AnyRestriction;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.features.topology.api.SelectionContext;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

abstract class SelectionContextToRestrictionConverter {
    public List<Restriction> getRestrictions(SelectionContext selectionContext) {
        List<Restriction> restrictions = new ArrayList<Restriction>();
        restrictions.add(getAnyRestriction(selectionContext));
        return restrictions;
    }

    private AnyRestriction getAnyRestriction(SelectionContext selectionContext) {
        List<Restriction> restrictions = new ArrayList<Restriction>();
        for (VertexRef eachRef : selectionContext.getSelectedVertexRefs()) {
            if ("nodes".equals(eachRef.getNamespace())) {
                try {
                    restrictions.add(createRestriction(eachRef));
                } catch (NumberFormatException e) {
                    LoggerFactory.getLogger(this.getClass()).warn("Cannot filter nodes with ID: {}", eachRef.getId());
                }
            }
        }
        return new AnyRestriction(restrictions.toArray(new Restriction[restrictions.size()]));
    }

    abstract protected Restriction createRestriction(VertexRef ref);
}
