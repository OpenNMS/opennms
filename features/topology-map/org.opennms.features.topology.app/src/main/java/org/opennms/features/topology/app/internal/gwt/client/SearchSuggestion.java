/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.app.internal.gwt.client;

import com.google.gwt.user.client.ui.SuggestOracle;

import java.io.Serializable;

public class SearchSuggestion implements SuggestOracle.Suggestion {


    String m_label;
    String m_vertexKey;
    String m_namespace;

    public void setLabel(String label){
        m_label = label;
    }

    public String getLabel(){
        return m_label;
    }

    public void setVertexKey(String key) {
        m_vertexKey = key;
    }

    public String getVertexKey(){
        return m_vertexKey;
    }

    public void setNamespace(String namespace) {
        m_namespace = namespace;
    }

    public String getNamespace() {
        return m_namespace;
    }

    @Override
    public String getDisplayString() {
        return "<div><b>" + getNamespace() + ": </b>" + getLabel() + "</div>";
    }

    @Override
    public String getReplacementString() {
        return m_label;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;

        SearchSuggestion ref = (SearchSuggestion)obj;

        return getNamespace().equals(ref.getNamespace()) && getVertexKey().equals(ref.getVertexKey());

    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getVertexKey() == null) ? 0 : getVertexKey().hashCode());
        result = prime * result
                + ((getNamespace() == null) ? 0 : getNamespace().hashCode());
        return result;
    }
}
