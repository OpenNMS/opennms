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

import java.util.List;

import org.opennms.features.topology.api.topo.Criteria;

/**
 * Factory to create {@link org.opennms.features.topology.api.GraphContainer.Callback}s.
 *
 * @author Markus von Rüden
 */
public abstract class Callbacks {

    private Callbacks() {

    }

    public static GraphContainer.Callback applyDefaultSemanticZoomLevel() {
        return (graphContainer, graphProvider) -> graphContainer.setSemanticZoomLevel(graphProvider.getDefaults().getSemanticZoomLevel());
    }

    public static GraphContainer.Callback clearCriteria() {
        return (graphContainer, graphProvider) -> graphContainer.clearCriteria();
    }

    public static GraphContainer.Callback applyDefaultCriteria() {
        return (graphContainer, graphProvider) -> {
            List<Criteria> defaultCriteriaList = graphProvider.getDefaults().getCriteria();
            if (defaultCriteriaList != null) {
                defaultCriteriaList.forEach(graphContainer::addCriteria);
            }
        };
    }

    public static GraphContainer.Callback redoLayout() {
        return (graphContainer, graphProvider) -> graphContainer.redoLayout();
    }

    public static GraphContainer.Callback applyDefaults() {
        return wrap(
                clearCriteria(),
                applyDefaultSemanticZoomLevel(),
                applyDefaultCriteria()
        );
    }

    // Convert a link of callbacks to a single callback.
    public static GraphContainer.Callback wrap(GraphContainer.Callback... callbacks) {
        if (callbacks != null) {
            return (graphContainer, graphProvider) -> {
              for (GraphContainer.Callback eachCallback : callbacks) {
                  eachCallback.callback(graphContainer, graphProvider);
              }
            };
        }
        return (graphContainer, graphProvider) -> {};
    }
}
