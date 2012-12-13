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

package org.opennms.features.topology.app.internal;

import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.topo.Connector;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.VertexRef;

import com.vaadin.data.Item;

public class TopoEdge implements Edge {
    
    public static final String SELECTED_PROPERTY = "selected";
	private final String m_key;
	private final Object m_itemId;
	private final Object m_sourceId;
	private final TopoVertex m_source;
	private final Object m_targetId;
	private final TopoVertex m_target;
	private final SimpleGraphContainer m_graphContainer;

	public TopoEdge(SimpleGraphContainer graphContainer, String key, Object itemId, Object sourceId, TopoVertex source, Object targetId, TopoVertex target) {; 
		m_graphContainer = graphContainer;
		m_key = key;
		m_itemId = itemId;
		m_sourceId = sourceId;
		m_source = source;
		m_targetId = targetId;
		m_target = target;
	}
	
	public SimpleGraphContainer getGraphContainer() {
		return m_graphContainer;
	}

	public Object getSourceId() {
		return m_sourceId;
	}

	public TopoVertex getSourceVertex(){
		return m_source;
	}
	
	public String getKey() {
		return m_key;
	}

	public Object getItemId() {
		return m_itemId;
	}
	
	public Object getTargetId() {
		return m_targetId;
	}
	
	public TopoVertex getTargetVertex(){
		return m_target;
	}
	
	@Override
	public String toString() {
	    return "Edge :: source: " + getSourceVertex() + " target: " + getTargetVertex();
	}

	public Item getItem() {
		return m_graphContainer.getEdgeContainer().getItem(m_itemId);
	}

    public String getTooltipText() {
        return getEdgeTooltipText(m_graphContainer, m_itemId);
    }
    
    private String getEdgeTooltipText(SimpleGraphContainer graphContainer,	Object edgeId) {
		Item item = graphContainer.getEdgeContainer().getItem(edgeId);
		if(item != null && item.getItemProperty("tooltipText") != null && item.getItemProperty("tooltipText").getValue() != null) {
            return (String) item.getItemProperty("tooltipText").getValue();
        }else {
            return getSourceVertex().getLabel() + " :: " + getTargetVertex().getLabel();
        }
	}

    private SelectionManager getSelectionManager() {
		return m_graphContainer.getSelectionManager();
	}
    
    
    public String getCssClass() {
    	return getSelectionManager().isEdgeRefSelected(this) ? getStyleName()+" selected" : getStyleName(); 
    }

	public String getStyleName() {
		return "path";
	}

	@Override
	public String getNamespace() {
		return "nodes";
	}

	@Override
	public String getId() {
		return getKey();
	}

	@Override
	public String getLabel() {
		return getSourceVertex().getLabel() + " :: " + getTargetVertex().getLabel();
	}

	@Override
	public Connector getSource() {
		return new Connector() {

			@Override
			public String getNamespace() {
				return TopoEdge.this.getNamespace();
			}

			@Override
			public String getId() {
				return TopoEdge.this.getId()+":source";
			}

			@Override
			public EdgeRef getEdge() {
				return TopoEdge.this;
			}

			@Override
			public VertexRef getVertex() {
				return TopoEdge.this.getSourceVertex();
			}

		};
	}

	@Override
	public Connector getTarget() {
		return new Connector() {

			@Override
			public String getNamespace() {
				return TopoEdge.this.getNamespace();
			}

			@Override
			public String getId() {
				return TopoEdge.this.getId()+":target";
			}

			@Override
			public EdgeRef getEdge() {
				return TopoEdge.this;
			}

			@Override
			public VertexRef getVertex() {
				return TopoEdge.this.getTargetVertex();
			}

		};
	}

}