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

package org.opennms.netmgt.vaadin.core;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
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
