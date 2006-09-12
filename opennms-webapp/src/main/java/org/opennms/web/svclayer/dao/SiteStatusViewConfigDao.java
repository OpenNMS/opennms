package org.opennms.web.svclayer.dao;

import org.opennms.netmgt.config.siteStatusViews.Views;
import org.opennms.netmgt.config.siteStatusViews.View;

public interface SiteStatusViewConfigDao {
    
    Views getViews();
    
    View getView(String viewName);
    
    View getDefaultView();

}
