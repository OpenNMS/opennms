/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created April 8, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.dao.castor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.config.surveillanceViews.SurveillanceViewConfiguration;
import org.opennms.netmgt.config.surveillanceViews.View;

/**
 * <p>SurveillanceViewConfig class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class SurveillanceViewConfig {
    private SurveillanceViewConfiguration m_config;
    private Map<String, View> m_viewsMap;
    
    /**
     * <p>Constructor for SurveillanceViewConfig.</p>
     *
     * @param config a {@link org.opennms.netmgt.config.surveillanceViews.SurveillanceViewConfiguration} object.
     */
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

    /**
     * <p>getConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.surveillanceViews.SurveillanceViewConfiguration} object.
     */
    public SurveillanceViewConfiguration getConfig() {
        return m_config;
    }

    /**
     * <p>getViewsMap</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, View> getViewsMap() {
        return m_viewsMap;
    }
}
