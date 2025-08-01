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
package org.opennms.features.vaadin.components.graph;

import java.util.Date;

import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;

/**
 * Graph container that allows pre-fabricated graphs to be
 * rendered using different graphing engines.
 *
 * See js/graph.js for details.
 *
 * @author jwhite
 */
@JavaScript({
    "theme://../opennms/assets/graphcontainer_connector.vaadin.js"
})
public class GraphContainer extends AbstractJavaScriptComponent {
    private static final long serialVersionUID = 2L;

    public GraphContainer(final String graphName, final String resourceId) {
        final GraphContainerState state = getState();
        state.graphName = graphName;
        state.resourceId = resourceId;

        setWidth(100, Unit.PERCENTAGE);
    }

    public void setBaseHref(String baseHref) {
        getState().baseHref = baseHref;
    }

    public void setStart(Date start) {
        getState().start = start.getTime();
    }

    public void setEnd(Date end) {
        getState().end = end.getTime();
    }

    public void setWidthRatio(Double widthRatio) {
        getState().widthRatio = widthRatio;
    }

    public void setHeightRatio(Double heightRatio) {
        getState().heightRatio = heightRatio;
    }

    public void setTitle(String title) {
        getState().title = title;
    }

    @Override
    protected GraphContainerState getState() {
        return (GraphContainerState) super.getState();
    }
}
