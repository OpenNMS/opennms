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
import java.util.HashMap;
import java.util.Map;

public class SharedEdge implements Serializable {

    private String m_key;
    private String m_sourceKey;
    private String m_targetKey;
    private boolean m_selected;
    private String cssClass;
    private String m_tooltipText;

    private String m_status;
    private Map<String, String> additionalStyling = new HashMap<>();

    public void setKey(String key) {
        m_key = key;
    }

    public void setSourceKey(String sourceKey) {
        m_sourceKey = sourceKey;
    }

    public void setTargetKey(String targetKey) {
        m_targetKey = targetKey;
    }

    public void setSelected(boolean selected) {
        m_selected = selected;
    }

    public void setCssClass(String styleName) {
        this.cssClass = styleName;
    }

    public String getCssClass() {
        return cssClass;
    }

    public void setTooltipText(String tooltipText) {
        m_tooltipText = tooltipText;
    }

    public String getKey() {
        return m_key;
    }

    public String getSourceKey() {
        return m_sourceKey;
    }

    public String getTargetKey() {
        return m_targetKey;
    }

    public boolean isSelected() {
        return m_selected;
    }

    public String getTooltipText() {
        return m_tooltipText;
    }

    public boolean getSelected() {
        return m_selected;
    }

    public String getStatus() { return m_status; }

    public void setStatus(String status) { m_status = status; }

    public void setAdditionalStyling(Map<String, String> additionalStyling) {
        this.additionalStyling = additionalStyling;
    }

    public Map<String, String> getAdditionalStyling() {
        return additionalStyling;
    }
}
