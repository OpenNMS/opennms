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

import com.vaadin.shared.AbstractComponentState;

public class HudDisplayState extends AbstractComponentState {
    static final long serialVersionUID = 1L;

    private int m_vertexFocusCount;
    private int m_edgeFocusCount;
    private int m_vertexContextCount;
    private int m_edgeContextCount;
    private int m_vertexSelectionCount;
    private int m_edgeSelectionCount;
    private int m_vertexTotalCount;
    private int m_edgeTotalCount;
    private String m_provider;

    public int getVertexFocusCount() {
        return m_vertexFocusCount;
    }

    public int getEdgeFocusCount() {
        return m_edgeFocusCount;
    }

    public int getVertexContextCount() {
        return m_vertexContextCount;
    }

    public int getEdgeContextCount() {
        return m_edgeContextCount;
    }

    public int getVertexSelectionCount() {
        return m_vertexSelectionCount;
    }

    public int getEdgeSelectionCount() {
        return m_edgeSelectionCount;
    }

    public int getVertexTotalCount() {
        return m_vertexTotalCount;
    }

    public int getEdgeTotalCount() {
        return m_edgeTotalCount;
    }

    public String getProvider() {
        return m_provider;
    }

    public void setProvider(String provider){
        m_provider = provider;
    }

    public void setVertexFocusCount(int vertexFocusCount) {
        this.m_vertexFocusCount = vertexFocusCount;
    }

    public void setEdgeFocusCount(int edgeFocusCount) {
        this.m_edgeFocusCount = edgeFocusCount;
    }

    public void setVertexContextCount(int vertexContextCount) {
        this.m_vertexContextCount = vertexContextCount;
    }

    public void setEdgeContextCount(int m_edgeContextCount) {
        this.m_edgeContextCount = m_edgeContextCount;
    }

    public void setVertexSelectionCount(int vertexSelectionCount) {
        this.m_vertexSelectionCount = vertexSelectionCount;
    }

    public void setEdgeSelectionCount(int edgeSelectionCount) {
        this.m_edgeSelectionCount = edgeSelectionCount;
    }

    public void setVertexTotalCount(int vertexTotalCount) {
        this.m_vertexTotalCount = vertexTotalCount;
    }

    public void setEdgeTotalCount(int edgeTotalCount) {
        this.m_edgeTotalCount = edgeTotalCount;
    }
}
