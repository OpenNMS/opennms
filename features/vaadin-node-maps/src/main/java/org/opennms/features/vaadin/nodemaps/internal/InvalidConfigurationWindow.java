/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.nodemaps.internal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Strings;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class InvalidConfigurationWindow extends Window {

    public InvalidConfigurationWindow(NodeMapConfiguration nodeMapConfiguration) {
        setModal(true);
        setResizable(false);
        setWidth(500, Unit.PIXELS);
        setHeight(250, Unit.PIXELS);
        setCaption("Configuration Error");

        final Button btnClose = new Button("OK", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });
        final String errorMessage = getErrorLabel(nodeMapConfiguration);
        final Label label = new Label(errorMessage, ContentMode.HTML);

        VerticalLayout rootLayout = new VerticalLayout();
        rootLayout.setSpacing(true);
        rootLayout.setMargin(true);
        rootLayout.addComponent(label);
        rootLayout.addComponent(btnClose);
        rootLayout.setExpandRatio(label, 1.0f);
        rootLayout.setComponentAlignment(btnClose, Alignment.BOTTOM_RIGHT);

        setContent(rootLayout);
        center();
    }

    public void open() {
        UI.getCurrent().addWindow(this);
    }

    private String getErrorLabel(NodeMapConfiguration nodeMapConfiguration) {
        try {
            String errorTemplate = IOUtils.toString(getClass().getResourceAsStream("/error-configuration.txt"), StandardCharsets.UTF_8);
            String problems = "";
            if (Strings.isNullOrEmpty(nodeMapConfiguration.getTileServerUrl())) {
                problems = "<li>No <i>gwt.openlayers.url</i> property defined</li>";
            }
            if (Strings.isNullOrEmpty(nodeMapConfiguration.getTileLayerAttribution())) {
                problems += "<li>No <i>gwt.openlayers.options.attribution</i> property defined</li>";
            }
            String errorMessage = errorTemplate.replace("{problems}", problems);
            return errorMessage;
        } catch (IOException e) {
            return "Error determining error message: " + e.getMessage();
        }
    }
}
