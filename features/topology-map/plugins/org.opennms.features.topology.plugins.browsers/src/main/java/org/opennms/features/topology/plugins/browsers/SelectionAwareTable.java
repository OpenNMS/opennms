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

package org.opennms.features.topology.plugins.browsers;

import com.vaadin.ui.Table;
import org.opennms.features.topology.api.SelectionListener;
import org.opennms.features.topology.api.SelectionNotifier;
import org.opennms.features.topology.api.VerticesUpdateManager;
import org.opennms.features.topology.api.osgi.EventConsumer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class SelectionAwareTable extends Table implements VerticesUpdateManager.VerticesUpdateListener {

	private static final long serialVersionUID = 2761774077365441249L;

	private final OnmsDaoContainer<?,? extends Serializable> m_container;
	private final Set<SelectionNotifier> m_selectionNotifiers = new CopyOnWriteArraySet<SelectionNotifier>();
	private List<String> nonCollapsibleColumns = new ArrayList<String>();

	/**
	 *  Leave OnmsDaoContainer without generics; the Aries blueprint code cannot match up
	 *  the arguments if you put the generic types in.
	 */
	public SelectionAwareTable(String caption, OnmsDaoContainer container) {
		super(caption, container);
		m_container = container;
	}

	/**
	 * Call this method before any of the {@link SelectionNotifier} methods to ensure
	 * that the {@link SelectionListener} instances are registered with all of the
	 * {@link ColumnGenerator} classes that also implement {@link SelectionNotifier}.
	 */
	public void setColumnGenerators(@SuppressWarnings("unchecked") Map generators) {
		for (Object key : generators.keySet()) {
			super.addGeneratedColumn(key, (ColumnGenerator)generators.get(key));
			// If any of the column generators are {@link SelectionNotifier} instances,
			// then register this component as a listener for events that they generate.
			try {
				m_selectionNotifiers.add((SelectionNotifier)generators.get(key));
			} catch (ClassCastException e) {}
		}
	}

	/**
	 * Call this method before any of the {@link SelectionNotifier} methods to ensure
	 * that the {@link SelectionListener} instances are registered with all of the
	 * {@link ColumnGenerator} classes that also implement {@link SelectionNotifier}.
	 */
	@Override
	public void setCellStyleGenerator(CellStyleGenerator generator) {
		super.setCellStyleGenerator(generator);
	}

	@Override
	public String toString() {
		Object value = getValue();
		if (value == null) {
			return null;
		} else {
			return value.toString();
		}
	}
	
	/**
	 * Sets the non collapsbile columns.
	 * @param nonCollapsibleColumns
	 */
	public void setNonCollapsibleColumns(List<String> nonCollapsibleColumns) {
	    // set all elements to collapsible
	    for (Object eachPropertyId : m_container.getContainerPropertyIds()) {
	        setColumnCollapsible(eachPropertyId,  true);
	    }
	    
	    // set new value
	    if (nonCollapsibleColumns == null) nonCollapsibleColumns = new ArrayList<String>();
        this.nonCollapsibleColumns = nonCollapsibleColumns;
        
        // set non collapsible
        for (Object eachPropertyId : this.nonCollapsibleColumns) {
            setColumnCollapsible(eachPropertyId,  false);
        }
    }

    @Override
    @EventConsumer
    public void verticesUpdated(VerticesUpdateManager.VerticesUpdateEvent event) {
        m_container.verticesUpdated(event);
    }
}
