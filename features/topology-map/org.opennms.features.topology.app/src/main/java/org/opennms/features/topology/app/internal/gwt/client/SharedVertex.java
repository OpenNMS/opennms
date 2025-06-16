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

public class SharedVertex implements Serializable {

    private static final long serialVersionUID = 1L;

    private String m_key;
    private int m_initialX;
    private int m_initialY;
    private int m_x;
    private int m_y;
    private boolean m_selected;
    private String m_status;
    private String m_iconUrl;
    private String m_svgIconId;
    private String m_label;
    private String m_tooltipText;
    private String m_statusCount = "0";
    private String m_styleName = "vertex";
    private boolean m_targets;
    private int m_edgePathOffset;

    /**
     * @return the statusCount
     */
    public String getStatusCount() {
        return m_statusCount;
    }

    /**
     * @param statusCount the statusCount to set
     */
    public void setStatusCount(String statusCount) {
        this.m_statusCount = statusCount;
    }

    public void setKey(String key) {
        m_key = key;
    }

    public void setInitialX(int x) {
        m_initialX = x;
    }

    public void setInitialY(int y) {
        m_initialY = y;
    }

    public void setX(int x) {
        m_x = x;
    }

    public void setY(int y) {
        m_y = y;
    }

    public void setSelected(boolean selected) {
        m_selected = selected;
    }

    public void setLabel(String label) {
        m_label = label;
    }

    public void setTooltipText(String tooltipText) {
        m_tooltipText = tooltipText;
    }

    public String getStatus() {
        return m_status;
    }

    public void setStatus(String status) {
        m_status = status;
    }

    public String getTooltipText() {
        return m_tooltipText;
    }

    public String getKey() {
        return m_key;
    }

    public int getInitialX() {
        return m_initialX;
    }

    public int getInitialY() {
        return m_initialY;
    }

    public int getX() {
        return m_x;
    }

    public int getY() {
        return m_y;
    }

    public boolean isSelected() {
        return m_selected;
    }

    public String getIconUrl() {
        return m_iconUrl;
    }

    public String getLabel() {
        return m_label;
    }

    public boolean getSelected() {
        return m_selected;
    }

    public String getSVGIconId() {
        return m_svgIconId;
    }

    public void setSVGIconId(String m_svgIconId) {
        this.m_svgIconId = m_svgIconId;
    }

    public void setStyleName(String CSSStyle) {
        m_styleName = CSSStyle;
    }

    public String getStyleName() {
        return m_styleName;
    }

    public boolean isTargets() {
        return m_targets;
    }

    public void setTargets(boolean targets) {
        m_targets = targets;
    }

    public void setEdgePathOffset(int edgePathOffset) {
        m_edgePathOffset = edgePathOffset;
    }

    public int getEdgePathOffset() {
        return m_edgePathOffset;
    }
}
