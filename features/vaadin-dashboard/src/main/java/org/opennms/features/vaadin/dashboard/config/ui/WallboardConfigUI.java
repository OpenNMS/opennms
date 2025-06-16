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
package org.opennms.features.vaadin.dashboard.config.ui;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import org.opennms.features.vaadin.dashboard.config.DashletSelector;
import org.opennms.features.vaadin.dashboard.model.DashletSelectorAccess;

/**
 * The Vaadin application used for wallboard configuration. This application instantiates a {@link WallboardConfigView} instance.
 *
 * @author Christian Pape
 */
@SuppressWarnings("serial")
@Theme("dashboard")
@Title("OpenNMS Ops Board")
public class WallboardConfigUI extends UI implements DashletSelectorAccess {
    /**
     * The {@link DashletSelector} instance used for querying configuration data
     */
    private DashletSelector m_dashletSelector;

    /**
     * Default constructor for instantiating a new instance
     */
    public WallboardConfigUI() {
    }

    /**
     * Method for setting the required {@link DashletSelector}.
     *
     * @param dashletSelector the {@link DashletSelector} to be set
     */
    public void setDashletSelector(DashletSelector dashletSelector) {
        this.m_dashletSelector = dashletSelector;
    }

    /**
     * Method for setting up the application.
     *
     * @param request the {@link VaadinRequest} object
     */
    @Override
    protected void init(VaadinRequest request) {
        setContent(new WallboardConfigView(m_dashletSelector));
    }

    /**
     * Returns the associated {@link DashletSelector} instance.
     *
     * @return the {@link DashletSelector} instance
     */
    public DashletSelector getDashletSelector() {
        return m_dashletSelector;
    }

    /**
     * Method for displaying notification for the user.
     *
     * @param message     the message to be displayed
     * @param description the description of this message
     */
    public void notifyMessage(String message, String description) {
        final Notification notification = new Notification("Message", Notification.Type.TRAY_NOTIFICATION);
        notification.setCaption(message);
        notification.setDescription(description);
        notification.setDelayMsec(1000);
        if (getUI() != null) {
            if (getPage() != null) {
                notification.show(getUI().getPage());
            }
        }
    }
}
