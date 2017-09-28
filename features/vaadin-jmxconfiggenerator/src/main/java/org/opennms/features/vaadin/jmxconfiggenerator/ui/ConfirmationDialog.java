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

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Created by mvrueden on 21/07/15.
 */
public class ConfirmationDialog extends Window implements Window.CloseListener, Button.ClickListener {

    /** The action to execute when the ok/cancel button is pressed. */
    public interface Action {
        void execute(ConfirmationDialog window);
    }

    private Action okAction;
    private Action cancelAction;
    private final VerticalLayout layout = new VerticalLayout();
    private final Label label = new Label("", ContentMode.HTML);
    private final Button cancelButton;
    private final Button okButton;

    private boolean okPressed;

    public ConfirmationDialog() {
        this("Continue?", "Do you really want to continue?");
    }

    public ConfirmationDialog(String caption, String description) {
        setCaption(caption);
        setModal(true);
        setResizable(false);
        setClosable(false);
        setWidth(400, Unit.PIXELS);
        setHeight(200, Unit.PIXELS);
        addCloseListener(this);

        okButton = UIHelper.createButton("ok", null, null, this);
        cancelButton = UIHelper.createButton("cancel", "cancels the current action.", null, this);
        label.setDescription(description);

        final HorizontalLayout buttonLayout = new HorizontalLayout(okButton, cancelButton);
        buttonLayout.setSpacing(true);

        layout.setSpacing(true);
        layout.setMargin(true);
        layout.addComponent(label);
        layout.addComponent(buttonLayout);
        layout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_RIGHT);

        setContent(layout);
        center();
    }

    public ConfirmationDialog withCaption(String caption) {
        setCaption(caption);
        return this;
    }

    public ConfirmationDialog withDescription(String description) {
        label.setValue(description);
        return this;
    }

    public ConfirmationDialog withOkAction(Action okAction) {
        this.okAction = okAction;
        return this;
    }

    public ConfirmationDialog withCancelAction(Action cancelAction) {
        this.cancelAction = cancelAction;
        return this;
    }

    public void open() {
        getUI().getCurrent().addWindow(this);
    }

    public ConfirmationDialog withOkLabel(String okLabel) {
        okButton.setCaption(okLabel);
        return this;
    }

    public ConfirmationDialog withCancelLabel(String cancelLabel) {
        cancelButton.setCaption(cancelLabel);
        return this;
    }


    @Override
    public void windowClose(CloseEvent e) {
        if (okPressed) {
            if (okAction != null) {
                okAction.execute(this);
            }
        } else {
            if (cancelAction != null) {
                cancelAction.execute(this);
            }
        }
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        okPressed = event.getSource() == okButton;
        close();
    }
}
