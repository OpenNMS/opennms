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
		// Remove any existing {@link CategoryHopCriteria}
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
