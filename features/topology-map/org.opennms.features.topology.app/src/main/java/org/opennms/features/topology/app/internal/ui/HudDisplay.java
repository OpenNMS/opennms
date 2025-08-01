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
package org.opennms.features.topology.app.internal.ui;

import org.opennms.features.topology.app.internal.gwt.client.HudDisplayState;

import com.vaadin.ui.AbstractComponent;

public class HudDisplay extends AbstractComponent {

    public void setVertexFocusCount(int count){
        getState().setVertexFocusCount(count);
    }

    public void setVertexSelectionCount(int count){
        getState().setVertexSelectionCount(count);
    }

    public void setVertexContextCount(int count){
        getState().setVertexContextCount(count);
    }

    public void setVertexTotalCount(int count){
        getState().setVertexTotalCount(count);
    }

    public void setEdgeFocusCount(int count){
        getState().setEdgeFocusCount(count);
    }

    public void setEdgeSelectionCount(int count){
        getState().setEdgeSelectionCount(count);
    }

    public void setEdgeContextCount(int count){
        getState().setEdgeContextCount(count);
    }

    public void setEdgeTotalCount(int count){
        getState().setEdgeTotalCount(count);
    }


    @Override
    protected HudDisplayState getState() {
        return (HudDisplayState) super.getState();
    }

    public void setProvider(String provider) {
        getState().setProvider(provider);
    }
}
