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

package org.opennms.features.topology.api;

import java.util.Collection;
import java.util.Set;

import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.osgi.VaadinApplicationContext;

import com.vaadin.data.Property;

public interface GraphContainer extends DisplayState {

    interface ChangeListener {
        void graphChanged(GraphContainer graphContainer);
    }

    /**
     * Callback which is invoked after the {@link GraphProvider} has been changed.
     *
     * @see #selectTopologyProvider(GraphProvider, Callback...)
     */
    interface Callback {
        /**
         * is invoked after the {@link GraphProvider} has changed.
         *
         * @param graphContainer The container
         * @param graphProvider The new graph provider
         */
        void callback(GraphContainer graphContainer, GraphProvider graphProvider);
    }

    void setMetaTopologyId(String metaTopologyId);

    String getMetaTopologyId();

    TopologyServiceClient getTopologyServiceClient();

    Criteria[] getCriteria();

    void addCriteria(Criteria criteria);

    void removeCriteria(Criteria criteria);

    // clears all criteria which are currently sets
    void clearCriteria();

    /**
     * Selects the specified {@link GraphProvider}.
     *
     * @param graphProvider the provider to select.
     * @param callbacks callbacks to invoke after the provider has been selected (e.g. apply semantic zoom level, etc)
     */
    void selectTopologyProvider(GraphProvider graphProvider, Callback... callbacks);

    void addChangeListener(ChangeListener listener);

    void removeChangeListener(ChangeListener listener);

    SelectionManager getSelectionManager();

    void setSelectionManager(SelectionManager selectionManager);

    Graph getGraph();

    void setApplicationContext(VaadinApplicationContext applicationContext);

    AutoRefreshSupport getAutoRefreshSupport();

    boolean hasAutoRefreshSupport();

    Collection<VertexRef> getVertexRefForest(Collection<VertexRef> vertexRefs);

    void setSelectedNamespace(String namespace);

    MapViewManager getMapViewManager();

    Property<Double> getScaleProperty();

    // TODO move to another location. This should not be stored here! (maybe VaadinApplicationContext is the right place)
    String getSessionId();

    VaadinApplicationContext getApplicationContext();

    void setDirty(boolean dirty);
    
    void fireGraphChanged();

    /**
     * Allows queriing the {@link GraphContainer} for specific types of criteria.
     *
     * @param criteriaType The type to look for. May not be null.
     * @param <T> The criteria class.
     * @return All criteria assigned to this {@link GraphContainer} which are of the same type (or a sub type) of <code>criteriaType</code>.
     */
    <T extends Criteria> Set<T> findCriteria(Class<T> criteriaType);

    /**
     * Does the same as {@link #findCriteria(Class)}, but only returns one Criteria. If multiple criteria for the same
     * type are found, the first one is returned. No exception is thrown in that case.
     *
     * @param criteriaType The type to look for.
     * @param <T> The criteria class.
     * @return The first found criteria, or null if none is found.
     */
    <T extends Criteria> T findSingleCriteria(Class<T> criteriaType);

    IconManager getIconManager();

    void setIconManager(IconManager iconManager);

    void saveLayout();
}
