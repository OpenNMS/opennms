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

package org.opennms.features.topology.app.internal;

import java.util.Collection;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.SelectionContext;
import org.opennms.features.topology.api.SelectionListener;
import org.opennms.features.topology.api.topo.VertexRef;

import com.vaadin.ui.Tree;

@SuppressWarnings({"serial"})
public class VertexSelectionTree extends Tree implements SelectionListener {

	private final String m_title;
    private final GraphContainer m_graphContainer;

    public VertexSelectionTree(String title, GraphContainer graphContainer) {
        super(null, new GCFilterableContainer(graphContainer));
        m_title = title;
        
        m_graphContainer = graphContainer;
        
        this.addValueChangeListener(new ValueChangeListener() {
            
            @Override
            public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {

            	@SuppressWarnings("unchecked")
				Collection<VertexRef> refs = (Collection<VertexRef>)event.getProperty().getValue();
            	
            	Collection<VertexRef> vertices = m_graphContainer.getVertexRefForest(refs);
            	m_graphContainer.getSelectionManager().setSelectedVertexRefs(vertices);
            	
            }
        });

    }

    /**
     * When a user clicks on a vertex or edge in the UI, update the selection in the tree view.
     */
    @Override
    public void selectionChanged(SelectionContext selectionContext) {
        setValue(selectionContext.getSelectedVertexRefs());
    }

    @Override
    public GCFilterableContainer getContainerDataSource() {
        return (GCFilterableContainer)super.getContainerDataSource();
    }

    public String getTitle() { return m_title; }

    @Override
    public String toString() {
        Object value = getValue();
        if (value == null) {
            return null;
        } else {
            return value.toString();
        }
    }
}
