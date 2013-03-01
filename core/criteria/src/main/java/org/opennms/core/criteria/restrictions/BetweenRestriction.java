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

package org.opennms.core.criteria.restrictions;

import java.util.Arrays;
import java.util.List;

public class BetweenRestriction extends AttributeValueRestriction {

    public BetweenRestriction(final String attribute, final Object begin, final Object end) {
        super(RestrictionType.BETWEEN, attribute, Arrays.asList(new Object[] { begin, end }));
    }

    public Object getBegin() {
        @SuppressWarnings("unchecked")
        final List<Object> value = (List<Object>) getValue();
        return value.get(0);
    }

    public Object getEnd() {
        @SuppressWarnings("unchecked")
        final List<Object> value = (List<Object>) getValue();
        return value.get(1);
    }

    @Override
    public void visit(final RestrictionVisitor visitor) {
        visitor.visitBetween(this);
    }

    @Override
    public String toString() {
        return "BetweenRestriction [attribute=" + getAttribute() + ", begin=" + getBegin() + ", end=" + getEnd() + "]";
    }
}
