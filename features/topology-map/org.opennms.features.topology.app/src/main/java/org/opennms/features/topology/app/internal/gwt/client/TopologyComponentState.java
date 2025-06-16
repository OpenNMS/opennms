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

import java.io.Serializable;
import java.util.List;

import com.vaadin.shared.AbstractComponentState;

public class TopologyComponentState extends AbstractComponentState implements Serializable {

    private int m_boundX;
    private int m_boundY;
    private int m_boundWidth;
    private int m_boundHeight;
    private int m_physicalWidth;
    private int m_physicalHeight;
    private String m_activeTool;
    private List<SharedVertex> m_vertices;
    private List<SharedEdge> m_edges;
    private List<String> m_svgDefs;
    private boolean m_highlightFocus = false;
    private String m_lastUpdateTime = "";

    public void setBoundX(int boundX) {
        m_boundX = boundX;
    }

    public void setBoundY(int boundY) {
        m_boundY = boundY;
    }

    public void setBoundWidth(int width) {
        m_boundWidth = width;
    }

    public void setBoundHeight(int height) {
        m_boundHeight = height;
    }

    public void setActiveTool(String activeTool) {
        m_activeTool = activeTool;
    }

    public int getBoundX() {
        return m_boundX;
    }

    public int getBoundY() {
        return m_boundY;
    }

    public int getBoundWidth() {
        return m_boundWidth;
    }

    public int getBoundHeight() {
        return m_boundHeight;
    }

    public String getActiveTool() {
        return m_activeTool;
    }

    public List<SharedVertex> getVertices() {
        return m_vertices;
    }

    public void setVertices(List<SharedVertex> vertices) {
        m_vertices = vertices;
    }

    public List<SharedEdge> getEdges() {
        return m_edges;
    }

    public void setEdges(List<SharedEdge> edges) {
        m_edges = edges;
    }

    public void setSVGDefFiles(List<String> svgFiles){
        m_svgDefs = svgFiles;
    }

    public List<String> getSVGDefFiles() {
        return m_svgDefs;
    }

    public void setHighlightFocus(boolean bool) {
        m_highlightFocus = bool;
    }

    public boolean isHighlightFocus(){
        return m_highlightFocus;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        m_lastUpdateTime = lastUpdateTime;
    }

    public String getLastUpdateTime() {
        return m_lastUpdateTime;
    }

    public int getPhysicalWidth() {
        return m_physicalWidth;
    }

    public int getPhysicalHeight() {
        return m_physicalHeight;
    }

    public void setPhysicalWidth(int width) {
        m_physicalWidth = width;
    }

    public void setPhysicalHeight(int height) {
        m_physicalHeight = height;
    }

    public void setPhysicalDimensions(int width, int height) {
        setPhysicalHeight(height);
        setPhysicalWidth(width);
    }
}
