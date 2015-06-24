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

package org.opennms.features.topology.api.support;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickListener;

public class DialogWindow extends Window implements ClickListener {
    private static final long serialVersionUID = -2235453349601991807L;
    final Button okayButton = new Button("OK", this);
    final UI parentWindow;
    
     public DialogWindow(final UI parentWindow, final String title, final String description) {
        this.parentWindow = parentWindow;
        setCaption(title);
        setImmediate(true);
        setResizable(false);
        setModal(true);
        setWidth(400, Unit.PIXELS);
        setContent(createContent(description));
        parentWindow.addWindow(this);
    }
     
     private Layout createMainArea(final String description) {
         HorizontalLayout layout = new HorizontalLayout();
         layout.setSpacing(true);
         layout.setMargin(true);
         layout.setWidth(100, Unit.PERCENTAGE);
         Label label = new Label(description, ContentMode.PREFORMATTED);
         label.setWidth(100, Unit.PERCENTAGE);
         layout.addComponent(label);
         return layout;
     }
     
     private Layout createContent(final String description) {
         VerticalLayout content = new VerticalLayout();
         content.setWidth(100, Unit.PERCENTAGE);
         
         Layout footer = createFooter();
         Layout mainArea = createMainArea(description);
         
         content.addComponent(mainArea);
         content.addComponent(footer);
         content.setExpandRatio(mainArea, 1);
         return content;
     }
     
     private Layout createFooter() {
         HorizontalLayout footer = new HorizontalLayout();
         footer.setSpacing(true);
         footer.setMargin(true);
         footer.setWidth(100, Unit.PERCENTAGE);
         footer.addComponent(okayButton);
         footer.setComponentAlignment(okayButton, Alignment.BOTTOM_RIGHT);
         return footer;
     }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        if (event.getButton() == okayButton) parentWindow.removeWindow(DialogWindow.this);
    }

}
