package org.opennms.web.rss;

import java.io.StringWriter;

import javax.servlet.ServletRequest;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedOutput;

public class AbstractFeed implements Feed {
    protected int m_maxEntries = 20;
    protected String m_feedType = "rss_2.0";
    protected String m_urlBase = "";
    protected ServletRequest m_servletRequest;
    
    public AbstractFeed() {
    }
    
    public AbstractFeed(String feedType) {
        setFeedType(feedType);
    }

    public String getUrlBase() {
        return m_urlBase;
    }
    
    public void setUrlBase(String urlBase) {
        m_urlBase = urlBase;
    }
    
    public String getFeedType() {
        return m_feedType;
    }
    
    public void setFeedType(String feedType) {
        m_feedType = feedType;
    }
    
    public int getMaxEntries() {
        return m_maxEntries;
    }
    
    public void setMaxEntries(int maxEntries) {
        m_maxEntries = maxEntries;
    }
    
    public ServletRequest getRequest() {
        return m_servletRequest;
    }
    
    public void setRequest(ServletRequest request) {
        m_servletRequest = request;
    }
    
    public SyndFeed getFeed() {
        return new SyndFeedImpl();
    }

    public String render() {
        SyndFeed feed = this.getFeed();
        feed.setFeedType(this.getFeedType());
        
        SyndFeedOutput output = new SyndFeedOutput();
        try {
            StringWriter writer = new StringWriter();
            output.output(feed, writer);
            return writer.toString();
        } catch (Exception e) {
            log().warn("unable to render feed", e);
            return "";
        }
    }

    protected String sanitizeTitle(String title) {
        title.replaceAll("<[^>]*>", "");
        return title;
    }
    
    protected Category log() {
        return ThreadCategory.getInstance();
    }

}
