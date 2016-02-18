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

import com.vaadin.data.Validator;
import com.vaadin.event.FieldEvents;
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
public class KeyValueInputDialogWindow extends Window implements Window.CloseListener, Button.ClickListener {

    /**
     * The callback interface
     */
    public interface Action {
        void execute(KeyValueInputDialogWindow window);
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
     * the input key field
     */
    private final TextField m_keyInputField;

    /**
     * the input value field
     */
    private final TextField m_valueInputField;

    /**
     * where to set focus
     */
    private boolean m_focusKey = true;

    /**
     * flag whether ok was pressed
     */
    private boolean m_okPressed;

    /**
     * Default constructor
     */
    public KeyValueInputDialogWindow() {
        this("Input required", "Key", "Value");
    }

    /**
     * Constructor responsible for creating new instances of this class
     *
     * @param caption   the window's title
     * @param keyName   the title of the key input field
     * @param valueName the title of the value input field
     */
    public KeyValueInputDialogWindow(String caption, String keyName, String valueName) {
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
         * add the key input field
         */
        m_keyInputField = new TextField(keyName);

        m_keyInputField.setValue("");
        m_keyInputField.setId("keyField");
        m_keyInputField.selectAll();
        m_keyInputField.setImmediate(true);
        m_keyInputField.focus();

        /**
         * add the value input field
         */
        m_valueInputField = new TextField(valueName);

        m_valueInputField.setValue("");
        m_valueInputField.setId("valueField");
        m_valueInputField.selectAll();
        m_valueInputField.setImmediate(true);

        /**
         * create nested FormLayout instance
         */
        FormLayout formLayout = new FormLayout();
        formLayout.setSizeUndefined();
        formLayout.setMargin(true);
        formLayout.addComponent(m_keyInputField);
        formLayout.addComponent(m_valueInputField);

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
        m_cancelButton.setId("cancelBtn");
        m_cancelButton.setClickShortcut(ShortcutAction.KeyCode.ESCAPE, null);
        m_cancelButton.addClickListener(this);

        horizontalLayout.addComponent(m_cancelButton);
        horizontalLayout.setExpandRatio(m_cancelButton, 1);
        horizontalLayout.setComponentAlignment(m_cancelButton, Alignment.TOP_RIGHT);

        /**
         * create ok button
         */
        m_okButton = new Button("OK");
        m_okButton.setId("okBtn");
        m_okButton.setClickShortcut(ShortcutAction.KeyCode.ENTER, null);
        m_okButton.addClickListener(this);

        horizontalLayout.addComponent(m_okButton);
        formLayout.addComponent(horizontalLayout);
        verticalLayout.addComponent(formLayout);

        addFocusListener(new FieldEvents.FocusListener() {
            @Override
            public void focus(FieldEvents.FocusEvent event) {
                if (m_focusKey) {
                    m_keyInputField.focus();
                } else {
                    m_valueInputField.focus();
                }
            }
        });

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
     * Sets the key validator to be used.
     *
     * @param validator the validator
     * @return the instance itself
     */
    public KeyValueInputDialogWindow withKeyValidator(Validator validator) {
        m_keyInputField.addValidator(validator);
        return this;
    }

    /**
     * Sets the value validator to be used.
     *
     * @param validator the validator
     * @return the instance itself
     */
    public KeyValueInputDialogWindow withValueValidator(Validator validator) {
        m_valueInputField.addValidator(validator);
        return this;
    }

    /**
     * Returns the value of the value input field.
     *
     * @return the current value
     */
    public String getValue() {
        return m_valueInputField.getValue();
    }

    /**
     * Returns the value of the key input field.
     *
     * @return the current value
     */
    public String getKey() {
        return m_keyInputField.getValue();
    }

    /**
     * Add value.
     *
     * @param value the value to be used
     * @return the instance itself
     */
    public KeyValueInputDialogWindow withValue(String value) {
        m_valueInputField.setValue(value);
        return this;
    }

    /**
     * Add key.
     *
     * @param key the key to be used
     * @return the instance itself
     */
    public KeyValueInputDialogWindow withKey(String key) {
        m_keyInputField.setValue(key);
        return this;
    }

    /**
     * Sets the caption of the window.
     *
     * @param caption the caption to be used
     * @return the instance itself
     */
    public KeyValueInputDialogWindow withCaption(String caption) {
        setCaption(caption);
        return this;
    }

    /**
     * Sets the label of the key input field.
     *
     * @param fieldName the field name to be used
     * @return the instance itself
     */
    public KeyValueInputDialogWindow withKeyFieldName(String fieldName) {
        m_keyInputField.setCaption(fieldName);
        return this;
    }

    /**
     * Sets the label of the value input field.
     *
     * @param fieldName the field name to be used
     * @return the instance itself
     */
    public KeyValueInputDialogWindow withValueFieldName(String fieldName) {
        m_valueInputField.setCaption(fieldName);
        return this;
    }

    /**
     * Sets the action to be performed when the dialog is confirmed.
     *
     * @param okAction the action to be executed
     * @return the instance itself
     */
    public KeyValueInputDialogWindow withOkAction(Action okAction) {
        this.m_okAction = okAction;
        return this;
    }

    /**
     * Sets the action to be performed when the dialog is cancelled.
     *
     * @param cancelAction the action to be executed
     * @return the instance itself
     */
    public KeyValueInputDialogWindow withCancelAction(Action cancelAction) {
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
    public KeyValueInputDialogWindow withOkLabel(String okLabel) {
        m_okButton.setCaption(okLabel);
        return this;
    }

    /**
     * Sets the label of the cancel button.
     *
     * @param cancelLabel the label to be used
     * @return the instance itself
     */
    public KeyValueInputDialogWindow withCancelLabel(String cancelLabel) {
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

        if ((m_okPressed && m_keyInputField.isValid() && m_valueInputField.isValid()) || !m_okPressed) {
            close();
        }
    }

    /**
     * Sets the focus to the key field.
     *
     * @return the instance itself
     */
    public KeyValueInputDialogWindow focusKey() {
        m_focusKey = true;
        return  this;
    }

    /**
     * Sets the focus to the value field.
     *
     * @return the instance itself
     */
    public KeyValueInputDialogWindow focusValue() {
        m_focusKey = false;
        return this;
    }

    /**
     * Enables the field.
     *
     * @return the instance itself
     */
    public KeyValueInputDialogWindow enableKey() {
        m_keyInputField.setEnabled(true);
        return this;
    }

    /**
     * Disables the field.
     *
     * @return the instance itself
     */
    public KeyValueInputDialogWindow disableKey() {
        m_keyInputField.setEnabled(false);
        return this;
    }

    /**
     * Enables the field.
     *
     * @return the instance itself
     */
    public KeyValueInputDialogWindow enableValue() {
        m_valueInputField.setEnabled(true);
        return this;
    }

    /**
     * Disables the field.
     *
     * @return the instance itself
     */
    public KeyValueInputDialogWindow disableValue() {
        m_valueInputField.setEnabled(false);
        return this;
    }

}
