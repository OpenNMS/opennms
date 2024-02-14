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
package org.opennms.features.topology.app.internal.gwt.client;

import com.google.gwt.core.client.GWT;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;

@Connect(org.opennms.features.topology.app.internal.ui.HudDisplay.class)
public class HudDisplayConnector extends AbstractComponentConnector {

    @Override
    protected VHudDisplay createWidget() {
        return GWT.create(VHudDisplay.class);
    }

    @Override
    public VHudDisplay getWidget() {
        return (VHudDisplay) super.getWidget();
    }

    @Override
    public HudDisplayState getState() {
        return (HudDisplayState)super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        HudDisplayState state = getState();

        VHudDisplay hudDisplay = getWidget();
        hudDisplay.setProviderLabel(state.getProvider());
        hudDisplay.setVertexFocus(state.getVertexFocusCount());
        hudDisplay.setEdgeFocus(state.getEdgeFocusCount());
        hudDisplay.setVertexContext(state.getVertexContextCount());
        hudDisplay.setEdgeContext(state.getEdgeContextCount());
        hudDisplay.setVertexSelection(state.getVertexSelectionCount());
        hudDisplay.setEdgeSelection(state.getEdgeSelectionCount());
        hudDisplay.setVertexTotal(state.getVertexTotalCount());
        hudDisplay.setEdgeTotal(state.getEdgeTotalCount());

    }

    public static native void debug(Object message) /*-{
        $wnd.console.debug(message);
    }-*/;
}
