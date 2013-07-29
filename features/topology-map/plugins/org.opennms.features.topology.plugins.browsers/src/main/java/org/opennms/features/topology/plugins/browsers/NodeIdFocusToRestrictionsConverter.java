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
import org.opennms.core.criteria.restrictions.Restriction;

import java.util.ArrayList;
import java.util.List;

abstract class NodeIdFocusToRestrictionsConverter {
    public List<Restriction> getRestrictions(List<Integer> nodeIdFocus) {
        List<Restriction> restrictions = new ArrayList<Restriction>();
        restrictions.add(getAnyRestriction(nodeIdFocus));
        return restrictions;
    }

    private AnyRestriction getAnyRestriction(List<Integer> nodeIdFocus) {
        List<Restriction> restrictions = new ArrayList<Restriction>();
        for (Integer eachNodeId : nodeIdFocus) {
            restrictions.add(createRestriction(eachNodeId));
        }
        return new AnyRestriction(restrictions.toArray(new Restriction[restrictions.size()]));
    }

    abstract protected Restriction createRestriction(Integer nodeId);
}
