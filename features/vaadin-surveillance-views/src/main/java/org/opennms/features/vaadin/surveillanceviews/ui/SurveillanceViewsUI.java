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
package org.opennms.features.vaadin.surveillanceviews.ui;

import org.opennms.features.vaadin.surveillanceviews.config.SurveillanceViewProvider;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.opennms.netmgt.config.surveillanceViews.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * The surveillance view application's "main" class
 *
 * @author Christian Pape
 */
@SuppressWarnings("serial")
@Theme("opennms")
@Title("OpenNMS Surveillance Views")
@JavaScript({
    "theme://../opennms/assets/surveillance-init.vaadin.js"
})
public class SurveillanceViewsUI extends UI {
    /**
     * the logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(SurveillanceViewsUI.class);
    /**
     * the surveillane view service
     */
    private SurveillanceViewService m_surveillanceViewService;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void init(VaadinRequest request) {
        /**
         * Force the reload of the configuration
         */
        SurveillanceViewProvider.getInstance().load();

        /**
         * create a layout
         */
        VerticalLayout rootLayout = new VerticalLayout();
        rootLayout.setSpacing(true);

        /**
         * check query parameters for viewName, dashboard
         */
        String viewName = request.getParameter("viewName");
        boolean dashboard = request.getParameter("dashboard") != null && "true".equals(request.getParameter("dashboard"));

        /**
         * retrieve the username
         */
        String username = request.getRemoteUser();

        /**
         * now select the right view
         */
        View view;

        if (viewName == null) {
            view = m_surveillanceViewService.selectDefaultViewForUsername(username);
        } else {
            view = SurveillanceViewProvider.getInstance().getView(viewName);
        }

        /**
         * set the poll interval
         */
        setPollInterval(1000);

        /**
         * check for dashboard role
         */
        boolean isDashboardRole = true;

        SecurityContext context = SecurityContextHolder.getContext();

        if ((context != null) && !(context.toString().contains(org.opennms.web.api.Authentication.ROLE_DASHBOARD))) {
            isDashboardRole = false;
        }

        LOG.debug("User {} is in dashboard role? {}", username, isDashboardRole);

        /**
         * now construct the surveillance view/dashboard
         */
        rootLayout.addComponent(new SurveillanceView(view, m_surveillanceViewService, dashboard, !isDashboardRole));

        setContent(rootLayout);

        getUI().getPage().getJavaScript().execute("function receiveMessage(event){\n" +
                "if(event.origin !== window.location.origin){ return; }\n" +
                "\n" +
                "event.source.postMessage( (document.getElementById('surveillance-window').offsetHeight + 17) + 'px', window.location.origin )\n" +
                "}\n" +
                "window.addEventListener(\"message\", receiveMessage, false);");
    }

    /**
     * Method for setting the {@link org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService} instance to be used
     *
     * @param surveillanceViewService the instance to be used
     */
    public void setSurveillanceViewService(SurveillanceViewService surveillanceViewService) {
        this.m_surveillanceViewService = surveillanceViewService;
    }
}
