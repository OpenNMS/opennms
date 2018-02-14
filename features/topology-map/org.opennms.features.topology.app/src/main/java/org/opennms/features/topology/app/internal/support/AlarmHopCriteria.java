/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.GroupRef;
import org.opennms.features.topology.api.topo.RefComparator;
import org.opennms.features.topology.api.topo.SearchCriteria;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.AlarmProvider;
import org.opennms.features.topology.app.internal.AlarmSearchProvider.AlarmSearchResult;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;

/**
 * This <Criteria> implementation supports the users selection of search results from an IPLIKE query
 * in the topology UI.
 * 
 * @author <a href=mailto:david@opennms.org>David Hustace</a>
 * @author <a href=mailto:thedesloge@opennms.org>Donald Desloge</a>
 * @author <a href=mailto:seth@opennms.org>Seth Leger</a>
 *
 */
public class AlarmHopCriteria extends VertexHopCriteria implements SearchCriteria {

	public static final String NAMESPACE = "alarm";
	
	private boolean m_collapsed = false;
	private AlarmVertex m_collapsedVertex;
	
	private AlarmProvider alarmProvider;
    private AlarmSearchResult m_searchResult;

	@Override
	public String getSearchString() {
		return m_searchResult.getQuery();
	}

	public static class AlarmVertex extends AbstractVertex implements GroupRef {
		private Set<VertexRef> m_children = new HashSet<>();

        public AlarmVertex(String namespace, String id, String label) {
			super(namespace, id, label);
			setIconKey("group");
		}

		@Override
		public boolean isGroup() {
			return true;
		}

        @Override
        public Set<VertexRef> getChildren() {
            return m_children;
        }

        public void setChildren(Set<VertexRef> children) {
            m_children = children;
        }
    }

	public AlarmHopCriteria(AlarmSearchResult result, AlarmProvider alarmProvider) {
        super(result.getId(), result.getNodeLabel());
        m_collapsed = result.isCollapsed();
        m_searchResult = result;
        this.alarmProvider = Objects.requireNonNull(alarmProvider);

        String severityLabel = OnmsSeverity.get(result.getLabel()).getLabel();
        
        m_collapsedVertex = new AlarmVertex(NAMESPACE, severityLabel, "Alarms: "+severityLabel);
        m_collapsedVertex.setChildren(getVertices());
        setId(result.getId());
    }

	public AlarmSearchResult getSearchResult() {
	    return m_searchResult;
	}
	
	public void setSearchResult(AlarmSearchResult searchResult) {
	    m_searchResult = searchResult;
	}

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        Integer alarmId = getSearchResult().getAlarmId();
        result = prime * result + ((alarmId == null) ? 0 : alarmId.hashCode());
        result = prime * result
                + ((getNamespace() == null) ? 0 : getNamespace().hashCode());
        return result;
    }

	@Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;

        if (obj instanceof AlarmHopCriteria) {
            AlarmHopCriteria ref = (AlarmHopCriteria)obj;
			String refAlarmId = ref.getSearchResult().getId();
            String alarmId = this.getSearchResult().getId();
            String refNamespace = ref.getNamespace();
            String namespace = this.getNamespace();
            return refAlarmId.equals(alarmId) && refNamespace.equals(namespace);
        }
        
        return false;
    }

	@Override
	public Set<VertexRef> getVertices() {
		List<OnmsAlarm> alarms = findAlarms();
		return createVertices(alarms);
	}

    private Set<VertexRef> createVertices(List<OnmsAlarm> alarms) {
        
        Set<VertexRef> vertices = new TreeSet<VertexRef>(new RefComparator());
		for (OnmsAlarm alarm : alarms) {
			OnmsNode node = alarm.getNode();
			if (node == null) {
			    continue;
			}
			vertices.add(new DefaultVertexRef("nodes", String.valueOf(node.getId()), node.getLabel()));
		}
        return vertices;
    }

    private List<OnmsAlarm> findAlarms() {
        CriteriaBuilder bldr = new CriteriaBuilder(OnmsAlarm.class);
		
        String query = getSearchResult().getQuery();
        if (isSeverityQuery()) {
            bldr.eq("severity", OnmsSeverity.get(query));
		} else {
            bldr.eq("id", getSearchResult().getAlarmId());
		}

		return alarmProvider.findMatchingAlarms(bldr.toCriteria());
    }

	private boolean isSeverityQuery() {
	    return m_searchResult.isSeverityQuery();
    }

    @Override
	public boolean isCollapsed() {
		return m_collapsed;
	}

	@Override
	public void setCollapsed(boolean collapsed) {
		if (collapsed != isCollapsed()) {
			this.m_collapsed = collapsed;
			setDirty(true);
		}
	}

	@Override
	public Vertex getCollapsedRepresentation() {
		return m_collapsedVertex;
	}
	
}
