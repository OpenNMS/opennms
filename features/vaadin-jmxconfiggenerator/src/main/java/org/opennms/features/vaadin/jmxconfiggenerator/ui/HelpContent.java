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
package org.opennms.features.vaadin.jmxconfiggenerator.ui;

import java.util.Objects;

import org.opennms.netmgt.vaadin.core.UIHelper;

import com.vaadin.server.Sizeable;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.v7.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.v7.ui.VerticalLayout;

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
