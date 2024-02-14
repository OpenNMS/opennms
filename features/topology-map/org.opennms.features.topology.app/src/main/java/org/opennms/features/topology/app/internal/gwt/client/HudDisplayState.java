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
