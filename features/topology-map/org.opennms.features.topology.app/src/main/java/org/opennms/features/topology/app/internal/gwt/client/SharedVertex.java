/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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
