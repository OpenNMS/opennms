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
package org.opennms.netmgt.vaadin.core;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.v7.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class InfoDialog extends Window {

    private final VerticalLayout layout = new VerticalLayout();
    private final Label label = new Label("");
    private final Button okButton;

    public InfoDialog(String caption, String description) {
        setCaption(caption);
        setModal(true);
        setResizable(false);
        setClosable(false);
        setWidth(400, Unit.PIXELS);
        setHeight(200, Unit.PIXELS);

        okButton = UIHelper.createButton("ok", null, null, event -> InfoDialog.this.close());
        okButton.setId("infoDialog.button.ok");
        label.setValue(description);

        final HorizontalLayout buttonLayout = new HorizontalLayout(okButton);
        buttonLayout.setSpacing(true);

        layout.setSpacing(true);
        layout.setMargin(true);
        layout.setSizeFull();
        layout.addComponent(label);
        layout.addComponent(buttonLayout);
        layout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_RIGHT);

        setContent(layout);
        center();
    }

    public void open() {
        UI.getCurrent().addWindow(this);
    }

}
