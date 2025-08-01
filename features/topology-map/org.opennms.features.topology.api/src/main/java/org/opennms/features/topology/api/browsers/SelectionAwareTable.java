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
package org.opennms.features.topology.api.browsers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.SelectionListener;
import org.opennms.features.topology.api.SelectionNotifier;
import org.opennms.features.topology.api.TopologyServiceClient;
import org.opennms.features.topology.api.VerticesUpdateManager;
import org.opennms.osgi.EventConsumer;
import org.opennms.osgi.EventProxy;
import org.opennms.osgi.EventProxyAware;

import com.vaadin.v7.ui.Table;

public class SelectionAwareTable extends Table implements VerticesUpdateManager.VerticesUpdateListener, EventProxyAware, SelectionChangedListener, GraphContainer.ChangeListener {

	private static final long serialVersionUID = 2761774077365441249L;

	private final OnmsVaadinContainer<?,? extends Serializable> m_container;
	private final Set<SelectionNotifier> m_selectionNotifiers = new CopyOnWriteArraySet<>();
	private EventProxy eventProxy;

    /**
     * Used to temporary disable the refreshing of the row cache.
     */
    private boolean m_disableRowCacheRefresh = false;

	/**
	 *  Leave OnmsVaadinContainer without generics; the Aries blueprint code cannot match up
	 *  the arguments if you put the generic types in.
	 */
	public SelectionAwareTable(String caption, OnmsVaadinContainer container) {
		super(caption, container);
		m_container = container;
	}

	/**
	 * Call this method before any of the {@link SelectionNotifier} methods to ensure
	 * that the {@link SelectionListener} instances are registered with all of the
	 * {@link ColumnGenerator} classes that also implement {@link SelectionNotifier}.
	 */
	public void setColumnGenerators(@SuppressWarnings("rawtypes") Map generators) {
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
	 * Sets the non collapsible columns.
	 *
	 * Temporarily disables row cache refreshing since every call to
	 * setColumnCollapsible() triggers one. The refresh is deferred
	 * until all columns have been processed.
	 *
	 * @param nonCollapsibleColumns
	 */
	public synchronized void setNonCollapsibleColumns(List<String> nonCollapsibleColumns) {
	    m_disableRowCacheRefresh = true;
	    try {
	        // set all elements to collapsible
	        for (Object eachPropertyId : m_container.getContainerPropertyIds()) {
	            setColumnCollapsible(eachPropertyId,  true);
	        }

	        // set new value
	        if (nonCollapsibleColumns == null) nonCollapsibleColumns = new ArrayList<>();

	        // set non collapsible
	        for (Object eachPropertyId : nonCollapsibleColumns) {
	            setColumnCollapsible(eachPropertyId,  false);
	        }
	    } finally {
	        m_disableRowCacheRefresh = false;
	    }
	    refreshRowCache();
    }

    @Override
    public void refreshRowCache() {
        if (m_disableRowCacheRefresh) {
            return;
        }
        super.refreshRowCache();
    }

    @Override
    @EventConsumer
    public void verticesUpdated(VerticesUpdateManager.VerticesUpdateEvent event) {
		if (isAttached()) {
			TopologyServiceClient source = event.getSource();
			if (event.getVertexRefs().isEmpty()) {
				selectionChanged(Selection.NONE);
			} else if (source.contributesTo(getContentType())) {
				SelectionChangedListener.Selection newSelection = source.getSelection(
						new ArrayList<>(event.getVertexRefs()),
						getContentType());
				selectionChanged(newSelection);
			}
		}
    }

	@Override
	public void selectionChanged(Selection newSelection) {
		if (isAttached()) {
			m_container.selectionChanged(newSelection);
		}
	}

	/**
     * Make sure that the OnmsVaadinContainer cache is reset.
     */
    @Override
    public void resetPageBuffer() {
        if (m_container != null && m_container.getCache() != null && m_container.getPage() != null) {
            m_container.getCache().reload(m_container.getPage());
        }
        super.resetPageBuffer();
    }

    @Override
    public void setEventProxy(EventProxy eventProxy) {
        this.eventProxy = eventProxy;

        // set EventProxy on all ColumnGenerators
        for (Object eachPropertyId : getContainerPropertyIds()) {
            ColumnGenerator columnGenerator = getColumnGenerator(eachPropertyId);
            if (columnGenerator != null && EventProxyAware.class.isAssignableFrom(columnGenerator.getClass())) {
                ((EventProxyAware) columnGenerator).setEventProxy(eventProxy);
            }
        }
    }

	@Override
	public void graphChanged(GraphContainer graphContainer) {
		if (isAttached()) {
			refreshRowCache();
		}
	}

	public ContentType getContentType() {
		if (m_container != null) {
			return m_container.getContentType();
		}
		return null;
	}

    protected EventProxy getEventProxy() {
		return Objects.requireNonNull(eventProxy, "EventProxy should not be null!");
    }
}
