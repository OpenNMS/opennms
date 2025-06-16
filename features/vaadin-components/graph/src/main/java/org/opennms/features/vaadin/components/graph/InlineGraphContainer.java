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

import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;

/**
 * Graph container that allows graphs to be rendered using different graphing
 * engines.
 *
 * See js/graph.js for details.
 *
 * @author jwhite
 * @author fooker
 */
@JavaScript({
    "theme://../opennms/assets/inline-graphcontainer_connector.vaadin.js"
})
public class InlineGraphContainer extends AbstractJavaScriptComponent {
    private static final long serialVersionUID = 2L;

    public InlineGraphContainer() {
        // make sure state gets initialized
        getState();
    }

    public void setBaseHref(String baseHref) {
        getState().baseHref = baseHref;
    }

    @Override
    protected GraphContainerState getState() {
        return (GraphContainerState) super.getState();
    }
}
