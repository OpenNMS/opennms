/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.vaadin.surveillanceviews.ui.dashboard;

import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.BaseTheme;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;

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
     * @param glyphIcon     the icon to be used
     * @param clickListener the listener
     * @return the button instance
     */
    protected Button getClickableIcon(String glyphIcon, Button.ClickListener clickListener) {
        Button button = new Button("<span class=\"" + glyphIcon + "\" aria-hidden=\"true\"></span>");
        button.setHtmlContentAllowed(true);
        button.setStyleName(BaseTheme.BUTTON_LINK);
        button.addStyleName("icon");
        button.setEnabled(m_enabled);
        button.addClickListener(clickListener);
        return button;
    }
}
