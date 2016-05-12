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

package org.opennms.features.topology.app.internal.ui.icons;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class IconSelectionDialog extends Window implements Window.CloseListener, Button.ClickListener {

    /** The action to execute when the ok/cancel button is pressed. */
    public interface Action {
        void execute(IconSelectionDialog window);
    }

    private final Button okButton;
    private final Button cancelButton;
    private final IconSelectionComponent iconSelectionComponent;
    private boolean okPressed;
    private Action okAction;

    public IconSelectionDialog(String selectedIconId) {
        setCaption("Change Icon");
        setModal(true);
        setResizable(false);
        setClosable(false);
        setWidth(700, Unit.PIXELS);
        setHeight(600, Sizeable.Unit.PIXELS);
        addCloseListener(this);

        okButton = new Button("Ok", this);
        okButton.setId("iconSelectionDialog.button.ok");
        cancelButton = new Button("Cancel", this);
        cancelButton.setId("iconSelectionDialog.button.cancel");

        iconSelectionComponent = new IconSelectionComponent(getElementsToShow(), selectedIconId);
        VerticalLayout iconLayout = new VerticalLayout();
        iconLayout.addStyleName("icon-selection-component");
        iconLayout.setSpacing(true);
        iconLayout.setSizeFull();
        iconLayout.addComponent(iconSelectionComponent);

        final HorizontalLayout buttonLayout = new HorizontalLayout(okButton, cancelButton);
        buttonLayout.setSpacing(true);

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.setSizeFull();
        mainLayout.addComponent(iconLayout);
        mainLayout.addComponent(buttonLayout);
        mainLayout.setExpandRatio(iconLayout, 1);
        mainLayout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_RIGHT);

        setContent(mainLayout);
        center();
    }

    public IconSelectionDialog withOkAction(Action okAction) {
        this.okAction = okAction;
        return this;
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        if (!Strings.isNullOrEmpty(iconSelectionComponent.getState().getSelectedIconId())) {
            okPressed = event.getSource() == okButton;
            close();
        }
    }

    @Override
    public void windowClose(CloseEvent e) {
        if (okPressed && okAction != null) {
            okAction.execute(this);
        }
    }

    public String getSelectedIcon() {
        return iconSelectionComponent.getState().getSelectedIconId();
    }

    public void open() {
        UI.getCurrent().addWindow(this);
    }

    private List<String> getElementsToShow() {
        final String opennmsHomeStr = System.getProperty("opennms.home", "");
        final String propertiesFilename = "org.opennms.features.topology.app.icons.list";
        final Path configPath = Paths.get(opennmsHomeStr, "etc", propertiesFilename);
        try {
            List<String> strings = Files.readAllLines(configPath);
            return strings.stream()
                    .filter(s -> s != null && s.trim().length() > 0 && !s.trim().startsWith("#"))
                    .collect(Collectors.toList());

        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).error("Could not read icon config file {}", configPath, e);
        }
        return Lists.newArrayList();
    }

}