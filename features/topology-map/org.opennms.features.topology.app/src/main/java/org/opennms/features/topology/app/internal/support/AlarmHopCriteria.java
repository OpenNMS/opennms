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
package org.opennms.features.topology.app.internal.support;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.topology.api.support.hops.VertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractCollapsibleVertex;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
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

	public static class AlarmVertex extends AbstractCollapsibleVertex {
        public AlarmVertex(String id, String label) {
			super(NAMESPACE, id, label);
			setIconKey("group");
		}
    }

	public AlarmHopCriteria(AlarmSearchResult result, AlarmProvider alarmProvider) {
        super(result.getId(), result.getNodeLabel());
        m_collapsed = result.isCollapsed();
        m_searchResult = result;
        this.alarmProvider = Objects.requireNonNull(alarmProvider);

        String severityLabel = OnmsSeverity.get(result.getLabel()).getLabel();
        
        m_collapsedVertex = new AlarmVertex(severityLabel, "Alarms: "+severityLabel);
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
