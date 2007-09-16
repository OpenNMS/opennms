package org.opennms.web.rss;

import javax.servlet.ServletRequest;

public interface Feed {

    public String getUrlBase();
    public void setUrlBase(String base);
    
    public String getFeedType();
    public void setFeedType(String type);
    
    public int getMaxEntries();
    public void setMaxEntries(int maxEntries);

    public ServletRequest getRequest();
    public void setRequest(ServletRequest request);
    
    public String render();
    
}
