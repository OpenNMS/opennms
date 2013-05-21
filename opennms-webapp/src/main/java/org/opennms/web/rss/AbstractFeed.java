/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.rss;

import java.io.StringWriter;

import javax.servlet.ServletRequest;

import org.opennms.core.utils.ThreadCategory;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * <p>AbstractFeed class.</p>
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class AbstractFeed implements Feed {
    protected int m_maxEntries = 20;
    protected String m_feedType = "rss_2.0";
    protected String m_urlBase = "";
    protected ServletRequest m_servletRequest;
    
    /**
     * <p>Constructor for AbstractFeed.</p>
     */
    public AbstractFeed() {
    }
    
    /**
     * <p>Constructor for AbstractFeed.</p>
     *
     * @param feedType a {@link java.lang.String} object.
     */
    public AbstractFeed(String feedType) {
        setFeedType(feedType);
    }

    /**
     * <p>getUrlBase</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getUrlBase() {
        return m_urlBase;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setUrlBase(String urlBase) {
        m_urlBase = urlBase;
    }
    
    /**
     * <p>getFeedType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getFeedType() {
        return m_feedType;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setFeedType(String feedType) {
        m_feedType = feedType;
    }
    
    /**
     * <p>getMaxEntries</p>
     *
     * @return a int.
     */
    @Override
    public int getMaxEntries() {
        return m_maxEntries;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setMaxEntries(int maxEntries) {
        m_maxEntries = maxEntries;
    }
    
    /**
     * <p>getRequest</p>
     *
     * @return a {@link javax.servlet.ServletRequest} object.
     */
    @Override
    public ServletRequest getRequest() {
        return m_servletRequest;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setRequest(ServletRequest request) {
        m_servletRequest = request;
    }
    
    /**
     * <p>getFeed</p>
     *
     * @return a {@link com.sun.syndication.feed.synd.SyndFeed} object.
     */
    public SyndFeed getFeed() {
        return new SyndFeedImpl();
    }

    /**
     * <p>render</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String render() {
        SyndFeed feed = this.getFeed();
        feed.setFeedType(this.getFeedType());
        
        SyndFeedOutput output = new SyndFeedOutput();
        try {
            StringWriter writer = new StringWriter();
            output.output(feed, writer);
            return writer.toString();
        } catch (Throwable e) {
            log().warn("unable to render feed", e);
            return "";
        }
    }

    /**
     * <p>sanitizeTitle</p>
     *
     * @param title a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    protected String sanitizeTitle(String title) {
        title.replaceAll("<.*?>", "");
        return title;
    }
    
    /**
     * <p>log</p>
     *
     * @return a {@link org.opennms.core.utils.ThreadCategory} object.
     */
    protected ThreadCategory log() {
        return ThreadCategory.getInstance();
    }

}
