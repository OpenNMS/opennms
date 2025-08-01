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
package org.opennms.netmgt.dao.jaxb;

import java.util.Collections;
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
        return m_config.getViews();
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
        return Collections.unmodifiableMap(m_viewsMap);
    }
}
