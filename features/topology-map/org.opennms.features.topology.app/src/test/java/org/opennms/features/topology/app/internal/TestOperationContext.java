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
package org.opennms.features.topology.app.internal;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;

public class TestOperationContext implements OperationContext {

    private final GraphContainer m_graphContainer;
    private final UI m_window;

    public TestOperationContext(GraphContainer graphContainer) {
        m_graphContainer = graphContainer;
        m_window = new UI() {
            @Override
            protected void init(VaadinRequest request) {
            }};
    }

    @Override
    public UI getMainWindow() {
        return m_window;
    }

    @Override
    public GraphContainer getGraphContainer() {
        return m_graphContainer;
    }

    @Override
    public DisplayLocation getDisplayLocation() {
        return DisplayLocation.MENUBAR;
    }

}
