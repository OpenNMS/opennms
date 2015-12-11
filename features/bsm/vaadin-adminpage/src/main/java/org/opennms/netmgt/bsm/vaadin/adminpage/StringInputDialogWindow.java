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
 * http://www.gnu.org/licenses/
 *
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.vaadin.adminpage;

import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class StringInputDialogWindow extends Window {

    public interface Callback {
        void valueEntered(String value);
    }

    public StringInputDialogWindow(String windowTitle, String fieldName, Callback callback) {
        super(windowTitle);

        setModal(true);
        setClosable(false);
        setResizable(false);

        VerticalLayout verticalLayout = new VerticalLayout();

        final TextField inputField = new TextField(fieldName);

        inputField.setValue("");
        inputField.focus();
        inputField.selectAll();
        inputField.setImmediate(true);

        inputField.addValidator(new AbstractStringValidator("Input must not be empty") {
            @Override
            protected boolean isValidValue(String s) {
                return (!"".equals(s));
            }
        });

        FormLayout formLayout = new FormLayout();
        formLayout.setSizeUndefined();
        formLayout.setMargin(true);
        formLayout.addComponent(inputField);

        HorizontalLayout horizontalLayout = new HorizontalLayout();

        horizontalLayout.setMargin(true);
        horizontalLayout.setSpacing(true);
        horizontalLayout.setWidth("100%");

        Button cancel = new Button("Cancel");
        cancel.setDescription("Cancel editing");
        cancel.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                StringInputDialogWindow.this.close();
            }
        });

        cancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE, null);

        horizontalLayout.addComponent(cancel);
        horizontalLayout.setExpandRatio(cancel, 1);
        horizontalLayout.setComponentAlignment(cancel, Alignment.TOP_RIGHT);

        Button ok = new Button("Save");
        ok.setDescription("Save configuration");
        ok.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (inputField.isValid()) {
                    StringInputDialogWindow.this.close();
                    callback.valueEntered(inputField.getValue());
                }
            }
        });

        ok.setClickShortcut(ShortcutAction.KeyCode.ENTER, null);
        horizontalLayout.addComponent(ok);

        formLayout.addComponent(horizontalLayout);
        verticalLayout.addComponent(formLayout);

        setContent(verticalLayout);
    }
}
