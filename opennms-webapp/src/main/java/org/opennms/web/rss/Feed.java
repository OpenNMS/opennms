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

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;

/**
 * <p>Feed interface.</p>
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @version $Id: $
 * @since 1.8.1
 */
public interface Feed {

    /**
     * <p>getUrlBase</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getUrlBase();
    /**
     * <p>setUrlBase</p>
     *
     * @param base a {@link java.lang.String} object.
     */
    void setUrlBase(String base);
    
    /**
     * <p>getFeedType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getFeedType();
    /**
     * <p>setFeedType</p>
     *
     * @param type a {@link java.lang.String} object.
     */
    void setFeedType(String type);
    
    /**
     * <p>getMaxEntries</p>
     *
     * @return a int.
     */
    int getMaxEntries();
    /**
     * <p>setMaxEntries</p>
     *
     * @param maxEntries a int.
     */
    void setMaxEntries(int maxEntries);

    /**
     * <p>getRequest</p>
     *
     * @return a {@link javax.servlet.ServletRequest} object.
     */
    ServletRequest getRequest();
    /**
     * <p>setRequest</p>
     *
     * @param request a {@link javax.servlet.ServletRequest} object.
     */
    void setRequest(ServletRequest request);
    
    ServletContext getServletContext();
    
    void setServletContext(ServletContext context);
    
    /**
     * <p>render</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String render();
}
