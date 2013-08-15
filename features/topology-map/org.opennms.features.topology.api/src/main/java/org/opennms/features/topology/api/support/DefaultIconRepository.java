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

package org.opennms.features.topology.api.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.IconRepository;

public class DefaultIconRepository implements IconRepository {

    private Map<String, String> m_iconMap = Collections.emptyMap();
    private List<String> m_svgDefs = new ArrayList<String>();

    @Override
    public boolean contains(String type) {
        return m_iconMap.containsKey(type);
    }
    
    public void setIconMap(Map<String, String> icons) {
        m_iconMap = icons;
    }

    public void setSVGDefs(List<String> svgDefs){
        m_svgDefs = svgDefs;
    }

    @Override
    public String getSVGIconId(String type) {
        return m_iconMap.get(type);
    }

    @Override
    public List<String> getSVGIconFiles() {
        return m_svgDefs;
    }

}
