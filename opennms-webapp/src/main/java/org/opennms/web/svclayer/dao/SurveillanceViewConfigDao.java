package org.opennms.web.svclayer.dao;

import org.opennms.netmgt.config.surveillanceViews.View;
import org.opennms.netmgt.config.surveillanceViews.Views;

public interface SurveillanceViewConfigDao {
    
    Views getViews();
    
    View getView(String viewName);

    View getDefaultView();

}
