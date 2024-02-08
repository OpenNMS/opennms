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
package org.opennms.web.controller.newsfeed;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * A controller that handles the News Feed panel.
 */
public class NewsFeedController extends AbstractController implements InitializingBean {
    private String m_successView;

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final String viewName = getSuccessView() != null ? getSuccessView() : "newsfeed/newsFeedPanel";
        final String shouldDisplayStr = System.getProperty("opennms.newsFeedPanel.show", "true");
        final boolean shouldDisplay = Boolean.parseBoolean(shouldDisplayStr);

        ModelAndView modelAndView = new ModelAndView(viewName);
        modelAndView.addObject("shouldDisplay", shouldDisplay);

        return modelAndView;
    }

    private String getSuccessView() {
        return m_successView;
    }

    public void setSuccessView(String successView) {
        m_successView = successView;
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(m_successView, "property successView must be set");
    }
}
