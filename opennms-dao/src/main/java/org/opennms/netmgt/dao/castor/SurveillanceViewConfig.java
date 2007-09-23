/**
 * 
 */
package org.opennms.netmgt.dao.castor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.config.surveillanceViews.SurveillanceViewConfiguration;
import org.opennms.netmgt.config.surveillanceViews.View;

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

    @SuppressWarnings("unchecked")
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
