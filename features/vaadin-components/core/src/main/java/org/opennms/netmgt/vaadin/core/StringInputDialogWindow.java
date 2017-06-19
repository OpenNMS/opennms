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

package org.opennms.netmgt.vaadin.core;

import com.vaadin.data.Validator;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Vaadin dialog window to query for a single String value.
 *
 * @author Christian Pape <christian@opennms.org>
 */
public class StringInputDialogWindow extends Window implements Window.CloseListener, Button.ClickListener {

    /**
     * The callback interface
     */
    public interface Action {
        void execute(StringInputDialogWindow window);
    }

    /**
     * the actions
     */
    private Action m_okAction;
    private Action m_cancelAction;

    /**
     * the buttons
     */
    private final Button m_cancelButton;
    private final Button m_okButton;

    /**
     * the input field
     */
    private final TextField m_inputField;

    /**
     * flag whether ok was pressed
     */
    private boolean m_okPressed;

    /**
     * Default constructor
     */
    public StringInputDialogWindow() {
        this("Input required", "Input");
    }

    /**
     * Constructor responsible for creating new instances of this class
     *
     * @param caption   the window's title
     * @param fieldName the title of the input field
     */
    public StringInputDialogWindow(String caption, String fieldName) {
        super(caption);

        /**
         * set window properties
         */
        setModal(true);
        setClosable(false);
        setResizable(false);

        /**
         * create the main layout
         */
        VerticalLayout verticalLayout = new VerticalLayout();

        /**
         * add the input field
         */
        m_inputField = new TextField(fieldName);

        m_inputField.setValue("");
        m_inputField.focus();
        m_inputField.selectAll();
        m_inputField.setImmediate(true);

        /**
         * create nested FormLayout instance
         */
        FormLayout formLayout = new FormLayout();
        formLayout.setSizeUndefined();
        formLayout.setMargin(true);
        formLayout.addComponent(m_inputField);

        /**
         * add the buttons in a horizontal layout
         */
        HorizontalLayout horizontalLayout = new HorizontalLayout();

        horizontalLayout.setMargin(true);
        horizontalLayout.setSpacing(true);
        horizontalLayout.setWidth("100%");

        /**
         * create cancel button
         */
        m_cancelButton = new Button("Cancel");
        m_cancelButton.setClickShortcut(ShortcutAction.KeyCode.ESCAPE, null);
        m_cancelButton.addClickListener(this);

        horizontalLayout.addComponent(m_cancelButton);
        horizontalLayout.setExpandRatio(m_cancelButton, 1);
        horizontalLayout.setComponentAlignment(m_cancelButton, Alignment.TOP_RIGHT);

        /**
         * create ok button
         */
        m_okButton = new Button("OK");
        m_okButton.setClickShortcut(ShortcutAction.KeyCode.ENTER, null);
        m_okButton.addClickListener(this);

        horizontalLayout.addComponent(m_okButton);
        formLayout.addComponent(horizontalLayout);
        verticalLayout.addComponent(formLayout);

        /**
         * the close listener
         */
        addCloseListener(this);

        /**
         * set the content
         */
        setContent(verticalLayout);
    }

    /**
     * Sets the validator to be used.
     * @param validator the validator
     * @return the instance itself
     */
    public StringInputDialogWindow withValidator(Validator validator) {
        m_inputField.addValidator(validator);
        return this;
    }

    /**
     * Returns the value of the input field.
     *
     * @return the current value
     */
    public String getValue() {
        return m_inputField.getValue();
    }

    /**
     * Sets the caption of the window.
     *
     * @param caption the caption to be used
     * @return the instance itself
     */
    public StringInputDialogWindow withCaption(String caption) {
        setCaption(caption);
        return this;
    }

    /**
     * Sets the label of the input field.
     *
     * @param fieldName the field name to be used
     * @return the instance itself
     */
    public StringInputDialogWindow withFieldName(String fieldName) {
        m_inputField.setCaption(fieldName);
        return this;
    }

    /**
     * Sets the action to be performed when the dialog is confirmed.
     *
     * @param okAction the action to be executed
     * @return the instance itself
     */
    public StringInputDialogWindow withOkAction(Action okAction) {
        this.m_okAction = okAction;
        return this;
    }

    /**
     * Sets the action to be performed when the dialog is cancelled.
     *
     * @param cancelAction the action to be executed
     * @return the instance itself
     */
    public StringInputDialogWindow withCancelAction(Action cancelAction) {
        this.m_cancelAction = cancelAction;
        return this;
    }

    public void open() {
        UI.getCurrent().addWindow(this);
    }

    /**
     * Sets the label of the ok button.
     *
     * @param okLabel the label to be used
     * @return the instance itself
     */
    public StringInputDialogWindow withOkLabel(String okLabel) {
        m_okButton.setCaption(okLabel);
        return this;
    }

    /**
     * Sets the label of the cancel button.
     *
     * @param cancelLabel the label to be used
     * @return the instance itself
     */
    public StringInputDialogWindow withCancelLabel(String cancelLabel) {
        m_cancelButton.setCaption(cancelLabel);
        return this;
    }

    @Override
    public void windowClose(CloseEvent e) {
        if (m_okPressed) {
            if (m_okAction != null) {
                m_okAction.execute(this);
            }
        } else {
            if (m_cancelAction != null) {
                m_cancelAction.execute(this);
            }
        }
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        m_okPressed = event.getSource() == m_okButton;

        if ((m_okPressed && m_inputField.isValid()) || !m_okPressed) {
            close();
        }
    }
}
