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
package org.opennms.web.rss;

import java.io.StringWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedOutput;

/**
 * <p>AbstractFeed class.</p>
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class AbstractFeed implements Feed {
	
	private static final Logger LOG = LoggerFactory.getLogger(AbstractFeed.class);

    protected int m_maxEntries = 20;
    protected String m_feedType = "rss_2.0";
    protected String m_urlBase = "";
    protected ServletRequest m_servletRequest;

    private ServletContext m_servletContext;
    
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
    public final String getUrlBase() {
        return m_urlBase;
    }
    
    /** {@inheritDoc} */
    @Override
    public final void setUrlBase(String urlBase) {
        m_urlBase = urlBase;
    }
    
    /**
     * <p>getFeedType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public final String getFeedType() {
        return m_feedType;
    }
    
    /** {@inheritDoc} */
    @Override
    public final void setFeedType(String feedType) {
        m_feedType = feedType;
    }
    
    /**
     * <p>getMaxEntries</p>
     *
     * @return a int.
     */
    @Override
    public final int getMaxEntries() {
        return m_maxEntries;
    }
    
    /** {@inheritDoc} */
    @Override
    public final void setMaxEntries(int maxEntries) {
        m_maxEntries = maxEntries;
    }
    
    /**
     * <p>getRequest</p>
     *
     * @return a {@link javax.servlet.ServletRequest} object.
     */
    @Override
    public final ServletRequest getRequest() {
        return m_servletRequest;
    }
    
    /** {@inheritDoc} */
    @Override
    public final void setRequest(ServletRequest request) {
        m_servletRequest = request;
    }
    
    /** {@inheritDoc} */
    @Override
    public final ServletContext getServletContext() {
        return m_servletContext;
    }
    
    /** {@inheritDoc} */
    @Override
    public final void setServletContext(ServletContext context) {
        m_servletContext = context;
    }
    
    /**
     * <p>getFeed</p>
     *
     * @return a {@link com.rometools.rome.feed.synd.SyndFeed} object.
     */
    public abstract SyndFeed getFeed();

    /**
     * <p>render</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public final String render() {
        SyndFeed feed = this.getFeed();
        feed.setFeedType(this.getFeedType());
        feed.setAuthor("OpenNMS");
        
        SyndFeedOutput output = new SyndFeedOutput();
        try {
            StringWriter writer = new StringWriter();
            output.output(feed, writer);
            return writer.toString();
        } catch (Throwable e) {
            LOG.warn("unable to render feed", e);
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
        return title.replaceAll("<.*?>", "");
    }
}
