/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: September 16, 2007
 *
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
package org.opennms.web.rss;

import java.io.StringWriter;

import javax.servlet.ServletRequest;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * 
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 */
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
        title.replaceAll("<.*?>", "");
        return title;
    }
    
    protected ThreadCategory log() {
        return ThreadCategory.getInstance();
    }

}
