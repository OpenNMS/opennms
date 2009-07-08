/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.dao.castor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.config.surveillanceViews.SurveillanceViewConfiguration;
import org.opennms.netmgt.config.surveillanceViews.View;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class SurveillanceViewConfig {
    private SurveillanceViewConfiguration m_config;
    private Map<String, View> m_viewsMap;
    
    public SurveillanceViewConfig(SurveillanceViewConfiguration config) {
        m_config = config;
        createViewsMap();
    }
    
    private void createViewsMap() {
        List<View> viewList = getViewList();
        m_viewsMap = new HashMap<String, View>(viewList.size());
        for (View view : viewList) {
            m_viewsMap.put(view.getName(), view);
        }
    }

    private List<View> getViewList() {
        return m_config.getViews().getViewCollection();
    }

    public SurveillanceViewConfiguration getConfig() {
        return m_config;
    }

    public Map<String, View> getViewsMap() {
        return m_viewsMap;
    }
}
