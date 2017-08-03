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

package org.opennms.features.topology.plugins.ncs;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.HistoryOperation;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.plugins.ncs.NCSSearchProvider.NCSHopCriteria;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;
import org.springframework.util.StringUtils;

public class NCSHopCriteriaHistoryOperation implements HistoryOperation {

	private static final String DELIMITER = ",";

	private NCSComponentRepository m_ncsComponentRepository;
	private NCSEdgeProvider m_ncsEdgeProvider;

	public void setNcsComponentRepository(NCSComponentRepository ncsComponentRepository) {
		m_ncsComponentRepository = ncsComponentRepository;
	}

	public void setNcsEdgeProvider(NCSEdgeProvider ncsEdgeProvider) {
		m_ncsEdgeProvider = ncsEdgeProvider;
	}

	@Override
	public void applyHistory(GraphContainer container, Map<String, String> settings) {
		// Remove any existing {@link NCSHopCriteria}
		Set<NCSHopCriteria> oldCriteria = Criteria.getCriteriaForGraphContainer(container, NCSHopCriteria.class);
		for (NCSHopCriteria criterium : oldCriteria) {
			container.removeCriteria(criterium);
		}

		String setting = settings.get(getClass().getName());
		if (setting != null && setting.length() > 0) {
			for (String idString : setting.split(DELIMITER)) {
				Long id = Long.parseLong(idString);
				Criteria criteria = NCSEdgeProvider.createCriteria(Collections.singletonList(id));
				container.addCriteria(new NCSHopCriteria(idString, NCSSearchProvider.getVertexRefsForEdges(m_ncsEdgeProvider, criteria), m_ncsComponentRepository.get(id).getName()));
			}
		}
	}

	@Override
	public Map<String, String> createHistory(GraphContainer graphContainer) {
		Set<NCSHopCriteria> criteria = Criteria.getCriteriaForGraphContainer(graphContainer, NCSHopCriteria.class);
		if (criteria.size() > 0) {
			Set<String> retval = new TreeSet<>();
			for (NCSHopCriteria criterium : criteria) {
				retval.add(criterium.getId());
			}
			return Collections.singletonMap(getClass().getName(), StringUtils.collectionToDelimitedString(retval, DELIMITER));
		} else {
			return Collections.emptyMap();
		}
	}
}
