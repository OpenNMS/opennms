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
