/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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
