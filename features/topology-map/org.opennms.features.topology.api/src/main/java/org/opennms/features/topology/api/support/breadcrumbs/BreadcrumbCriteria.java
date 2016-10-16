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
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.api.support.breadcrumbs;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.opennms.features.topology.api.topo.Criteria;

import com.google.common.collect.Lists;

/**
 * Criteria to store breadcrumbs in order allow Navigation backwards.
 *
 * @author mvrueden
 */
public class BreadcrumbCriteria extends Criteria {

    private List<Breadcrumb> breadcrumbs = Lists.newArrayList();

    public void setNewRoot(final Breadcrumb breadcrumb) {
        if (breadcrumbs.contains(breadcrumb)) {
            int index = breadcrumbs.indexOf(breadcrumb);
            breadcrumbs = breadcrumbs.subList(0, index + 1);
        } else {
            breadcrumbs.add(breadcrumb);
        }
    }

    public void clear() {
        breadcrumbs.clear();
    }

    public List<Breadcrumb> getBreadcrumbs() {
        return Collections.unmodifiableList(breadcrumbs);
    }

    public boolean isEmpty() {
        return breadcrumbs.isEmpty();
    }

    @Override
    public ElementType getType() {
        return ElementType.GRAPH;
    }

    @Override
    public String getNamespace() {
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(breadcrumbs);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof BreadcrumbCriteria) {
            BreadcrumbCriteria other = (BreadcrumbCriteria) obj;
            boolean equals = Objects.equals(breadcrumbs, other.breadcrumbs);
            return equals;
        }
        return false;
    }

}
