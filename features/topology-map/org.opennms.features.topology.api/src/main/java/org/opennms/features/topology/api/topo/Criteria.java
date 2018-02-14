/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api.topo;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.opennms.features.topology.api.GraphContainer;

/**
 * The interface is extended by plugin developers to allow the setting of criteria for their Providers
 * 
 * @author brozow
 *
 */
public abstract class Criteria {

	public static <T extends Criteria> T getSingleCriteriaForGraphContainer(GraphContainer graphContainer, Class<T> criteriaClass, boolean createIfAbsent) {
		Criteria[] criteria = graphContainer.getCriteria();
		if (criteria != null) {
			for (Criteria criterium : criteria) {
				try {
					T hopCriteria = criteriaClass.cast(criterium);
					return hopCriteria;
				} catch (ClassCastException e) {}
			}
		}

		if (createIfAbsent) {
			T hopCriteria;
			try {
				hopCriteria = criteriaClass.newInstance();
			} catch (InstantiationException e) {
				throw new IllegalArgumentException("Cannot create instance of " + criteriaClass.getName() + " with empty constructor", e);
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException("Cannot create instance of " + criteriaClass.getName() + " with empty constructor", e);
			}
			graphContainer.addCriteria(hopCriteria);
			return hopCriteria;
		} else {
			return null;
		}
	}

	public static <T extends Criteria> Set<T> getCriteriaForGraphContainer(GraphContainer graphContainer, Class<T> criteriaClass) {
		Set<T> retval = new HashSet<>();
		Criteria[] criteria = graphContainer.getCriteria();
		if (criteria != null) {
			for (Criteria criterium : criteria) {
				try {
					T hopCriteria = criteriaClass.cast(criterium);
					retval.add(hopCriteria);
				} catch (ClassCastException e) {}
			}
		}
		return retval;
	}

    public enum ElementType { GRAPH, VERTEX, EDGE;}

    private volatile AtomicBoolean m_criteriaDirty = new AtomicBoolean(Boolean.TRUE);

	/**
	 * This criteria applies to only providers of the indicated type
	 */
	public abstract ElementType getType();

	/**
	 * This criteria only applies to providers for this namespace
	 */
	public abstract String getNamespace();

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    public void resetDirty() {
        setDirty(false);
    }

    public boolean isDirty() {
        synchronized (m_criteriaDirty) {
            return m_criteriaDirty.get();
        }
    }

    protected void setDirty(boolean isDirty) {
        synchronized (m_criteriaDirty) {
            m_criteriaDirty.set(isDirty);
        }
    }
}
