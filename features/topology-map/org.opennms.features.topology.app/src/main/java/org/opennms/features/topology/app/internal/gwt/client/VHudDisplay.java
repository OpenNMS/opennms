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
