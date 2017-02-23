/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.support;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.HistoryOperation;
import org.opennms.features.topology.api.topo.Criteria;
import org.springframework.util.StringUtils;

public class CategoryHopCriteriaHistoryOperation implements HistoryOperation {

	private static final String DELIMITER = ",";
	private CategoryHopCriteriaFactory m_categoryHopCriteriaFactory;

	public void setCategoryHopCriteriaFactory(CategoryHopCriteriaFactory categoryHopCriteriaFactory) {
		this.m_categoryHopCriteriaFactory = categoryHopCriteriaFactory;
	}

	@Override
	public void applyHistory(GraphContainer container, Map<String, String> settings) {
		// Remove any existing CategoryHopCriteria
		Set<CategoryHopCriteria> oldCriteria = Criteria.getCriteriaForGraphContainer(container, CategoryHopCriteria.class);
		for (CategoryHopCriteria criterium : oldCriteria) {
			container.removeCriteria(criterium);
		}

		String setting = settings.get(getClass().getName());
		if (setting != null && setting.length() > 0) {
			for (String categorySettings : setting.split(DELIMITER)) {
                String categoryName = categorySettings.split("\\|")[0];
                String collapsed = categorySettings.split("\\|")[1];
                CategoryHopCriteria hopCriteria = m_categoryHopCriteriaFactory.getCriteria(categoryName);
                hopCriteria.setCollapsed(collapsed.toLowerCase().equals("true"));
                container.addCriteria(hopCriteria);
			}
		}
	}

	@Override
	public Map<String, String> createHistory(GraphContainer graphContainer) {
		Set<CategoryHopCriteria> criteria = Criteria.getCriteriaForGraphContainer(graphContainer, CategoryHopCriteria.class);
		if (criteria.size() > 0) {
			Set<String> retval = new TreeSet<String>();
			for (CategoryHopCriteria criterium : criteria) {
				retval.add(criterium.getLabel() + "|" + criterium.isCollapsed());
			}
			return Collections.singletonMap(getClass().getName(), StringUtils.collectionToDelimitedString(retval, DELIMITER));
		} else {
			return Collections.emptyMap();
		}
	}
}
