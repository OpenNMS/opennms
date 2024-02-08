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

import com.google.gwt.user.client.ui.SuggestOracle;

public class SearchSuggestion implements Serializable, SuggestOracle.Suggestion {

    private static final long serialVersionUID = 1876970713330053849L;

    private String m_id;
    private String m_namespace;
    private String m_label;
    private boolean m_collapsible = false;
    private boolean m_collapsed = false;
    private boolean m_focused = false;

    private String m_query;

    public SearchSuggestion() {}

    public SearchSuggestion(String namespace, String id, String label) {
        setId(id);
        setNamespace(namespace);
        setLabel(label);
    }

    public final void setLabel(String label) {
        m_label = label;
    }

    public final String getLabel() {
        return m_label;
    }

    public final void setId(String id) {
        m_id = id;
    }

    public final String getId() {
        return m_id;
    }

    public final void setNamespace(String namespace) {
        m_namespace = namespace;
    }

    public final String getNamespace() {
        return m_namespace;
    }

    @Override
    public String getDisplayString() {
        String namespace = getNamespace();
        final String capitalized = namespace.substring(0, 1).toUpperCase() + namespace.substring(1);
        return "<div><b>" + capitalized + ": </b>" + getLabel() + "</div>";
    }

    @Override
    public String getReplacementString() {
        return getLabel();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;

        SearchSuggestion ref = (SearchSuggestion)obj;

        return getNamespace().equals(ref.getNamespace()) && getId().equals(ref.getId());

    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result
                + ((getNamespace() == null) ? 0 : getNamespace().hashCode());
        return result;
    }

	public final boolean isCollapsible() {
		return m_collapsible;
	}

	public final void setCollapsible(boolean collapsible) {
		this.m_collapsible = collapsible;
	}

	public final boolean isCollapsed() {
		return m_collapsed;
	}

	public final void setCollapsed(boolean collapsed) {
		this.m_collapsed = collapsed;
	}

    public void setFocused(boolean focused) {
        m_focused = focused;
    }

    public boolean isFocused() {
        return m_focused;
    }

    @Override
    public String toString() {
        return "SearchSuggestion[namespace:" + m_namespace + ",id:" + m_id + ",label:" + m_label + ",focused:" + m_focused + ",collapsible:" + m_collapsible + ",collapsed:" + m_collapsed + "]";
    }
    
    public String getQuery() {
        return m_query;
    }

    public void setQuery(String query) {
        m_query = query;
    }
}
