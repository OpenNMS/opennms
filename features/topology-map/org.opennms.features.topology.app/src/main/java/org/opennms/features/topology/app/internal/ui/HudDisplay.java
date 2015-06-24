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
