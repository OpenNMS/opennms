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
			Set<String> retval = new TreeSet<String>();
			for (NCSHopCriteria criterium : criteria) {
				retval.add(criterium.getId());
			}
			return Collections.singletonMap(getClass().getName(), StringUtils.collectionToDelimitedString(retval, DELIMITER));
		} else {
			return Collections.emptyMap();
		}
	}
}
