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
package org.opennms.features.vaadin.surveillanceviews.ui.dashboard;

import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.themes.BaseTheme;

/**
 * This abstract class represents a table based component that is refreshable by an associated surveillance view.
 *
 * @author Christian Pape
 */
public abstract class SurveillanceViewDetailTable extends Table implements SurveillanceViewDetail {
    /**
     * the surveillance view service
     */
    private SurveillanceViewService m_surveillanceViewService;
    /**
     * field for storing whether links are enabled in this component
     */
    protected boolean m_enabled;

    /**
     * Constructor to creating new instances.
     *
     * @param title                   the title for this table
     * @param surveillanceViewService the surveillance service to be used
     * @param enabled                 are links enabled?
     */
    public SurveillanceViewDetailTable(String title, SurveillanceViewService surveillanceViewService, boolean enabled) {
        super(title);

        m_surveillanceViewService = surveillanceViewService;
        m_enabled = enabled;

        setSizeFull();
        setPageLength(5);
    }

    /**
     * Returns the associated surveillance view service.
     *
     * @return the surveillance view service
     */
    protected SurveillanceViewService getSurveillanceViewService() {
        return m_surveillanceViewService;
    }

    /**
     * Returns the image severity layout for the given content.
     *
     * @param content the content
     * @return the label with the applied style
     */
    protected HorizontalLayout getImageSeverityLayout(String content) {
        HorizontalLayout horizontalLayout = new HorizontalLayout();

        Label placeholder = new Label();
        placeholder.addStyleName("placeholder");
        horizontalLayout.addComponent(placeholder);

        Label contentLabel = new Label(content);
        contentLabel.addStyleName("content");

        horizontalLayout.addComponent(contentLabel);

        return horizontalLayout;
    }

    /**
     * Returns a clickable glyph icon with the given {@link com.vaadin.ui.Button.ClickListener}.
     *
     * @param icon     the icon to be used
     * @param clickListener the listener
     * @return the button instance
     */
    protected Button getClickableIcon(FontAwesome icon, Button.ClickListener clickListener) {
        Button button = new Button(icon.getHtml());
        button.setHtmlContentAllowed(true);
        button.setStyleName(BaseTheme.BUTTON_LINK);
        button.addStyleName("icon");
        button.setEnabled(m_enabled);
        button.addClickListener(clickListener);
        return button;
    }
}
