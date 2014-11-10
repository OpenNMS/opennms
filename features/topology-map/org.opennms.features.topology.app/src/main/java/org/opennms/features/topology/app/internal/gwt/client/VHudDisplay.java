/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;

public class VHudDisplay extends Composite {

    Grid m_grid;
    Label m_providerLabel = new Label();
    Label m_vertexFocusLabel = new Label();
    Label m_edgeFocusLabel = new Label();
    Label m_vertexSelectionLabel = new Label();
    Label m_edgeSelectionLabel = new Label();
    Label m_vertexContextLabel = new Label();
    Label m_edgeContextLabel = new Label();
    Label m_vertexTotalLabel = new Label();
    Label m_edgeTotalLabel = new Label();

    public VHudDisplay(){

        m_grid = new Grid(5, 3);
        m_grid.setStyleName("topoHudDisplay");

        m_grid.setWidget(0,0, m_providerLabel);
        Label vertexLabel = new Label("Vertices");
        m_grid.setWidget(0,1, vertexLabel);
        Label edgeLabel = new Label("Edges");
        m_grid.setWidget(0,2, edgeLabel);

        Label focusLabel = new Label("Focus");
        m_grid.setWidget(1, 0, focusLabel);
        m_grid.setWidget(1, 1, m_vertexFocusLabel);
        m_grid.setWidget(1, 2, m_edgeFocusLabel);

        Label selectionLabel = new Label("Selection");
        m_grid.setWidget(2, 0, selectionLabel);
        m_grid.setWidget(2, 1, m_vertexSelectionLabel);
        m_grid.setWidget(2, 2, m_edgeSelectionLabel);

        Label contextLabel = new Label("Context");
        m_grid.setWidget(3, 0, contextLabel);
        m_grid.setWidget(3, 1, m_vertexContextLabel);
        m_grid.setWidget(3, 2, m_edgeContextLabel);

        Label totalLabel = new Label("Total");
        m_grid.setWidget(4, 0, totalLabel);
        m_grid.setWidget(4, 1, m_vertexTotalLabel);
        m_grid.setWidget(4, 2, m_edgeTotalLabel);

        initWidget(m_grid);

        m_providerLabel.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
        vertexLabel.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
        edgeLabel.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
        focusLabel.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
        selectionLabel.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
        contextLabel.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
        totalLabel.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
    }

    public void setProviderLabel(String provider){
        m_providerLabel.setText("Provider: " + provider);
    }

    public void setVertexFocus(int count){
        m_vertexFocusLabel.setText(String.valueOf(count));
    }

    public void setEdgeFocus(int count){
        m_edgeFocusLabel.setText(String.valueOf(count));
    }

    public void setVertexContext(int count){
        m_vertexContextLabel.setText(String.valueOf(count));
    }

    public void setEdgeContext(int count){
        m_edgeContextLabel.setText(String.valueOf(count));
    }

    public void setVertexSelection(int count){
        m_vertexSelectionLabel.setText(String.valueOf(count));
    }

    public void setEdgeSelection(int count){
        m_edgeSelectionLabel.setText(String.valueOf(count));
    }

    public void setVertexTotal(int count){
        m_vertexTotalLabel.setText(String.valueOf(count));
    }

    public void setEdgeTotal(int count) {
        m_edgeTotalLabel.setText(String.valueOf(count));
    }


}
