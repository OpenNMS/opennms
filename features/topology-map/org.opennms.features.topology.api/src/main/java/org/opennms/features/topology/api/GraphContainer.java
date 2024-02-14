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
package org.opennms.features.topology.api;

import java.util.Collection;
import java.util.Set;

import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.osgi.VaadinApplicationContext;

import com.vaadin.v7.data.Property;

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
