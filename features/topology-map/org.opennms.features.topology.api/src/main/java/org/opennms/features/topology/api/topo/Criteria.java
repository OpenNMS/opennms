/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
