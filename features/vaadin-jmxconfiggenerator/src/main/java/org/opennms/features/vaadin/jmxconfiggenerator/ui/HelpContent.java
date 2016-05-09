/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.jmxconfiggenerator.ui;

import java.util.Objects;

import org.opennms.netmgt.vaadin.core.UIHelper;

import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.VerticalLayout;

public class HelpContent implements PopupView.Content {

    private final VerticalLayout layout;

    public HelpContent(UiState uiState) {
        Objects.requireNonNull(uiState);
        if (!uiState.hasUi()) {
            throw new IllegalArgumentException("The provided uiState " + uiState + " does not have a ui.");
        }

        String content = UIHelper.loadContentFromFile(getClass(), String.format("/help/%s.html", uiState.name()));
        content = content.replaceAll("%title%", uiState.getDescription());

        layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setWidth(400, Sizeable.Unit.PIXELS);
        layout.setSpacing(true);
        layout.addComponent(new Label(content, ContentMode.HTML));
    }

    @Override
    public final Component getPopupComponent() {
        return layout;
    }

    @Override
    public final String getMinimizedValueAsHTML() {
        return "";
    }
}
